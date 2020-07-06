package org.osgi.test.junit5.service;

import static org.osgi.test.junit5.testutils.TestKitUtils.assertThatTest;

import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.service.ServiceAware;

public class ServiceExtension_SanityCheckingTest {

	static class MyService {}

	@ExtendWith(ServiceExtension.class)
	static class TestBase {
		@Test
		void myTest() {}
	}

	@ExtendWith(ServiceExtension.class)
	static class NonRawParameterType {
		@SuppressWarnings("unused")
		@Test
		void myParameterTest(@InjectService AtomicReference<?> param) {}
	}

	@ExtendWith(ServiceExtension.class)
	static class ListOfNonRawParameterType {
		@SuppressWarnings("unused")
		@Test
		void myParameterTest(@InjectService List<AtomicReference<?>> param) {}
	}

	@ExtendWith(ServiceExtension.class)
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

	static class StaticField extends TestBase {
		@InjectService
		static Date bc = null;

		@Override
		@Test
		void myTest() {}
	}

	@Test
	void annotatedField_thatIsStatic_throwsException() {
		assertThatTest(StaticField.class).isInstanceOf(ExtensionConfigurationException.class)
			.hasMessageMatching("@InjectService field \\[bc\\] must not be.*static.*");
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
