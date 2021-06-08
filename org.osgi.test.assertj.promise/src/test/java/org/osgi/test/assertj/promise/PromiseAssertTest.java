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

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.osgi.test.assertj.promise.PromiseAssert.assertThat;
import static org.osgi.test.assertj.testutils.TestUtil.waitForThreadToWait;

import java.lang.reflect.InvocationTargetException;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;

import org.assertj.core.api.AbstractThrowableAssert;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.assertj.core.api.StandardSoftAssertionsProvider;
import org.assertj.core.api.junit.jupiter.InjectSoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.util.promise.Deferred;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.Promises;

@ExtendWith(SoftAssertionsExtension.class)
public class PromiseAssertTest {
	public static final Duration	WAIT_TIME	= Duration.ofSeconds(2L);

	static final long				TIMEOUT_MS	= 10000;

	static class OurSoftAssertions extends PromiseSoftAssertions implements StandardSoftAssertionsProvider {}

	@InjectSoftAssertions
	OurSoftAssertions softly;

	@Test
	void testPromise_thatIsExpectedToResolveInTheFuture_butDoesnt() throws InterruptedException {
		final String value = new String("value");
		final Deferred<String> d = new Deferred<>();
		final Promise<String> p = d.getPromise();

		AtomicReference<AbstractThrowableAssert<?, ?>> pa = new AtomicReference<>();
		Thread t = new Thread(() -> pa.set(assertThatCode(() -> assertThat(p).as("foo")
			.doesNotResolveWithin(WAIT_TIME))), "assertThread");
		t.start();
		try {
			waitForThreadToWait(t, softly);
			d.resolve(value);
		} finally {
			t.join(TIMEOUT_MS);
		}
		softly.assertThat(pa.get())
			.as("expectedAssertion")
			.isNotNull();
		if (softly.wasSuccess()) {
			pa.get()
				.isInstanceOf(AssertionError.class)
				.hasMessageContaining("foo")
				.hasMessageContaining("not")
				.hasMessageContaining("resolved");
		}
	}

	@Test
	void testPromise_thatIsExpectedNotToResolveInTheFuture_butDoes() throws InterruptedException {
		final String value = new String("value");
		final Deferred<String> d = new Deferred<>();
		final Promise<String> p = d.getPromise();

		AtomicReference<AbstractThrowableAssert<?, ?>> pa = new AtomicReference<>();
		Thread t = new Thread(() -> pa.set(assertThatCode(() -> assertThat(p).as("foo")
			.resolvesWithin(WAIT_TIME))), "assertThread");
		t.start();
		try {
			waitForThreadToWait(t, softly);
			d.resolve(value);
		} finally {
			t.join(TIMEOUT_MS);
		}
		softly.assertThat(pa.get())
			.as("expectedAssertion")
			.isNotNull();
		if (softly.wasSuccess()) {
			pa.get()
				.doesNotThrowAnyException();
		}
	}

	@Test
	void hasValueThat_withFailedPromise() throws Exception {
		final String value = new String("value");
		final Deferred<String> d = new Deferred<>();
		final Promise<String> p = mock(Promise.class);

		final String eMsg = "customMessage";
		Exception e = new Exception(eMsg);
		InvocationTargetException ite = new InvocationTargetException(e);
		// Have to actually break the contract of Promise to exercise this
		// code path - a Promise shouldn't return true for isDone(), null for
		// getFailure()
		// *and* have getValue() return an InvocationTargetException all at
		// once.
		when(p.getValue()).thenThrow(ite);
		when(p.isDone()).thenReturn(true);
		when(p.getFailure()).thenReturn(null);

		softly.assertThatCode(() -> assertThat(p).as("foo")
			.hasValueThat())
			.isInstanceOf(AssertionError.class)
			.hasMessageContaining(eMsg);
	}

