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

package org.osgi.test.common.dictionary;

import static java.util.Objects.requireNonNull;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.osgi.framework.ServiceReference;
import org.osgi.test.common.stream.MapStream;

public class Dictionaries {

	private Dictionaries() {}

	/**
	 * Return a Map wrapper around a Dictionary.
	 *
	 * @param <K> The type of the key.
	 * @param <V> The type of the value.
	 * @param dictionary The dictionary to wrap.
	 * @return A Map object which wraps the specified dictionary. If the
	 *         specified dictionary can be cast to a Map, then the specified
	 *         dictionary is returned.
	 */
	public static <K, V> Map<K, V> asMap(Dictionary<K, V> dictionary) {
		if (dictionary instanceof Map) {
			@SuppressWarnings("unchecked")
			Map<K, V> coerced = (Map<K, V>) dictionary;
			return coerced;
		}
		return new DictionaryAsMap<>(dictionary);
	}

	private static class DictionaryAsMap<K, V> extends AbstractMap<K, V> {
		private final Dictionary<K, V> dict;

		DictionaryAsMap(Dictionary<K, V> dict) {
			this.dict = requireNonNull(dict);
		}

		Iterator<K> keys() {
			List<K> keys = new ArrayList<>(dict.size());
			for (Enumeration<K> e = dict.keys(); e.hasMoreElements();) {
				keys.add(e.nextElement());
			}
			return keys.iterator();
		}

		@Override
		public int size() {
			return dict.size();
		}

		@Override
		public boolean isEmpty() {
			return dict.isEmpty();
		}

		@Override
		public boolean containsKey(Object key) {
			if (key == null) {
				return false;
			}
			return dict.get(key) != null;
		}

		@Override
		public V get(Object key) {
			if (key == null) {
				return null;
			}
			return dict.get(key);
		}

		@Override
		public V put(K key, V value) {
			return dict.put(requireNonNull(key, "a Dictionary cannot contain a null key"),
				requireNonNull(value, "a Dictionary cannot contain a null value"));
		}

		@Override
		public V remove(Object key) {
			if (key == null) {
				return null;
			}
			return dict.remove(key);
		}

		@Override
		public void clear() {
			for (Iterator<K> iter = keys(); iter.hasNext();) {
				dict.remove(iter.next());
			}
		}

		@Override
		public Set<K> keySet() {
			return new KeySet();
		}

		@Override
		public Set<Map.Entry<K, V>> entrySet() {
			return new EntrySet();
		}

		@Override
		public String toString() {
			return dict.toString();
		}

		final class KeySet extends AbstractSet<K> {
			@Override
			public Iterator<K> iterator() {
				return new KeyIterator();
			}

			@Override
			public int size() {
				return DictionaryAsMap.this.size();
			}

			@Override
			public boolean isEmpty() {
				return DictionaryAsMap.this.isEmpty();
			}

			@Override
			public boolean contains(Object key) {
				return DictionaryAsMap.this.containsKey(key);
			}

			@Override
			public boolean remove(Object key) {
				return DictionaryAsMap.this.remove(key) != null;
			}

			@Override
			public void clear() {
				DictionaryAsMap.this.clear();
			}
		}

		final class KeyIterator implements Iterator<K> {
			private final Iterator<K>	keys	= DictionaryAsMap.this.keys();
			private K					key		= null;

			@Override
			public boolean hasNext() {
				return keys.hasNext();
			}

			@Override
			public K next() {
				return key = keys.next();
			}

			@Override
			public void remove() {
				if (key == null) {
					throw new IllegalStateException();
				}
				DictionaryAsMap.this.remove(key);
				key = null;
			}
		}

		final class EntrySet extends AbstractSet<Map.Entry<K, V>> {
			@Override
			public Iterator<Map.Entry<K, V>> iterator() {
				return new EntryIterator();
			}

			@Override
			public int size() {
				return DictionaryAsMap.this.size();
			}

			@Override
			public boolean isEmpty() {
				return DictionaryAsMap.this.isEmpty();
			}

