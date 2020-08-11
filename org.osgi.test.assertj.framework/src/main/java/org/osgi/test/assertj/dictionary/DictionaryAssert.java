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

import static java.util.Objects.requireNonNull;
import static org.assertj.core.error.ShouldNotBeNull.shouldNotBeNull;

import java.util.Dictionary;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.assertj.core.api.AbstractListAssert;
import org.assertj.core.api.InstanceOfAssertFactory;
import org.assertj.core.api.ObjectAssert;
import org.assertj.core.groups.Tuple;
import org.osgi.test.common.dictionary.Dictionaries;

public class DictionaryAssert<KEY, VALUE>
	extends AbstractDictionaryAssert<DictionaryAssert<KEY, VALUE>, Map<KEY, VALUE>, KEY, VALUE> {

	public DictionaryAssert(Dictionary<KEY, VALUE> actual) {
		this(Dictionaries.asMap(actual));
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

	// override methods to annotate them with @SafeVarargs, we unfortunately
	// can't do that in AbstractMapAssert as it is
	// used in soft assertions which need to be able to proxy method -
	// @SafeVarargs requiring method to be final prevents
	// using proxies.

	@SafeVarargs
	@Override
	public final DictionaryAssert<KEY, VALUE> contains(Map.Entry<? extends KEY, ? extends VALUE>... entries) {
		return super.contains(entries);
	}

	@SafeVarargs
	@Override
	public final DictionaryAssert<KEY, VALUE> containsAnyOf(Map.Entry<? extends KEY, ? extends VALUE>... entries) {
		return super.containsAnyOf(entries);
	}

	@SafeVarargs
	@Override
	public final DictionaryAssert<KEY, VALUE> containsOnly(Map.Entry<? extends KEY, ? extends VALUE>... entries) {
		return super.containsOnly(entries);
	}

	@SafeVarargs
	@Override
	public final DictionaryAssert<KEY, VALUE> containsExactly(Map.Entry<? extends KEY, ? extends VALUE>... entries) {
		return super.containsExactly(entries);
	}

	@SafeVarargs
	@Override
	public final DictionaryAssert<KEY, VALUE> containsKeys(KEY... keys) {
		return super.containsKeys(keys);
	}

	@SafeVarargs
	@Override
	public final DictionaryAssert<KEY, VALUE> containsOnlyKeys(KEY... keys) {
		return super.containsOnlyKeys(keys);
	}

	@SafeVarargs
	@Override
	public final DictionaryAssert<KEY, VALUE> containsValues(VALUE... values) {
		return super.containsValues(values);
	}

	@SafeVarargs
	@Override
	public final DictionaryAssert<KEY, VALUE> doesNotContainKeys(KEY... keys) {
		return super.doesNotContainKeys(keys);
	}

	@SafeVarargs
	@Override
	public final DictionaryAssert<KEY, VALUE> doesNotContain(Map.Entry<? extends KEY, ? extends VALUE>... entries) {
		return super.doesNotContain(entries);
	}

	@SafeVarargs
	@Override
	public final AbstractListAssert<?, List<?>, Object, ObjectAssert<Object>> extracting(
		Function<? super Map<KEY, VALUE>, ?>... extractors) {
		return super.extracting(extractors);
	}

	@SafeVarargs
	@Override
	public final AbstractListAssert<?, List<? extends VALUE>, VALUE, ObjectAssert<VALUE>> extractingByKeys(
		KEY... keys) {
		return super.extractingByKeys(keys);
	}

	@SafeVarargs
	@Override
	public final AbstractListAssert<?, List<? extends Tuple>, Tuple, ObjectAssert<Tuple>> extractingFromEntries(
		Function<? super Map.Entry<KEY, VALUE>, Object>... extractors) {
		return super.extractingFromEntries(extractors);
	}
}
