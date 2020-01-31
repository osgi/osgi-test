package org.osgi.test.assertj.promise;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.osgi.test.assertj.promise.PromiseAssert.assertThat;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.osgi.util.promise.Deferred;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;

public class PromiseAssertTest {
	public static final Duration	WAIT_TIME	= Duration.ofSeconds(2L);

	ExecutorService					callbackExecutor;
	ScheduledExecutorService		scheduledExecutor;
	PromiseFactory					factory;

	@BeforeEach
	public void setUp() {
		callbackExecutor = Executors.newFixedThreadPool(2);
		scheduledExecutor = Executors.newScheduledThreadPool(2);
		factory = new PromiseFactory(callbackExecutor, scheduledExecutor);
	}

	@AfterEach
	public void tearDown() {
		callbackExecutor.shutdown();
		scheduledExecutor.shutdown();
	}

	@Test
	public void testUnresolvedPromise() throws Exception {
		final String value = new String("value");
		final Deferred<String> d = factory.deferred();
		final Promise<String> p = d.getPromise();

		PromiseSoftAssertions softly = new PromiseSoftAssertions();
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

		softly.assertAll();
	}

	@Test
	public void testResolvedPromise() throws Exception {
		final String value = new String("value");
		final Promise<String> p = factory.resolved(value);

		PromiseSoftAssertions softly = new PromiseSoftAssertions();
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

		softly.assertAll();
	}

	public static class TestException extends Exception {
		private static final long serialVersionUID = 1L;

		public TestException(String message) {
			super(message);
		}
	}

	@Test
	public void testFailedPromise() throws Exception {
		final Throwable cause = new NullPointerException("cause");
		final Throwable failed = new TestException("failed").initCause(cause);
		final Promise<String> p = factory.failed(failed);

		PromiseSoftAssertions softly = new PromiseSoftAssertions();
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

		softly.assertAll();
	}

	@Test
	public void testInstanceFactories() throws Exception {
		final String value = new String("value");
		final Promise<String> p = factory.resolved(value);
		final Optional<Promise<String>> optional = Optional.of(p);

		PromiseAssert<String> stringPromiseAssert = assertThat(optional).get(PromiseAssert.promise(String.class));
		stringPromiseAssert.isDone()
			.hasSameValue(value);

		PromiseAssert<Object> objectPromiseAssert = assertThat(optional).get(PromiseAssert.PROMISE);
		stringPromiseAssert.isDone()
			.hasSameValue(value);

		assertThatNullPointerException().isThrownBy(() -> {
			PromiseAssert.promise(null);
		});
	}

}
