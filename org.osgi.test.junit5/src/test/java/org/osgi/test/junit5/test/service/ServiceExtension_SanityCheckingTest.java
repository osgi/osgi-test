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
		void myParameterTest(@InjectService AtomicReference<?> param) {}
	}

	static class ListOfNonRawParameterType {
		@SuppressWarnings("unused")
		@Test
		void myParameterTest(@InjectService List<AtomicReference<?>> param) {}
	}

	static class ServiceAwareOfNonRawParameterType {
		@SuppressWarnings("unused")
		@Test
		void myParameterTest(@InjectService ServiceAware<AtomicReference<?>> param) {}
	}

	@ParameterizedTest
	@ValueSource(classes = {
		NonRawParameterType.class, ListOfNonRawParameterType.class, ServiceAwareOfNonRawParameterType.class
	})
	void annotatedParameter_withNonRawType_throwsException(Class<?> test) {
		assertThatTest(test).isInstanceOf(ParameterResolutionException.class)
			.hasMessageEndingWith(
				"Can only resolve @InjectService parameter for services with non-generic types, service type was: "
					+ AtomicReference.class.getName() + "<?>");
	}

	static class FinalField extends TestBase {
		@InjectService
		final Date bc = null;

		@Override
		@Test
		void myTest() {}
	}

	@Test
	void annotatedField_thatIsFinal_throwsException() {
		assertThatTest(FinalField.class).isInstanceOf(ExtensionConfigurationException.class)
			.hasMessageMatching("@InjectService field \\[bc\\] must not be.*final.*");
	}

	static class PrivateField extends TestBase {
		@InjectService
		private Date bc = null;

		@Override
		@Test
		void myTest() {}
	}

	@Test
	void annotatedField_thatIsPrivate_throwsException() {
		assertThatTest(PrivateField.class).isInstanceOf(ExtensionConfigurationException.class)
			.hasMessageMatching("@InjectService field \\[bc\\] must not be.*private.*");
	}
}
