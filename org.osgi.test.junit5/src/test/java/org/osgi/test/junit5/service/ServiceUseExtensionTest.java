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

package org.osgi.test.junit5.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Hashtable;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Store;
import org.mockito.stubbing.Answer;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.test.common.tracking.TrackServices;
import org.osgi.test.junit5.ExecutorExtension;
import org.osgi.test.junit5.ExecutorParameter;
import org.osgi.test.junit5.types.Foo;
import org.osgi.test.junit5.types.MockStore;

@ExtendWith(ExecutorExtension.class)
public class ServiceUseExtensionTest {

	@ExecutorParameter
	ScheduledExecutorService	executor;

	ExtensionContext	extensionContext;
	Store				store;

	@BeforeEach
	public void beforeEach() {
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
		try (WithServiceUseExtension<Foo> it = new WithServiceUseExtension<Foo>(extensionContext, //
			Foo.class, null, 0, TrackServices.DEFAULT_TIMEOUT)) {

			it.bceInit();
			it.init();

			SoftAssertions softly = new SoftAssertions();

			softly.assertThat(it.getExtension()
				.isEmpty())
				.isTrue();
			softly.assertThat(it.getExtension()
				.getTimeout())
				.isEqualTo(TrackServices.DEFAULT_TIMEOUT);
			softly.assertThat(it.getExtension()
				.getCardinality())
				.isEqualTo(0);

			softly.assertAll();
		}
	}

	@Test
	public void requiredFailsWhenNoService() throws Exception {
		assertThatExceptionOfType(AssertionError.class) //
			.isThrownBy(() -> {
				try (WithServiceUseExtension<Foo> it = new WithServiceUseExtension<Foo>(extensionContext, //
					Foo.class, null, 1, TrackServices.DEFAULT_TIMEOUT)) {

					it.bceInit();
					it.init();
				}
			})
			.withMessageContaining(
				" services (objectClass=org.osgi.test.junit5.types.Foo) didn't arrive within 200ms");
	}

	@Test
	public void requiredFailsWhenNoServiceWithTimeout() throws Exception {
		assertThatExceptionOfType(AssertionError.class) //
			.isThrownBy(() -> {
				try (WithServiceUseExtension<Foo> it = new WithServiceUseExtension<Foo>(extensionContext, //
					Foo.class, null, 1, 50)) {

					it.bceInit();
					it.init();
				}
			})
			.withMessageContaining(
				" services (objectClass=org.osgi.test.junit5.types.Foo) didn't arrive within 50ms");
	}

	@SuppressWarnings("serial")
	@Test
	public void successWhenService() throws Exception {
		try (WithServiceUseExtension<Foo> it = new WithServiceUseExtension<Foo>(extensionContext, //
			Foo.class, null, 1, TrackServices.DEFAULT_TIMEOUT)) {

			it.bceInit();

			final Foo afoo = new Foo() {};

			executor.schedule(
				() -> it.getBundleContext()
					.registerService(Foo.class, afoo, new Hashtable<String, Object>() {
						{
							put("case", "successWhenService");
						}
					}),
				0, TimeUnit.MILLISECONDS);

			it.init();

			SoftAssertions softly = new SoftAssertions();

			softly.assertThat(it.getExtension()
				.isEmpty())
				.isFalse();
			softly.assertThat(it.getExtension()
				.size())
				.isEqualTo(1);
			softly.assertThat(it.getExtension()
				.getTimeout())
				.isEqualTo(TrackServices.DEFAULT_TIMEOUT);
			softly.assertThat(it.getExtension()
				.getCardinality())
				.isGreaterThan(0);

			softly.assertAll();
		}
	}

	@SuppressWarnings("serial")
	@Test
	public void successWhenServiceWithTimeout() throws Exception {
		try (WithServiceUseExtension<Foo> it = new WithServiceUseExtension<Foo>(extensionContext, //
			Foo.class, null, 1, 1000)) {

			it.bceInit();

			final Foo afoo = new Foo() {};

			executor.schedule(
				() -> it.getBundleContext()
					.registerService(Foo.class, afoo, new Hashtable<String, Object>() {
						{
							put("case", "successWhenServiceWithTimeout");
						}
					}),
				0, TimeUnit.MILLISECONDS);

			it.init();

			SoftAssertions softly = new SoftAssertions();

			softly.assertThat(it.getExtension()
				.getService())
				.isEqualTo(afoo);
			softly.assertThat(it.getExtension()
				.getServiceReference())
				.isNotNull();
			softly.assertThat(it.getExtension()
				.getServiceReferences())
				.hasSize(1);
			softly.assertThat(it.getExtension()
				.getServices())
				.containsExactly(afoo);
			softly.assertThat(it.getExtension()
				.getTimeout())
				.isEqualTo(1000);
			softly.assertThat(it.getExtension()
				.getTracked())
				.hasSize(1);
			softly.assertThat(it.getExtension()
				.getTrackingCount())
				.isEqualTo(1);
			softly.assertThat(it.getExtension()
				.getCardinality())
				.isEqualTo(1);
			softly.assertThat(it.getExtension()
				.size())
				.isEqualTo(1);
			softly.assertThat(it.getExtension()
				.isEmpty())
				.isFalse();
			softly.assertThat(it.getExtension()
				.waitForService(20))
				.isEqualTo(afoo);

			softly.assertAll();
		}
	}

