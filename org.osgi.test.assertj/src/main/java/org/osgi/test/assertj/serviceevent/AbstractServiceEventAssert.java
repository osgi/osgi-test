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

import static org.osgi.framework.ServiceEvent.MODIFIED;
import static org.osgi.framework.ServiceEvent.MODIFIED_ENDMATCH;
import static org.osgi.framework.ServiceEvent.REGISTERED;
import static org.osgi.framework.ServiceEvent.UNREGISTERING;

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.assertj.core.api.AbstractAssert;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceReference;

public abstract class AbstractServiceEventAssert<SELF extends AbstractServiceEventAssert<SELF, ACTUAL>, ACTUAL extends ServiceEvent>
	extends AbstractAssert<SELF, ACTUAL> {

	protected AbstractServiceEventAssert(ACTUAL actual, Class<SELF> selfType) {
		super(actual, selfType);
	}

	public SELF hasServiceReference(ServiceReference<?> expected) {
		isNotNull();
		if (!Objects.equals(actual.getServiceReference(), expected)) {
			failWithActualExpectedAndMessage(actual.getServiceReference(),
				expected,
				"%nExpecting%n <%s>%nto have service reference:%n <%s>%n but was:%n<%s>", actual, expected,
				actual.getServiceReference());
		}
		return myself;
	}

	public SELF isOfType(int expected) {
		isNotNull();
		if ((expected & (expected - 1)) != 0) {
			throw new IllegalArgumentException(
				"Multiple bits set in expected (" + expected + ") - do you mean to use isOfTypeMaskedBy()?");
		}
		final String expectedString = typeToString(expected);
		if ((actual.getType() & expected) == 0) {
			failWithMessage("%nExpecting%n <%s>%nto be of type:%n <%d:%s>%n but was of type:%n <%s>", actual, expected,
				expectedString, typeMaskToString(actual.getType()));
		}
		return myself;
	}

	public SELF isNotOfType(int expected) {
		isNotNull();
		final String expectedType = typeToString(expected);
		if ((actual.getType() & expected) != 0) {
			failWithMessage("%nExpecting%n <%s>%nnot to be of type:%n <%d:%s>%nbut it was", actual,
				expected,
				expectedType);
		}
		return myself;
	}

	public SELF isOfTypeMaskedBy(int mask) {
		isNotNull();
		if (mask <= 0 || mask > KNOWN_MASK) {
			throw new IllegalArgumentException("Mask testing for an illegal type: " + mask);
		}
		if ((actual.getType() & mask) == 0) {
			final String types = typeMaskToString(mask);
			failWithMessage("%nExpecting%n <%s>%nto be of one of types:%n [%s]%n but was of type:%n <%s>",
				actual,
				types, typeMaskToString(actual.getType()));
		}
		return myself;
	}

	public SELF isNotOfTypeMaskedBy(int mask) {
		isNotNull();
		if (mask <= 0 || mask >= KNOWN_MASK) {
			throw new IllegalArgumentException("Mask testing for an illegal type: " + mask);
		}
		if ((actual.getType() & mask) != 0) {
			final String types = typeMaskToString(mask);
			failWithMessage("%nExpecting%n <%s>%nto not be of one of types:%n [%s]%n but was of type:%n <%s>",
				actual,
				types, typeMaskToString(actual.getType()));
		}
		return myself;
	}

	// Types in the order that they are listed in ServiceEvent
	private static final int[]	TYPES			= IntStream.of(REGISTERED, MODIFIED, UNREGISTERING, MODIFIED_ENDMATCH)
		.sorted()
		.toArray();

	private static final int	KNOWN_MASK		= IntStream.of(TYPES)
		.reduce((x, y) -> x | y)
		.getAsInt();
	private static final int	UNKNOWN_MASK	= ~KNOWN_MASK;

	private static String typeMaskToString(int type) {
		Stream<String> bits = IntStream.of(
			TYPES)
			.filter(x -> (x
				& type) != 0)
			.mapToObj(AbstractServiceEventAssert::typeToString);

		if ((type & UNKNOWN_MASK) != 0) {
			bits = Stream.concat(bits, Stream.of("UNKNOWN"));
		}
		return type + ":" + bits.collect(Collectors.joining(" | "));
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
