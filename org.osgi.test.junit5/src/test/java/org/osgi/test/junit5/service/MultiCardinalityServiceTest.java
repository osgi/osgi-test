/*
 * Copyright (c) OSGi Alliance (2020). All Rights Reserved.
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

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.dictionary.Dictionaries;
import org.osgi.test.common.service.ServiceAware;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.types.Foo;

@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
public class MultiCardinalityServiceTest {

	private static List<ServiceRegistration<?>> registrations = new CopyOnWriteArrayList<>();

	@BeforeAll
	public static void beforeEach() {
		// We can't use the @InjectBundleContext because we're testing the test
		// support
		BundleContext bundleContext = FrameworkUtil.getBundle(
			MultiCardinalityServiceTest.class)
			.getBundleContext();
		registrations.add(bundleContext.registerService(Foo.class, new Foo() {},
			Dictionaries.dictionaryOf("entry", "1")));
		registrations.add(bundleContext.registerService(Foo.class, new Foo() {},
			Dictionaries.dictionaryOf("entry", "2")));
		registrations.add(bundleContext.registerService(Foo.class, new Foo() {},
			Dictionaries.dictionaryOf("entry", "3")));
	}

	@AfterAll
	public static void afterEach() {
		registrations.removeIf(reg -> {
			try {
				reg.unregister();
			} catch (Exception e) {}
			return true;
		});
	}

	@InjectService(cardinality = 3)
	ServiceAware<Foo> fServiceAware;

	@Test
	public void testServiceAware3ServicesField() throws Exception {
		assertThat(fServiceAware.getServices()).size()
			.isEqualTo(3);
	}

	@Test
	public void testServiceAware3ServicesParam(
		@InjectService(cardinality = 3) ServiceAware<Foo> fServiceAware)
		throws Exception {
		assertThat(fServiceAware.getServices()).size()
			.isEqualTo(3);
	}

	@InjectService(cardinality = 3)
	List<Foo> foos;

	@Test
	public void testList3ServicesField() throws Exception {
		assertThat(foos).size()
			.isEqualTo(3);
	}

	@Test
	public void testList3Services(@InjectService(cardinality = 3) List<Foo> foos) throws Exception {
		assertThat(foos).size()
			.isEqualTo(3);
	}

}
