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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.osgi.framework.Bundle.INSTALLED;
import static org.osgi.framework.Bundle.UNINSTALLED;
import static org.osgi.test.junit5.TestUtil.getBundle;

import java.util.concurrent.atomic.AtomicReference;

import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Store;
import org.mockito.stubbing.Answer;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceObjects;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.SynchronousBundleListener;
import org.osgi.test.junit5.common.InsideOsgiFrameworkOnlyTest;
import org.osgi.test.junit5.types.Foo;
import org.osgi.test.junit5.types.MockStore;

public class BundleContextExtensionTest {

	ExtensionContext	extensionContext;
	Store				store;

	@BeforeEach
	public void beforeEach() {
		extensionContext = mock(ExtensionContext.class);
		store = new MockStore();

		when(extensionContext.getRequiredTestClass()).then((Answer<Class<?>>) a -> getClass());
		when(extensionContext.getStore(any())).then((Answer<Store>) a -> store);
	}

	@Test
	@InsideOsgiFrameworkOnlyTest
	public void testInstallBundle_B() throws Exception {
		Bundle bundle = null;

		try (WithBundleContextExtension it = new WithBundleContextExtension(extensionContext)) {
			bundle = it.getExtension()
				.getInstallbundle(extensionContext)
				.installBundle("foo/tbfoo.jar", false);

			assertThat(bundle).extracting(Bundle::getState)
				.is(new Condition<Integer>(state -> (state & INSTALLED) == INSTALLED, "Installed"));
		}

		assertThat(bundle).extracting(Bundle::getState)
			.is(new Condition<Integer>(state -> (state & UNINSTALLED) == UNINSTALLED, "Uninstalled"));
	}

	@Test
	@InsideOsgiFrameworkOnlyTest
	public void testInstallBundle() throws Exception {
		Bundle bundle = null;

		try (WithBundleContextExtension it = new WithBundleContextExtension(extensionContext)) {
			bundle = it.getExtension()
				.getInstallbundle(extensionContext)
				.installBundle("tb1.jar", false);

			assertThat(bundle).extracting(Bundle::getState)
				.is(new Condition<Integer>(state -> (state & INSTALLED) == INSTALLED, "Installed"));
		}

		assertThat(bundle).extracting(Bundle::getState)
			.is(new Condition<Integer>(state -> (state & UNINSTALLED) == UNINSTALLED, "Uninstalled"));
	}

	@Test
	@InsideOsgiFrameworkOnlyTest
	public void test() throws Exception {
		try (WithBundleContextExtension it = new WithBundleContextExtension(extensionContext)) {
			BundleContext bundleContext = it.getBundleContext();

			assertThat(bundleContext).isNotNull()
				.extracting(BundleContext::getBundle)
				.isEqualTo(FrameworkUtil.getBundle(getClass()));
		}
	}

	@Test
	@InsideOsgiFrameworkOnlyTest // TODO: use Tags as flag?
	public void cleansUpServices() throws Exception {
		Bundle bundle = FrameworkUtil.getBundle(getClass());

		try (WithBundleContextExtension it = new WithBundleContextExtension(extensionContext)) {
			BundleContext bundleContext = it.getBundleContext();

			ServiceRegistration<Foo> serviceRegistration = bundleContext.registerService(Foo.class, new Foo() {}, null);

			assertThat(bundle.getRegisteredServices()).isNotEmpty()
				.contains(serviceRegistration.getReference());
		}

		assertThat(bundle.getRegisteredServices()).isNull();
	}

	@Test

	public void cleansUpServicesOutsideOSGi() throws Exception {

		BundleContext bundleContext = null;
		Bundle bundle = null;
		ServiceRegistration<Foo> serviceRegistration = null;
		ServiceReference<Foo> serviceReference = null;
		try (WithBundleContextExtension it = new WithBundleContextExtension(extensionContext)) {
			bundleContext = it.getBundleContext();
			bundle = bundleContext.getBundle();
			serviceRegistration = bundleContext.registerService(Foo.class, new Foo() {}, null);
			serviceReference = serviceRegistration.getReference();
			assertThat(bundle.getRegisteredServices()).isNotEmpty()
				.contains(serviceRegistration.getReference());
		}

		final ServiceRegistration<Foo> serviceRegistrationF = serviceRegistration;
		Assertions.assertThrows(IllegalStateException.class, () -> {
			serviceRegistrationF.getReference();
		});
		assertThat(bundle.getRegisteredServices()).doesNotContain(serviceReference);
	}

