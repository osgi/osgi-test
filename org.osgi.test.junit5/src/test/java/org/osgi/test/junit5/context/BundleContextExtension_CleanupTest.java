/*
 * Copyright (c) OSGi Alliance (2019, 2021). All Rights Reserved.
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
import static org.osgi.test.junit5.context.MultiLevelCleanupTest.CallbackPoint.AFTER_CLASS;
import static org.osgi.test.junit5.context.MultiLevelCleanupTest.CallbackPoint.BEFORE_CLASS;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import org.osgi.test.junit5.context.MultiLevelCleanupTest.CallbackPoint;
import org.osgi.test.junit5.testutils.OSGiSoftAssertions;
import org.osgi.test.junit5.types.Foo;

public class BundleContextExtension_CleanupTest {

	/**
	 * This function runs the given concrete subclass of MultiLevelCleanupTest
	 * using {@link EngineTestKit}. It then takes the result and transforms it
	 * into DynamicNode test hierarchy to recreate the original test so that it
	 * shows in the test output - this makes it easier to diagnose test
	 * failures.
	 *
	 * @see DynamicNodeGenerator
	 */
	private static <R> Stream<DynamicNode> runMultilevelTestClass(String type,
		Class<? extends MultiLevelCleanupTest> testClass) {
		BundleContext bc = FrameworkUtil.getBundle(MultiLevelCleanupTest.class)
			.getBundleContext();
		final OSGiSoftAssertions beforeSoftly = new OSGiSoftAssertions();

		MultiLevelCleanupTest.resourcesMap = new HashMap<>();
		@SuppressWarnings("unchecked")
		AbstractResourceChecker<R> checker = (AbstractResourceChecker<R>) MultiLevelCleanupTest
			.getGlobalResourceChecker(testClass);
		checker.assertSetup(beforeSoftly, BEFORE_CLASS, EnumSet.noneOf(CallbackPoint.class));
		checker.setupResource(BEFORE_CLASS);
		EnumSet<CallbackPoint> expectedSetup = EnumSet.of(BEFORE_CLASS);
		checker.assertSetup(beforeSoftly, BEFORE_CLASS, expectedSetup);

		try {
			DynamicNode beforeTest = dynamicTest("beforeClass", () -> {
				beforeSoftly.assertAll();
			});

			Map<TestDescriptor, Event> eventMap = new HashMap<>();

			AtomicReference<TestDescriptor> root = new AtomicReference<>();

			Logger logger = Logger.getLogger("org.junit.jupiter");
			Level oldLevel = logger.getLevel();
			try {
				// Suppress log output while the testkit is running (see issue
				// #133).
				logger.setLevel(Level.OFF);
				EngineTestKit.engine(new JupiterTestEngine())
					.selectors(selectClass(testClass))
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
			} finally {
				// Restore the filter to what it was so that we do not interfere
				// with the parent test
				logger.setLevel(oldLevel);
			}

			OSGiSoftAssertions afterSoftly = new OSGiSoftAssertions();
			checker.assertSetup(afterSoftly, AFTER_CLASS, expectedSetup);

			DynamicNode afterTest = dynamicTest("afterClass", () -> {
				afterSoftly.assertAll();
			});

			DynamicNode classContainer = new DynamicNodeGenerator(eventMap).toNode(root.get()
				.getChildren()
				.stream()
				.findFirst()
				.get());
			return Stream.of(beforeTest, classContainer, afterTest);
		} finally {
			checker.tearDownResource(BEFORE_CLASS);
		}
	}

	static InputStream getBundleForCallbackPoint(CallbackPoint callbackPoint) {
		return getBundle(callbackPoint.toString()
			.replace(".", "/") + ".jar");
	}

	/**
	 * Resource checker for installing bundles. For each callback point our test
	 * bundle has a corresponding embedded bundle that is installed by the
	 * "doSetupResource" callback; look in the bnd subdirectory of this project
	 * to see them. Mapping between the callback point and embedded bundle path
	 * is done by {@link #getBundleForCallbackPoint(CallbackPoint)}
	 */
	static class BundleChecker extends BundleContextResourceChecker<Bundle> {

		public BundleChecker(BundleContext bc, Map<CallbackPoint, Bundle> resourcesMap) {
			super(bc, resourcesMap);
		}

		@Override
		public Bundle doSetupResource(CallbackPoint currentPoint) throws BundleException {
			return bc.installBundle(currentPoint.toString(), getBundleForCallbackPoint(currentPoint));
		}

		@Override
		public void doAssertSetup(CallbackPoint currentPoint, CallbackPoint pointThatShouldBeSetup,
			Bundle installedBundle) {
			OSGiSoftAssertions softly = new OSGiSoftAssertions();
			softly.assertThat(installedBundle)
				.as(currentPoint + " from point " + pointThatShouldBeSetup)
				.isNotInState(Bundle.UNINSTALLED);
			softly.assertThat(bc)
				.as(currentPoint + " from point " + pointThatShouldBeSetup)
				.hasBundleWithIdThat(installedBundle.getBundleId())
				.isSameAs(installedBundle);
			softly.assertThat(bc)
				.hasBundlesThat()
				.as(currentPoint + " from point " + pointThatShouldBeSetup)
				.contains(installedBundle);
			softly.assertAll();
		}

		@Override
		public void doAssertNotSetup(CallbackPoint currentPoint, CallbackPoint pointThatShouldNotBeSetup,
			Bundle installedBundle) {
			OSGiSoftAssertions softly = new OSGiSoftAssertions();
			softly.assertThat(installedBundle)
				.as(currentPoint + " from point " + pointThatShouldNotBeSetup)
				.isInState(Bundle.UNINSTALLED);
			softly.assertThat(bc)
				.as(currentPoint + " from point " + pointThatShouldNotBeSetup)
				.doesNotHaveBundleWithId(installedBundle.getBundleId());
			softly.assertThat(bc)
				.as(currentPoint + " from point " + pointThatShouldNotBeSetup)
				.hasBundlesThat()
				.doesNotContain(installedBundle);
			softly.assertAll();
		}

		@Override
		public void doTearDownResource(CallbackPoint callbackPoint, Bundle bundle) throws BundleException {
			bundle.uninstall();
		}
	}

	/**
	 * Uses the {@link BundleChecker} resource checker (above) to install a
	 * unique bundle at each callback point.
	 */
	@TestFactory
	public Stream<DynamicNode> cleansUpBundlesMultiLevel() {
		BundleContextMultiLevelCleanupTest.setFactory(BundleChecker::new);
		return runMultilevelTestClass("Bundle", BundleContextMultiLevelCleanupTest.class);
	}

	@TestFactory
	public Stream<DynamicNode> cleansUpBundleInstallerMultiLevel() {
		return runMultilevelTestClass("Install Bundle", BundleInstallerMultiLevelCleanupTest.class);
	}

	static public class BundleListenerResourceChecker extends BundleContextResourceChecker<BundleListener> {

		final Bundle bundle;

		BundleListenerResourceChecker(BundleContext bc, Map<CallbackPoint, BundleListener> resourcesMap,
			Bundle bundle) {
			super(bc, resourcesMap);
			this.bundle = bundle;
		}

		@Override
		public BundleListener doSetupResource(CallbackPoint currentPoint) {
			BundleListener listener = mock(SynchronousBundleListener.class);
			bc.addBundleListener(listener);
			return listener;
		}

		@Override
		public void doAssertSetup(CallbackPoint currentPoint, CallbackPoint pointThatShouldBeSetup,
			BundleListener listener) throws BundleException {
			reset(listener);
			assertThat(bundle)
				.isInStateMaskedBy(Bundle.RESOLVED | Bundle.INSTALLED);
			bundle.start();

			OSGiSoftAssertions softly = new OSGiSoftAssertions();
			try {

				final ArgumentCaptor<BundleEvent> startEventCapture = ArgumentCaptor.forClass(BundleEvent.class);
				softly.check(() -> verify(listener, atLeast(2)).bundleChanged(startEventCapture.capture()));
				List<BundleEvent> ourBundleEvents = startEventCapture.getAllValues()
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
				ourBundleEvents = stopEventCapture.getAllValues()
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
		public void doAssertNotSetup(CallbackPoint currentPoint, CallbackPoint pointThatShouldNotBeSetup,
			BundleListener listener) throws BundleException {
			reset(listener);
			bundle.start();
			bundle.stop();
			verifyNoInteractions(listener);
		}

		@Override
		void doTearDownResource(CallbackPoint currentPoint, BundleListener r) {
			bc.removeBundleListener(r);
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
				Map<CallbackPoint, BundleListener> map) -> new BundleListenerResourceChecker(bc, map, installedBundle));
			return runMultilevelTestClass("BundleListener", BundleContextMultiLevelCleanupTest.class);
		} finally {
			installedBundle.uninstall();
		}
	}

	static class ServiceResourceChecker extends BundleContextResourceChecker<ServiceRegistration<String>> {

		public ServiceResourceChecker(BundleContext bc, Map<CallbackPoint, ServiceRegistration<String>> resources) {
			super(bc, resources);
		}

		@Override
		public ServiceRegistration<String> doSetupResource(CallbackPoint currentPoint) {
			return bc.registerService(String.class, "Service for callback point " + currentPoint,
				dictionaryOf("multileveltest.scope", currentPoint.toString()));
		}

		@Override
		public void doAssertSetup(CallbackPoint currentPoint, CallbackPoint pointThatShouldBeSetup,
			ServiceRegistration<String> registration) {
			assertThat(registration).isNotNull();
			try {
				ServiceReference<String> reference = registration.getReference();
				assertThat(bundle.getRegisteredServices())
					.as(currentPoint + ": checking resource from " + pointThatShouldBeSetup + "\n")
					.contains(registration.getReference());
			} catch (IllegalStateException e) {
				fail(String.format("[%s: checking resource from %s]:\nservice not registered: %s", currentPoint,
					pointThatShouldBeSetup, registration), e);
			}
		}

		@Override
		public void doAssertNotSetup(CallbackPoint currentPoint, CallbackPoint pointThatShouldNotBeSetup,
			ServiceRegistration<String> registration) {
			assertThat(registration).isNotNull();
			ServiceReference<?>[] registeredServices = bundle.getRegisteredServices();
			try {
				registration.getReference();
				fail(String.format("[%s: checking resource from %s]:\nservice was not unregistered: %s ", currentPoint,
					pointThatShouldNotBeSetup, registration));
			} catch (IllegalStateException e) {}
		}

		@Override
		void doTearDownResource(CallbackPoint currentPoint, ServiceRegistration<String> r) throws Exception {
			r.unregister();
		}

	}

	@TestFactory
	public Stream<DynamicNode> cleansUpRegisteredServicesMultiLevel() {
		BundleContextMultiLevelCleanupTest.setFactory(ServiceResourceChecker::new);
		return runMultilevelTestClass("ServiceRegistration", BundleContextMultiLevelCleanupTest.class);
	}

	static public class ServiceListenerResourceChecker extends BundleContextResourceChecker<ServiceListener> {
		final Bundle bundle;

		ServiceListenerResourceChecker(BundleContext bc, Map<CallbackPoint, ServiceListener> resources, Bundle bundle) {
			super(bc, resources);
			this.bundle = bundle;
		}

		@Override
		public ServiceListener doSetupResource(CallbackPoint currentPoint) throws InvalidSyntaxException {
			ServiceListener listener = mock(ServiceListener.class);
			// Use a filter so that we're not getting interfered with by other
			// tests
			bc.addServiceListener(listener, "(servicelistener.test.scope=" + currentPoint + ")");
			return listener;
		}

		@Override
		public void doAssertSetup(CallbackPoint currentPoint, CallbackPoint pointThatShouldBeSetup,
			ServiceListener listener) throws BundleException {
			reset(listener);
			ServiceRegistration<String> reg = bundle.getBundleContext()
				.registerService(String.class, "test",
					dictionaryOf("servicelistener.test.scope", pointThatShouldBeSetup.toString()));
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
		public void doAssertNotSetup(CallbackPoint currentPoint, CallbackPoint pointThatShouldNotBeSetup,
			ServiceListener listener) {
			reset(listener);
			ServiceRegistration<String> reg = bundle.getBundleContext()
				.registerService(String.class, "test",
					dictionaryOf("servicelistener.test.scope", pointThatShouldNotBeSetup.toString()));
			reg.unregister();
			verifyNoInteractions(listener);
		}

		@Override
		void doTearDownResource(CallbackPoint currentPoint, ServiceListener r) throws Exception {
			bc.removeServiceListener(r);
		}
	}

	@TestFactory
	public Stream<DynamicNode> cleansUpServiceListenersMultiLevel() throws Exception {
		Bundle bundle = FrameworkUtil.getBundle(getClass());
		Bundle installedBundle = bundle.getBundleContext()
			.installBundle("it", getBundle("tb1.jar"));
		try {
			installedBundle.start();
			BundleContextMultiLevelCleanupTest.setFactory(
				(BundleContext bc, Map<CallbackPoint, ServiceListener> map) -> new ServiceListenerResourceChecker(bc,
					map, installedBundle));
			return runMultilevelTestClass("ServiceListener", BundleContextMultiLevelCleanupTest.class);
		} finally {
			installedBundle.uninstall();
		}
	}

	static public class FrameworkListenerResourceChecker extends BundleContextResourceChecker<FrameworkListener> {

		final BundleContext			originalContext;
		final Bundle				systemBundle;
		final FrameworkStartLevel	startLevel;

		FrameworkListenerResourceChecker(BundleContext bc, Map<CallbackPoint, FrameworkListener> resources) {
			super(bc, resources);
			originalContext = FrameworkUtil.getBundle(FrameworkListenerResourceChecker.class)
				.getBundleContext();
			systemBundle = originalContext.getBundle(0);
			startLevel = systemBundle.adapt(FrameworkStartLevel.class);

		}

		@Override
		public FrameworkListener doSetupResource(CallbackPoint currentPoint) throws InvalidSyntaxException {
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
		public void doAssertSetup(CallbackPoint currentPoint, CallbackPoint pointThatShouldBeSetup,
			FrameworkListener listener) {
			reset(listener);
			tryToStart();

			OSGiSoftAssertions softly = new OSGiSoftAssertions();
			try {

				final ArgumentCaptor<FrameworkEvent> startEventCapture = ArgumentCaptor.forClass(FrameworkEvent.class);
				softly.check(() -> verify(listener).frameworkEvent(startEventCapture.capture()));
				softly.assertThat(startEventCapture.getValue())
					.as("registered")
					.isOfType(FrameworkEvent.STARTLEVEL_CHANGED)
					.hasBundle(systemBundle);
			} finally {
				softly.assertAll();
			}
		}

		@Override
		public void doAssertNotSetup(CallbackPoint currentPoint, CallbackPoint pointThatShouldNotBeSetup,
			FrameworkListener listener) {
			reset(listener);
			tryToStart();
			verifyNoInteractions(listener);
		}

		@Override
		void doTearDownResource(CallbackPoint currentPoint, FrameworkListener listener) throws Exception {
			bc.removeFrameworkListener(listener);
		}

	}

	@TestFactory
	public Stream<DynamicNode> cleansUpFrameworkListenersMultiLevel() throws Exception {
		BundleContextMultiLevelCleanupTest.setFactory(FrameworkListenerResourceChecker::new);
		return runMultilevelTestClass("FrameworkListener", BundleContextMultiLevelCleanupTest.class);
	}

	static class GottenServicesResourceChecker extends BundleContextResourceChecker<ServiceReference<Foo>> {
		GottenServicesResourceChecker(BundleContext bc, Map<CallbackPoint, ServiceReference<Foo>> resources) {
			super(bc, resources);
		}

		@Override
		public ServiceReference<Foo> doSetupResource(CallbackPoint currentPoint) throws InvalidSyntaxException {
			ServiceReference<Foo> reference = bc.getServiceReferences(Foo.class, "(test.scope=" + currentPoint + ")")
				.iterator()
				.next();
			bc.getService(reference);
			bc.getService(reference);
			return reference;
		}

		@Override
		public void doAssertSetup(CallbackPoint currentPoint, CallbackPoint pointThatShouldBeSetup,
			ServiceReference<Foo> reference) {
			assertThat(reference)
				.as(String.format("%s checking resource from %s setup: reference", currentPoint,
					pointThatShouldBeSetup))
				.isNotNull();
			assertThat(bundle.getServicesInUse())
				.as(String.format("%s checking resource from %s setup: inUse", currentPoint, pointThatShouldBeSetup))
				.isNotNull()
				.contains(reference);
		}

		@Override
		public void doAssertNotSetup(CallbackPoint currentPoint, CallbackPoint pointThatShouldNotBeSetup,
			ServiceReference<Foo> reference) {
			assertThat(reference)
				.as(String.format("%s checking resource from %s not setup: reference", currentPoint,
					pointThatShouldNotBeSetup))
				.isNotNull();
			ServiceReference<?>[] inUse = bundle.getServicesInUse();
			if (inUse != null) {
				Stream.of(inUse)
					.forEach(ref -> {
						String testScope = (String) ref.getProperty("test.scope");
						if (pointThatShouldNotBeSetup.name()
							.equals(testScope)) {
							fail(
								String.format("[%s: checking resource from %s]:\nstill had a reference to service: %s ",
									currentPoint, pointThatShouldNotBeSetup, ref));
						}
					});
			}
		}

		@Override
		void doTearDownResource(CallbackPoint currentPoint, ServiceReference<Foo> reference) throws Exception {
			bc.ungetService(reference);
			bc.ungetService(reference);
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
			Stream.of(CallbackPoint.values())
				.map(Object::toString)
				.forEach(callbackPoint -> {
					registrations.add(installedBundleContext.registerService(Foo.class, new Foo() {},
						dictionaryOf("test.scope", callbackPoint)));
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

	static class GottenServiceObjects {
		Foo					service1;
		Foo					service2;
		ServiceObjects<Foo>	services;
	}

	static class GottenServiceObjectsResourceChecker extends BundleContextResourceChecker<GottenServiceObjects> {
		GottenServiceObjectsResourceChecker(BundleContext bc, Map<CallbackPoint, GottenServiceObjects> resources) {
			super(bc, resources);
		}

		@Override
		public GottenServiceObjects doSetupResource(CallbackPoint currentPoint) throws InvalidSyntaxException {
			ServiceReference<Foo> reference = bc.getServiceReferences(Foo.class, "(test.scope=" + currentPoint + ")")
				.iterator()
				.next();
			GottenServiceObjects serviceObjects = new GottenServiceObjects();
			ServiceObjects<Foo> services = bc.getServiceObjects(reference);
			serviceObjects.services = services;
			serviceObjects.service1 = services.getService();
			serviceObjects.service2 = services.getService();
			return serviceObjects;
		}

		@Override
		public void doAssertSetup(CallbackPoint currentPoint, CallbackPoint pointThatShouldBeSetup,
			GottenServiceObjects services) {
			assertThat(services.services)
				.as(String.format("%s checking resource from %s setup: services", currentPoint, pointThatShouldBeSetup))
				.isNotNull();
			assertThat(bundle.getServicesInUse())
				.as(String.format("%s checking resource from %s setup: inUse", currentPoint, pointThatShouldBeSetup))
				.isNotNull()
				.contains(services.services.getServiceReference());
		}

		@Override
		public void doAssertNotSetup(CallbackPoint currentPoint, CallbackPoint pointThatShouldNotBeSetup,
			GottenServiceObjects services) {
			assertThat(services.services)
				.as(String.format("%s checking resource from %s not setup: services", currentPoint,
					pointThatShouldNotBeSetup))
				.isNotNull();
			ServiceReference<?>[] inUse = bundle.getServicesInUse();
			if (inUse != null) {
				assertThat(inUse)
					.as(String.format("%s checking resource from %s not setup: inUse", currentPoint,
						pointThatShouldNotBeSetup))
					.doesNotContain(services.services.getServiceReference());
			}
		}

		@Override
		void doTearDownResource(CallbackPoint currentPoint, GottenServiceObjects r) throws Exception {
			r.services.ungetService(r.service1);
			r.services.ungetService(r.service2);
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
			Stream.of(CallbackPoint.values())
				.map(Object::toString)
				.forEach(callbackPoint -> {
					registrations.add(installedBundleContext.registerService(Foo.class, new Foo() {},
						dictionaryOf("test.scope", callbackPoint.toString())));
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
