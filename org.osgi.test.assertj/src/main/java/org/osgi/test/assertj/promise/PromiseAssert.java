/*
 * Copyright (c) OSGi Alliance (2019-2020). All Rights Reserved.
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
package org.osgi.test.assertj.promise;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.error.ShouldNotBeNull.shouldNotBeNull;

import org.assertj.core.api.InstanceOfAssertFactory;
import org.osgi.util.promise.Promise;

/**
 * Assertions for {@link Promise}s.
 *
 * @param <RESULT> The type of the value contained in the {@link Promise}.
 */
public class PromiseAssert<RESULT>
	extends AbstractPromiseAssert<PromiseAssert<RESULT>, Promise<? extends RESULT>, RESULT> {
	/*
	 * Note: The second generic argument to AbstractPromiseAssert must be
	 * Promise for soft assertion proxying to work as it uses the second generic
	 * argument to our super type to determine the ACTUAL type.
	 */

	/**
	 * Create an assertion for a {@link Promise}.
	 *
	 * @param actual The {@link Promise}.
	 */
	public PromiseAssert(Promise<? extends RESULT> actual) {
		super(actual, PromiseAssert.class);
	}

	/**
	 * Create an assertion for a {@link Promise}.
	 *
	 * @param actual The {@link Promise}.
	 * @param <RESULT> The type of the value contained in the {@link Promise}.
	 * @return The created assertion.
	 */
	public static <RESULT> PromiseAssert<RESULT> assertThat(Promise<? extends RESULT> actual) {
		return new PromiseAssert<>(actual);
	}

	/**
	 * {@link InstanceOfAssertFactory} for a {@link PromiseAssert}.
	 *
	 * @param <RESULT> The {@code Promise} result type.
	 * @param resultType The result type class.
	 * @return The factory instance.
	 * @see #PROMISE
	 */
	public static <ACTUAL extends Promise<? extends RESULT>, RESULT> InstanceOfAssertFactory<ACTUAL, PromiseAssert<RESULT>> promise(
		Class<RESULT> resultType) {
		requireNonNull(resultType, shouldNotBeNull("resultType").create());
		@SuppressWarnings({
			"rawtypes", "unchecked"
		})
		Class<ACTUAL> type = (Class) Promise.class;
		return new InstanceOfAssertFactory<>(type, PromiseAssert::<RESULT> assertThat);
	}

	/**
	 * {@link InstanceOfAssertFactory} for a {@link PromiseAssert} using
	 * {@code Object} as the result type.
	 *
	 * @see #promise(Class)
	 */
	public static final InstanceOfAssertFactory<Promise<?>, PromiseAssert<Object>> PROMISE = promise(
		Object.class);

}
