package org.osgi.test.assertj.servicereference;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.Version;

@ExtendWith(SoftAssertionsExtension.class)
public class ServiceRefSoftAssertTest {

	@Test
	public void isAssignableTo(ServiceReferenceSoftAssertions softly) throws Exception {

		ServiceReference<?> sr = mock(ServiceReference.class);
		when(sr.isAssignableTo(null, null)).thenReturn(true);

		softly.assertThat(sr).isAssignableTo(null, null);

		when(sr.isAssignableTo(null, null)).thenReturn(false);

		softly.assertThatCode(() -> ServiceReferenceAssert.assertThat(sr).isAssignableTo(null, null))
				.as("is not assignable to")
				.isInstanceOf(AssertionError.class);
	}

	@Test
	public void isRegisteredInBundle(ServiceReferenceSoftAssertions softly) throws Exception {

		Bundle bundle = mock(Bundle.class);
		Bundle otherBundle = mock(Bundle.class);
		ServiceReference<?> sr = mock(ServiceReference.class);

		when(sr.getBundle()).thenReturn(bundle);
		when(bundle.getSymbolicName()).thenReturn("foo");
		Version version = Version.parseVersion("1.0.0");
		when(bundle.getVersion()).thenReturn(version);

		softly.assertThat(sr).isRegisteredInBundle(bundle);
		softly.assertThat(sr).isRegisteredInBundle("foo");
		softly.assertThat(sr).isRegisteredInBundle("foo", "1.0.0");

		when(sr.isAssignableTo(null, null)).thenReturn(false);

		softly
				.assertThatCode(
						() -> ServiceReferenceAssert.assertThat(sr).isRegisteredInBundle(otherBundle))
				.as("is not registered in Bundle")
				.isInstanceOf(AssertionError.class);

		softly.assertThatCode(() -> ServiceReferenceAssert.assertThat(sr).isRegisteredInBundle("bar"))
				.as("is not registered in Bundle")
				.isInstanceOf(AssertionError.class);

		softly
				.assertThatCode(
						() -> ServiceReferenceAssert.assertThat(sr).isRegisteredInBundle("foo", "0.0.1"))
				.as("is not registered in Bundle")
				.isInstanceOf(AssertionError.class);

	}

}
