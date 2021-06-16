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

import static org.assertj.core.api.Assertions.fail;

import java.lang.reflect.InvocationTargetException;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assert;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.assertj.core.api.InstanceOfAssertFactory;
import org.assertj.core.api.ObjectAssert;
import org.assertj.core.api.ThrowableAssert;
import org.osgi.test.common.exceptions.Exceptions;
import org.osgi.util.promise.Promise;

/**
 * Assertions for {@link Promise}s.
 *
 * @param <RESULT> The type of the value contained in the {@link Promise}.
 */
public abstract class AbstractPromiseAssert<SELF extends AbstractPromiseAssert<SELF, ACTUAL, RESULT>, ACTUAL extends Promise<? extends RESULT>, RESULT>
	extends AbstractAssert<SELF, ACTUAL> {
	protected AbstractPromiseAssert(ACTUAL actual, Class<?> selfType) {
		super(actual, selfType);
	}

	void assertDone() {
		if (!actual.isDone()) {
			throw failure("%nExpecting%n  <%s>%nto be done.", actual);
		}
	}

	/**
	 * Verifies that the {@link Promise} is resolved. That is,
	 * {@link Promise#isDone()} returns {@code true}.
	 *
	 * @return This assertion object.
	 * @throws AssertionError If the {@link Promise} is unresolved.
	 */
	public SELF isDone() {
		isNotNull().assertDone();
		return myself;
	}

	void assertNotDone() {
		if (actual.isDone()) {
			throw failure("%nExpecting%n  <%s>%nto not be done.", actual);
		}
	}

	/**
	 * Verifies that the {@link Promise} is unresolved. That is,
	 * {@link Promise#isDone()} returns {@code false}.
	 *
	 * @return This assertion object.
	 * @throws AssertionError If the {@link Promise} is resolved.
	 */
	public SELF isNotDone() {
		isNotNull().assertNotDone();
		return myself;
	}

	/**
	 * Verifies that the {@link Promise} is resolved or does resolve within the
	 * specified timeout.
	 *
	 * @param timeout The specified timeout.
	 * @param unit The unit for the specified timeout.
	 * @return This assertion object.
	 * @throws AssertionError If the {@link Promise} is unresolved when the
	 *             timeout expires.
	 */
	public SELF resolvesWithin(long timeout, TimeUnit unit) {
		isNotNull();
		if (!actual.isDone()) {
			final CountDownLatch latch = new CountDownLatch(1);
			actual.onResolve(latch::countDown);
			try {
				if (!latch.await(timeout, unit)) {
					throw failure("%nExpecting%n  <%s>%nto have resolved.", actual);
				}
			} catch (InterruptedException e) {
				Thread.currentThread()
					.interrupt();
				fail("unexpected exception", e);
			}
			assertDone();
		}
		return myself;
	}

	/**
	 * Verifies that the {@link Promise} is resolved or does resolve within the
	 * specified timeout.
	 *
	 * @param timeout The specified timeout.
	 * @return This assertion object.
	 * @throws AssertionError If the {@link Promise} is unresolved when the
	 *             timeout expires.
	 */
	public SELF resolvesWithin(Duration timeout) {
		return resolvesWithin(timeout.toNanos(), TimeUnit.NANOSECONDS);
	}

	/**
	 * Verifies that the {@link Promise} is unresolved and does not resolve
	 * within the specified timeout.
	 *
	 * @param timeout The specified timeout.
	 * @param unit The unit for the specified timeout.
	 * @return This assertion object.
	 * @throws AssertionError If the {@link Promise} is resolved or resolves
	 *             before the timeout expires.
	 */
	public SELF doesNotResolveWithin(long timeout, TimeUnit unit) {
		isNotDone();
		final CountDownLatch latch = new CountDownLatch(1);
		actual.onResolve(latch::countDown);
		try {
			if (latch.await(timeout, unit)) {
				throw failure("%nExpecting%n  <%s>%nto not have resolved.", actual);
			}
		} catch (InterruptedException e) {
			Thread.currentThread()
				.interrupt();
			fail("unexpected exception", e);
		}
		assertNotDone();
		return myself;
	}

	/**
	 * Verifies that the {@link Promise} is unresolved and does not resolve
	 * within the specified timeout.
	 *
	 * @param timeout The specified timeout.
	 * @return This assertion object.
	 * @throws AssertionError If the {@link Promise} is resolved or resolves
	 *             before the timeout expires.
	 */
	public SELF doesNotResolveWithin(Duration timeout) {
		return doesNotResolveWithin(timeout.toNanos(), TimeUnit.NANOSECONDS);
	}

	Throwable getFailure(ACTUAL promise) {
		try {
			return promise.getFailure();
		} catch (InterruptedException e) {
			Thread.currentThread()
				.interrupt();
			fail("unexpected exception", e);
			return null;
		}
	}

	void assertFailed() {
		Throwable fail = getFailure(actual);
		if (fail == null) {
			throw failure("%nExpecting%n  <%s>%nto have failed.", actual);
		}
	}

	/**
	 * Verifies that the {@link Promise} is resolved with a failure.
	 *
	 * @return This assertion object.
	 * @throws AssertionError If the {@link Promise} is unresolved or is
	 *             resolved successfully.
	 */
	public SELF hasFailed() {
		isDone().assertFailed();
		return myself;
	}

	/**
	 * Verifies that the {@link Promise} is resolved with a failure and returns
	 * an assertion on the failure.
	 *
	 * @return A {@link ThrowableAssert} holding the failure of the
	 *         {@link Promise}.
	 * @throws AssertionError If the {@link Promise} is unresolved or is
	 *             resolved successfully.
	 */
	public ThrowableAssert hasFailedWithThrowableThat() {
		return (ThrowableAssert) hasFailed().extracting(this::getFailure, InstanceOfAssertFactories.THROWABLE);
	}

	void assertNotFailed() {
		if (actual.isDone()) {
			Throwable fail = getFailure(actual);
			if (fail != null) {
				throw failure("%nExpecting%n  <%s>%nto have not failed but failed with:%n%s", actual,
					Exceptions.toString(fail));
			}
		}
	}

	/**
	 * Verifies that the {@link Promise} is unresolved or is resolved
	 * successfully.
	 *
	 * @return This assertion object.
	 * @throws AssertionError If the {@link Promise} is resolved with a failure.
	 */
	public SELF hasNotFailed() {
		isNotNull().assertNotFailed();
		return myself;
	}

	/**
	 * Verifies that the {@link Promise} is resolved successfully.
	 *
	 * @return This assertion object.
	 * @throws AssertionError If the {@link Promise} is unresolved or is
	 *             resolved with a failure.
	 */
	public SELF isSuccessful() {
		isDone().assertNotFailed();
		return myself;
	}

	RESULT getValue(ACTUAL promise) {
		try {
			return promise.getValue();
		} catch (InvocationTargetException e) {
			throw failure("%nExpecting%n  <%s>%nto have not failed but failed with %s", promise,
				Exceptions.toString(e.getCause()));
		} catch (InterruptedException e) {
			Thread.currentThread()
				.interrupt();
			throw new AssertionError("unexpected exception", e);
		}
	}

	/**
	 * Verifies that the {@link Promise} is resolved successfully and returns an
	 * assertion on the value of the {@link Promise}.
	 *
	 * @return An {@link ObjectAssert} holding the value of the {@link Promise}.
	 * @throws AssertionError If the {@link Promise} is unresolved or is
	 *             resolved with a failure.
	 */
	public ObjectAssert<RESULT> hasValueThat() {
		return isSuccessful().extracting(this::getValue, Assertions::<RESULT> assertThat);
	}

	/**
	 * Verifies that the {@link Promise} is resolved successfully and returns an
	 * assertion on the value of the {@link Promise} narrowed by the specified
	 * assertion factory.
	 *
	 * @param <ASSERT> The type of the return assertion on the value contained
	 *            in the {@link Promise}.
	 * @param assertFactory The factory which verifies the type and creates the
	 *            new {@link Assert}. See
	 *            {@link Assert#asInstanceOf(InstanceOfAssertFactory)}.
	 * @return A narrowed {@link Assert} holding the value of the
	 *         {@link Promise}.
	 * @throws AssertionError If the {@link Promise} is unresolved, is resolved
	 *             with a failure, or if the type of the value of the
	 *             {@link Promise} is not compatible with the specified
	 *             assertion factory.
	 */
	public <ASSERT extends AbstractAssert<?, ?>> ASSERT hasValueThat(
		InstanceOfAssertFactory<? super RESULT, ASSERT> assertFactory) {
		return isSuccessful().extracting(this::getValue, assertFactory);
	}

	/**
	 * Verifies that the {@link Promise} is resolved successfully and has a
	 * value that is equal to the {@code expected} value.
	 *
	 * @param expected The expected value.
	 * @return This assertion object.
	 * @throws AssertionError If the {@link Promise} is unresolved, is resolved
	 *             with a failure, or has a value that is not equal to the
	 *             {@code expected} value.
	 */
	public SELF hasValue(RESULT expected) {
		hasValueThat().isEqualTo(expected);
		return myself;
	}

	/**
	 * Verifies that the {@link Promise} is resolved successfully and has a
	 * value that is identical, using {@code ==}, to the {@code expected} value.
	 *
	 * @param expected The expected value.
	 * @return This assertion object.
	 * @throws AssertionError If the {@link Promise} is unresolved, is resolved
	 *             with a failure, or has a value that is not identical to the
	 *             {@code expected} value.
	 */
	public SELF hasSameValue(RESULT expected) {
		hasValueThat().isSameAs(expected);
		return myself;
	}

	/**
	 * Verifies that the {@link Promise} is resolved successfully and has a
	 * value for which the {@code predicate} returns {@code true}.
	 *
	 * @param predicate The predicate to use on the value.
	 * @param predicateDescription A description of the predicate to use in an
	 *            error message.
	 * @return This assertion object.
	 * @throws AssertionError If the {@link Promise} is unresolved, is resolved
	 *             with a failure, or if the {@code predicate} returns
	 *             {@code false}.
	 */
	public SELF hasValueMatching(Predicate<? super RESULT> predicate, String predicateDescription) {
		hasValueThat().matches(predicate, predicateDescription);
		return myself;
	}

	/**
	 * Verifies that the {@link Promise} is resolved successfully and has a
	 * value for which the {@code predicate} returns {@code true}.
	 *
	 * @param predicate The predicate to use on the value.
	 * @return This assertion object.
	 * @throws AssertionError If the {@link Promise} is unresolved, is resolved
	 *             with a failure, or if the {@code predicate} returns
	 *             {@code false}.
	 */
	public SELF hasValueMatching(Predicate<? super RESULT> predicate) {
		return hasValueMatching(predicate, "given");
	}
}
