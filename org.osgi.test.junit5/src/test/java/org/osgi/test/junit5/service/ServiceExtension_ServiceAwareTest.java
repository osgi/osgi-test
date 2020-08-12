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

import static org.osgi.test.common.annotation.InjectService.DEFAULT_TIMEOUT;

import java.util.List;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.service.ServiceAware;
import org.osgi.test.junit5.types.Foo;

public class ServiceExtension_ServiceAwareTest extends AbstractServiceExtensionTest {

	static class ServiceWithZeroCardinality extends TestBase {
		@InjectService(cardinality = 0)
		ServiceAware<Foo> serviceAware;

		@Override
		@Test
		void test() throws Exception {
			softly.assertThat(serviceAware.isEmpty())
				.as("isEmpty %s", serviceAware)
				.isTrue();
			softly.assertThat(serviceAware.getTimeout())
				.as("getTimeout %s", serviceAware)
				.isEqualTo(DEFAULT_TIMEOUT);
			softly.assertThat(serviceAware.getCardinality())
				.as("getCardinality %s", serviceAware)
				.isEqualTo(0);
		}
	}

	@Test
	public void basicAssumptions() throws Exception {
		futureAssertThatTest(ServiceWithZeroCardinality.class).doesNotThrowAnyException();
		SoftAssertions softly = TestBase.lastSoftAssertions.get();

		softly.assertAll();
	}

	static class ServiceWithDefaults extends TestBase {
		@InjectService
		ServiceAware<Foo> serviceAware;

		@Override
		Foo getService() {
			return serviceAware.getService();
		}

		@Override
		List<Foo> getServices() {
			return serviceAware.getServices();
		}

		@Override
		@Test
		void test() throws Exception {
			softly.assertThat(serviceAware.isEmpty())
				.as("isEmpty %s", serviceAware)
				.isFalse();
			softly.assertThat(serviceAware.size())
				.as("size %s", serviceAware)
				.isEqualTo(1);
			softly.assertThat(serviceAware.getTimeout())
				.as("getTimeout %s", serviceAware)
				.isEqualTo(DEFAULT_TIMEOUT);
			softly.assertThat(serviceAware.getCardinality())
				.as("getCardinality %s", serviceAware)
				.isEqualTo(1);
		}
	}

	@Test
	public void successWhenService() throws Exception {
		final Foo afoo = new Foo() {};

		schedule(afoo);

		futureAssertThatTest(ServiceWithDefaults.class).doesNotThrowAnyException();
		SoftAssertions softly = TestBase.lastSoftAssertions.get();
		Foo service = TestBase.lastService.get();

		softly.assertThat(service)
			.as("getService %s", service)
			.isSameAs(afoo);

		softly.assertAll();
	}

	static class ServiceWithLongerTimeout extends TestBase {
		@InjectService(timeout = 1000)
		ServiceAware<Foo> serviceAware;

		@Override
		Foo getService() {
			return serviceAware.getService();
		}

		@Override
		List<Foo> getServices() {
			return serviceAware.getServices();
		}

		@Override
		@Test
		void test() throws Exception {
			softly.assertThat(serviceAware.getService())
				.as("getService %s", serviceAware)
				.isNotNull();
			softly.assertThat(serviceAware.getServiceReference())
				.as("getServiceReference %s", serviceAware)
				.isNotNull();
			softly.assertThat(serviceAware.getServiceReferences())
				.as("getServiceReferences %s", serviceAware)
				.hasSize(1);
			softly.assertThat(serviceAware.getTimeout())
				.as("getTimeout %s", serviceAware)
				.isEqualTo(1000);
			softly.assertThat(serviceAware.getTracked())
				.as("getTracked %s", serviceAware)
				.hasSize(1);
			softly.assertThat(serviceAware.getTrackingCount())
				.as("getTrackingCount %s", serviceAware)
				.isGreaterThanOrEqualTo(1);
			softly.assertThat(serviceAware.getCardinality())
				.as("getCardinality %s", serviceAware)
				.isEqualTo(1);
			softly.assertThat(serviceAware.size())
				.as("size %s", serviceAware)
				.isEqualTo(1);
			softly.assertThat(serviceAware.isEmpty())
				.as("isEmpty %s", serviceAware)
				.isFalse();
			softly.assertThat(serviceAware.waitForService(20))
				.as("waitForService %s", serviceAware)
				.isNotNull();
		}
	}

	@Test
	public void successWhenServiceWithTimeout() throws Exception {
		final Foo afoo = new Foo() {};

		schedule(afoo);

		futureAssertThatTest(ServiceWithLongerTimeout.class).doesNotThrowAnyException();
		SoftAssertions softly = TestBase.lastSoftAssertions.get();
		Foo service = TestBase.lastService.get();
		List<Foo> services = TestBase.lastServices.get();

		softly.assertThat(service)
			.as("getService %s", service)
			.isSameAs(afoo);
		softly.assertThat(services)
			.as("getServices %s", services)
			.containsExactly(afoo);

		softly.assertAll();
	}

