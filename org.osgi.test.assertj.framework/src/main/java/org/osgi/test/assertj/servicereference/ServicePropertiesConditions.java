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

package org.osgi.test.assertj.servicereference;

import java.util.Collections;
import java.util.Dictionary;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import org.assertj.core.api.Condition;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.test.common.dictionary.Dictionaries;

/**
 * A Utility-Class thats Provides public static methods to create
 * {@link Condition}s for ServiceProperties
 *
 * @since 1.2
 */
public final class ServicePropertiesConditions {
	public static Condition<Dictionary<String, Object>> servicePropertiesContains(
		Dictionary<String, Object> dictionary) {
		return servicePropertiesContains(Dictionaries.asMap(dictionary));
	}

	public static Condition<Dictionary<String, Object>> servicePropertiesMatch(String filter)
		throws InvalidSyntaxException {
		Filter f = FrameworkUtil.createFilter(filter);

		return new Condition<Dictionary<String, Object>>(d -> {
			return f.match(d);
		}, "machts filter %s", filter);

	}

	public static Condition<Dictionary<String, Object>> servicePropertiesContains(Map<String, Object> map) {
		return new Condition<Dictionary<String, Object>>(d -> {
			List<String> keys = Collections.list(d.keys());
			for (Entry<String, Object> entry : map.entrySet()) {
				if (!keys.contains(entry.getKey())) {
					return false;
				}
				if (!Objects.equals(d.get(entry.getKey()), entry.getValue())) {
					return false;
				}
			}
			return true;
		}, "contains ServiceProperties %s", map);
	}

	public static Condition<Dictionary<String, Object>> servicePropertyContains(final String key, Object value) {
		return servicePropertiesContains(Dictionaries.dictionaryOf(key, value));
	}
}
