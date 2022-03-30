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

import static org.osgi.test.junit5.test.testutils.TestKitUtils.assertThatTest;

import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.service.ServiceAware;

public class ServiceExtension_SanityCheckingTest {

	static class MyService {}

	static class TestBase {
		@Test
		void myTest() {}
	}

	static class NonRawParameterType {
		@SuppressWarnings("unused")
		@Test
		void myParameterTest(@InjectService
		AtomicReference<?> param) {}
	}

	static class ListOfNonRawParameterType {
		@SuppressWarnings("unused")
		@Test
		void myParameterTest(@InjectService
		List<AtomicReference<?>> param) {}
	}

	static class ServiceAwareOfNonRawParameterType {
		@SuppressWarnings("unused")
		@Test
		void myParameterTest(@InjectService
		ServiceAware<AtomicReference<?>> param) {}
	}

	static class ServiceAwareOfNonRawUpperBoundType {
		@SuppressWarnings("unused")
		@Test
		void myParameterTest(@InjectService
		ServiceAware<? extends AtomicReference<?>> param) {}
	}

	static class ServiceAwareOfNonRawLowerBoundType {
		@SuppressWarnings("unused")
		@Test
		void myParameterTest(@InjectService(service = AtomicReference.class)
		ServiceAware<? super AtomicReference<?>> param) {}
	}

	@ParameterizedTest
	@ValueSource(classes = {
		NonRawParameterType.class, ListOfNonRawParameterType.class, ServiceAwareOfNonRawParameterType.class,
		ServiceAwareOfNonRawUpperBoundType.class, ServiceAwareOfNonRawLowerBoundType.class
	})
	void annotatedParameter_withNonRawType_throwsException(Class<?> test) {
		assertThatTest(test).isInstanceOf(ParameterResolutionException.class)
			.hasMessageContainingAll("non-generic type", "@InjectService", AtomicReference.class.getName());
	}

	static class FinalField extends TestBase {
		@InjectService
		final Date bc = null;
	}

	@Test
	void annotatedField_thatIsFinal_throwsException() {
		assertThatTest(FinalField.class).isInstanceOf(ExtensionConfigurationException.class)
			.hasMessageContainingAll("bc", "must not be final", "@InjectService");
	}

	static class PrivateField extends TestBase {
		@InjectService
		private Date bc;
	}

	@Test
	void annotatedField_thatIsPrivate_throwsException() {
		assertThatTest(PrivateField.class).isInstanceOf(ExtensionConfigurationException.class)
			.hasMessageContainingAll("bc", "must not be private", "@InjectService");
	}

	static class MismatchedServiceType extends TestBase {
		@InjectService(service = AtomicBoolean.class)
		Date bc;
	}

	static class MismatchedServiceType_List extends TestBase {
		@InjectService(service = AtomicBoolean.class)
		List<Date> bc;
	}

	static class MismatchedServiceType_ServiceAware extends TestBase {
		@InjectService(service = AtomicBoolean.class)
		ServiceAware<Date> bc;
	}

	static class MismatchedServiceType_ServiceAware_UpperWildcard extends TestBase {
		// AtomicBoolean is not a subclass of Date
		@InjectService(service = AtomicBoolean.class)
		ServiceAware<? extends Date> bc;
	}

	static class MismatchedServiceType_ServiceAware_LowerWildcard extends TestBase {
		// AtomicBoolean is not a superclass of Date
		@InjectService(service = AtomicBoolean.class)
		ServiceAware<? super Date> bc;
	}

	@ParameterizedTest
	@ValueSource(classes = {
		MismatchedServiceType.class, MismatchedServiceType_List.class, MismatchedServiceType_ServiceAware.class,
	})
	void annotatedField_withExplicitServiceType_thatDoesntMatchField_throwsException(Class<?> clazz) {
		assertThatTest(clazz).isInstanceOf(ExtensionConfigurationException.class)
			.hasMessageContainingAll("bc", "service type " + AtomicBoolean.class.getName(),
				"expects " + Date.class.getName(),
				"@InjectService");
	}

	@Test
	void annotatedField_withExplicitServiceType_andUpperBoundThatDoesntMatch_throwsException() {
		assertThatTest(MismatchedServiceType_ServiceAware_UpperWildcard.class)
			.isInstanceOf(ExtensionConfigurationException.class)
			.hasMessageContainingAll("bc", "service type " + AtomicBoolean.class.getName(),
				"expects ? extends " + Date.class.getName(), "@InjectService");
	}

	@Test
	void annotatedField_withExplicitServiceType_andLowerBoundThatDoesntMatch_throwsException() {
		assertThatTest(MismatchedServiceType_ServiceAware_LowerWildcard.class)
			.isInstanceOf(ExtensionConfigurationException.class)
			.hasMessageContainingAll("bc", "service type " + AtomicBoolean.class.getName(),
				"expects ? super " + Date.class.getName(), "@InjectService");
	}

	static class MismatchedServiceType_Parameter {
		@Test
		void myParameterTest(@InjectService(service = AtomicBoolean.class)
		Date bc) {}
	}

	static class MismatchedServiceType_Parameter_List {
		@Test
		void myParameterTest(@InjectService(service = AtomicBoolean.class)
		List<Date> bc) {}
	}

	static class MismatchedServiceType_Parameter_ServiceAware {
		@Test
		void myParameterTest(@InjectService(service = AtomicBoolean.class)
		ServiceAware<Date> bc) {}
	}

	@ParameterizedTest
	@ValueSource(classes = {
		MismatchedServiceType_Parameter.class, MismatchedServiceType_Parameter_List.class,
		MismatchedServiceType_Parameter_ServiceAware.class
	})
	void annotatedParameter_withExplicitServiceType_thatDoesntMatchParameter_throwsException(Class<?> testClass) {
		assertThatTest(testClass)
			.isInstanceOf(ParameterResolutionException.class)
			.hasMessageContainingAll("service type " + AtomicBoolean.class.getName(),
				"expects " + Date.class.getName(), "@InjectService");
	}
}
