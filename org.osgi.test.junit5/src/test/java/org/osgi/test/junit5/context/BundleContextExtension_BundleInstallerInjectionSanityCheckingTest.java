package org.osgi.test.junit5.context;

import static org.osgi.test.junit5.testutils.TestKitUtils.assertThatTest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.osgi.test.common.annotation.InjectBundleInstaller;
import org.osgi.test.common.install.BundleInstaller;

public class BundleContextExtension_BundleInstallerInjectionSanityCheckingTest {

	@ExtendWith(BundleContextExtension.class)
	static class TestBase {
		@Test
		void myTest() {}
	}

	@ExtendWith(BundleContextExtension.class)
	static class IncorrectParameterType {
		@SuppressWarnings("unused")
		@Test
		void myParameterTest(@InjectBundleInstaller String param) {}
	}

	@Test
	void annotatedParameter_withIncorrectType_throwsException() {
		assertThatTest(IncorrectParameterType.class).isInstanceOf(ParameterResolutionException.class)
			.hasMessageEndingWith(
				"Can only resolve @InjectBundleInstaller parameter of type org.osgi.test.common.install.BundleInstaller but was: java.lang.String");
	}

	static class IncorrectFieldType extends TestBase {

		@InjectBundleInstaller
		String myField;

		@Override
		@Test
		void myTest() {}
	}

	@Test
	void annotatedField_withIncorrectType_throwsException() {
		assertThatTest(IncorrectFieldType.class).isInstanceOf(ExtensionConfigurationException.class)
			.hasMessage(
				"[myField] Can only resolve @InjectBundleInstaller field of type org.osgi.test.common.install.BundleInstaller but was: java.lang.String");
	}

	static class IncorrectStaticFieldType extends TestBase {

		@InjectBundleInstaller
		static String myStaticField;

		@Override
		@Test
		void myTest() {}
	}

	@Test
	void annotatedStaticField_withIncorrectType_throwsException() {
		assertThatTest(IncorrectStaticFieldType.class).isInstanceOf(ExtensionConfigurationException.class)
			.hasMessage(
				"[myStaticField] Can only resolve @InjectBundleInstaller field of type org.osgi.test.common.install.BundleInstaller but was: java.lang.String");
	}

	static class FinalStaticField extends TestBase {
		@InjectBundleInstaller
		static final BundleInstaller bc = null;

		@Override
		@Test
		void myTest() {}
	}

	@Test
	void annotatedStaticField_thatIsFinal_throwsException() {
		assertThatTest(FinalStaticField.class).isInstanceOf(ExtensionConfigurationException.class)
			.hasMessageMatching("@InjectBundleInstaller field \\[bc\\] must not be .*final.*");
	}

	static class FinalField extends TestBase {
		@InjectBundleInstaller
		final BundleInstaller bc = null;

		@Override
		@Test
		void myTest() {}
	}

	@Test
	void annotatedField_thatIsFinal_throwsException() {
		assertThatTest(FinalField.class).isInstanceOf(ExtensionConfigurationException.class)
			.hasMessageMatching("@InjectBundleInstaller field \\[bc\\] must not be .*final.*");
	}

	static class PrivateStaticField extends TestBase {
		@InjectBundleInstaller
		static final BundleInstaller bc = null;

		@Override
		@Test
		void myTest() {}
	}

	@Test
	void annotatedStaticField_thatIsPrivate_throwsException() {
		assertThatTest(PrivateStaticField.class).isInstanceOf(ExtensionConfigurationException.class)
			.hasMessageMatching("@InjectBundleInstaller field \\[bc\\] must not be .*private.*");
	}

	static class PrivateField extends TestBase {
		@InjectBundleInstaller
		final BundleInstaller bc = null;

		@Override
		@Test
		void myTest() {}
	}

	@Test
	void annotatedField_thatIsPrivate_throwsException() {
		assertThatTest(PrivateField.class).isInstanceOf(ExtensionConfigurationException.class)
			.hasMessageMatching("@InjectBundleInstaller field \\[bc\\] must not be .*private.*");
	}
}
