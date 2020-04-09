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

package org.osgi.test.junit5.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Store;
import org.mockito.stubbing.Answer;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceRegistration;
import org.osgi.test.common.dictionary.Dictionaries;
import org.osgi.test.common.tracking.TrackServices;
import org.osgi.test.junit5.ExecutorExtension;
import org.osgi.test.junit5.ExecutorParameter;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.context.WithBundleContextExtension;
import org.osgi.test.junit5.types.Foo;
import org.osgi.test.junit5.types.MockStore;

@ExtendWith(ExecutorExtension.class)
public class ServiceExtensionTest {

	@ExecutorParameter
	ScheduledExecutorService	executor;

	ExtensionContext			extensionContext;
	Store						store;

	String						testMethodName;

	@BeforeEach
	public void beforeEach(TestInfo testInfo) {
		testMethodName = testInfo.getTestMethod()
			.get()
			.getName();
		extensionContext = mock(ExtensionContext.class);
		store = new MockStore();

		when(extensionContext.getRequiredTestClass()).then((Answer<Class<?>>) a -> getClass());
		when(extensionContext.getStore(any())).then((Answer<Store>) a -> store);
	}

	@AfterEach
	public void afterEach() {
		assertThat(FrameworkUtil.getBundle(getClass())
			.getRegisteredServices()).as("registered services")
				.isNull();
	}

	@Test
	public void basicAssumptions() throws Exception {
		try (WithServiceExtension<Foo> it = new WithServiceExtension<Foo>(Foo.class, null, 0,
			TrackServices.DEFAULT_TIMEOUT)) {

			it.init(BundleContextExtension.getBundleContext(extensionContext));

			SoftAssertions softly = new SoftAssertions();

			softly.assertThat(it.getExtension()
				.isEmpty())
				.as("isEmpty %s", it.getExtension())
				.isTrue();
			softly.assertThat(it.getExtension()
				.getTimeout())
				.as("getTimeout %s", it.getExtension())
				.isEqualTo(TrackServices.DEFAULT_TIMEOUT);
			softly.assertThat(it.getExtension()
				.getCardinality())
				.as("getCardinality %s", it.getExtension())
				.isEqualTo(0);

			softly.assertAll();
		}
	}

	@Test
	public void requiredFailsWhenNoService() throws Exception {
		assertThatExceptionOfType(AssertionError.class) //
			.isThrownBy(() -> {
				try (WithServiceExtension<Foo> it = new WithServiceExtension<Foo>(Foo.class, null, 1,
					TrackServices.DEFAULT_TIMEOUT)) {
					it.init(BundleContextExtension.getBundleContext(extensionContext));
				}
			})
			.withMessageContaining(" services (objectClass=org.osgi.test.junit5.types.Foo) didn't arrive within 200ms");
	}

	@Test
	public void requiredFailsWhenNoServiceWithTimeout() throws Exception {
		assertThatExceptionOfType(AssertionError.class) //
			.isThrownBy(() -> {
				try (WithServiceExtension<Foo> it = new WithServiceExtension<Foo>(Foo.class, null, 1, 50)) {
					it.init(BundleContextExtension.getBundleContext(extensionContext));
				}
			})
			.withMessageContaining(" services (objectClass=org.osgi.test.junit5.types.Foo) didn't arrive within 50ms");
	}

	@Test
	public void successWhenService() throws Exception {
		try (WithBundleContextExtension bce = new WithBundleContextExtension(extensionContext);
			WithServiceExtension<Foo> it = new WithServiceExtension<Foo>(Foo.class, null,
				1,
			TrackServices.DEFAULT_TIMEOUT)) {

			final Foo afoo = new Foo() {};

			BundleContext bundleContext = bce.getBundleContext();

			ScheduledFuture<ServiceRegistration<?>> scheduledFuture = executor.schedule(
				() -> bundleContext.registerService(Foo.class, afoo, Dictionaries.dictionaryOf("case", testMethodName)),
				0, TimeUnit.MILLISECONDS);

			it.init(bundleContext);
			// Make sure the scheduled event is processed
			assertThat(scheduledFuture.get()).isNotNull();

			SoftAssertions softly = new SoftAssertions();

			softly.assertThat(it.getExtension()
				.isEmpty())
				.as("isEmpty %s", it.getExtension())
				.isFalse();
			softly.assertThat(it.getExtension()
				.size())
				.as("size %s", it.getExtension())
				.isEqualTo(1);
			softly.assertThat(it.getExtension()
				.getTimeout())
				.as("getTimeout %s", it.getExtension())
				.isEqualTo(TrackServices.DEFAULT_TIMEOUT);
			softly.assertThat(it.getExtension()
				.getCardinality())
				.as("getCardinality %s", it.getExtension())
				.isGreaterThan(0);

			softly.assertAll();
		}
	}

