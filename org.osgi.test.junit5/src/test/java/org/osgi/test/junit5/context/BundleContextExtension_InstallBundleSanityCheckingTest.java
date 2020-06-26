package org.osgi.test.junit5.context;

import static org.osgi.test.junit5.testutils.TestKitUtils.assertThatTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.osgi.test.common.annotation.InjectInstallBundle;
import org.osgi.test.common.install.InstallBundle;

public class BundleContextExtension_InstallBundleSanityCheckingTest {

	@ExtendWith(BundleContextExtension.class)
	static class TestBase {
		@Test
		void myTest() {}
	}

	protected String					testMethodName;

	@BeforeEach
	public void beforeEach(TestInfo testInfo) {
		testMethodName = testInfo.getTestMethod()
			.get()
			.getName();
	}

	@ExtendWith(BundleContextExtension.class)
	static class IncorrectParameterType {
		@SuppressWarnings("unused")
		@Test
		void myParameterTest(@InjectInstallBundle String param) {}
	}

	@Test
	void annotatedParameter_withIncorrectType_throwsException() {
		assertThatTest(IncorrectParameterType.class).isInstanceOf(ParameterResolutionException.class)
			.hasMessageEndingWith(
				"Can only resolve @InjectInstallBundle parameter of type org.osgi.test.common.install.InstallBundle but was: java.lang.String");
	}

	static class IncorrectFieldType extends TestBase {

		@InjectInstallBundle
		String myField;

		@Override
		@Test
		void myTest() {}
	}

	@Test
	void annotatedField_withIncorrectType_throwsException() {
		assertThatTest(IncorrectFieldType.class).isInstanceOf(ExtensionConfigurationException.class)
			.hasMessage(
				"[myField] Can only resolve @InjectInstallBundle field of type org.osgi.test.common.install.InstallBundle but was: java.lang.String");
	}

	static class IncorrectStaticFieldType extends TestBase {

		@InjectInstallBundle
		static String myStaticField;

		@Override
		@Test
		void myTest() {}
	}

	@Test
	void annotatedStaticField_withIncorrectType_throwsException() {
		assertThatTest(IncorrectStaticFieldType.class).isInstanceOf(ExtensionConfigurationException.class)
			.hasMessage(
				"[myStaticField] Can only resolve @InjectInstallBundle field of type org.osgi.test.common.install.InstallBundle but was: java.lang.String");
	}

	static class FinalStaticField extends TestBase {
		@InjectInstallBundle
		static final InstallBundle bc = null;

		@Override
		@Test
		void myTest() {}
	}

	@Test
	void annotatedStaticField_thatIsFinal_throwsException() {
		assertThatTest(FinalStaticField.class).isInstanceOf(ExtensionConfigurationException.class)
			.hasMessage(
				"@InjectInstallBundle field [bc] must not be final");
	}

	static class FinalField extends TestBase {
		@InjectInstallBundle
		final InstallBundle bc = null;

		@Override
		@Test
		void myTest() {}
	}

	@Test
	void annotatedField_thatIsFinal_throwsException() {
		assertThatTest(FinalField.class).isInstanceOf(ExtensionConfigurationException.class)
			.hasMessage(
				"@InjectInstallBundle field [bc] must not be final");
	}
}
