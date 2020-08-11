/*
 * Copyright (c) OSGi Alliance (2020). All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.osgi.test.assertj.dictionary;

import java.util.Dictionary;
import java.util.Map;

import org.assertj.core.api.ProxyableMapAssert;
import org.assertj.core.api.SoftAssertionsProvider;
import org.osgi.test.common.dictionary.Dictionaries;

public interface DictionarySoftAssertionsProvider extends SoftAssertionsProvider {
	/**
	 * Create soft assertion for {@link java.util.Dictionary}.
	 *
	 * @param actual the actual value.
	 * @return the created assertion object.
	 */
	@SuppressWarnings("unchecked")
	default <K, V> ProxyableMapAssert<K, V> assertThat(Dictionary<K, V> actual) {
		return proxy(ProxyableMapAssert.class, Map.class, Dictionaries.asMap(actual));
	}
}