			@Override
			public boolean contains(Object o) {
				if (o instanceof Map.Entry) {
					Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
					return containsEntry(e);
				}
				return false;
			}

			private boolean containsEntry(Map.Entry<?, ?> e) {
				Object key = e.getKey();
				if (key == null) {
					return false;
				}
				Object value = e.getValue();
				if (value == null) {
					return false;
				}
				return Objects.equals(DictionaryAsMap.this.get(key), value);
			}

			@Override
			public boolean remove(Object o) {
				if (o instanceof Map.Entry) {
					Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
					if (containsEntry(e)) {
						DictionaryAsMap.this.remove(e.getKey());
						return true;
					}
				}
				return false;
			}

			@Override
			public void clear() {
				DictionaryAsMap.this.clear();
			}
		}

		final class EntryIterator implements Iterator<Map.Entry<K, V>> {
			private final Iterator<K>	keys	= DictionaryAsMap.this.keys();
			private K					key		= null;

			@Override
			public boolean hasNext() {
				return keys.hasNext();
			}

			@Override
			public Map.Entry<K, V> next() {
				return new Entry(key = keys.next());
			}

			@Override
			public void remove() {
				if (key == null) {
					throw new IllegalStateException();
				}
				DictionaryAsMap.this.remove(key);
				key = null;
			}
		}

		final class Entry extends SimpleEntry<K, V> {
			private static final long serialVersionUID = 1L;

			Entry(K key) {
				super(key, DictionaryAsMap.this.get(key));
			}

			@Override
			public V setValue(V value) {
				DictionaryAsMap.this.put(getKey(), value);
				return super.setValue(value);
			}
		}
	}

	/**
	 * Return a Dictionary wrapper around a Map.
	 *
	 * @param <K> The type of the key.
	 * @param <V> The type of the value.
	 * @param map The map to wrap.
	 * @return A Dictionary object which wraps the specified map. If the
	 *         specified map can be cast to a Dictionary, then the specified map
	 *         is returned.
	 */
	public static <K, V> Dictionary<K, V> asDictionary(Map<K, V> map) {
		if (map instanceof Dictionary) {
			@SuppressWarnings("unchecked")
			Dictionary<K, V> coerced = (Dictionary<K, V>) map;
			return coerced;
		}
		return new MapAsDictionary<>(map);
	}

	private static class MapAsDictionary<K, V> extends Dictionary<K, V> implements Map<K, V> {
		private final Map<K, V> map;

		MapAsDictionary(Map<K, V> map) {
			this.map = requireNonNull(map);
			boolean nullKey;
			try {
				nullKey = map.containsKey(null);
			} catch (NullPointerException e) {
				nullKey = false; // map does not allow null key
			}
			if (nullKey) {
				throw new NullPointerException("a Dictionary cannot contain a null key");
			}
			boolean nullValue;
			try {
				nullValue = map.containsValue(null);
			} catch (NullPointerException e) {
				nullValue = false; // map does not allow null value
			}
			if (nullValue) {
				throw new NullPointerException("a Dictionary cannot contain a null value");
			}
		}

		@Override
		public boolean containsKey(Object key) {
			return map.containsKey(key);
		}

		@Override
		public boolean containsValue(Object value) {
			return map.containsValue(value);
		}

		@Override
		public void putAll(Map<? extends K, ? extends V> m) {
			MapStream.of(m)
				.forEachOrdered(this::put);
		}

		@Override
		public void clear() {
			map.clear();
		}

		@Override
		public Set<K> keySet() {
			return map.keySet();
		}

		@Override
		public Collection<V> values() {
			return map.values();
		}

