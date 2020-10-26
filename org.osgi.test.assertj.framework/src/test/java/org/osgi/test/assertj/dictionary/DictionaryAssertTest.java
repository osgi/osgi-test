package org.osgi.test.assertj.dictionary;

import java.util.Collections;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.assertj.core.api.Assert;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(SoftAssertionsExtension.class)
public class DictionaryAssertTest {

	@Test
	public void keysAndValues_areTheRightWayAround(SoftAssertions softly) throws Exception {
		Dictionary<Object, Object> sut = new TestDictionary<>();
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
		Dictionary<Object, Object> sut = new TestDictionary<>();
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
				retvalRef.set(new ProxyableDictionaryAssert((Dictionary) actual));
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

			@Override
			public void succeeded() {}
		};

		softly.assertThat(provider.assertThat(sut))
			.as("returned assertion")
			.isSameAs(retvalRef.get());
		softly.assertThat(actualRef.get())
			.as("equal")
			.isEqualTo(sut);
	}

	@Test
	public void instanceof_factory(SoftAssertions softly) throws Exception {
		Dictionary<String, String> dict1 = new TestDictionary<>();
		dict1.put("key1", "value1");
		dict1.put("key2", "value2");
		Dictionary<String, String> dict2 = new TestDictionary<>();
		dict2.put("key1", "value1");
		dict2.put("key2", "value2");

		DictionaryAssert<CharSequence, CharSequence> charseqDictionaryAssert = softly.assertThatObject(dict1)
			.asInstanceOf(DictionaryAssert.dictionary(CharSequence.class, CharSequence.class));
		charseqDictionaryAssert.containsKeys("key1", "key2")
			.containsValues("value1", "value2")
			.containsExactlyInAnyOrderEntriesOf(dict2);

		softly.assertThatCode(() -> DictionaryAssert.dictionary(CharSequence.class, null))
			.isInstanceOf(NullPointerException.class);
		softly.assertThatCode(() -> DictionaryAssert.dictionary(null, CharSequence.class))
			.isInstanceOf(NullPointerException.class);
		softly.assertThatCode(() -> DictionaryAssert.dictionary(null, null))
			.isInstanceOf(NullPointerException.class);
	}

	@Test
	public void containsAllEntriesOf(DictionarySoftAssertions softly) throws Exception {
		Dictionary<String, String> dict1 = new TestDictionary<>();
		dict1.put("key1", "value1");
		dict1.put("key2", "value2");
		Dictionary<String, String> dict2 = new TestDictionary<>();
		dict2.put("key1", "value1");

		softly.assertThat(dict1)
			.containsAllEntriesOf(dict2);
		softly.assertThatCode(() -> DictionaryAssert.assertThat(dict2)
			.containsAllEntriesOf(dict1))
			.as("does not containsAllEntriesOf")
			.isInstanceOf(AssertionError.class);
	}

	@Test
	public void containsExactlyEntriesOf(DictionarySoftAssertions softly) throws Exception {
		Dictionary<String, String> dict1 = new TestDictionary<>();
		dict1.put("key1", "value1");
		dict1.put("key2", "value2");
		Dictionary<String, String> dict2 = new TestDictionary<>();
		dict2.put("key1", "value1");
		dict2.put("key2", "value2");
		Dictionary<String, String> dict3 = new TestDictionary<>();
		dict3.put("key1", "value1");
		Dictionary<String, String> dict4 = new TestDictionary<>();
		dict4.put("key2", "value2");
		dict4.put("key1", "value1");
		Dictionary<String, String> dict5 = new TestDictionary<>();
		dict5.put("key1", "value1");
		dict5.put("key2", "value2");
		dict5.put("key3", "value3");

		softly.assertThat(dict1)
			.containsExactlyEntriesOf(dict2);
		softly.assertThatCode(() -> DictionaryAssert.assertThat(dict1)
			.containsExactlyEntriesOf(dict3))
			.as("does not containsExactlyEntriesOf")
			.isInstanceOf(AssertionError.class);
		softly.assertThatCode(() -> DictionaryAssert.assertThat(dict1)
			.containsExactlyEntriesOf(dict4))
			.as("does not containsExactlyEntriesOf")
			.isInstanceOf(AssertionError.class);
		softly.assertThatCode(() -> DictionaryAssert.assertThat(dict1)
			.containsExactlyEntriesOf(dict5))
			.as("does not containsExactlyEntriesOf")
			.isInstanceOf(AssertionError.class);
	}

	@Test
	public void containsExactlyInAnyOrderEntriesOf(DictionarySoftAssertions softly) throws Exception {
		Dictionary<String, String> dict1 = new TestDictionary<>();
		dict1.put("key1", "value1");
		dict1.put("key2", "value2");
		Dictionary<String, String> dict2 = new TestDictionary<>();
		dict2.put("key1", "value1");
		dict2.put("key2", "value2");
		Dictionary<String, String> dict3 = new TestDictionary<>();
		dict3.put("key1", "value1");
		Dictionary<String, String> dict4 = new TestDictionary<>();
		dict4.put("key2", "value2");
		dict4.put("key1", "value1");
		Dictionary<String, String> dict5 = new TestDictionary<>();
		dict5.put("key1", "value1");
		dict5.put("key2", "value2");
		dict5.put("key3", "value3");

		softly.assertThat(dict1)
			.containsExactlyInAnyOrderEntriesOf(dict2);
		softly.assertThat(dict1)
			.containsExactlyInAnyOrderEntriesOf(dict4);
		softly.assertThatCode(() -> DictionaryAssert.assertThat(dict1)
			.containsExactlyInAnyOrderEntriesOf(dict3))
			.as("does not containsExactlyInAnyOrderEntriesOf")
			.isInstanceOf(AssertionError.class);
		softly.assertThatCode(() -> DictionaryAssert.assertThat(dict1)
			.containsExactlyInAnyOrderEntriesOf(dict5))
			.as("does not containsExactlyInAnyOrderEntriesOf")
			.isInstanceOf(AssertionError.class);
	}

	@Test
	public void hasSameSizeAs(DictionarySoftAssertions softly) throws Exception {
		Dictionary<String, String> dict1 = new TestDictionary<>();
		dict1.put("key1", "value1");
		dict1.put("key2", "value2");
		Dictionary<String, String> dict2 = new TestDictionary<>();
		dict2.put("key1", "value1");
		dict2.put("key2", "value2");
		Dictionary<String, String> dict3 = new TestDictionary<>();
		dict3.put("key1", "value1");

		softly.assertThat(dict1)
			.hasSameSizeAs(dict2);
		softly.assertThatCode(() -> DictionaryAssert.assertThat(dict1)
			.hasSameSizeAs(dict3))
			.as("does not hasSameSizeAs")
			.isInstanceOf(AssertionError.class);
	}

	public static class TestDictionary<K, V> extends Dictionary<K, V> {
		private final Map<K, V> map;

		public TestDictionary() {
			this.map = new LinkedHashMap<>();
		}

		@Override
		public int size() {
			return map.size();
		}

		@Override
		public boolean isEmpty() {
			return map.isEmpty();
		}

		@Override
		public Enumeration<K> keys() {
			return Collections.enumeration(map.keySet());
		}

		@Override
		public Enumeration<V> elements() {
			return Collections.enumeration(map.values());
		}

		@Override
		public V get(Object key) {
			if (key == null) {
				return null;
			}
			return map.get(key);
		}

		@Override
		public V put(K key, V value) {
			if ((key == null) || (value == null)) {
				throw new NullPointerException();
			}
			return map.put(key, value);
		}

		@Override
		public V remove(Object key) {
			if (key == null) {
				return null;
			}
			return map.remove(key);
		}

		@Override
		public String toString() {
			return map.toString();
		}
	}

}
