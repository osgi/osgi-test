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

package org.osgi.test.common.exceptions;

import java.util.function.BiConsumer;

/**
 * BiConsumer interface that allows exceptions.
 *
 * @param <T> the type of the first argument
 * @param <U> the type of the second argument
 */
@FunctionalInterface
public interface BiConsumerWithException<T, U> {
	void accept(T t, U u) throws Exception;

	default BiConsumer<T, U> orElseThrow() {
		return (t, u) -> {
			try {
				accept(t, u);
			} catch (Exception e) {
				throw Exceptions.duck(e);
			}
		};
	}

	default BiConsumer<T, U> ignoreException() {
		return (t, u) -> {
			try {
				accept(t, u);
			} catch (Exception e) {}
		};
	}

	static <T, U> BiConsumer<T, U> asBiConsumer(BiConsumerWithException<T, U> unchecked) {
		return unchecked.orElseThrow();
	}

	static <T, U> BiConsumer<T, U> asBiConsumerIgnoreException(BiConsumerWithException<T, U> unchecked) {
		return unchecked.ignoreException();
	}
}
