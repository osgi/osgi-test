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

import java.util.function.Consumer;

/**
 * Consumer interface that allows exceptions.
 *
 * @param <T> the type of the argument
 */
@FunctionalInterface
public interface ConsumerWithException<T> {
	void accept(T t) throws Exception;

	default Consumer<T> orElseThrow() {
		return t -> {
			try {
				accept(t);
			} catch (Exception e) {
				throw Exceptions.duck(e);
			}
		};
	}

	default Consumer<T> ignoreException() {
		return t -> {
			try {
				accept(t);
			} catch (Exception e) {}
		};
	}

	static <T> Consumer<T> asConsumer(ConsumerWithException<T> unchecked) {
		return unchecked.orElseThrow();
	}

	static <T> Consumer<T> asConsumerIgnoreException(ConsumerWithException<T> unchecked) {
		return unchecked.ignoreException();
	}
}
