/*
 * Copyright (c) OSGi Alliance (2019, 2020). All Rights Reserved.
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

package org.osgi.test.assertj.bundleevent;

import static org.osgi.framework.BundleEvent.INSTALLED;
import static org.osgi.framework.BundleEvent.LAZY_ACTIVATION;
import static org.osgi.framework.BundleEvent.RESOLVED;
import static org.osgi.framework.BundleEvent.STARTED;
import static org.osgi.framework.BundleEvent.STARTING;
import static org.osgi.framework.BundleEvent.STOPPED;
import static org.osgi.framework.BundleEvent.STOPPING;
import static org.osgi.framework.BundleEvent.UNINSTALLED;
import static org.osgi.framework.BundleEvent.UNRESOLVED;
import static org.osgi.framework.BundleEvent.UPDATED;
import static org.osgi.test.assertj.bundle.BundleAssert.BUNDLE;

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.assertj.core.api.AbstractAssert;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;
import org.osgi.test.assertj.bundle.AbstractBundleAssert;

public abstract class AbstractBundleEventAssert<SELF extends AbstractBundleEventAssert<SELF, ACTUAL>, ACTUAL extends BundleEvent>
	extends AbstractAssert<SELF, ACTUAL> {

	protected AbstractBundleEventAssert(ACTUAL actual, Class<SELF> selfType) {
		super(actual, selfType);
	}

	public SELF hasBundle(Bundle expected) {
		isNotNull();
		if (!Objects.equals(actual.getBundle(), expected)) {
			failWithActualExpectedAndMessage(actual.getBundle(), expected,
				"%nExpecting%n <%s>%nto have bundle source:%n <%s>%n but was:%n<%s>", actual, expected,
				actual.getBundle());
		}
		return myself;
	}

	public AbstractBundleAssert<?, ? extends Bundle> hasBundleThat() {
		return isNotNull().extracting(BundleEvent::getBundle, BUNDLE)
			.as(actual + ".bundle");
	}

	public SELF hasOrigin(Bundle expected) {
		isNotNull();
		if (!Objects.equals(actual.getOrigin(), expected)) {
			failWithActualExpectedAndMessage(actual.getOrigin(), expected,
				"%nExpecting%n <%s>%nto have originating bundle:%n <%s>%n but was:%n<%s>", actual, expected,
				actual.getOrigin());
		}
		return myself;
	}

	public AbstractBundleAssert<?, ? extends Bundle> hasOriginThat() {
		return isNotNull().extracting(BundleEvent::getOrigin, BUNDLE)
			.as(actual + ".origin");
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

	private static final int[]	TYPES			= {
		INSTALLED, STARTED, STOPPED, UPDATED, UNINSTALLED, RESOLVED, UNRESOLVED, STARTING, STOPPING, LAZY_ACTIVATION
	};

	private static final int	KNOWN_MASK		= INSTALLED | LAZY_ACTIVATION | RESOLVED | STARTED | STARTING | STOPPED
		| STOPPING | UNINSTALLED | UNRESOLVED | UPDATED;
	private static final int	UNKNOWN_MASK	= ~KNOWN_MASK;

	private static String typeMaskToString(int type) {
		Stream<String> bits = IntStream.of(
			TYPES)
			.filter(x -> (x
				& type) != 0)
			.mapToObj(AbstractBundleEventAssert::typeToString);

		if ((type & UNKNOWN_MASK) != 0) {
			bits = Stream.concat(bits, Stream.of("UNKNOWN"));
		}
		return type + ":" + bits.collect(Collectors.joining(" | "));
	}

	private static String typeToString(int type) {
		switch (type) {
			case UNINSTALLED :
				return "UNINSTALLED";
			case UNRESOLVED :
				return "UNRESOLVED";
			case UPDATED :
				return "UPDATED";
			case INSTALLED :
				return "INSTALLED";
			case RESOLVED :
				return "RESOLVED";
			case STARTING :
				return "STARTING";
			case STARTED :
				return "STARTED";
			case STOPPING :
				return "STOPPING";
			case STOPPED :
				return "STOPPED";
			case LAZY_ACTIVATION :
				return "LAZY_ACTIVATION";
			default :
				return "UNKNOWN";
		}
	}
}
