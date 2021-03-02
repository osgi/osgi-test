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
import java.util.List;
import java.util.Map;

import org.assertj.core.api.AbstractListAssert;
import org.assertj.core.api.ObjectAssert;
import org.assertj.core.api.ProxyableListAssert;
import org.osgi.test.common.dictionary.Dictionaries;

public class ProxyableDictionaryAssert<KEY, VALUE>
	extends AbstractDictionaryAssert<ProxyableDictionaryAssert<KEY, VALUE>, Map<KEY, VALUE>, KEY, VALUE> {

	public ProxyableDictionaryAssert(Dictionary<KEY, VALUE> actual) {
		this(Dictionaries.asMap(actual));
	}

	protected ProxyableDictionaryAssert(Map<KEY, VALUE> actual) {
		super(actual, ProxyableDictionaryAssert.class);
	}

	@Override
	protected <ELEMENT> AbstractListAssert<?, List<? extends ELEMENT>, ELEMENT, ObjectAssert<ELEMENT>> newListAssertInstance(
		List<? extends ELEMENT> newActual) {
		return new ProxyableListAssert<>(newActual);
	}
}
