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
import org.osgi.framework.Bundle;
import org.osgi.test.common.annotation.InjectInstalledBundle;

public class BundleInjection_SanityCheckingTest {

	static final String TB1_JAR = "tb1.jar";
	// Path to a jar that does not exist in the bundle.
	static final String	UNKNOWN_JAR	= "unknown.jar";

	static class IncorrectParameterType {
		@SuppressWarnings("unused")
		@Test
		void myParameterTest(@InjectInstalledBundle(TB1_JAR)
		String param) {}
	}

	@Test
	void annotatedParameter_withIncorrectType_throwsException() {
		assertThatTest(IncorrectParameterType.class).isInstanceOf(ParameterResolutionException.class)
			.hasMessageContainingAll("java.lang.String", "@InjectInstalledBundle", "org.osgi.framework.Bundle");
	}

	static class IncorrectFieldType {

		@InjectInstalledBundle(TB1_JAR)
		String myField;

		@Test
		void myTest() {}
	}

	@Test
	void annotatedField_withIncorrectType_throwsException() {
		assertThatTest(IncorrectFieldType.class).isInstanceOf(ExtensionConfigurationException.class)
			.hasMessageContainingAll("myField", "java.lang.String", "@InjectInstalledBundle",
				"org.osgi.framework.Bundle");
	}

	static class IncorrectStaticFieldType {

		@InjectInstalledBundle(TB1_JAR)
		static String myStaticField;

		@Test
		void myTest() {}
	}

	@Test
	void annotatedStaticField_withIncorrectType_throwsException() {
		assertThatTest(IncorrectStaticFieldType.class).isInstanceOf(ExtensionConfigurationException.class)
			.hasMessageContainingAll("myStaticField", "java.lang.String", "@InjectInstalledBundle",
				"org.osgi.framework.Bundle");
	}

	static class FinalStaticField {
		@InjectInstalledBundle(TB1_JAR)
		static final Bundle bundle = null;

		@Test
		void myTest() {}
	}

	@Test
	void annotatedStaticField_thatIsFinal_throwsException() {
		assertThatTest(FinalStaticField.class).isInstanceOf(ExtensionConfigurationException.class)
			.hasMessageContainingAll("bundle", "must not be final", "@InjectInstalledBundle");
	}

	static class FinalField {
		@InjectInstalledBundle(TB1_JAR)
		final Bundle bundle = null;

		@Test
		void myTest() {}
	}

	@Test
	void annotatedField_thatIsFinal_throwsException() {
		assertThatTest(FinalField.class).isInstanceOf(ExtensionConfigurationException.class)
			.hasMessageContainingAll("bundle", "must not be final", "@InjectInstalledBundle");
	}

	static class PrivateStaticField {
		@InjectInstalledBundle(TB1_JAR)
		static private Bundle bundle;

		@Test
		void myTest() {}
	}

	@Test
	void annotatedStaticField_thatIsPrivate_throwsException() {
		assertThatTest(PrivateStaticField.class).isInstanceOf(ExtensionConfigurationException.class)
			.hasMessageContainingAll("bundle", "must not be private", "@InjectInstalledBundle");
	}

	static class PrivateField {
		@InjectInstalledBundle(TB1_JAR)
		private Bundle bundle;

		@Test
		void myTest() {}
	}

	@Test
	void annotatedField_thatIsPrivate_throwsException() {
		assertThatTest(PrivateField.class).isInstanceOf(ExtensionConfigurationException.class)
			.hasMessageContainingAll("bundle", "must not be private", "@InjectInstalledBundle");
	}

	static class MissingBundle {
		@InjectInstalledBundle(UNKNOWN_JAR)
		Bundle bundle;

		@Test
		void myTest() {}
	}

	@Test
	void missingBundle_throwsException() {
		assertThatTest(MissingBundle.class).isInstanceOf(ExtensionConfigurationException.class)
			.hasMessageMatching("@InjectInstalledBundle \\[bundle\\]:.*" + UNKNOWN_JAR + ".*not found.*");
	}

	static class MissingBundleWithPath {
		@InjectInstalledBundle("some/path/" + UNKNOWN_JAR)
		Bundle bundle;

		@Test
		void myTest() {}
	}

	@Test
	void missingBundle_withPath_throwsException() {
		assertThatTest(MissingBundleWithPath.class).isInstanceOf(ExtensionConfigurationException.class)
			.hasMessageMatching("@InjectInstalledBundle \\[bundle\\]:.*some/path/" + UNKNOWN_JAR + ".*not found.*");
	}

	final static String MALFORMED_BUNDLE = "https://&*^(**/bal.jar";

	static class BundleWithMalformedURL {
		@InjectInstalledBundle(MALFORMED_BUNDLE)
		Bundle bundle;

		@Test
		void myTest() {}
	}

	@Test
	void bundle_withMalformedURL_throwsException() {
		assertThatTest(BundleWithMalformedURL.class).isInstanceOf(ExtensionConfigurationException.class)
			.hasMessageMatching(
				"@InjectInstalledBundle \\[bundle\\]: couldn't resolve bundle .*\\Q" + MALFORMED_BUNDLE + "\\E.*");
	}

	static class MissingBundleParameter {
		@Test
		void myParameterizedTest(@InjectInstalledBundle(UNKNOWN_JAR)
		Bundle bundle) {}
	}

	@Test
	void missingBundleParameter_throwsException() {
		assertThatTest(MissingBundleParameter.class).isInstanceOf(ParameterResolutionException.class)
			.hasMessageMatching("@InjectInstalledBundle \\[arg0\\]:.*" + UNKNOWN_JAR + ".*not found.*");
	}

	static class MissingBundleWithPathParameter {
		@Test
		void myParameterizedTest(@InjectInstalledBundle("some/path/" + UNKNOWN_JAR)
		Bundle bundle) {}
	}

	@Test
	void missingBundle_withPathParameter_throwsException() {
		assertThatTest(MissingBundleWithPathParameter.class).isInstanceOf(ParameterResolutionException.class)
			.hasMessageMatching("@InjectInstalledBundle \\[arg0\\]:.*some/path/" + UNKNOWN_JAR + ".*not found.*");
	}

	static class BundleWithMalformedURLParameter {
		@Test
		void myParameterTest(@InjectInstalledBundle(MALFORMED_BUNDLE)
		Bundle bundle) {}
	}

	@Test
	void bundle_withMalformedURLParameter_throwsException() {
		assertThatTest(BundleWithMalformedURLParameter.class).isInstanceOf(ParameterResolutionException.class)
			.hasMessageMatching(
				"@InjectInstalledBundle \\[arg0\\]: couldn't resolve bundle .*\\Q" + MALFORMED_BUNDLE + "\\E.*");
	}
}