		@Override
		public Set<Entry<K, V>> entrySet() {
			return map.entrySet();
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
			return map.put(requireNonNull(key, "a Dictionary cannot contain a null key"),
				requireNonNull(value, "a Dictionary cannot contain a null value"));
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

	public static <K, V> Dictionary<K, V> dictionaryOf() {
		return new MapAsDictionary<>(Collections.emptyMap());
	}

	public static <K, V> Dictionary<K, V> dictionaryOf(K k1, V v1) {
		return MapStream.of(k1, v1)
			.collect(toDictionary());
	}

	public static <K, V> Dictionary<K, V> dictionaryOf(K k1, V v1, K k2, V v2) {
		return MapStream.of(k1, v1, k2, v2)
			.collect(toDictionary());
	}

	public static <K, V> Dictionary<K, V> dictionaryOf(K k1, V v1, K k2, V v2, K k3, V v3) {
		return MapStream.of(k1, v1, k2, v2, k3, v3)
			.collect(toDictionary());
	}

	public static <K, V> Dictionary<K, V> dictionaryOf(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4) {
		return MapStream.of(k1, v1, k2, v2, k3, v3, k4, v4)
			.collect(toDictionary());
	}

	private static <K, V> Collector<? super Map.Entry<? extends K, ? extends V>, ?, Dictionary<K, V>> toDictionary() {
		return Collectors.collectingAndThen(MapStream.toMap((u, v) -> {
			throw new IllegalArgumentException("duplicate keys");
		}, (Supplier<Map<K, V>>) LinkedHashMap::new), map -> new MapAsDictionary<>(Collections.unmodifiableMap(map)));
	}

	private static class ServiceReferenceAsDictionary extends Dictionary<String, Object> {
		private final ServiceReference<?> serviceReference;

		ServiceReferenceAsDictionary(ServiceReference<?> serviceReference) {
			this.serviceReference = requireNonNull(serviceReference);
			boolean nullKey = keysInternal().contains(null);

			if (nullKey) {
				throw new NullPointerException("a Dictionary cannot contain a null key");
			}
			boolean nullValue = false;

			for (String key : keysInternal()) {
				if (Objects.isNull(serviceReference.getProperty(key))) {
					nullValue = true;
					break;
				}
			}

			if (nullValue) {
				throw new NullPointerException("a Dictionary cannot contain a null value");
			}
		}

		private List<String> keysInternal() {
			String[] keys = serviceReference.getPropertyKeys();
			return Objects.isNull(keys) ? Collections.emptyList() : Arrays.asList(keys);
		}

		@Override
		public int size() {
			return keysInternal().size();
		}

		@Override
		public boolean isEmpty() {
			return keysInternal().isEmpty();
		}

		@Override
		public Enumeration<String> keys() {
			return Collections.enumeration(keysInternal());
		}

		@Override
		public Enumeration<Object> elements() {
			List<Object> values = new ArrayList<>();
			for (String key : keysInternal()) {
				values.add(serviceReference.getProperty(key));
			}
			return Collections.enumeration(values);
		}

		@Override
		public Object get(Object key) {
			if (key instanceof String || key == null) {
				return serviceReference.getProperty((String) key);
			}
			throw new IllegalArgumentException();
		}

		@Override
		public Object put(String key, Object value) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Object remove(Object key) {
			throw new UnsupportedOperationException();
		}
	}

	public static <K, V> Dictionary<String, Object> asDictionary(ServiceReference<?> serviceReference) {
		return new ServiceReferenceAsDictionary(serviceReference);
	}

	/**
	 * Return a human readable String with the content of the Dictionary
	 *
	 * @param dict The Dictionary to transform.
	 * @return A a human readable String with the content of the Dictionary
	 */
	public static String toString(Dictionary<?, ?> dict) {

		StringBuilder mapAsString = new StringBuilder("{");

		Enumeration<?> keys = dict.keys();
		while (keys.hasMoreElements()) {
			Object key = keys.nextElement();
			Object value = dict.get(key);
			mapAsString.append(key + "=" + objectToString(value) + ", ");
		}
		mapAsString.delete(mapAsString.length() - 2, mapAsString.length())
			.append("}");

		return mapAsString.toString();

	}

	private static String objectToString(Object obj) {

		if (obj == null) {
			return "<null>";
		} else if (obj instanceof Object[]) {
			return Arrays.toString((Object[]) obj);
		}
		return obj.toString();
	}
}
