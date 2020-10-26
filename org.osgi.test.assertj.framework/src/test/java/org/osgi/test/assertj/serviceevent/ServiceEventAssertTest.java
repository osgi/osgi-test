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

package org.osgi.test.assertj.serviceevent;

import static org.mockito.Mockito.mock;
import static org.osgi.framework.ServiceEvent.MODIFIED;
import static org.osgi.framework.ServiceEvent.MODIFIED_ENDMATCH;
import static org.osgi.framework.ServiceEvent.REGISTERED;
import static org.osgi.framework.ServiceEvent.UNREGISTERING;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceReference;
import org.osgi.test.assertj.testutil.AbstractAssertTest;

class ServiceEventAssertTest extends AbstractAssertTest<ServiceEventAssert, ServiceEvent> {

	ServiceEventAssertTest() {
		super(ServiceEventAssert::assertThat);
	}

	ServiceReference<?>	reference;
	ServiceReference<?>	otherReference;

	@BeforeEach
	void setUp() {
		reference = mock(ServiceReference.class);
		otherReference = mock(ServiceReference.class);
		setActual(new ServiceEvent(0, reference));
	}

	@Test
	void hasServiceReference() {
		assertEqualityAssertion("serviceReference", aut::hasServiceReference, reference, otherReference);
	}

	@ParameterizedTest
	@TypeSource
	public void isOfType(int passingType) {
		final int actualType = passingType | (passingType << 2);
		setActual(new ServiceEvent(actualType, reference));

		int failingType = (passingType > KNOWN_MASK) ? UNREGISTERING : passingType << 1;
		assertPassing(aut::isOfType, passingType);
		assertFailing(aut::isOfType, failingType)
			.hasMessageMatching("(?si).*expect.*of type.*" + failingType + ":" + typeToString(failingType)
				+ ".*but was of type.*" + actualType + ":" + Pattern.quote(typeMaskToString(actualType)) + ".*");
	}

	@ParameterizedTest
	@TypeSource
	public void isOfType_withMultipleTypes_throwsIAE(int expected) {
		final int mask = expected | (expected << 1);
		softly().assertThatThrownBy(() -> aut.isOfType(mask))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageMatching("(?si).*" + mask + ".*isOfTypeMaskedBy.*");
	}

	@ParameterizedTest
	@TypeSource
	public void isNotOfType(int type) {
		setActual(new ServiceEvent(type, reference));

		int passingType = type == REGISTERED ? UNREGISTERING : REGISTERED;
		AtomicReference<ServiceEventAssert> retval = new AtomicReference<>();
		assertPassing(aut::isNotOfType, passingType);
		assertFailing(aut::isNotOfType, type)
			.hasMessageMatching("(?s).*not.* of type.*" + type + ".*" + typeToString(type) + ".*but it was.*");
	}

	@Test
	public void isOfTypeMaskedBy() {
		setActual(new ServiceEvent(REGISTERED, reference));

		assertPassing(aut::isOfTypeMaskedBy, REGISTERED | UNREGISTERING);
		assertPassing(aut::isOfTypeMaskedBy, REGISTERED);
		assertPassing(aut::isOfTypeMaskedBy, REGISTERED | MODIFIED | UNREGISTERING);
		assertFailing(aut::isOfTypeMaskedBy, MODIFIED_ENDMATCH)
			.hasMessageMatching("(?si).*of one of types.*\\[8:MODIFIED_ENDMATCH\\].*but was of type.*1:REGISTERED.*");
		assertFailing(aut::isOfTypeMaskedBy, UNREGISTERING | MODIFIED | MODIFIED_ENDMATCH).hasMessageMatching(
			"(?si).*of one of types.*\\Q[14:MODIFIED | UNREGISTERING | MODIFIED_ENDMATCH]\\E.*but was of type.*1:REGISTERED.*");

		setActual(new ServiceEvent(MODIFIED_ENDMATCH, reference));

		assertPassing(aut::isOfTypeMaskedBy, MODIFIED_ENDMATCH | REGISTERED);
		assertPassing(aut::isOfTypeMaskedBy, MODIFIED_ENDMATCH);
		assertPassing(aut::isOfTypeMaskedBy, UNREGISTERING | REGISTERED | MODIFIED_ENDMATCH);
		assertFailing(aut::isOfTypeMaskedBy, REGISTERED)
			.hasMessageMatching("(?si).*of one of types.*\\[1:REGISTERED\\].*but was of type.*8:MODIFIED_ENDMATCH.*");
		assertFailing(aut::isOfTypeMaskedBy, REGISTERED | UNREGISTERING | MODIFIED).hasMessageMatching(
			"(?si).*of one of types.*\\Q[7:REGISTERED | MODIFIED | UNREGISTERING]\\E.*but was of type.*8:MODIFIED_ENDMATCH.*");
	}

