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

package org.osgi.test.assertj.promise;

import org.assertj.core.api.SoftAssertionsProvider;
import org.osgi.util.promise.Promise;

/**
 * Soft assertions for {@link Promise}s.
 */
public interface PromiseSoftAssertionsProvider extends SoftAssertionsProvider {
	/**
	 * Create a soft assertion for a {@link Promise}.
	 *
	 * @param actual The {@link Promise}.
	 * @param <T> The type of the value contained in the {@link Promise}.
	 * @return The created soft assertion.
	 */
	default <T> PromiseAssert<T> assertThat(Promise<? extends T> actual) {
		@SuppressWarnings("unchecked")
		PromiseAssert<T> softly = proxy(PromiseAssert.class, Promise.class, actual);
		return softly;
	}
}
