package org.osgi.test.junit5.dictionary;

import java.util.Dictionary;
import java.util.Hashtable;

import org.junit.jupiter.api.Test;
import org.osgi.test.assertj.dictionary.DictionarySoftAssertions;
import org.osgi.test.assertj.dictionary.DictionarySoftAssertionsProvider;

public class DictionaryAssertTest {

	@Test
	void testDictionarySoftAssertion() {
		Dictionary<String, String> dict1 = new Hashtable<>();
		dict1.put("key1", "value1");
		dict1.put("key2", "value2");
		Dictionary<String, String> dict2 = new Hashtable<>();
		dict2.put("key1", "value1");
		dict2.put("key2", "value2");

		DictionarySoftAssertionsProvider softly = new DictionarySoftAssertions();
		softly.assertThat(dict1)
			.containsAllEntriesOf(dict2);
		softly.assertAll();
	}

}
