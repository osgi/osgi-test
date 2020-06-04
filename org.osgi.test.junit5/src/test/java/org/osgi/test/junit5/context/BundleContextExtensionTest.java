/*
 * Copyright (c) OSGi Alliance (2019). All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.osgi.test.junit5.context;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.testkit.engine.EventType.FINISHED;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.osgi.test.assertj.bundle.BundleAssert.assertThat;
import static org.osgi.test.common.dictionary.Dictionaries.dictionaryOf;
import static org.osgi.test.junit5.TestUtil.getBundle;
import static org.osgi.test.junit5.context.MultiLevelCleanupTest.Scope.AFTER_ALL;
import static org.osgi.test.junit5.context.MultiLevelCleanupTest.Scope.AFTER_CLASS;
import static org.osgi.test.junit5.context.MultiLevelCleanupTest.Scope.AFTER_EACH;
import static org.osgi.test.junit5.context.MultiLevelCleanupTest.Scope.BEFORE_ALL;
import static org.osgi.test.junit5.context.MultiLevelCleanupTest.Scope.BEFORE_CLASS;
import static org.osgi.test.junit5.context.MultiLevelCleanupTest.Scope.BEFORE_EACH;
import static org.osgi.test.junit5.context.MultiLevelCleanupTest.Scope.INNER_TEST;
import static org.osgi.test.junit5.context.MultiLevelCleanupTest.Scope.NESTED_AFTER_EACH;
import static org.osgi.test.junit5.context.MultiLevelCleanupTest.Scope.NESTED_BEFORE_EACH;
import static org.osgi.test.junit5.context.MultiLevelCleanupTest.Scope.NESTED_TEST;
import static org.osgi.test.junit5.context.MultiLevelCleanupTest.Scope.PARAMETERIZED_TEST;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.engine.JupiterTestEngine;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.testkit.engine.EngineTestKit;
import org.junit.platform.testkit.engine.Event;
import org.mockito.ArgumentCaptor;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleException;
import org.osgi.framework.BundleListener;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.FrameworkListener;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceObjects;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.SynchronousBundleListener;
import org.osgi.framework.startlevel.FrameworkStartLevel;
import org.osgi.test.common.exceptions.Exceptions;
import org.osgi.test.junit5.context.MultiLevelCleanupTest.Scope;
import org.osgi.test.junit5.testutils.OSGiSoftAssertions;
import org.osgi.test.junit5.types.Foo;

public class BundleContextExtensionTest {

	private static <R> Stream<DynamicNode> runMultilevelTestClass(String type,
		Class<? extends MultiLevelCleanupTest> testClass) {
		BundleContext bc = FrameworkUtil.getBundle(MultiLevelCleanupTest.class)
			.getBundleContext();
		final OSGiSoftAssertions beforeSoftly = new OSGiSoftAssertions();

		MultiLevelCleanupTest.scopedResourcesMap = new HashMap<>();
		MultiLevelCleanupTest.getGlobalResourceChecker(testClass)
			.assertNotSetup(beforeSoftly, BEFORE_CLASS, BEFORE_ALL, BEFORE_EACH, INNER_TEST, PARAMETERIZED_TEST,
				NESTED_BEFORE_EACH, NESTED_TEST, NESTED_AFTER_EACH, AFTER_EACH, AFTER_ALL, AFTER_CLASS);

		DynamicNode beforeTest = dynamicTest("beforeClass", () -> {
			beforeSoftly.assertAll();
		});

		Map<TestDescriptor, Event> eventMap = new HashMap<>();

		AtomicReference<TestDescriptor> root = new AtomicReference<>();

		EngineTestKit.engine(new JupiterTestEngine())
			.selectors(selectClass(
				testClass))
			.execute()
			.allEvents()
			// .debug(
			// System.err)
			.stream()
			.filter(event -> event.getType()
				.equals(FINISHED))
			.forEach(event -> {
				TestDescriptor current = event.getTestDescriptor();
				eventMap.put(current, event);
				if (!current.getParent()
					.isPresent()) {
					root.set(current);
				}
			});

		OSGiSoftAssertions afterSoftly = new OSGiSoftAssertions();
		MultiLevelCleanupTest.getGlobalResourceChecker(testClass)
			.assertNotSetup(beforeSoftly, AFTER_CLASS, BEFORE_ALL, BEFORE_EACH, INNER_TEST, PARAMETERIZED_TEST,
				NESTED_BEFORE_EACH, NESTED_TEST, NESTED_AFTER_EACH, AFTER_EACH, AFTER_ALL);
		DynamicNode afterTest = dynamicTest("afterClass", () -> {
			afterSoftly.assertAll();
		});

		DynamicNode classContainer = new DynamicNodeGenerator(eventMap).toNode(root.get()
			.getChildren()
			.stream()
			.findFirst()
			.get());
		return Stream.of(beforeTest, classContainer, afterTest);
	}

	static InputStream getBundleForScope(Scope scope) {
		return getBundle(scope.toString()
			.replace(".", "/") + ".jar");
	}

	static class BundleChecker extends BundleContextResourceChecker<Bundle> {

		public BundleChecker(BundleContext bc, Map<Scope, Bundle> scopedResourcesMap) {
			super(bc, scopedResourcesMap);
		}

		@Override
		public Bundle doSetupResource(Scope inScope) throws BundleException {
			return bc.installBundle(inScope.toString(), getBundleForScope(inScope));
		}

		@Override
		public void doAssertSetup(Scope inScope, Scope fromScope, Bundle installedBundle) {
			OSGiSoftAssertions softly = new OSGiSoftAssertions();
			softly.assertThat(installedBundle)
				.as(inScope + " from scope "
					+ fromScope)
				.isNotInState(Bundle.UNINSTALLED);
			softly.assertThat(bc)
				.as(inScope + " from scope "
					+ fromScope)
				.hasBundleWithIdThat(installedBundle.getBundleId())
				.isSameAs(installedBundle);
			softly.assertThat(bc)
				.hasBundlesThat()
				.as(inScope + " from scope "
					+ fromScope)
				.contains(installedBundle);
			softly.assertAll();
		}

		@Override
		public void doAssertNotSetup(Scope inScope, Scope fromScope, Bundle installedBundle) {
			OSGiSoftAssertions softly = new OSGiSoftAssertions();
			softly.assertThat(installedBundle)
				.as(inScope + " from scope "
					+ fromScope)
				.isInState(Bundle.UNINSTALLED);
			softly.assertThat(bc)
				.as(inScope + " from scope "
					+ fromScope)
				.doesNotHaveBundleWithId(installedBundle.getBundleId());
			softly.assertThat(bc)
				.as(inScope + " from scope "
					+ fromScope)
				.hasBundlesThat()
				.doesNotContain(installedBundle);
			softly.assertAll();
		}

	}

	@TestFactory
	public Stream<DynamicNode> cleansUpBundlesMultiLevel() {
		BundleContextMultiLevelCleanupTest.setFactory(BundleChecker::new);
		return runMultilevelTestClass("Bundle", BundleContextMultiLevelCleanupTest.class);
	}

	@TestFactory
	public Stream<DynamicNode> cleansUpInstallBundlesMultiLevel() {
		// Uses the BundleChecker AbstractResourceChecker to facilitate stuff.
		return runMultilevelTestClass("Install Bundle", InstallBundleMultiLevelCleanupTest.class);
	}

	static public class BundleListenerResourceChecker extends BundleContextResourceChecker<BundleListener> {

		final Bundle bundle;

		BundleListenerResourceChecker(BundleContext bc, Map<Scope, BundleListener> scopedResourcesMap, Bundle bundle) {
			super(bc, scopedResourcesMap);
			this.bundle = bundle;
		}

		@Override
		public BundleListener doSetupResource(Scope inScope) {
			BundleListener listener = mock(SynchronousBundleListener.class);
			bc.addBundleListener(listener);
			return listener;
		}

		@Override
		public void doAssertSetup(Scope inScope, Scope fromScope, BundleListener listener) throws BundleException {
			reset(listener);
			assertThat(bundle).isInStateMaskedBy(Bundle.RESOLVED | Bundle.INSTALLED);
			bundle.start();

			OSGiSoftAssertions softly = new OSGiSoftAssertions();
			try {

				final ArgumentCaptor<BundleEvent> startEventCapture = ArgumentCaptor.forClass(BundleEvent.class);
				softly.check(() -> verify(listener, atLeast(2)).bundleChanged(startEventCapture.capture()));
				List<BundleEvent> ourBundleEvents = startEventCapture
					.getAllValues()
					.stream()
					.filter(ev -> ev.getBundle() == bundle)
					.collect(Collectors.toList());
				softly.assertThat(ourBundleEvents)
					.hasSizeBetween(2, 3);
				Iterator<BundleEvent> it = ourBundleEvents.iterator();

				// First time we start the bundle it might not be resolved yet.
				if (ourBundleEvents.size() == 3) {
					softly.assertThat(it.next())
						.as("zeroth")
						.isOfType(BundleEvent.RESOLVED);
				}
				softly.assertThat(it.next())
					.as("first")
					.isOfType(BundleEvent.STARTING);
				softly.assertThat(it.next())
					.as("second")
					.isOfType(BundleEvent.STARTED);

				reset(listener);
				bundle.stop();
				final ArgumentCaptor<BundleEvent> stopEventCapture = ArgumentCaptor.forClass(BundleEvent.class);
				softly.check(() -> verify(listener, times(2)).bundleChanged(stopEventCapture.capture()));
				ourBundleEvents = stopEventCapture
					.getAllValues()
					.stream()
					.filter(ev -> ev.getBundle() == bundle)
					.collect(Collectors.toList());
				softly.assertThat(ourBundleEvents)
					.as("stopEvents")
					.hasSize(2);
				it = ourBundleEvents.iterator();
				softly.assertThat(it.next())
					.as("third")
					.isOfType(BundleEvent.STOPPING);
				softly.assertThat(it.next())
					.as("fourth")
					.isOfType(BundleEvent.STOPPED);
			} finally {
				softly.assertAll();
			}
		}

		@Override
		public void doAssertNotSetup(Scope inScope, Scope fromScope, BundleListener listener) throws BundleException {
			reset(listener);
			bundle.start();
			bundle.stop();
			verifyNoInteractions(listener);
		}
	}

	@TestFactory
	public Stream<DynamicNode> cleansUpBundleListenersMultiLevel() throws Exception {
		Bundle bundle = FrameworkUtil.getBundle(getClass());

		Bundle installedBundle = bundle.getBundleContext()
			.installBundle("it", getBundle("tb1.jar"));

		try {
			installedBundle.stop();
			BundleContextMultiLevelCleanupTest.setFactory((BundleContext bc,
				Map<Scope, BundleListener> map) -> new BundleListenerResourceChecker(bc, map, installedBundle));
			return runMultilevelTestClass("BundleListener", BundleContextMultiLevelCleanupTest.class);
		} finally {
			installedBundle.uninstall();
		}
	}

	static class ServiceResourceChecker extends BundleContextResourceChecker<ServiceRegistration<String>> {

		public ServiceResourceChecker(BundleContext bc, Map<Scope, ServiceRegistration<String>> scopedResources) {
			super(bc, scopedResources);
		}

		@Override
		public ServiceRegistration<String> doSetupResource(Scope inScope) {
			return bc.registerService(String.class, "Service for scope" + inScope,
				dictionaryOf("multileveltest.scope", inScope));
		}

		@Override
		public void doAssertSetup(Scope inScope, Scope fromScope, ServiceRegistration<String> registration) {
			assertThat(registration).isNotNull();
			try {
				ServiceReference<String> reference = registration.getReference();
				assertThat(bundle.getRegisteredServices()).as(inScope + ": checking resource from " + fromScope + "\n")
					.contains(registration.getReference());
			} catch (IllegalStateException e) {
				fail(String.format("[%s: checking resource from %s]:\nservice not registered: %s", inScope, fromScope,
					registration), e);
			}
		}

		@Override
		public void doAssertNotSetup(Scope inScope, Scope fromScope, ServiceRegistration<String> registration) {
			assertThat(registration).isNotNull();
			ServiceReference<?>[] registeredServices = bundle.getRegisteredServices();
			try {
				registration.getReference();
				fail(String.format("[%s: checking resource from %s]:\nservice was not unregistered: %s ", inScope,
					fromScope, registration));
			} catch (IllegalStateException e) {}
		}

	}

	@TestFactory
	public Stream<DynamicNode> cleansUpRegisteredServicesMultiLevel() {
		BundleContextMultiLevelCleanupTest.setFactory(ServiceResourceChecker::new);
		return runMultilevelTestClass("ServiceRegistration", BundleContextMultiLevelCleanupTest.class);
	}

	static public class ServiceListenerResourceChecker extends BundleContextResourceChecker<ServiceListener> {
		final Bundle bundle;

		ServiceListenerResourceChecker(BundleContext bc, Map<Scope, ServiceListener> scopedResources, Bundle bundle) {
			super(bc, scopedResources);
			this.bundle = bundle;
		}

		@Override
		public ServiceListener doSetupResource(Scope inScope) throws InvalidSyntaxException {
			ServiceListener listener = mock(ServiceListener.class);
			// Use a filter so that we're not getting interfered with by other
			// tests
			bc.addServiceListener(listener, "(servicelistener.test.scope=" + inScope + ")");
			return listener;
		}

		@Override
		public void doAssertSetup(Scope inScope, Scope fromScope, ServiceListener listener) throws BundleException {
			reset(listener);
			ServiceRegistration<String> reg = bundle.getBundleContext()
				.registerService(String.class, "test",
					dictionaryOf("servicelistener.test.scope", fromScope.toString()));
			ServiceReference<String> ref = reg.getReference();
			OSGiSoftAssertions softly = new OSGiSoftAssertions();
			try {
				try {
					final ArgumentCaptor<ServiceEvent> startEventCapture = ArgumentCaptor.forClass(ServiceEvent.class);
					softly.check(() -> verify(listener).serviceChanged(startEventCapture.capture()));
					softly.assertThat(startEventCapture.getValue())
						.as("started")
						.isOfType(ServiceEvent.REGISTERED)
						.hasServiceReference(ref);
					reset(listener);
				} finally {
					reg.unregister();
				}
				final ArgumentCaptor<ServiceEvent> stopEventCapture = ArgumentCaptor.forClass(ServiceEvent.class);
				softly.check(() -> verify(listener).serviceChanged(stopEventCapture.capture()));
				softly.assertThat(stopEventCapture.getValue())
					.as("stopped")
					.isOfType(ServiceEvent.UNREGISTERING);
			} finally {
				softly.assertAll();
			}
		}

		@Override
		public void doAssertNotSetup(Scope inScope, Scope fromScope, ServiceListener listener) {
			reset(listener);
			ServiceRegistration<String> reg = bundle.getBundleContext()
				.registerService(String.class, "test",
					dictionaryOf("servicelistener.test.scope", fromScope.toString()));
			reg.unregister();
			verifyNoInteractions(listener);
		}
	}

	@TestFactory
	public Stream<DynamicNode> cleansUpServiceListenersMultiLevel() throws Exception {
		Bundle bundle = FrameworkUtil.getBundle(getClass());
		Bundle installedBundle = bundle.getBundleContext()
			.installBundle("it", getBundle("tb1.jar"));
		try {
			installedBundle.start();
			BundleContextMultiLevelCleanupTest.setFactory((BundleContext bc,
				Map<Scope, ServiceListener> map) -> new ServiceListenerResourceChecker(bc, map, installedBundle));
			return runMultilevelTestClass("ServiceListener", BundleContextMultiLevelCleanupTest.class);
		} finally {
			installedBundle.uninstall();
		}
	}

	static public class FrameworkListenerResourceChecker extends BundleContextResourceChecker<FrameworkListener> {

		final BundleContext			originalContext;
		final Bundle				systemBundle;
		final FrameworkStartLevel	startLevel;

		FrameworkListenerResourceChecker(BundleContext bc, Map<Scope, FrameworkListener> scopedResources) {
			super(bc, scopedResources);
			originalContext = FrameworkUtil.getBundle(FrameworkListenerResourceChecker.class)
				.getBundleContext();
			systemBundle = originalContext.getBundle(0);
			startLevel = systemBundle.adapt(FrameworkStartLevel.class);

		}

		@Override
		public FrameworkListener doSetupResource(Scope inScope) throws InvalidSyntaxException {
			FrameworkListener listener = mock(FrameworkListener.class);
			bc.addFrameworkListener(listener);
			return listener;
		}

		protected void tryToStart() {
			CountDownLatch flag = new CountDownLatch(1);
			// The framework event we are generating in loadClass() is delivered
			// asynchronously.
			// This custom listener is installed so that it will also be
			// notified (after the listener-under-test)
			// and can be used to synchronize so that the mock verification
			// doesn't happen until after the event is delivered.
			FrameworkListener sync = new FrameworkListener() {

				@Override
				public void frameworkEvent(FrameworkEvent event) {
					flag.countDown();
				}

			};
			originalContext.addFrameworkListener(sync);
			// According to the doc, this shouldn't start/stop anything, but
			// should still generate a STARTLEVEL_CHANGED event.
			startLevel.setStartLevel(startLevel.getStartLevel());
			try {
				if (!flag.await(1000, TimeUnit.MILLISECONDS)) {
					throw new IllegalStateException("Timed out waiting for framework event");
				}
			} catch (Exception e) {
				throw Exceptions.duck(e);
			} finally {
				originalContext.removeFrameworkListener(sync);
			}

		}

		@Override
		public void doAssertSetup(Scope inScope, Scope fromScope, FrameworkListener listener) {
			reset(listener);
			tryToStart();

			OSGiSoftAssertions softly = new OSGiSoftAssertions();
			try {

				final ArgumentCaptor<FrameworkEvent> startEventCapture = ArgumentCaptor.forClass(FrameworkEvent.class);
				softly.check(() -> verify(listener).frameworkEvent(startEventCapture.capture()));
				softly.assertThat(startEventCapture.getValue())
					.as("registered")
					.isOfType(
						FrameworkEvent.STARTLEVEL_CHANGED)
					.hasBundle(systemBundle);
			} finally {
				softly.assertAll();
			}
		}

		@Override
		public void doAssertNotSetup(Scope inScope, Scope fromScope, FrameworkListener listener) {
			reset(listener);
			tryToStart();
			verifyNoInteractions(listener);
		}

	}

	@TestFactory
	public Stream<DynamicNode> cleansUpFrameworkListenersMultiLevel() throws Exception {
		BundleContextMultiLevelCleanupTest.setFactory(FrameworkListenerResourceChecker::new);
		return runMultilevelTestClass("FrameworkListener", BundleContextMultiLevelCleanupTest.class);
	}

	static class GottenServicesResourceChecker extends BundleContextResourceChecker<ServiceReference<Foo>> {
		GottenServicesResourceChecker(BundleContext bc, Map<Scope, ServiceReference<Foo>> scopedResources) {
			super(bc, scopedResources);
		}

		@Override
		public ServiceReference<Foo> doSetupResource(Scope inScope) throws InvalidSyntaxException {
			ServiceReference<Foo> reference = bc.getServiceReferences(Foo.class, "(test.scope=" + inScope
				+ ")")
				.iterator()
				.next();
			bc.getService(reference);
			bc.getService(reference);
			return reference;
		}

		@Override
		public void doAssertSetup(Scope inScope, Scope fromScope, ServiceReference<Foo> reference) {
			assertThat(reference).as(String.format("%s checking resource from %s setup: reference", inScope,
				fromScope))
				.isNotNull();
			assertThat(bundle.getServicesInUse()).as(String.format("%s checking resource from %s setup: inUse", inScope,
				fromScope))
				.isNotNull()
				.contains(reference);
		}

		@Override
		public void doAssertNotSetup(Scope inScope, Scope fromScope, ServiceReference<Foo> reference) {
			assertThat(reference).as(String.format("%s checking resource from %s not setup: reference", inScope,
				fromScope))
				.isNotNull();
			ServiceReference<?>[] inUse = bundle.getServicesInUse();
			if (inUse != null) {
				Stream.of(inUse)
					.forEach(ref -> {
						String testScope = (String) ref.getProperty("test.scope");
						if (fromScope.equals(testScope)) {
							fail(
								String.format("[%s: checking resource from %s]:\nstill had a reference to service: %s ",
									inScope, fromScope, ref));
						}
						});
			}
		}
	}

	@TestFactory
	public Stream<DynamicNode> cleansUpGottenServicesMultiLevel() throws Exception {
		Bundle bundle = FrameworkUtil.getBundle(getClass());
		Bundle installedBundle = bundle.getBundleContext()
			.installBundle("it", getBundle("tb1.jar"));
		try {
			installedBundle.start();
			BundleContext installedBundleContext = installedBundle.getBundleContext();

			List<ServiceRegistration<Foo>> registrations = new ArrayList<>();
			Stream.of(Scope.values())
				.map(
					Object::toString)
				.forEach(scope -> {
					registrations.add(installedBundleContext.registerService(Foo.class, new Foo() {},
						dictionaryOf("test.scope", scope)));
				});

			try {
				BundleContextMultiLevelCleanupTest.setFactory(GottenServicesResourceChecker::new);
				return runMultilevelTestClass("Gotten Services", BundleContextMultiLevelCleanupTest.class);
			} finally {
				for (ServiceRegistration<Foo> registration : registrations) {
					try {
						registration.unregister();
					} catch (Exception e) {}
				}
			}
		} finally {
			installedBundle.uninstall();
		}
	}

	static class GottenServiceObjectsResourceChecker extends BundleContextResourceChecker<ServiceObjects<Foo>> {
		GottenServiceObjectsResourceChecker(BundleContext bc, Map<Scope, ServiceObjects<Foo>> scopedResources) {
			super(bc, scopedResources);
		}

		@Override
		public ServiceObjects<Foo> doSetupResource(Scope inScope) throws InvalidSyntaxException {
			ServiceReference<Foo> reference = bc.getServiceReferences(Foo.class, "(test.scope=" + inScope + ")")
				.iterator()
				.next();
			ServiceObjects<Foo> services = bc.getServiceObjects(reference);
			services.getService();
			services.getService();
			return services;
		}

		@Override
		public void doAssertSetup(Scope inScope, Scope fromScope, ServiceObjects<Foo> services) {
			assertThat(services).as(String.format("%s checking resource from %s setup: services", inScope,
				fromScope))
				.isNotNull();
			assertThat(bundle.getServicesInUse())
				.as(String.format("%s checking resource from %s setup: inUse", inScope, fromScope))
				.isNotNull()
				.contains(services.getServiceReference());
		}

		@Override
		public void doAssertNotSetup(Scope inScope, Scope fromScope, ServiceObjects<Foo> services) {
			assertThat(services).as(String.format("%s checking resource from %s not setup: services", inScope,
				fromScope))
				.isNotNull();
			ServiceReference<?>[] inUse = bundle.getServicesInUse();
			if (inUse != null) {
				assertThat(inUse).as(String.format("%s checking resource from %s not setup: inUse", inScope, fromScope))
					.doesNotContain(services.getServiceReference());
			}
		}
	}

	@TestFactory
	public Stream<DynamicNode> cleansUpGottenServiceObjectsMultiLevel() throws Exception {
		Bundle bundle = FrameworkUtil.getBundle(getClass());
		Bundle installedBundle = bundle.getBundleContext()
			.installBundle("it", getBundle("tb1.jar"));
		try {
			installedBundle.start();
			BundleContext installedBundleContext = installedBundle.getBundleContext();

			List<ServiceRegistration<Foo>> registrations = new ArrayList<>();
			Stream.of(Scope.values())
				.map(
					Object::toString)
				.forEach(scope -> {
					registrations.add(installedBundleContext.registerService(Foo.class, new Foo() {},
						dictionaryOf("test.scope", scope)));
				});

			try {
				BundleContextMultiLevelCleanupTest.setFactory(GottenServiceObjectsResourceChecker::new);
				return runMultilevelTestClass("Gotten ServiceObjects", BundleContextMultiLevelCleanupTest.class);
			} finally {
				for (ServiceRegistration<Foo> registration : registrations) {
					try {
						registration.unregister();
					} catch (Exception e) {}
				}
			}
		} finally {
			installedBundle.uninstall();
		}
	}
}