	static class ServiceWithFilter extends TestBase {
		@InjectService(filter = FILTER)
		ServiceAware<Foo> serviceAware;

		@Override
		Foo getService() {
			return serviceAware.getService();
		}

		@Override
		List<Foo> getServices() {
			return serviceAware.getServices();
		}

		@Override
		@Test
		void test() throws Exception {
			softly.assertThat(serviceAware.getService())
				.as("getService %s", serviceAware)
				.isNotNull();
			softly.assertThat(serviceAware.getServiceReference())
				.as("getServiceReference %s", serviceAware)
				.isNotNull();
			softly.assertThat(serviceAware.getServiceReferences())
				.as("getServiceReferences %s", serviceAware)
				.isNotNull();
			softly.assertThat(serviceAware.getTimeout())
				.as("getTimeout %s", serviceAware)
				.isEqualTo(DEFAULT_TIMEOUT);
			softly.assertThat(serviceAware.getTracked())
				.as("waitForService %s", serviceAware)
				.isNotEmpty();
			softly.assertThat(serviceAware.getTrackingCount())
				.as("getTrackingCount %s", serviceAware)
				.isGreaterThanOrEqualTo(1);
			softly.assertThat(serviceAware.getCardinality())
				.as("getCardinality %s", serviceAware)
				.isEqualTo(1);
			softly.assertThat(serviceAware.size())
				.as("size %s", serviceAware)
				.isEqualTo(1);
			softly.assertThat(serviceAware.isEmpty())
				.as("isEmpty %s", serviceAware)
				.isFalse();
			softly.assertThat(serviceAware.waitForService(20))
				.as("waitForService %s", serviceAware)
				.isNotNull();
		}
	}

	@Test
	public void matchByFilter() throws Exception {
		final Foo afoo = new Foo() {};

		schedule(afoo, "foo", "bar");
		futureAssertThatTest(ServiceWithFilter.class).doesNotThrowAnyException();
		SoftAssertions softly = TestBase.lastSoftAssertions.get();
		Foo service = TestBase.lastService.get();

		softly.assertThat(service)
			.as("getService %s", service)
			.isSameAs(afoo);

		softly.assertAll();
	}

	static class ServiceWithCardinality2 extends TestBase {
		@InjectService(cardinality = 2)
		ServiceAware<Foo> serviceAware;

		@Override
		Foo getService() {
			return serviceAware.getService();
		}

		@Override
		List<Foo> getServices() {
			return serviceAware.getServices();
		}

		@Override
		@Test
		void test() throws Exception {
			softly.assertThat(serviceAware.getServiceReference())
				.as("getServiceReference %s", serviceAware)
				.isNotNull();
			softly.assertThat(serviceAware.getServiceReferences())
				.as("getServiceReferences %s", serviceAware)
				.isNotNull();
			softly.assertThat(serviceAware.getTimeout())
				.as("getTimeout %s", serviceAware)
				.isEqualTo(DEFAULT_TIMEOUT);
			softly.assertThat(serviceAware.getTracked())
				.as("getTracked %s", serviceAware)
				.isNotEmpty();
			softly.assertThat(serviceAware.getTrackingCount())
				.as("getTrackingCount %s", serviceAware)
				.isGreaterThanOrEqualTo(2);
			softly.assertThat(serviceAware.getCardinality())
				.as("getCardinality %s", serviceAware)
				.isEqualTo(2);
			softly.assertThat(serviceAware.size())
				.as("size %s", serviceAware)
				.isEqualTo(2);
			softly.assertThat(serviceAware.isEmpty())
				.as("isEmpty %s", serviceAware)
				.isFalse();
			softly.assertThat(serviceAware.waitForService(20))
				.as("waitForService %s", serviceAware)
				.isNotNull();
		}
	}

	@Test
	public void matchMultiple() throws Exception {
		Foo s1 = new Foo() {}, s2 = new Foo() {};
		schedule(s1);
		schedule(s2);

		futureAssertThatTest(ServiceWithCardinality2.class).doesNotThrowAnyException();
		SoftAssertions softly = TestBase.lastSoftAssertions.get();
		Foo service = TestBase.lastService.get();
		List<Foo> services = TestBase.lastServices.get();

		softly.assertThat(service)
			.as("getService %s", service)
			.isIn(s1, s2);
		softly.assertThat(services)
			.as("getServices %s", services)
			.containsExactlyInAnyOrder(s1, s2);

		softly.assertAll();
	}
}
