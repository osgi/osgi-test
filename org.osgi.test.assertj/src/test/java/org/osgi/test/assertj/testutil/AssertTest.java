package org.osgi.test.assertj.testutil;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.AbstractThrowableAssert;
import org.assertj.core.api.Assert;
import org.assertj.core.api.SoftAssertions;
import org.opentest4j.AssertionFailedError;
import org.osgi.test.common.exceptions.Exceptions;

public interface AssertTest<SELF extends Assert<SELF, ACTUAL>, ACTUAL> {

	/**
	 * Returns the actual object
	 *
	 * @return The actual object that our assertion-under-test is asserting on.
	 */
	ACTUAL actual();

	/**
	 * Returns the Assertion Under Test (AUT).
	 *
	 * @return The assertion under test
	 */
	SELF aut();

	/**
	 * @return The soft assertions object used for the current tests.
	 */
	SoftAssertions softly();

	default <T> AbstractThrowableAssert<?, ?> assertEqualityAssertion(String field, Function<T, SELF> assertion,
		T actual, T failing) {
		return assertEqualityAssertion(null, field, assertion, actual, failing);
	}

	/**
	 * Routine containing boilerplate code for checking correct assertion
	 * behavior. This function calls {@link #assertPassing assertPassing()} and
	 * {@link #assertFailing assertFailing()}. It also asserts that the failure
	 * message matches the general pattern "expected <%s>, but was <%s>". It
	 * returns the {@link AbstractThrowableAssert} instance so that you can
	 * perform further assertions on the exception.
	 *
	 * @param <T> the type of the arguments accepted by the assertion method
	 *            under test.
	 * @param softly the {@code SoftAssertions} object passed in to the original
	 *            test message.
	 * @param assertion reference to the assertion method of the {@link #aut()}
	 * @param actual the value that the method under test will actually return.
	 * @param failing an argument to pass to the assertion method that should
	 *            fail.
	 * @return The {@code AbstractThrowableAssert} instance used for chaining
	 *         further assertions.
	 */
	default <T> AbstractThrowableAssert<?, ?> assertEqualityAssertion(String msg, String field,
		Function<T, SELF> assertion, T actual, T failing) {
		assertPassing(msg, assertion, actual);
		AbstractThrowableAssert<?, ?> retval = assertFailing(msg, assertion, failing)
			.hasMessageMatching("(?si).*expecting.*" + field + ".*" + failing + ".*but.*was.*" + actual + ".*")
			.isInstanceOf(AssertionFailedError.class);
		try {
			Field f = AbstractAssert.class.getDeclaredField("actual");
			f.setAccessible(true);
			Object a = f.get(retval);
			if (!(a instanceof AssertionFailedError)) {
				return retval;
			}
			AssertionFailedError afe = (AssertionFailedError) a;
			softly().assertThat(afe.getActual()
				.getStringRepresentation())
				.as("actual")
				.isEqualTo(String.valueOf(actual));
			softly().assertThat(afe.getExpected()
				.getStringRepresentation())
				.as("expected")
				.isEqualTo(String.valueOf(failing));
		} catch (Exception e) {
			throw Exceptions.duck(e);
		}
		return retval;
	}

	default <T> void assertPassing(Function<T, SELF> assertion, T passing) {
		assertPassing(null, assertion, passing);
	}

	default <T> void assertPassing(String msg, Function<T, SELF> assertion, T passing) {
		AtomicReference<SELF> retval = new AtomicReference<>();
		softly().assertThatCode(() -> retval.set(assertion.apply(passing)))
			.as(msg == null ? "passing" : msg + ":passing")
			.doesNotThrowAnyException();
		if (softly().wasSuccess()) {
			softly().assertThat(retval.get())
				.as(msg == null ? "chaining" : msg + ":chaining")
				.isSameAs(aut());
		}
	}

	default <T> AbstractThrowableAssert<?, ?> assertFailing(Function<T, SELF> assertion, T failing) {
		return assertFailing(null, assertion, failing);
	}

	default <T> AbstractThrowableAssert<?, ?> assertFailing(String msg, Function<T, SELF> assertion, T failing) {
		return softly().assertThatThrownBy(() -> assertion.apply(failing))
			.as(msg == null ? "failing" : msg + ":failing")
			.isInstanceOf(AssertionError.class)
			.hasMessageContaining(actual().toString());
	}

	default <CHILD extends AbstractAssert<?, ?>> void assertChildAssertion(String msg,
		Supplier<CHILD> childSupplier, Supplier<?> field) {
		CHILD child = childSupplier.get();
		softly().assertThat(child)
			.extracting("actual")
			.as(msg == null ? "child" : msg + ":child")
			.isEqualTo(field.get());
	}
}