	@Test
	public void successWhenServiceWithTimeout() throws Exception {
		try (WithBundleContextExtension bce = new WithBundleContextExtension(extensionContext);
			WithServiceExtension<Foo> it = new WithServiceExtension<Foo>(Foo.class, null, 1, 1000)) {

			final Foo afoo = new Foo() {};

			BundleContext bundleContext = bce.getBundleContext();

			ScheduledFuture<ServiceRegistration<?>> scheduledFuture = executor.schedule(
				() -> bundleContext.registerService(Foo.class, afoo, Dictionaries.dictionaryOf("case", testMethodName)),
				0, TimeUnit.MILLISECONDS);

			it.init(bundleContext);
			// Make sure the scheduled event is processed
			assertThat(scheduledFuture.get()).isNotNull();

			SoftAssertions softly = new SoftAssertions();

			softly.assertThat(it.getExtension()
				.getService())
				.as("getService %s", it.getExtension())
				.isEqualTo(afoo);
			softly.assertThat(it.getExtension()
				.getServiceReference())
				.as("getServiceReference %s", it.getExtension())
				.isNotNull();
			softly.assertThat(it.getExtension()
				.getServiceReferences())
				.as("getServiceReferences %s", it.getExtension())
				.hasSize(1);
			softly.assertThat(it.getExtension()
				.getServices())
				.as("getServices %s", it.getExtension())
				.containsExactly(afoo);
			softly.assertThat(it.getExtension()
				.getTimeout())
				.as("getTimeout %s", it.getExtension())
				.isEqualTo(1000);
			softly.assertThat(it.getExtension()
				.getTracked())
				.as("getTracked %s", it.getExtension())
				.hasSize(1);
			softly.assertThat(it.getExtension()
				.getTrackingCount())
				.as("getTrackingCount %s", it.getExtension())
				.isGreaterThanOrEqualTo(1);
			softly.assertThat(it.getExtension()
				.getCardinality())
				.as("getCardinality %s", it.getExtension())
				.isEqualTo(1);
			softly.assertThat(it.getExtension()
				.size())
				.as("size %s", it.getExtension())
				.isEqualTo(1);
			softly.assertThat(it.getExtension()
				.isEmpty())
				.as("isEmpty %s", it.getExtension())
				.isFalse();
			softly.assertThat(it.getExtension()
				.waitForService(20))
				.as("waitForService %s", it.getExtension())
				.isEqualTo(afoo);

			softly.assertAll();
		}
	}

	@Test
	public void matchByFilter() throws Exception {
		try (WithBundleContextExtension bce = new WithBundleContextExtension(extensionContext);
			WithServiceExtension<Foo> it = new WithServiceExtension<Foo>(Foo.class, "(foo=bar)",
				1,
			TrackServices.DEFAULT_TIMEOUT)) {

			final Foo afoo = new Foo() {};

			BundleContext bundleContext = bce.getBundleContext();

			ScheduledFuture<ServiceRegistration<?>> scheduledFuture = executor.schedule(() -> bundleContext
				.registerService(Foo.class, afoo, Dictionaries.dictionaryOf("foo", "bar", "case", testMethodName)), 0,
				TimeUnit.MILLISECONDS);

			it.init(bundleContext);
			// Make sure the scheduled event is processed
			assertThat(scheduledFuture.get()).isNotNull();

			SoftAssertions softly = new SoftAssertions();

			softly.assertThat(it.getExtension()
				.getService())
				.as("getService %s", it.getExtension())
				.isNotNull();
			softly.assertThat(it.getExtension()
				.getServiceReference())
				.as("getServiceReference %s", it.getExtension())
				.isNotNull();
			softly.assertThat(it.getExtension()
				.getServiceReferences())
				.as("getServiceReferences %s", it.getExtension())
				.isNotNull();
			softly.assertThat(it.getExtension()
				.getServices())
				.as("getServices %s", it.getExtension())
				.contains(afoo);
			softly.assertThat(it.getExtension()
				.getTimeout())
				.as("getTimeout %s", it.getExtension())
				.isEqualTo(TrackServices.DEFAULT_TIMEOUT);
			softly.assertThat(it.getExtension()
				.getTracked())
				.as("waitForService %s", it.getExtension())
				.isNotEmpty();
			softly.assertThat(it.getExtension()
				.getTrackingCount())
				.as("getTrackingCount %s", it.getExtension())
				.isGreaterThanOrEqualTo(1);
			softly.assertThat(it.getExtension()
				.getCardinality())
				.as("getCardinality %s", it.getExtension())
				.isEqualTo(1);
			softly.assertThat(it.getExtension()
				.size())
				.as("size %s", it.getExtension())
				.isEqualTo(1);
			softly.assertThat(it.getExtension()
				.isEmpty())
				.as("isEmpty %s", it.getExtension())
				.isFalse();
			softly.assertThat(it.getExtension()
				.waitForService(20))
				.as("waitForService %s", it.getExtension())
				.isNotNull();

			softly.assertAll();
		}
	}