	@ParameterizedTest
	@ValueSource(ints = {
		-23, 0, 1024, 2048
	})
	public void isOfTypeMask_throwsIAE_forInvalidMask(int mask) {
		setActual(new ServiceEvent(REGISTERED, reference));

		softly().assertThatThrownBy(() -> aut.isOfTypeMaskedBy(mask))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining(Integer.toString(mask));
	}

	@Test
	public void isNotOfTypeMaskedBy() {
		setActual(new ServiceEvent(REGISTERED, reference));

		assertPassing(aut::isNotOfTypeMaskedBy, MODIFIED_ENDMATCH | UNREGISTERING);
		assertPassing(aut::isNotOfTypeMaskedBy, MODIFIED_ENDMATCH);
		assertPassing(aut::isNotOfTypeMaskedBy, MODIFIED_ENDMATCH | MODIFIED | UNREGISTERING);
		assertFailing(aut::isNotOfTypeMaskedBy, REGISTERED)
			.hasMessageMatching("(?si).*not.*of one of types.*\\Q[1:REGISTERED]\\E.*but was of type.*1:REGISTERED.*");
		assertFailing(aut::isNotOfTypeMaskedBy, UNREGISTERING | REGISTERED | MODIFIED).hasMessageMatching(
			"(?si).*not.*of one of types.*\\Q[7:REGISTERED | MODIFIED | UNREGISTERING]\\E.*but was of type.*1:REGISTERED.*");

		setActual(new ServiceEvent(UNREGISTERING, reference));

		assertPassing(aut::isNotOfTypeMaskedBy, REGISTERED);
		assertPassing(aut::isNotOfTypeMaskedBy, MODIFIED_ENDMATCH | MODIFIED | REGISTERED);
		assertFailing(aut::isNotOfTypeMaskedBy, UNREGISTERING).hasMessageMatching(
			"(?si).*not.*of one of types.*\\Q[4:UNREGISTERING]\\E.*but was of type.*4:UNREGISTERING.*");
		assertFailing(aut::isNotOfTypeMaskedBy, UNREGISTERING | MODIFIED | MODIFIED_ENDMATCH).hasMessageMatching(
			"(?si).*not.*of one of types.*\\Q[14:MODIFIED | UNREGISTERING | MODIFIED_ENDMATCH]\\E.*but was of type.*4:UNREGISTERING.*");
		assertFailing(aut::isNotOfTypeMaskedBy, MODIFIED | UNREGISTERING | REGISTERED).hasMessageMatching(
			"(?si).*not.*of one of types.*\\Q[7:REGISTERED | MODIFIED | UNREGISTERING]\\E.*but was of type.*4:UNREGISTERING.*");
	}

	@ParameterizedTest
	@ValueSource(ints = {
		-23, 0, 1024, 2048
	})
	public void isNotOfTypeMaskedBy_throwsIAE_forInvalidMask(int mask) {
		setActual(new ServiceEvent(UNREGISTERING, reference));

		softly().assertThatThrownBy(() -> aut.isNotOfTypeMaskedBy(
			mask))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining(Integer.toString(mask));
	}

	public final static int UNKNOWN = 0x00000400;

	@ValueSource(ints = {
		REGISTERED, MODIFIED, UNREGISTERING, MODIFIED_ENDMATCH, UNKNOWN
	})
	@Retention(RetentionPolicy.RUNTIME)
	static @interface TypeSource {}

	// Types in the order that they are listed in ServiceEvent
	private static final int[]	TYPES			= IntStream.of(REGISTERED, MODIFIED, UNREGISTERING,
		MODIFIED_ENDMATCH)
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
			.mapToObj(ServiceEventAssertTest::typeToString);

		if ((type & UNKNOWN_MASK) != 0) {
			bits = Stream.concat(bits, Stream.of("UNKNOWN"));
		}
		return bits.collect(Collectors.joining(" | "));
	}

	private static String typeToString(int type) {
		switch (type) {
			case REGISTERED :
				return "REGISTERED";
			case MODIFIED :
				return "MODIFIED";
			case UNREGISTERING :
				return "UNREGISTERING";
			case MODIFIED_ENDMATCH :
				return "MODIFIED_ENDMATCH";
			default :
				return "UNKNOWN";
		}
	}

}
