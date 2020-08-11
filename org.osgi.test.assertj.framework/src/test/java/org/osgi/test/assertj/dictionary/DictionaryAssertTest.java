package org.osgi.test.assertj.dictionary;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.assertj.core.api.Assert;
import org.assertj.core.api.ProxyableMapAssert;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(SoftAssertionsExtension.class)
public class DictionaryAssertTest {

	@Test
	public void keysAndValues_areTheRightWayAround(SoftAssertions softly) throws Exception {
		Hashtable<Object, Object> sut = new Hashtable<>();
		sut.put("key", "value");

		softly.assertThatCode(() -> DictionaryAssert.assertThat(sut)
			.containsEntry("key", "value")).as("correct")
				.doesNotThrowAnyException();
		softly.assertThatCode(() -> DictionaryAssert.assertThat(sut)
			.containsEntry("value", "key")).as("reversed")
				.isInstanceOf(AssertionError.class);
	}

	@Test
	public void keysAndValues_areTheRightWayAround_forSoftAssertion(SoftAssertions softly) throws Exception {
		Hashtable<Object, Object> sut = new Hashtable<>();
		sut.put("key", "value");

		AtomicReference<Object> actualRef = new AtomicReference<>();
		AtomicReference<Object> retvalRef = new AtomicReference<>();

		DictionarySoftAssertionsProvider provider = new DictionarySoftAssertionsProvider() {

			@SuppressWarnings({
				"unchecked", "rawtypes"
			})
			@Override
			public <SELF extends Assert<? extends SELF, ? extends ACTUAL>, ACTUAL> SELF proxy(Class<SELF> assertClass,
				Class<ACTUAL> actualClass, ACTUAL actual) {
				actualRef.set(actual);
				retvalRef.set(new ProxyableMapAssert((Map) actual));
				return (SELF) retvalRef.get();
			}

			@Override
			public void assertAll() {}

			@Override
			public boolean wasSuccess() {
				return false;
			}

			@Override
			public void collectAssertionError(AssertionError error) {}

			@Override
			public List<AssertionError> assertionErrorsCollected() {
				return null;
			}
		};

		softly.assertThat(provider.assertThat(sut))
			.as("returned assertion")
			.isSameAs(retvalRef.get());
		softly.assertThat(actualRef.get())
			.as("equal")
			.isEqualTo(sut);
	}
}
