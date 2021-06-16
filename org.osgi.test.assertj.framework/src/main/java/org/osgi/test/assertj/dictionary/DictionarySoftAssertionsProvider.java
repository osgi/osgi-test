/*******************************************************************************
 * Copyright (c) Contributors to the Eclipse Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 *******************************************************************************/

package org.osgi.test.assertj.dictionary;

import java.util.Dictionary;

import org.assertj.core.api.SoftAssertionsProvider;

public interface DictionarySoftAssertionsProvider extends SoftAssertionsProvider {
	/**
	 * Create soft assertion for {@link java.util.Dictionary}.
	 *
	 * @param actual the actual value.
	 * @param <K> the type of the keys used in the dictionary
	 * @param <V> the type of the values used in the dictionary
	 * @return the created assertion object.
	 */
	default <K, V> DictionaryAssert<K, V> assertThat(Dictionary<K, V> actual) {
		@SuppressWarnings("unchecked")
		DictionaryAssert<K, V> softly = proxy(DictionaryAssert.class, Dictionary.class, actual);
		return softly;
	}
}
