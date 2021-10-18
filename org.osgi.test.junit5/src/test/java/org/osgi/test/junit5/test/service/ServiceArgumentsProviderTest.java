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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.dictionary.Dictionaries;
import org.osgi.test.junit5.service.ServiceSource;
import org.osgi.test.junit5.test.types.Foo;

public class ServiceArgumentsProviderTest {

	@InjectBundleContext
	static BundleContext			classLevelContext;
	static ServiceRegistration<Foo>	sr1;
	static ServiceRegistration<Foo>	sr2;
	static ServiceRegistration<Foo>	sr3;

	static final List<String>		counter	= new ArrayList<>();

	@BeforeAll
	public static void before(@InjectBundleContext BundleContext bundleContext) {

		sr1 = bundleContext.registerService(Foo.class, new Foo() {}, Dictionaries.dictionaryOf("1", "2"));
		sr2 = bundleContext.registerService(Foo.class, new Foo() {}, Dictionaries.dictionaryOf("1", "3"));
		sr3 = bundleContext.registerService(Foo.class, new Foo() {}, Dictionaries.dictionaryOf("1", "4"));

	}

	@ParameterizedTest
	@ServiceSource(serviceType = Foo.class)
	public void testAllOrder1(Foo foo, ServiceReference<Foo> sr, Map<String, Object> map, TestInfo testInfo)
		throws Exception {
		assertThat(foo).isNotNull();
		assertThat(sr).isNotNull();
		assertThat(map).isNotNull();
		testInfo.getTestMethod()
			.ifPresent(name -> counter.add(name.getName()));
	}

	@ParameterizedTest
	@ServiceSource(serviceType = Foo.class)
	public void testAllOrder2(ServiceReference<Foo> sr, Map<String, Object> map, Foo foo, TestInfo testInfo)
		throws Exception {
		assertThat(foo).isNotNull();
		assertThat(sr).isNotNull();
		assertThat(map).isNotNull();
		testInfo.getTestMethod()
			.ifPresent(name -> counter.add(name.getName()));
	}

	@ParameterizedTest
	@ServiceSource(serviceType = Foo.class)
	public void testAllOrder3(Map<String, Object> map, Foo foo, ServiceReference<Foo> sr, TestInfo testInfo)
		throws Exception {
		assertThat(foo).isNotNull();
		assertThat(sr).isNotNull();
		assertThat(map).isNotNull();
		testInfo.getTestMethod()
			.ifPresent(name -> counter.add(name.getName()));
	}

	@ParameterizedTest
	@ServiceSource(serviceType = Foo.class)
	public void testOnlyMap(Map<String, Object> map, TestInfo testInfo) throws Exception {
		assertThat(map).isNotNull();
		testInfo.getTestMethod()
			.ifPresent(name -> counter.add(name.getName()));
	}

	@ParameterizedTest
	@ServiceSource(serviceType = Foo.class)
	public void testOnlyService(Foo foo, TestInfo testInfo) throws Exception {
		assertThat(foo).isNotNull();
		testInfo.getTestMethod()
			.ifPresent(name -> counter.add(name.getName()));
	}

	@ParameterizedTest
	@ServiceSource(serviceType = Foo.class)
	public void testOnlyServiceRef(ServiceReference<Foo> sr, TestInfo testInfo) throws Exception {
		assertThat(sr).isNotNull();
		testInfo.getTestMethod()
			.ifPresent(name -> counter.add(name.getName()));
	}

	@ParameterizedTest
	@ServiceSource(serviceType = Foo.class, filter = "(%s=%s)", filterArguments = {
		"1", "*"
	})
	public void testfilterArgumentsWildcard(ServiceReference<Foo> sr, TestInfo testInfo) throws Exception {
		assertThat(sr).isNotNull();
		testInfo.getTestMethod()
			.ifPresent(name -> counter.add(name.getName()));
	}

	@ParameterizedTest
	@ServiceSource(serviceType = Foo.class, filter = "(%s=%s)", filterArguments = {
		"1", "2"
	})
	public void testfilterArgumentsFix(ServiceReference<Foo> sr, TestInfo testInfo) throws Exception {
		assertThat(sr).isNotNull();
		testInfo.getTestMethod()
			.ifPresent(name -> counter.add(name.getName()));
	}

	@AfterAll
	public static void afterAll() {
		testCount("testOnlyMap", 3);
		testCount("testOnlyServiceRef", 3);
		testCount("testOnlyService", 3);
		testCount("testfilterArgumentsWildcard", 3);
		testCount("testAllOrder1", 3);
		testCount("testAllOrder2", 3);
		testCount("testAllOrder3", 3);
		testCount("testfilterArgumentsFix", 1);

		sr1.unregister();
		sr2.unregister();
		sr3.unregister();
	}

	private static void testCount(String testName, long i) {
		long count = counter.stream()
			.filter(s -> s.equals(testName))
			.count();
		assertThat(count).as("Wrong test-execution-count on : %s", testName)
			.isEqualTo(i);
	}
}
