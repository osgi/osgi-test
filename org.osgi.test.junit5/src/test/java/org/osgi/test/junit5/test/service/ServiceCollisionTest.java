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
import org.osgi.test.junit5.service.ServiceExtension;
import org.osgi.test.junit5.test.types.Foo;

@ExtendWith(ServiceExtension.class)
public class ServiceCollisionTest {

	private static List<ServiceRegistration<?>> registrations = new CopyOnWriteArrayList<>();

	@BeforeAll
	public static void beforeEach() {
		// We can't use the @InjectBundleContext because we're testing the test
		// support
		BundleContext bundleContext = FrameworkUtil.getBundle(MultiCardinalityServiceTest.class)
			.getBundleContext();
		registrations
			.add(bundleContext.registerService(Foo.class, new Foo() {}, Dictionaries.dictionaryOf("entry", "1")));
		registrations.add(bundleContext.registerService(String.class, "bar", Dictionaries.dictionaryOf("entry", "2")));
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

	@InjectService
	Foo		foo;
	@InjectService
	String	bar;

	@Test
	public void testServiceAware3Services() throws Exception {
		assertThat(foo).isNotNull();
		assertThat(bar).isNotNull();
	}

}
