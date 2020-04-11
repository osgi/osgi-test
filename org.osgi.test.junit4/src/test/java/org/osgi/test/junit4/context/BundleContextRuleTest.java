/*
 * Copyright (c) OSGi Alliance (2019, 2020). All Rights Reserved.
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

package org.osgi.test.junit4.context;

import static org.assertj.core.api.Assertions.assertThat;
import static org.osgi.framework.Bundle.INSTALLED;
import static org.osgi.framework.Bundle.UNINSTALLED;
import static org.osgi.test.junit4.TestUtil.getBundle;

import java.util.concurrent.atomic.AtomicReference;

import org.assertj.core.api.Condition;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceObjects;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.SynchronousBundleListener;
import org.osgi.test.common.context.CloseableBundleContext;
import org.osgi.test.common.dictionary.Dictionaries;
import org.osgi.test.common.install.InstallBundle;
import org.osgi.test.junit4.types.Foo;

public class BundleContextRuleTest {

	@Rule
	public TestName name = new TestName();

	@Test
	public void testInstallBundle_B() throws Exception {
		Bundle bundle = null;

		try (WithContextRule it = new WithContextRule(this)) {
			InstallBundle installBundle = new InstallBundle(it.rule.getBundleContext());

			bundle = installBundle.installBundle("foo/tbfoo.jar", false);

			assertThat(bundle).extracting(Bundle::getState)
				.is(new Condition<Integer>(state -> (state & INSTALLED) == INSTALLED, "Installed"));
		}

		assertThat(bundle).extracting(Bundle::getState)
			.is(new Condition<Integer>(state -> (state & UNINSTALLED) == UNINSTALLED, "Uninstalled"));
	}

	@Test
	public void testInstallBundle() throws Exception {
		Bundle bundle = null;

		try (WithContextRule it = new WithContextRule(this)) {
			InstallBundle installBundle = new InstallBundle(it.rule.getBundleContext());

			bundle = installBundle.installBundle("tb1.jar", false);

			assertThat(bundle).extracting(Bundle::getState)
				.is(new Condition<Integer>(state -> (state & INSTALLED) == INSTALLED, "Installed"));
		}

		assertThat(bundle).extracting(Bundle::getState)
			.is(new Condition<Integer>(state -> (state & UNINSTALLED) == UNINSTALLED, "Uninstalled"));
	}

	@Test
	public void test() throws Exception {
		try (WithContextRule it = new WithContextRule(this)) {
			BundleContext bundleContext = it.rule.getBundleContext();

			assertThat(bundleContext).isNotNull()
				.extracting(BundleContext::getBundle)
				.isEqualTo(FrameworkUtil.getBundle(getClass()));
		}
	}

	@Test
	public void cleansUpServices() throws Exception {
		Bundle bundle = FrameworkUtil.getBundle(getClass());

		try (WithContextRule it = new WithContextRule(this)) {
			BundleContext bundleContext = it.rule.getBundleContext();

			ServiceRegistration<Foo> serviceRegistration = bundleContext.registerService(Foo.class, new Foo() {},
				Dictionaries.dictionaryOf("case", name.getMethodName()));

			assertThat(bundle.getRegisteredServices()).contains(serviceRegistration.getReference());
		}

		assertThat(bundle.getRegisteredServices()).isNull();
	}

	@Test
	public void cleansUpBundles() throws Exception {
		Bundle bundle = FrameworkUtil.getBundle(getClass());
		Bundle installedBundle = null;
		long bundleId = -1;

		try (WithContextRule it = new WithContextRule(this)) {
			BundleContext bundleContext = it.rule.getBundleContext();

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
	public void cleansUpListeners() throws Exception {
		Bundle bundle = FrameworkUtil.getBundle(getClass());
		Bundle installedBundle = null;

		final AtomicReference<BundleEvent> ref = new AtomicReference<BundleEvent>();

		BundleListener bl = new SynchronousBundleListener() {
			@Override
			public void bundleChanged(BundleEvent event) {
				ref.set(event);
			}
		};

		try (WithContextRule it = new WithContextRule(this)) {
			BundleContext bundleContext = it.rule.getBundleContext();

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
			installedBundle = bundle.getBundleContext()
				.installBundle("it", getBundle("tb1.jar"));

			// check that the listener didn't notice this last bundle install
			assertThat(ref.get()).isNull();
		} finally {
			installedBundle.uninstall();
		}
	}

	@Test
	public void cleansUpGottenServices() throws Exception {
		Bundle bundle = FrameworkUtil.getBundle(getClass());
		Bundle installedBundle = bundle.getBundleContext()
			.installBundle("it", getBundle("tb1.jar"));
		installedBundle.start();

		try (WithContextRule it = new WithContextRule(this)) {
			BundleContext bundleContext = it.rule.getBundleContext();

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
	public void cleansUpGottenServiceObjects() throws Exception {
		Bundle bundle = FrameworkUtil.getBundle(getClass());
		Bundle installedBundle = bundle.getBundleContext()
			.installBundle("it", getBundle("tb1.jar"));
		installedBundle.start();

		try (WithContextRule it = new WithContextRule(this)) {
			BundleContext bundleContext = it.rule.getBundleContext();

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

	@Test
	public void closeableBundleContext_handlesSelectedMethodsOfObject() throws Exception {
		BundleContext upstream = FrameworkUtil.getBundle(getClass())
			.getBundleContext();
		BundleContext closeableBC = CloseableBundleContext.proxy(getClass(), upstream);

		closeableBC.toString();
		assertThat(closeableBC).as("toString")
			.hasToString("CloseableBundleContext[" + System.identityHashCode(closeableBC) + "]:" + upstream.toString())
			.isEqualTo(upstream);
		assertThat(closeableBC.hashCode()).as("hashcode")
			.isEqualTo(upstream.hashCode());
	}
}