	@Test
	public void cleansUpBundlesInsideOSGi() throws Exception {
		Bundle bundle = FrameworkUtil.getBundle(getClass());
		Bundle installedBundle = null;
		long bundleId = -1;

		try (WithBundleContextExtension it = new WithBundleContextExtension(extensionContext)) {
			BundleContext bundleContext = it.getBundleContext();

			installedBundle = bundleContext.installBundle("it", getBundle("tb1.jar"));

			bundleId = installedBundle.getBundleId();

			assertThat(bundle.getBundleContext()
				.getBundle(bundleId)).isNotNull()
					.matches(installedBundle::equals);
		}

		assertThat(bundle.getBundleContext()
			.getBundle(bundleId)).isNull();
		assertThat(installedBundle).isNotNull()
			.extracting(Bundle::getState)
			.isEqualTo(Bundle.UNINSTALLED);
	}

	@Test
	public void cleansUpBundlesOutsideOsgi() throws Exception {
		Bundle installedBundle = null;
		long bundleId = -1;
		BundleContext bundleContext = null;
		try (WithBundleContextExtension it = new WithBundleContextExtension(extensionContext)) {
			bundleContext = it.getBundleContext();

			installedBundle = bundleContext.installBundle("it", getBundle("tb1.jar"));

			bundleId = installedBundle.getBundleId();

			assertThat(bundleContext
				.getBundle(bundleId)).isNotNull()
					.matches(installedBundle::equals);
		}
		assertThat(bundleContext.getBundle(bundleId)).isNull();


		assertThat(installedBundle).isNotNull()
			.extracting(Bundle::getState)
			.isEqualTo(Bundle.UNINSTALLED);
	}

	@Test
	public void cleansUpListeners() throws Exception {

		Bundle installedBundle = null;

		final AtomicReference<BundleEvent> ref = new AtomicReference<BundleEvent>();

		BundleListener bl = new SynchronousBundleListener() {
			@Override
			public void bundleChanged(BundleEvent event) {
				ref.set(event);
			}
		};

		BundleContext bundleContext = null;
		try (WithBundleContextExtension it = new WithBundleContextExtension(extensionContext)) {
			bundleContext = it.getBundleContext();

			bundleContext.addBundleListener(bl);

			installedBundle = bundleContext.installBundle("it", getBundle("tb1.jar"));

			assertThat(ref.get()).isNotNull()
				.extracting(BundleEvent::getBundle)
				.isEqualTo(installedBundle);
		}

		assertThat(installedBundle).isNotNull()
			.extracting(Bundle::getState)
			.isEqualTo(Bundle.UNINSTALLED);

		// now reset the ref
		ref.set(null);

		try {
			// re-install the bundle
			installedBundle = bundleContext
				.installBundle("it", getBundle("tb1.jar"));

			// check that the listener didn't notice this last bundle
			// install
			assertThat(ref.get()).isNull();
		} finally {
			installedBundle.uninstall();
		}
	}

	@Test
	@InsideOsgiFrameworkOnlyTest
	public void cleansUpGottenServices() throws Exception {
		Bundle bundle = FrameworkUtil.getBundle(getClass());
		Bundle installedBundle = bundle.getBundleContext()
			.installBundle("it", getBundle("tb1.jar"));
		installedBundle.start();

		try (WithBundleContextExtension it = new WithBundleContextExtension(extensionContext)) {
			BundleContext bundleContext = it.getBundleContext();

			ServiceReference<Foo> serviceReference = bundleContext.getServiceReference(Foo.class);

			Foo foo = bundleContext.getService(serviceReference);

			assertThat(foo).isNotNull();
			assertThat(bundle.getServicesInUse()).isNotNull()
				.contains(serviceReference);
		} finally {
			installedBundle.uninstall();
		}

		assertThat(bundle.getServicesInUse()).isNull();
	}

	@Test
	@InsideOsgiFrameworkOnlyTest
	public void cleansUpGottenServiceObjects() throws Exception {
		Bundle bundle = FrameworkUtil.getBundle(getClass());
		Bundle installedBundle = bundle.getBundleContext()
			.installBundle("it", getBundle("tb1.jar"));
		installedBundle.start();

		try (WithBundleContextExtension it = new WithBundleContextExtension(extensionContext)) {
			BundleContext bundleContext = it.getBundleContext();

			ServiceReference<Foo> serviceReference = bundleContext.getServiceReference(Foo.class);

			ServiceObjects<Foo> serviceObjects = bundleContext.getServiceObjects(serviceReference);

			assertThat(serviceObjects).isNotNull();
			assertThat(serviceObjects.getService()).isNotNull();
			assertThat(bundle.getServicesInUse()).isNotNull()
				.contains(serviceReference);
		} finally {
			installedBundle.uninstall();
		}

		assertThat(bundle.getServicesInUse()).isNull();
	}

}
