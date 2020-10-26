package org.osgi.test.assertj.bundlecontext;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.test.assertj.bundlereference.AbstractBundleReferenceAssertTest;

class BundleContextAssertTest extends AbstractBundleReferenceAssertTest<BundleContextAssert, BundleContext> {

	BundleContextAssertTest() {
		super(BundleContextAssert::assertThat, BundleContext.class);
	}

	@Test
	void hasProperty() throws Exception {
		when(actual.getProperty("key1")).thenReturn("value1");

		assertPassing("has", aut::hasProperty, "key1");
		assertFailing("has", aut::hasProperty, "key2")
			.hasMessageMatching("(?si).*have property.*key2.*but it did not.*");

		assertPassing("doesNotHave", aut::doesNotHaveProperty, "key2");
		assertFailing("doesNotHave", aut::doesNotHaveProperty, "key1")
			.hasMessageMatching("(?si).*not.*have property.*key1.*but it did.*value1.*");
	}

	@ParameterizedTest
	@CsvSource({
		"key1,value1", "key2,value2", "random121,fewl"
	})
	void hasPropertyWithKeyThat(String key, String value) throws Exception {
		when(actual.getProperty(key)).thenReturn(value);

		assertChildAssertion("property(" + key + ")", () -> aut.hasPropertyWithKeyThat(key),
			() -> actual.getProperty(key));
	}

	@Test
	void hasBundleWithId() {
		when(actual.getBundle(1L)).thenReturn(otherBundle);

		assertPassing("has", aut::hasBundleWithId, 1L);
		assertFailing("has", aut::hasBundleWithId, 2L)
			.hasMessageMatching("(?si).*have bundle with id.*2.*but it did not.*");

		assertPassing("doesNotHave", aut::doesNotHaveBundleWithId, 2L);
		assertFailing("doesNotHave", aut::doesNotHaveBundleWithId, 1L).hasMessageMatching(
			"(?si).*not.*have bundle with id.*1.*but it did.*" + Pattern.quote(otherBundle.toString()) + ".*");
	}

	@Test
	void hasBundleWithIdThat() throws Exception {
		when(actual.getBundle(1L)).thenReturn(otherBundle);

		assertChildAssertion("bundle(1)", () -> aut.hasBundleWithIdThat(1), () -> actual.getBundle(1));
	}

	@Test
	void hasBundleWithLocation() {
		when(actual.getBundle("some/location")).thenReturn(otherBundle);

		assertPassing("has", aut::hasBundleWithLocation, "some/location");
		assertFailing("has", aut::hasBundleWithLocation, "some/other/location")
			.hasMessageMatching("(?si).*have bundle with location.*some/other/location.*but it did not.*");

		assertPassing("doesNotHave", aut::doesNotHaveBundleWithLocation, "some/other/location");
		assertFailing("doesNotHave", aut::doesNotHaveBundleWithLocation, "some/location")
			.hasMessageMatching(
				"(?si).*not.*have bundle with location.*some/location.*but it did.*"
				+ Pattern.quote(otherBundle.toString()) + ".*");
	}

	@Test
	void hasBundleWithLocationThat() throws Exception {
		when(actual.getBundle("some/location")).thenReturn(otherBundle);

		assertChildAssertion("bundle(some/location)", () -> aut.hasBundleWithLocationThat("some/location"),
			() -> actual.getBundle("some/location"));
	}

	@Test
	void hasBundles() throws Exception {
		Bundle[] bundles = new Bundle[] {
			bundle, otherBundle
		};
		when(actual.getBundles()).thenReturn(bundles);

		Bundle dummy = mock(Bundle.class);
		assertChildAssertion("bundles", aut::hasBundlesThat, () -> Arrays.asList(bundles));
	}
}