	@SuppressWarnings({
		"rawtypes", "serial", "unchecked"
	})
	@Test
	public void matchByFilter() throws Exception {
		try (WithServiceUseExtension<Foo> it = new WithServiceUseExtension<Foo>(extensionContext, //
			Foo.class, "(foo=bar)", 1, TrackServices.DEFAULT_TIMEOUT)) {

			it.bceInit();

			final Foo afoo = new Foo() {};

			executor.schedule(() -> it.getBundleContext()
				.registerService(Foo.class, afoo, new Hashtable() {
					{
						put("foo", "bar");
						put("case", "matchByFilter");
					}
				}), 0, TimeUnit.MILLISECONDS);

			it.init();

			SoftAssertions softly = new SoftAssertions();

			softly.assertThat(it.getExtension()
				.getService())
				.isNotNull();
			softly.assertThat(it.getExtension()
				.getServiceReference())
				.isNotNull();
			softly.assertThat(it.getExtension()
				.getServiceReferences())
				.isNotNull();
			softly.assertThat(it.getExtension()
				.getServices())
				.isNotEmpty()
				.contains(afoo);
			softly.assertThat(it.getExtension()
				.getTimeout())
				.isEqualTo(TrackServices.DEFAULT_TIMEOUT);
			softly.assertThat(it.getExtension()
				.getTracked())
				.isNotEmpty();
			softly.assertThat(it.getExtension()
				.getTrackingCount())
				.isEqualTo(1);
			softly.assertThat(it.getExtension()
				.getCardinality())
				.isEqualTo(1);
			softly.assertThat(it.getExtension()
				.size())
				.isEqualTo(1);
			softly.assertThat(it.getExtension()
				.isEmpty())
				.isFalse();
			softly.assertThat(it.getExtension()
				.waitForService(20))
				.isNotNull();

			softly.assertAll();
		}
	}

	@SuppressWarnings("serial")
	@Test
	public void matchMultiple() throws Exception {
		try (WithServiceUseExtension<Foo> it = new WithServiceUseExtension<Foo>(extensionContext, //
			Foo.class, null, 2, TrackServices.DEFAULT_TIMEOUT)) {

			it.bceInit();

			Foo s1 = new Foo() {}, s2 = new Foo() {};
			executor.schedule(() -> it.getBundleContext()
				.registerService(Foo.class, s1, new Hashtable<String, Object>() {
					{
						put("case", "matchMultiple_1");
					}
				}), 0, TimeUnit.MILLISECONDS);
			executor.schedule(() -> it.getBundleContext()
				.registerService(Foo.class, s2, new Hashtable<String, Object>() {
					{
						put("case", "matchMultiple_2");
					}
				}), 0, TimeUnit.MILLISECONDS);

			it.init();

			SoftAssertions softly = new SoftAssertions();

			softly.assertThat(it.getExtension()
				.getService())
				.as("getService")
				.isIn(s1, s2);
			softly.assertThat(it.getExtension()
				.getServiceReference())
				.as("getServiceReference")
				.isNotNull();
			softly.assertThat(it.getExtension()
				.getServiceReferences())
				.as("getServiceReferences")
				.isNotNull();
			softly.assertThat(it.getExtension()
				.getServices())
				.as("getServices")
				.containsExactlyInAnyOrder(s1, s2);
			softly.assertThat(it.getExtension()
				.getTimeout())
				.as("getTimeout")
				.isEqualTo(TrackServices.DEFAULT_TIMEOUT);
			softly.assertThat(it.getExtension()
				.getTracked())
				.as("getTracked")
				.isNotEmpty();
			softly.assertThat(it.getExtension()
				.getTrackingCount())
				.as("getTrackingCount")
				.isEqualTo(2);
			softly.assertThat(it.getExtension()
				.getCardinality())
				.as("getCardinality")
				.isEqualTo(2);
			softly.assertThat(it.getExtension()
				.size())
				.as("size")
				.isEqualTo(2);
			softly.assertThat(it.getExtension()
				.isEmpty())
				.as("isEmpty")
				.isFalse();
			softly.assertThat(it.getExtension()
				.waitForService(20))
				.as("waitForService(20)")
				.isNotNull();

			softly.assertAll();
		}
	}

	@SuppressWarnings({
		"rawtypes", "serial", "unchecked"
	})
	@Test
	public void nomatchByFilter() throws Exception {
		assertThatExceptionOfType(AssertionError.class) //
			.isThrownBy(() -> {
				try (WithServiceUseExtension<Foo> it = new WithServiceUseExtension<Foo>(extensionContext, //
					Foo.class, "(foo=baz)", 1, TrackServices.DEFAULT_TIMEOUT)) {

					it.bceInit();

					final Foo afoo = new Foo() {};

					executor.schedule(() -> it.getBundleContext()
						.registerService(Foo.class, afoo, new Hashtable() {
							{
								put("foo", "bar");
								put("case", "nomatchByFilter");
							}
						}), 0, TimeUnit.MILLISECONDS);

					it.init();
				}
			});
	}

	@Test
	public void malformedFilter() throws Exception {
		assertThatExceptionOfType(InvalidSyntaxException.class) //
			.isThrownBy(() -> {
				try (WithServiceUseExtension<Foo> it = new WithServiceUseExtension<Foo>(extensionContext, //
					Foo.class, "(foo=baz", 1, TrackServices.DEFAULT_TIMEOUT)) {

				}
			});
	}

}