	@Test
	public void matchMultiple() throws Exception {
		try (WithBundleContextExtension bce = new WithBundleContextExtension(extensionContext);
			WithServiceExtension<Foo> it = new WithServiceExtension<Foo>(Foo.class, null,
				2,
			TrackServices.DEFAULT_TIMEOUT)) {

			Foo s1 = new Foo() {}, s2 = new Foo() {};
			BundleContext bundleContext = bce.getBundleContext();

			ScheduledFuture<ServiceRegistration<?>> scheduledFuture1 = executor.schedule(() -> bundleContext
				.registerService(Foo.class, s1, Dictionaries.dictionaryOf("foo", testMethodName.concat("_1"))), 0,
				TimeUnit.MILLISECONDS);
			ScheduledFuture<ServiceRegistration<?>> scheduledFuture2 = executor.schedule(() -> bundleContext
				.registerService(Foo.class, s2, Dictionaries.dictionaryOf("foo", testMethodName.concat("_2"))), 0,
				TimeUnit.MILLISECONDS);

			it.init(bundleContext);
			// Make sure the scheduled event is processed
			assertThat(scheduledFuture1.get()).isNotNull();
			assertThat(scheduledFuture2.get()).isNotNull();

			SoftAssertions softly = new SoftAssertions();

			softly.assertThat(it.getExtension()
				.getService())
				.as("getService %s", it.getExtension())
				.isIn(s1, s2);
			softly.assertThat(it.getExtension()
				.getServiceReference())
				.as("getServiceReference %s", it.getExtension())
				.isNotNull();
			softly.assertThat(it.getExtension()
				.getServiceReferences())
				.as("getServiceReferences %s", it.getExtension())
				.isNotNull();
			softly.assertThat(it.getExtension()
				.getServices())
				.as("getServices %s", it.getExtension())
				.containsExactlyInAnyOrder(s1, s2);
			softly.assertThat(it.getExtension()
				.getTimeout())
				.as("getTimeout %s", it.getExtension())
				.isEqualTo(TrackServices.DEFAULT_TIMEOUT);
			softly.assertThat(it.getExtension()
				.getTracked())
				.as("getTracked %s", it.getExtension())
				.isNotEmpty();
			softly.assertThat(it.getExtension()
				.getTrackingCount())
				.as("getTrackingCount %s", it.getExtension())
				.isGreaterThanOrEqualTo(2);
			softly.assertThat(it.getExtension()
				.getCardinality())
				.as("getCardinality %s", it.getExtension())
				.isEqualTo(2);
			softly.assertThat(it.getExtension()
				.size())
				.as("size %s", it.getExtension())
				.isEqualTo(2);
			softly.assertThat(it.getExtension()
				.isEmpty())
				.as("isEmpty %s", it.getExtension())
				.isFalse();
			softly.assertThat(it.getExtension()
				.waitForService(20))
				.as("waitForService %s", it.getExtension())
				.isNotNull();

			softly.assertAll();
		}
	}

	@Test
	public void nomatchByFilter() throws Exception {
		assertThatExceptionOfType(AssertionError.class) //
			.isThrownBy(() -> {
				try (WithBundleContextExtension bce = new WithBundleContextExtension(extensionContext);
					WithServiceExtension<Foo> it = new WithServiceExtension<Foo>(Foo.class, "(foo=baz)",
						1,
					TrackServices.DEFAULT_TIMEOUT)) {

					final Foo afoo = new Foo() {};
						BundleContext bundleContext = bce.getBundleContext();

						ScheduledFuture<ServiceRegistration<?>> scheduledFuture = executor.schedule(
							() -> bundleContext.registerService(Foo.class, afoo,
								Dictionaries.dictionaryOf("foo", "bar", "case", testMethodName)),
							0, TimeUnit.MILLISECONDS);

						it.init(bundleContext);
					// Make sure the scheduled event is processed
					assertThat(scheduledFuture.get()).isNotNull();
				}
			});
	}

	@Test
	public void malformedFilter() throws Exception {
		assertThatExceptionOfType(InvalidSyntaxException.class) //
			.isThrownBy(() -> {
				try (WithServiceExtension<Foo> it = new WithServiceExtension<Foo>(Foo.class, "(foo=baz", 1,
					TrackServices.DEFAULT_TIMEOUT)) {
				}
			});
	}

	@Test
	public void negativeCardinality() throws Exception {
		assertThatExceptionOfType(IllegalArgumentException.class) //
			.isThrownBy(() -> {
				try (WithServiceExtension<Foo> it = new WithServiceExtension<Foo>(Foo.class, "", -1,
					TrackServices.DEFAULT_TIMEOUT)) {
				}
			});
	}

	@Test
	public void negativeTimeout() throws Exception {
		assertThatExceptionOfType(IllegalArgumentException.class) //
			.isThrownBy(() -> {
				try (WithServiceExtension<Foo> it = new WithServiceExtension<Foo>(Foo.class, "", 1, -1)) {
				}
			});
	}

}
