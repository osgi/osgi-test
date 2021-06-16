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

import static java.util.Objects.requireNonNull;
import static org.assertj.core.error.ShouldNotBeNull.shouldNotBeNull;

import java.util.Dictionary;
import java.util.Map;

import org.assertj.core.api.InstanceOfAssertFactory;
import org.osgi.test.common.dictionary.Dictionaries;

public class DictionaryAssert<KEY, VALUE>
	extends AbstractDictionaryAssert<DictionaryAssert<KEY, VALUE>, Map<KEY, VALUE>, KEY, VALUE> {

	public DictionaryAssert(Dictionary<KEY, VALUE> actual) {
		this((actual != null) ? Dictionaries.asMap(actual) : null);
	}

	protected DictionaryAssert(Map<KEY, VALUE> actual) {
		super(actual, DictionaryAssert.class);
	}

	public static <K, V> DictionaryAssert<K, V> assertThat(Dictionary<K, V> actual) {
		return new DictionaryAssert<>(actual);
	}

	public static final <ACTUAL extends Dictionary<K, V>, K, V> InstanceOfAssertFactory<ACTUAL, DictionaryAssert<K, V>> dictionary(
		Class<K> keyType, Class<V> valueType) {
		requireNonNull(keyType, shouldNotBeNull("keyType").create());
		requireNonNull(valueType, shouldNotBeNull("valueType").create());
		@SuppressWarnings({
			"unchecked", "rawtypes"
		})
		Class<ACTUAL> type = (Class) Dictionary.class;
		return new InstanceOfAssertFactory<>(type, DictionaryAssert::<K, V> assertThat);
	}
}
