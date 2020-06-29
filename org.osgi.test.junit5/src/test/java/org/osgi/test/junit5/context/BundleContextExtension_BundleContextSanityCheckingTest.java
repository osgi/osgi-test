package org.osgi.test.junit5.context;

import static org.osgi.test.junit5.testutils.TestKitUtils.assertThatTest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.osgi.framework.BundleContext;
import org.osgi.test.common.annotation.InjectBundleContext;

public class BundleContextExtension_BundleContextSanityCheckingTest {

	@ExtendWith(BundleContextExtension.class)
	static class TestBase {
		@Test
		void myTest() {}
	}

	protected String					testMethodName;

	@ExtendWith(BundleContextExtension.class)
	static class IncorrectParameterType {
		@SuppressWarnings("unused")
		@Test
		void myParameterTest(@InjectBundleContext String param) {}
	}

	@Test
	void annotatedParameter_withIncorrectType_throwsException() {
		assertThatTest(IncorrectParameterType.class).isInstanceOf(ParameterResolutionException.class)
			.hasMessageEndingWith(
				"Can only resolve @InjectBundleContext parameter of type org.osgi.framework.BundleContext but was: java.lang.String");
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
			.hasMessage(
				"[myField] Can only resolve @InjectBundleContext field of type org.osgi.framework.BundleContext but was: java.lang.String");
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
			.hasMessage(
				"[myStaticField] Can only resolve @InjectBundleContext field of type org.osgi.framework.BundleContext but was: java.lang.String");
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
			.hasMessageMatching("@InjectBundleContext field \\[bc\\] must not be .*final.*");
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
			.hasMessageMatching("@InjectBundleContext field \\[bc\\] must not be .*final.*");
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
			.hasMessageMatching("@InjectBundleContext field \\[bc\\] must not be .*private.*");
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
			.hasMessageMatching("@InjectBundleContext field \\[bc\\] must not be .*private.*");
	}

	static class AnnotatedConstructor extends TestBase {
		AnnotatedConstructor(@InjectBundleContext BundleContext context) {

		}

		@Override
		@Test
		void myTest() {}
	}

	@Test
	void annotatedConstructor_throwsException() {
		assertThatTest(AnnotatedConstructor.class).isInstanceOf(ParameterResolutionException.class)
			.hasMessageMatching("BundleContextExtension does not support parameter injection on constructors");
	}
}
