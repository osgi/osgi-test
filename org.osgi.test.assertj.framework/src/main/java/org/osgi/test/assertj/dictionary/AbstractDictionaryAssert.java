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

import org.assertj.core.api.AbstractMapAssert;
import org.osgi.test.common.dictionary.Dictionaries;

public abstract class AbstractDictionaryAssert<SELF extends AbstractDictionaryAssert<SELF, ACTUAL, K, V>, ACTUAL extends Map<K, V>, K, V>
	extends AbstractMapAssert<SELF, ACTUAL, K, V> {

	protected AbstractDictionaryAssert(ACTUAL actual, Class<?> selfType) {
		super(actual, selfType);
	}

	public SELF containsAllEntriesOf(Dictionary<? extends K, ? extends V> dictionary) {
		return containsAllEntriesOf(Dictionaries.asMap(dictionary));
	}

	public SELF containsExactlyEntriesOf(Dictionary<? extends K, ? extends V> dictionary) {
		return containsExactlyEntriesOf(Dictionaries.asMap(dictionary));
	}

	public SELF containsExactlyInAnyOrderEntriesOf(Dictionary<? extends K, ? extends V> dictionary) {
		return containsExactlyInAnyOrderEntriesOf(Dictionaries.asMap(dictionary));
	}

	public SELF hasSameSizeAs(Dictionary<?, ?> dictionary) {
		return hasSameSizeAs(Dictionaries.asMap(dictionary));
	}
}