	@Test
	void testUnresolvedPromise() throws Exception {
		final String value = new String("value");
		final Deferred<String> d = new Deferred<>();
		final Promise<String> p = d.getPromise();

		softly.assertThat(p)
			.isNotDone();
		softly.assertThat(p)
			.hasNotFailed();
		softly.assertThat(p)
			.doesNotResolveWithin(WAIT_TIME);

		softly.assertThatCode(() -> assertThat(p).as("foo")
			.isDone())
			.isInstanceOf(AssertionError.class)
			.hasMessageContaining("foo");
		softly.assertThatCode(() -> assertThat(p).as("foo")
			.isSuccessful())
			.isInstanceOf(AssertionError.class)
			.hasMessageContaining("foo");
		softly.assertThatCode(() -> assertThat(p).as("foo")
			.hasFailed())
			.isInstanceOf(AssertionError.class)
			.hasMessageContaining("foo");
		softly.assertThatCode(() -> assertThat(p).as("foo")
			.hasFailedWithThrowableThat())
			.isInstanceOf(AssertionError.class)
			.hasMessageContaining("foo");
		softly.assertThatCode(() -> assertThat(p).as("foo")
			.hasValueThat())
			.isInstanceOf(AssertionError.class)
			.hasMessageContaining("foo");
		softly.assertThatCode(() -> assertThat(p).as("foo")
			.hasValueThat(InstanceOfAssertFactories.STRING))
			.isInstanceOf(AssertionError.class)
			.hasMessageContaining("foo");
		softly.assertThatCode(() -> assertThat(p).as("foo")
			.hasValue("value"))
			.isInstanceOf(AssertionError.class)
			.hasMessageContaining("foo");
		softly.assertThatCode(() -> assertThat(p).as("foo")
			.hasSameValue("value"))
			.isInstanceOf(AssertionError.class)
			.hasMessageContaining("foo");
		softly.assertThatCode(() -> assertThat(p).as("foo")
			.hasValueMatching("value"::equals))
			.isInstanceOf(AssertionError.class)
			.hasMessageContaining("foo");
		softly.assertThatCode(() -> assertThat(p).as("foo")
			.resolvesWithin(WAIT_TIME))
			.isInstanceOf(AssertionError.class)
			.hasMessageContaining("foo");

		d.resolve(value);

		softly.assertThat(p)
			.resolvesWithin(WAIT_TIME);
		softly.assertThat(p)
			.isDone();
		softly.assertThat(p)
			.isSuccessful();
		softly.assertThat(p)
			.hasValueThat()
			.isSameAs(value);
		softly.assertThat(p)
			.hasSameValue(value);
		softly.assertThat(p)
			.hasNotFailed();
	}

	@Test
	void testResolvedPromise() throws Exception {
		final String value = new String("value");
		final Promise<String> p = Promises.resolved(value);

		softly.assertThat(p)
			.isDone();
		softly.assertThat(p)
			.isSuccessful();
		softly.assertThat(p)
			.hasNotFailed();
		softly.assertThat(p)
			.hasValueThat()
			.isSameAs(value);
		softly.assertThat(p)
			.hasValueThat(InstanceOfAssertFactories.CHAR_SEQUENCE)
			.contains("alu")
			.startsWith("va")
			.endsWith("ue");
		softly.assertThat(p)
			.hasValueThat(InstanceOfAssertFactories.STRING)
			.isEqualTo("%sl%s", "va", "ue");
		softly.assertThat(p)
			.hasValue(new String("value"));
		softly.assertThat(p)
			.hasSameValue(value);
		softly.assertThat(p)
			.hasValueMatching(v -> v == value, "same");
		softly.assertThat(p)
			.hasValueMatching(value::equals);
		softly.assertThat(p)
			.resolvesWithin(WAIT_TIME);

		softly.assertThatCode(() -> assertThat(p).as("foo")
			.isNotDone())
			.isInstanceOf(AssertionError.class)
			.hasMessageContaining("foo");
		softly.assertThatCode(() -> assertThat(p).as("foo")
			.hasFailed())
			.isInstanceOf(AssertionError.class)
			.hasMessageContaining("foo");
		softly.assertThatCode(() -> assertThat(p).as("foo")
			.hasFailedWithThrowableThat())
			.isInstanceOf(AssertionError.class)
			.hasMessageContaining("foo");
		softly.assertThatCode(() -> assertThat(p).as("foo")
			.hasValueThat()
			.isNull())
			.isInstanceOf(AssertionError.class)
			.hasMessageContaining("foo");
		softly.assertThatCode(() -> assertThat(p).as("foo")
			.hasValue("failed"))
			.isInstanceOf(AssertionError.class)
			.hasMessageContaining("foo");
		softly.assertThatCode(() -> assertThat(p).as("foo")
			.hasSameValue(new String("value")))
			.isInstanceOf(AssertionError.class)
			.hasMessageContaining("foo");
		softly.assertThatCode(() -> assertThat(p).as("foo")
			.hasValueMatching("failed"::equals))
			.isInstanceOf(AssertionError.class)
			.hasMessageContaining("foo");
		softly.assertThatCode(() -> assertThat(p).as("foo")
			.doesNotResolveWithin(WAIT_TIME))
			.isInstanceOf(AssertionError.class)
			.hasMessageContaining("foo");
		softly.assertThatCode(() -> assertThat(p).as("foo")
			.hasValueThat(InstanceOfAssertFactories.CHAR_SEQUENCE)
			.contains("alxu")
			.startsWith("va")
			.endsWith("ue"))
			.isInstanceOf(AssertionError.class)
			.hasMessageContaining("foo");
	}

