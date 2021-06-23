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

package org.osgi.test.junit5.test.types;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import org.junit.jupiter.api.extension.ExtensionContext.Store;

public class MockStore implements Store {

	private final Map<Object, Object> store = new ConcurrentHashMap<>();

	final StackTraceElement[]			whoMadeMe;

	public MockStore() {
		whoMadeMe = Thread.currentThread()
			.getStackTrace();
	}

	@Override
	public Object get(Object key) {
		return store.get(key);
	}

	@Override
	public <V> V get(Object key, Class<V> requiredType) {
		return requiredType.cast(store.get(key));
	}

	@Override
	@SuppressWarnings("unchecked")
	public <K, V> Object getOrComputeIfAbsent(K key, Function<K, V> defaultCreator) {
		return store.computeIfAbsent((Object) key, (Function<Object, Object>) defaultCreator);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <K, V> V getOrComputeIfAbsent(K key, Function<K, V> defaultCreator, Class<V> requiredType) {
		return requiredType.cast(store.computeIfAbsent((Object) key, (Function<Object, Object>) defaultCreator));
	}

	@Override
	public void put(Object key, Object value) {
		store.put(key, value);
	}

	@Override
	public Object remove(Object key) {
		return store.remove(key);
	}

	@Override
	public <V> V remove(Object key, Class<V> requiredType) {
		return requiredType.cast(store.remove(key));
	}

}
