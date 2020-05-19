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
import static org.osgi.test.common.annotation.InjectService.DEFAULT_TIMEOUT;

import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceRegistration;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.dictionary.Dictionaries;
import org.osgi.test.junit5.types.Foo;

public class ServiceExtensionTest extends AbstractServiceExtensionTest {

	static class ServiceWithDefaults extends TestBase {
		@InjectService
		Foo foo;

		@Override
		Foo getService() {
			return foo;
		}
	}

	@Test
	public void requiredFailsWhenNoServiceWithDefaultTimeout() throws Exception {
		assertThatTest(ServiceWithDefaults.class).isInstanceOf(
			AssertionError.class)
			.hasMessageContaining(
				"1/1 services (objectClass=org.osgi.test.junit5.types.Foo) didn't arrive within "
				+ DEFAULT_TIMEOUT + "ms");
	}

	static class ServiceWithShortTimeout extends TestBase {
		@InjectService(timeout = 50)
		Foo foo;

		@Override
		Foo getService() {
			return foo;
		}
	}

	@Test
	public void requiredFailsWhenNoServiceWithExplicitTimeout() throws Exception {
		assertThatTest(ServiceWithShortTimeout.class).isInstanceOf(
			AssertionError.class)
			.hasMessageContaining(
				"1/1 services (objectClass=org.osgi.test.junit5.types.Foo) didn't arrive within 50ms");
	}

	@Test
	public void successWhenService() throws Exception {
		final Foo afoo = new Foo() {};

		ScheduledFuture<ServiceRegistration<?>> scheduledFuture = executor.schedule(
			() -> bundleContext.registerService(Foo.class, afoo, Dictionaries.dictionaryOf("case", testMethodName)), 10,
			TimeUnit.MILLISECONDS);

		futureAssertThatTest(ServiceWithDefaults.class).doesNotThrowAnyException();
		assertThat(ServiceWithDefaults.lastService.get()).isSameAs(afoo);
	}

	static class ServiceWithLongerTimeout extends TestBase {
		@InjectService(timeout = 1000)
		Foo foo;

		@Override
		Foo getService() {
			return foo;
		}
	}

	@Test
	public void successWhenServiceWithExplicitTimeout() throws Exception {
		final Foo afoo = new Foo() {};

		ScheduledFuture<ServiceRegistration<?>> scheduledFuture = executor.schedule(
			() -> bundleContext.registerService(Foo.class, afoo, Dictionaries.dictionaryOf("case", testMethodName)), 0,
			TimeUnit.MILLISECONDS);

		futureAssertThatTest(ServiceWithLongerTimeout.class).doesNotThrowAnyException();
		assertThat(TestBase.lastService.get()).isSameAs(afoo);
	}

	static class ServiceWithFilter extends TestBase {
		@InjectService(filter = FILTER)
		Foo foo;

		@Override
		Foo getService() {
			return foo;
		}
	}

	@Test
	public void matchByFilter() throws Exception {
		final Foo afoo = new Foo() {};

		schedule(afoo, "foo", "bar");

		futureAssertThatTest(ServiceWithFilter.class).doesNotThrowAnyException();
		assertThat(TestBase.lastService.get()).isSameAs(afoo);
	}

	static class ServiceWithMultiple extends TestBase {
		@InjectService(cardinality = 2)
		List<Foo> foos;

		@Override
		List<Foo> getServices() {
			return foos;
		}
	}

	@Test
	public void matchMultiple() throws Exception {
		Foo s1 = new Foo() {}, s2 = new Foo() {};
		ScheduledFuture<ServiceRegistration<?>> scheduledFuture1 = executor.schedule(() -> bundleContext
			.registerService(Foo.class, s1, Dictionaries.dictionaryOf("foo", testMethodName.concat("_1"))), 0,
			TimeUnit.MILLISECONDS);
		ScheduledFuture<ServiceRegistration<?>> scheduledFuture2 = executor.schedule(() -> bundleContext
			.registerService(Foo.class, s2, Dictionaries.dictionaryOf("foo", testMethodName.concat("_2"))), 0,
			TimeUnit.MILLISECONDS);

		futureAssertThatTest(ServiceWithMultiple.class).doesNotThrowAnyException();
		assertThat(TestBase.lastServices.get()).containsExactlyInAnyOrder(s1, s2);
	}

	@Test
	public void requiredFailsWhenOnly1ServiceAndCardinality2WithDefaultTimeout() throws Exception {
		Foo s1 = new Foo() {};
		schedule(s1);
		futureAssertThatTest(ServiceWithMultiple.class).isInstanceOf(
			AssertionError.class)
			.hasMessageContaining(
				"1/2 services (objectClass=org.osgi.test.junit5.types.Foo) didn't arrive within "
				+ DEFAULT_TIMEOUT + "ms");
	}

	@Test
	public void requiredFailsWhenNoServiceAndCardinality2WithDefaultTimeout() throws Exception {
		assertThatTest(ServiceWithMultiple.class).isInstanceOf(AssertionError.class)
			.hasMessageContaining(
				"2/2 services (objectClass=org.osgi.test.junit5.types.Foo) didn't arrive within "
				+ DEFAULT_TIMEOUT + "ms");
	}

	@Test
	public void nomatchByFilter() throws Exception {
		final Foo afoo = new Foo() {};

		ScheduledFuture<ServiceRegistration<?>> scheduledFuture = executor.schedule(() -> bundleContext
			.registerService(Foo.class, afoo,
				Dictionaries.dictionaryOf("foo", "baz", "case", testMethodName)),
			0,
			TimeUnit.MILLISECONDS);

		futureAssertThatTest(ServiceWithFilter.class).isInstanceOf(AssertionError.class)
			.hasMessageMatching(".*" + Pattern.quote(FILTER) + ".*didn't arrive within " + DEFAULT_TIMEOUT + "ms");
		assertThat(TestBase.lastService.get()).isNull();
	}

	static class ServiceWithMalformedFilter extends TestBase {
		@InjectService(filter = MALFORMED_FILTER)
		Foo foo;

		@Override
		Foo getService() {
			return foo;
		}
	}

	@Test
	public void malformedFilter() throws Exception {
		futureAssertThatTest(ServiceWithMalformedFilter.class).isInstanceOf(
			InvalidSyntaxException.class)
			.hasMessageContaining(MALFORMED_FILTER);
		assertThat(TestBase.lastService.get()).isNull();
	}

	static class ServiceWithNegativeCardinality extends TestBase {
		@InjectService(cardinality = -1)
		Foo foo;

		@Override
		Foo getService() {
			return foo;
		}
	}

	@Test
	public void negativeCardinality() throws Exception {
		futureAssertThatTest(ServiceWithNegativeCardinality.class).isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("cardinality must be zero or greater");
		assertThat(TestBase.lastService.get()).isNull();
	}

	static class ServiceWithNegativeTimeout extends TestBase {
		@InjectService(timeout = -1)
		Foo foo;

		@Override
		Foo getService() {
			return foo;
		}
	}

	@Test
	public void negativeTimeout() throws Exception {
		futureAssertThatTest(ServiceWithNegativeTimeout.class).isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("timeout must be zero or greater");
		assertThat(TestBase.lastService.get()).isNull();
	}

}