	public static class TestException extends Exception {
		private static final long serialVersionUID = 1L;

		public TestException(String message) {
			super(message);
		}
	}

	@Test
	public void testFailedPromise(PromiseSoftAssertions softly) throws Exception {
		final Throwable cause = new NullPointerException("cause");
		final Throwable failed = new TestException("failed").initCause(cause);
		final Promise<String> p = Promises.failed(failed);

		softly.assertThat(p)
			.isDone();
		softly.assertThat(p)
			.hasFailed();
		softly.assertThat(p)
			.hasFailedWithThrowableThat()
			.isSameAs(failed);
		softly.assertThat(p)
			.resolvesWithin(WAIT_TIME);

		softly.assertThatCode(() -> assertThat(p).as("foo")
			.isNotDone())
			.isInstanceOf(AssertionError.class)
			.hasMessageContaining("foo");
		softly.assertThatCode(() -> assertThat(p).as("foo")
			.hasNotFailed())
			.isInstanceOf(AssertionError.class)
			.hasMessageContaining("foo");
		softly.assertThatCode(() -> assertThat(p).as("foo")
			.isSuccessful())
			.isInstanceOf(AssertionError.class)
			.hasMessageContaining("foo");
		softly.assertThatCode(() -> assertThat(p).as("foo")
			.hasValueThat())
			.isInstanceOf(AssertionError.class)
			.hasMessageContaining("foo");
		softly.assertThatCode(() -> assertThat(p).as("foo")
			.hasValueThat(InstanceOfAssertFactories.STRING))
			.isInstanceOf(AssertionError.class)
			.hasMessageContaining("foo");
		softly.assertThatCode(() -> assertThat(p).as("foo")
			.hasValue("failed"))
			.isInstanceOf(AssertionError.class)
			.hasMessageContaining("foo");
		softly.assertThatCode(() -> assertThat(p).as("foo")
			.hasSameValue(new String("value")))
			.isInstanceOf(AssertionError.class)
			.hasMessageContaining("foo");
		softly.assertThatCode(() -> assertThat(p).as("foo")
			.hasValueMatching("failed"::equals))
			.isInstanceOf(AssertionError.class)
			.hasMessageContaining("foo");
		softly.assertThatCode(() -> assertThat(p).as("foo")
			.doesNotResolveWithin(WAIT_TIME))
			.isInstanceOf(AssertionError.class)
			.hasMessageContaining("foo");
	}

	@Test
	public void testInstanceFactories(PromiseSoftAssertions softly) throws Exception {
		final String value = new String("value");
		final Promise<String> p = Promises.resolved(value);

		PromiseAssert<String> stringPromiseAssert = softly.assertThatObject(p)
			.asInstanceOf(PromiseAssert.promise(String.class));
		stringPromiseAssert.isDone()
			.hasSameValue(value);
		stringPromiseAssert.hasValueThat(InstanceOfAssertFactories.STRING)
			.isEqualTo("%sl%s", "va", "ue");
		stringPromiseAssert.hasValueThat(InstanceOfAssertFactories.CHAR_SEQUENCE)
			.contains("alu")
			.startsWith("va")
			.endsWith("ue");

		PromiseAssert<CharSequence> charseqPromiseAssert = softly.assertThatObject(p)
			.asInstanceOf(PromiseAssert.promise(CharSequence.class));
		charseqPromiseAssert.isDone()
			.hasSameValue(value);
		charseqPromiseAssert.hasValueThat(InstanceOfAssertFactories.CHAR_SEQUENCE)
			.contains("alu")
			.startsWith("va")
			.endsWith("ue");

		PromiseAssert<Object> objectPromiseAssert = softly.assertThatObject(p)
			.asInstanceOf(PromiseAssert.PROMISE);
		objectPromiseAssert.isDone()
			.hasSameValue(value);

		softly.assertThatCode(() -> PromiseAssert.promise(null))
			.isInstanceOf(NullPointerException.class);
	}

}
