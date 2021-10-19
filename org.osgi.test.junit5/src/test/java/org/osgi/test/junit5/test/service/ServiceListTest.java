/*******************************************************************************
 * Copyright (c) Contributors to the Eclipse Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 *******************************************************************************/

package org.osgi.test.junit5.test.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.log.LogService;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.service.ServiceAware;

public class ServiceListTest {

	@InjectBundleContext
	BundleContext										bc;
	@InjectService
	List<LogService>									logServices;

	@InjectService(cardinality = 0)
	List<TestServiceWithMultipleCardinality>			testServices;

	@InjectService(cardinality = 0)
	ServiceAware<TestServiceWithMultipleCardinality>	testServicesAware;

	@Test
	public void testField() throws Exception {
		assertThat(logServices).size()
			.isEqualTo(1);
	}

	@Test
	public void testParam(@InjectService
	List<LogService> logServices) throws Exception {
		assertThat(logServices).size()
			.isEqualTo(1);
	}

	@Test
	public void servicesWithMultipleCardinalityCanAlsoBeInjected() throws InterruptedException {

		Assertions.assertEquals(0, this.testServicesAware.getCardinality(),
			"getCardinality() delivers the initial Cardinality (e.g. set via @InjectService(cardinality = 0)");

		Assertions.assertEquals(0, this.testServices.size(), "Initially no test service should be found");
		Assertions.assertEquals(0, this.testServicesAware.getCardinality(),
			"The ServiceAware should reflect the same situation");
		// register service through bundle context
		final ServiceRegistration<TestServiceWithMultipleCardinality> reg = bc.registerService(
			TestServiceWithMultipleCardinality.class, new TestServiceWithMultipleCardinalityImpl(), null);

		assertThat(testServicesAware.waitForService(1000l)).isNotNull();
		Assertions.assertEquals(0, this.testServicesAware.getCardinality(), "and the cardinality will not change");

		Assertions.assertEquals(1, this.testServices.size(), "After registration, there should be 1 service");
		Assertions.assertEquals(1, this.testServicesAware.getServices()
			.size(), "The ServiceAware should reflect the same situation");

		Assertions.assertEquals(1, this.testServicesAware.getTrackingCount(),
			"changes happened could be tracked via getTrackingCount");

		reg.unregister();
		Assertions.assertEquals(0, this.testServices.size(), "After unregister, no test service should be found");
		Assertions.assertEquals(0, this.testServicesAware.getCardinality(),
			"The ServiceAware should reflect the same situation");

		Assertions.assertEquals(2, this.testServicesAware.getTrackingCount(),
			"changes happened could be tracked via getTrackingCount");

	}

	interface TestServiceWithMultipleCardinality {

	}

	class TestServiceWithMultipleCardinalityImpl implements TestServiceWithMultipleCardinality {

	}

}
