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

package org.osgi.test.junit5.test.context;

import static org.osgi.test.junit5.test.testutils.TestKitUtils.assertThatTest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.osgi.framework.BundleContext;
import org.osgi.test.common.annotation.InjectBundleContext;

public class BundleContextExtension_BundleContextInjectionSanityCheckingTest {

	static class TestBase {
		@Test
		void myTest() {}
	}

	static class IncorrectParameterType {
		@SuppressWarnings("unused")
		@Test
		void myParameterTest(@InjectBundleContext String param) {}
	}

	@Test
	void annotatedParameter_withIncorrectType_throwsException() {
		assertThatTest(IncorrectParameterType.class).isInstanceOf(ParameterResolutionException.class)
			.hasMessageContainingAll("java.lang.String", "@InjectBundleContext", "org.osgi.framework.BundleContext");
	}

	static class IncorrectFieldType extends TestBase {

		@InjectBundleContext
		String myField;

		@Override
		@Test
		void myTest() {}
	}

	@Test
	void annotatedField_withIncorrectType_throwsException() {
		assertThatTest(IncorrectFieldType.class).isInstanceOf(ExtensionConfigurationException.class)
			.hasMessageContainingAll("myField", "java.lang.String", "@InjectBundleContext",
				"org.osgi.framework.BundleContext");
	}

	static class IncorrectStaticFieldType extends TestBase {

		@InjectBundleContext
		static String myStaticField;

		@Override
		@Test
		void myTest() {}
	}

	@Test
	void annotatedStaticField_withIncorrectType_throwsException() {
		assertThatTest(IncorrectStaticFieldType.class).isInstanceOf(ExtensionConfigurationException.class)
			.hasMessageContainingAll("myStaticField", "java.lang.String", "@InjectBundleContext",
				"org.osgi.framework.BundleContext");
	}

	static class FinalStaticField extends TestBase {
		@InjectBundleContext
		static final BundleContext bc = null;

		@Override
		@Test
		void myTest() {}
	}

	@Test
	void annotatedStaticField_thatIsFinal_throwsException() {
		assertThatTest(FinalStaticField.class).isInstanceOf(ExtensionConfigurationException.class)
			.hasMessageContainingAll("bc", "must not be private or final", "@InjectBundleContext");
	}

	static class FinalField extends TestBase {
		@InjectBundleContext
		final BundleContext bc = null;

		@Override
		@Test
		void myTest() {}
	}

	@Test
	void annotatedField_thatIsFinal_throwsException() {
		assertThatTest(FinalField.class).isInstanceOf(ExtensionConfigurationException.class)
			.hasMessageContainingAll("bc", "must not be private or final", "@InjectBundleContext");
	}

	static class PrivateField extends TestBase {
		@InjectBundleContext
		private BundleContext bc = null;

		@Override
		@Test
		void myTest() {}
	}

	@Test
	void annotatedField_thatIsPrivate_throwsException() {
		assertThatTest(PrivateField.class).isInstanceOf(ExtensionConfigurationException.class)
			.hasMessageContainingAll("bc", "must not be private or final", "@InjectBundleContext");
	}

	static class StaticPrivateField extends TestBase {
		@InjectBundleContext
		static private BundleContext bc = null;

		@Override
		@Test
		void myTest() {}
	}

	@Test
	void annotatedStaticField_thatIsPrivate_throwsException() {
		assertThatTest(StaticPrivateField.class).isInstanceOf(ExtensionConfigurationException.class)
			.hasMessageContainingAll("bc", "must not be private or final", "@InjectBundleContext");
	}
}
