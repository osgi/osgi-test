package org.osgi.test.assertj.promise;

import static org.osgi.test.assertj.promise.PromiseAssert.assertThat;

import java.time.Duration;
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

	ExecutorService				callbackExecutor;
	ScheduledExecutorService	scheduledExecutor;
	PromiseFactory				factory;

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

		softly.assertThatCode(() -> assertThat(p).isDone())
			.isInstanceOf(AssertionError.class);
		softly.assertThatCode(() -> assertThat(p).isSuccessful())
			.isInstanceOf(AssertionError.class);
		softly.assertThatCode(() -> assertThat(p).hasFailed())
			.isInstanceOf(AssertionError.class);
		softly.assertThatCode(() -> assertThat(p).hasFailedWithThrowableThat())
			.isInstanceOf(AssertionError.class);
		softly.assertThatCode(() -> assertThat(p).hasValueThat())
			.isInstanceOf(AssertionError.class);
		softly.assertThatCode(() -> assertThat(p).hasValueThat(InstanceOfAssertFactories.STRING))
			.isInstanceOf(AssertionError.class);
		softly.assertThatCode(() -> assertThat(p).hasValue("value"))
			.isInstanceOf(AssertionError.class);
		softly.assertThatCode(() -> assertThat(p).hasSameValue("value"))
			.isInstanceOf(AssertionError.class);
		softly.assertThatCode(() -> assertThat(p).hasValueMatching("value"::equals))
			.isInstanceOf(AssertionError.class);
		softly.assertThatCode(() -> assertThat(p).resolvesWithin(WAIT_TIME))
			.isInstanceOf(AssertionError.class);

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

		softly.assertThatCode(() -> assertThat(p).isNotDone())
			.isInstanceOf(AssertionError.class);
		softly.assertThatCode(() -> assertThat(p).hasFailed())
			.isInstanceOf(AssertionError.class);
		softly.assertThatCode(() -> assertThat(p).hasFailedWithThrowableThat())
			.isInstanceOf(AssertionError.class);
		softly.assertThatCode(() -> assertThat(p).hasValueThat()
			.isNull())
			.isInstanceOf(AssertionError.class);
		softly.assertThatCode(() -> assertThat(p).hasValue("failed"))
			.isInstanceOf(AssertionError.class);
		softly.assertThatCode(() -> assertThat(p).hasSameValue(new String("value")))
			.isInstanceOf(AssertionError.class);
		softly.assertThatCode(() -> assertThat(p).hasValueMatching("failed"::equals))
			.isInstanceOf(AssertionError.class);
		softly.assertThatCode(() -> assertThat(p).doesNotResolveWithin(WAIT_TIME))
			.isInstanceOf(AssertionError.class);

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

		softly.assertThatCode(() -> assertThat(p).isNotDone())
			.isInstanceOf(AssertionError.class);
		softly.assertThatCode(() -> assertThat(p).hasNotFailed())
			.isInstanceOf(AssertionError.class);
		softly.assertThatCode(() -> assertThat(p).isSuccessful())
			.isInstanceOf(AssertionError.class);
		softly.assertThatCode(() -> assertThat(p).hasValueThat())
			.isInstanceOf(AssertionError.class);
		softly.assertThatCode(() -> assertThat(p).hasValueThat(InstanceOfAssertFactories.STRING))
			.isInstanceOf(AssertionError.class);
		softly.assertThatCode(() -> assertThat(p).hasValue("failed"))
			.isInstanceOf(AssertionError.class);
		softly.assertThatCode(() -> assertThat(p).hasSameValue(new String("value")))
			.isInstanceOf(AssertionError.class);
		softly.assertThatCode(() -> assertThat(p).hasValueMatching("failed"::equals))
			.isInstanceOf(AssertionError.class);
		softly.assertThatCode(() -> assertThat(p).doesNotResolveWithin(WAIT_TIME))
			.isInstanceOf(AssertionError.class);

		softly.assertAll();
	}

}
