/*
 * Copyright (c) OSGi Alliance (2020). All Rights Reserved.
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

package org.osgi.test.assertj.frameworkevent;

import static org.mockito.Mockito.mock;
import static org.osgi.framework.FrameworkEvent.ERROR;
import static org.osgi.framework.FrameworkEvent.INFO;
import static org.osgi.framework.FrameworkEvent.PACKAGES_REFRESHED;
import static org.osgi.framework.FrameworkEvent.STARTED;
import static org.osgi.framework.FrameworkEvent.STARTLEVEL_CHANGED;
import static org.osgi.framework.FrameworkEvent.STOPPED;
import static org.osgi.framework.FrameworkEvent.STOPPED_BOOTCLASSPATH_MODIFIED;
import static org.osgi.framework.FrameworkEvent.STOPPED_UPDATE;
import static org.osgi.framework.FrameworkEvent.WAIT_TIMEDOUT;
import static org.osgi.framework.FrameworkEvent.WARNING;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkEvent;
import org.osgi.test.assertj.testutil.AbstractAssertTest;

@ExtendWith(SoftAssertionsExtension.class)
class FrameworkEventAssertTest extends AbstractAssertTest<FrameworkEventAssert, FrameworkEvent> {

	FrameworkEventAssertTest() {
		super(FrameworkEventAssert::assertThat);
	}

	Bundle		bundle;
	Bundle		otherBundle;
	Throwable	throwable;
	Throwable	otherThrowable;

	@BeforeEach
	void setUp() {
		bundle = mock(Bundle.class);
		otherBundle = mock(Bundle.class);
		throwable = new Exception();
		otherThrowable = new RuntimeException();
		setActual(STARTED);
	}

	void setActual(int type) {
		setActual(new FrameworkEvent(type, bundle, throwable));
	}

	@Test
	void hasBundle(SoftAssertions softly) {
		this.softly = softly;

		assertEqualityAssertion("bundle", aut::hasBundle, bundle, otherBundle);
	}

	@Test
	void hasThrowable(SoftAssertions softly) {
		this.softly = softly;

		assertEqualityAssertion("throwable", aut::hasThrowable, throwable, otherThrowable);
	}

	@ParameterizedTest
	@TypeSource
	public void isOfType(int passingType, SoftAssertions softly) {
		this.softly = softly;
		final int actualType = passingType | (passingType << 2);
		setActual(actualType);

		int failingType = (passingType > KNOWN_MASK) ? ERROR : passingType << 1;
		assertPassing(aut::isOfType, passingType);
		assertFailing(aut::isOfType, failingType)
			.hasMessageMatching("(?si).*expect.*of type.*" + failingType + ":" + typeToString(failingType)
				+ ".*but was of type.*" + actualType + ":" + Pattern.quote(typeMaskToString(actualType)) + ".*");
	}

	@ParameterizedTest
	@TypeSource
	public void isOfType_withMultipleTypes_throwsIAE(int expected, SoftAssertions softly) {
		final int mask = expected | (expected << 1);
		softly.assertThatThrownBy(() -> aut.isOfType(mask))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageMatching("(?si).*" + mask + ".*isOfTypeMaskedBy.*");
	}

	@ParameterizedTest
	@TypeSource
	public void isNotOfType(int type, SoftAssertions softly) {
		this.softly = softly;
		setActual(type);

		int passingType = type == STARTED ? STOPPED : STARTED;
		AtomicReference<FrameworkEventAssert> retval = new AtomicReference<>();
		assertPassing(aut::isNotOfType, passingType);
		assertFailing(aut::isNotOfType, type)
			.hasMessageMatching("(?s).*not.* of type.*" + type + ".*" + typeToString(type) + ".*but it was.*");
	}

	@Test
	 public void isOfTypeMaskedBy(SoftAssertions softly) {
	 this.softly = softly;
		setActual(STARTED);

		assertPassing(aut::isOfTypeMaskedBy, STARTED | WARNING);
		assertPassing(aut::isOfTypeMaskedBy, STARTED);
		assertPassing(aut::isOfTypeMaskedBy, STARTED | ERROR |
			WARNING);
		assertFailing(aut::isOfTypeMaskedBy, INFO)
			.hasMessageMatching("(?si).*of one of types.*\\[32:INFO\\].*but was of type.*1:STARTED.*");
		assertFailing(aut::isOfTypeMaskedBy, WARNING | ERROR | INFO).hasMessageMatching(
			"(?si).*of one of types.*\\Q[50:ERROR | WARNING | INFO]\\E.*but was of type.*1:STARTED.*");

		setActual(INFO);

		assertPassing(aut::isOfTypeMaskedBy, INFO | STARTED);
		assertPassing(aut::isOfTypeMaskedBy, INFO);
		assertPassing(aut::isOfTypeMaskedBy, WARNING | STARTED | INFO);
		assertFailing(aut::isOfTypeMaskedBy, STARTED)
			.hasMessageMatching("(?si).*of one of types.*\\[1:STARTED\\].*but was of type.*32:INFO.*");
		assertFailing(aut::isOfTypeMaskedBy, STARTED | WARNING | ERROR).hasMessageMatching(
			"(?si).*of one of types.*\\Q[19:STARTED | ERROR | WARNING]\\E.*but was of type.*32:INFO.*");
	 }

	@ParameterizedTest
	@ValueSource(ints = {
		-23, 0, 1024, 2048
	})
	public void isOfTypeMask_throwsIAE_forInvalidMask(int mask, SoftAssertions softly) {
		setActual(STARTED);

		softly.assertThatThrownBy(() -> aut.isOfTypeMaskedBy(mask))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining(Integer.toString(mask));
	}

	@Test
	public void isNotOfTypeMaskedBy(SoftAssertions softly) {
		this.softly = softly;
		setActual(STARTED);

		assertPassing(aut::isNotOfTypeMaskedBy, INFO | WARNING);
		assertPassing(aut::isNotOfTypeMaskedBy, INFO);
		assertPassing(aut::isNotOfTypeMaskedBy, INFO | ERROR | WARNING);
		assertFailing(aut::isNotOfTypeMaskedBy, STARTED)
			.hasMessageMatching("(?si).*not.*of one of types.*\\Q[1:STARTED]\\E.*but was of type.*1:STARTED.*");
		assertFailing(aut::isNotOfTypeMaskedBy, WARNING | STARTED | ERROR).hasMessageMatching(
			"(?si).*not.*of one of types.*\\Q[19:STARTED | ERROR | WARNING]\\E.*but was of type.*1:STARTED.*");

		setActual(WARNING);

		assertPassing(aut::isNotOfTypeMaskedBy, STARTED);
		assertPassing(aut::isNotOfTypeMaskedBy, INFO | ERROR | STARTED);
		assertFailing(aut::isNotOfTypeMaskedBy, WARNING)
			.hasMessageMatching("(?si).*not.*of one of types.*\\Q[16:WARNING]\\E.*but was of type.*16:WARNING.*");
		assertFailing(aut::isNotOfTypeMaskedBy, WARNING | ERROR | INFO).hasMessageMatching(
			"(?si).*not.*of one of types.*\\Q[50:ERROR | WARNING | INFO]\\E.*but was of type.*16:WARNING.*");
		assertFailing(aut::isNotOfTypeMaskedBy, ERROR | WARNING | STARTED).hasMessageMatching(
			"(?si).*not.*of one of types.*\\Q[19:STARTED | ERROR | WARNING]\\E.*but was of type.*16:WARNING.*");
	}

	@ParameterizedTest
	@ValueSource(ints = {
		-23, 0, 1024, 2048
	})
	public void isNotOfTypeMaskedBy_throwsIAE_forInvalidMask(int mask, SoftAssertions softly) {
		setActual(STOPPED);

		softly.assertThatThrownBy(() -> aut.isNotOfTypeMaskedBy(mask))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining(Integer.toString(mask));
	}

	public final static int UNKNOWN = 0x00000400;

	@ValueSource(ints = {
		STARTED, ERROR, WARNING, INFO, PACKAGES_REFRESHED, STARTLEVEL_CHANGED, STOPPED,
		STOPPED_BOOTCLASSPATH_MODIFIED,
		STOPPED_UPDATE, WAIT_TIMEDOUT, UNKNOWN
	})
	@Retention(RetentionPolicy.RUNTIME)
	static @interface TypeSource {}

	// Types in the order that they are listed in FrameworkEvent
	private static final int[]	TYPES			= IntStream.of(STARTED, ERROR, WARNING, INFO, PACKAGES_REFRESHED,
		STARTLEVEL_CHANGED, STOPPED,
		STOPPED_BOOTCLASSPATH_MODIFIED,
		STOPPED_UPDATE,
		WAIT_TIMEDOUT)
		.sorted()
		.toArray();

	private static final int	KNOWN_MASK		= IntStream.of(
		TYPES)
		.reduce((x, y) -> x | y)
		.getAsInt();
	private static final int	UNKNOWN_MASK	= ~KNOWN_MASK;

	private static String typeMaskToString(int type) {
		Stream<String> bits = IntStream.of(TYPES)
			.filter(x -> (x
				& type) != 0)
			.mapToObj(FrameworkEventAssertTest::typeToString);

		if ((type & UNKNOWN_MASK) != 0) {
			bits = Stream.concat(bits, Stream.of("UNKNOWN"));
		}
		return bits.collect(Collectors.joining(" | "));
	}

	private static String typeToString(int type) {
		switch (type) {
			case STARTED :
				return "STARTED";
			case ERROR :
				return "ERROR";
			case WARNING :
				return "WARNING";
			case INFO :
				return "INFO";
			case PACKAGES_REFRESHED :
				return "PACKAGES_REFRESHED";
			case STARTLEVEL_CHANGED :
				return "STARTLEVEL_CHANGED";
			case STOPPED :
				return "STOPPED";
			case STOPPED_BOOTCLASSPATH_MODIFIED :
				return "STOPPED_BOOTCLASSPATH_MODIFIED";
			case STOPPED_UPDATE :
				return "STOPPED_UPDATE";
			case WAIT_TIMEDOUT :
				return "WAIT_TIMEDOUT";
			default :
				return "UNKNOWN";
		}
	}

}
