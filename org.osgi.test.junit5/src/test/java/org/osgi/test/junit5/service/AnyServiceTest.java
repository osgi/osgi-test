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

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.framework.BundleContext;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.annotation.InjectService.AnyService;
import org.osgi.test.common.dictionary.Dictionaries;
import org.osgi.test.common.service.ServiceAware;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.types.Foo;

@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
public class AnyServiceTest {

	@BeforeAll
	public static void beforeAll(@InjectBundleContext BundleContext bundleContext) throws Exception {
		bundleContext.registerService(B.class, new B() {}, Dictionaries.dictionaryOf("a", "b"));
		bundleContext.registerService(Object.class, new Foo() {}, Dictionaries.dictionaryOf("a", "b"));

	}

	@InjectService(filter = "(a=b)")
	ServiceAware<?>			field_ServiceAwareWildcard_Default;

	@InjectService(filter = "(a=b)", service = AnyService.class)
	ServiceAware<?>			field_ServiceAwareWildcard_AnyService;

	@InjectService(filter = "(a=b)")
	ServiceAware<Object>	field_ServiceAwareObject_Default;

	@InjectService(filter = "(a=b)", service = AnyService.class)
	ServiceAware<Object>	field_ServiceAwareObject_AnyService;

	@Test
	public void test_field_ServiceAwareWildcard_Default() throws Exception {
		assertThat(field_ServiceAwareWildcard_Default.getServices()).hasSize(1)
			.hasOnlyElementsOfTypes(Foo.class);
	}

	@Test
	public void test_field_ServiceAwareWildcard_AnyService() throws Exception {
		assertThat(field_ServiceAwareWildcard_AnyService.getServices()).hasSize(2)
			.hasOnlyElementsOfTypes(B.class, Foo.class);
	}

	@Test
	public void test_field_ServiceAwareObject_Default() throws Exception {
		assertThat(field_ServiceAwareObject_Default.getServices()).hasSize(1)
			.hasOnlyElementsOfTypes(Foo.class);
	}

	@Test
	public void test_field_ServiceAwareObject_AnyService() throws Exception {
		assertThat(field_ServiceAwareObject_AnyService.getServices()).hasSize(2)
			.hasOnlyElementsOfTypes(B.class, Foo.class);
	}

	@Test
	public void test_param_ServiceAwareWildcard_Default(
		@InjectService(filter = "(a=b)") ServiceAware<?> param_ServiceAwareWildcard_Default) throws Exception {
		assertThat(param_ServiceAwareWildcard_Default.getServices()).hasSize(1)
			.hasOnlyElementsOfTypes(Foo.class);
	}

	public void test_param_ServiceAwareWildcard_AnyService(
		@InjectService(filter = "(a=b)", service = AnyService.class) ServiceAware<?> param_ServiceAwareWildcard_AnyService)
		throws Exception {
		assertThat(param_ServiceAwareWildcard_AnyService.getServices()).hasSize(2)
			.hasOnlyElementsOfTypes(B.class, Foo.class);
	}

	@Test
	public void testWithServiceAwareCorrectSuperClass(
		@InjectService(filter = "(a=b)", service = B.class) ServiceAware<A> anyServiceParameter) throws Exception {
		assertThat(anyServiceParameter.getService()).isNotNull();
	}

	@Test
	public void testParameterObjectWithAnyService(
		@InjectService(filter = "(a=b)", service = AnyService.class) ServiceAware<Object> anyServiceParameter)
		throws Exception {
		assertThat(anyServiceParameter.getServices()).hasSize(2)
			.hasOnlyElementsOfTypes(B.class, Foo.class);
	}

	@Test
	public void testParameterAWithWithServiceB(
		@InjectService(filter = "(a=b)", service = B.class) A anyServiceParameter) throws Exception {
		assertThat(anyServiceParameter).isNotNull();
	}

	@Test
	public void testParameterWithSameServiceClasse(
		@InjectService(filter = "(a=b)", service = B.class) B anyServiceParameter) throws Exception {
		assertThat(anyServiceParameter).isNotNull();
	}

	@Test
	public void testParameterServiceBandparemObject(
		@InjectService(filter = "(a=b)", service = B.class) Object anyServiceParameter) throws Exception {
		assertThat(anyServiceParameter).isNotNull();
	}

	interface A {

	}

	interface B extends A {

	}
}
