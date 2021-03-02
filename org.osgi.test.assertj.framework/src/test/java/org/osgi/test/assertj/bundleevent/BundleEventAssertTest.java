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

package org.osgi.test.assertj.bundleevent;

import static org.mockito.Mockito.mock;
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
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;
import org.osgi.test.assertj.testutil.AbstractAssertTest;

class BundleEventAssertTest extends AbstractAssertTest<BundleEventAssert, BundleEvent> {

	BundleEventAssertTest() {
		super(BundleEventAssert::assertThat);
	}

	Bundle	bundle;
	Bundle	origin;
	// Used when we need a bundle that is not equal to either of the above two
	Bundle	thirdBundle;

	@BeforeEach
	void setUp() {
		bundle = mock(Bundle.class);
		origin = mock(Bundle.class);
		thirdBundle = mock(Bundle.class);
		setActual(new BundleEvent(0, bundle, origin));
	}

	@Test
	void hasBundle() {
		assertEqualityAssertion("bundle", aut::hasBundle, bundle, thirdBundle);
	}

	@Test
	void hasBundleThat() {
		assertChildAssertion("bundle", aut::hasBundleThat, actual::getBundle);
	}

	@Test
	void hasOrigin() {
		assertEqualityAssertion("origin", aut::hasOrigin, origin, thirdBundle);
	}

	@Test
	void hasOriginThat() {
		assertChildAssertion("bundle", aut::hasOriginThat, actual::getOrigin);
	}

	@ParameterizedTest
	@TypeSource
	public void isOfType(int passingType) {
		final int actualType = passingType | (passingType << 2);
		setActual(new BundleEvent(actualType, bundle, origin));

		int failingType = (passingType > KNOWN_MASK) ? INSTALLED : passingType << 1;
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
		setActual(new BundleEvent(type, bundle, origin));

		int passingType = type == LAZY_ACTIVATION ? RESOLVED : LAZY_ACTIVATION;
		AtomicReference<BundleEventAssert> retval = new AtomicReference<>();
		assertPassing(aut::isNotOfType, passingType);
		assertFailing(aut::isNotOfType, type)
			.hasMessageMatching("(?s).*not.* of type.*" + type + ".*" + typeToString(type) + ".*but it was.*");
	}

	@Test
	public void isOfTypeMaskedBy() {
		setActual(new BundleEvent(LAZY_ACTIVATION, bundle, origin));

		assertPassing(aut::isOfTypeMaskedBy, LAZY_ACTIVATION | INSTALLED);
		assertPassing(aut::isOfTypeMaskedBy, LAZY_ACTIVATION);
		assertPassing(aut::isOfTypeMaskedBy, LAZY_ACTIVATION | STOPPING | RESOLVED);
		assertFailing(aut::isOfTypeMaskedBy, INSTALLED)
			.hasMessageMatching("(?si).*of one of types.*\\[1:INSTALLED\\].*but was of type.*512:LAZY_ACTIVATION.*");
		assertFailing(aut::isOfTypeMaskedBy, INSTALLED | STOPPING | RESOLVED).hasMessageMatching(
			"(?si).*of one of types.*\\Q[289:INSTALLED | RESOLVED | STOPPING]\\E.*but was of type.*512:LAZY_ACTIVATION.*");
		assertFailing(aut::isOfTypeMaskedBy, STOPPING | RESOLVED | STARTING).hasMessageMatching(
			"(?si).*of one of types.*\\Q[416:RESOLVED | STARTING | STOPPING]\\E.*but was of type.*512:LAZY_ACTIVATION.*");

		setActual(new BundleEvent(RESOLVED, bundle, origin));

		assertPassing(aut::isOfTypeMaskedBy, RESOLVED | INSTALLED);
		assertPassing(aut::isOfTypeMaskedBy, RESOLVED);
		assertPassing(aut::isOfTypeMaskedBy, LAZY_ACTIVATION | STOPPING | RESOLVED);
		assertFailing(aut::isOfTypeMaskedBy, INSTALLED)
			.hasMessageMatching("(?si).*of one of types.*\\[1:INSTALLED\\].*but was of type.*32:RESOLVED.*");
		assertFailing(aut::isOfTypeMaskedBy, INSTALLED | STOPPING | LAZY_ACTIVATION).hasMessageMatching(
			"(?si).*of one of types.*\\Q[769:INSTALLED | STOPPING | LAZY_ACTIVATION]\\E.*but was of type.*32:RESOLVED.*");
		assertFailing(aut::isOfTypeMaskedBy, STOPPING | LAZY_ACTIVATION | STARTING).hasMessageMatching(
			"(?si).*of one of types.*\\Q[896:STARTING | STOPPING | LAZY_ACTIVATION]\\E.*but was of type.*32:RESOLVED.*");
	}

	@ParameterizedTest
	@ValueSource(ints = {
		-23, 0, 1024, 2048
	})
	public void isOfTypeMask_throwsIAE_forInvalidMask(int mask) {
		setActual(new BundleEvent(LAZY_ACTIVATION, bundle, origin));

		softly().assertThatThrownBy(() -> aut.isOfTypeMaskedBy(
			mask))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining(Integer.toString(mask));
	}

	@Test
	public void isNotOfTypeMaskedBy() {
		setActual(new BundleEvent(STARTING, bundle, origin));

		assertPassing(aut::isNotOfTypeMaskedBy, LAZY_ACTIVATION | INSTALLED);
		assertPassing(aut::isNotOfTypeMaskedBy, LAZY_ACTIVATION);
		assertPassing(aut::isNotOfTypeMaskedBy, LAZY_ACTIVATION | STOPPING | RESOLVED);
		assertFailing(
			aut::isNotOfTypeMaskedBy,
			STARTING)
				.hasMessageMatching(
					"(?si).*not.*of one of types.*\\Q[128:STARTING]\\E.*but was of type.*128:STARTING.*");
		assertFailing(aut::isNotOfTypeMaskedBy, INSTALLED | STARTING
			| STOPPING)
			.hasMessageMatching(
					"(?si).*not.*of one of types.*\\Q[385:INSTALLED | STARTING | STOPPING]\\E.*but was of type.*128:STARTING.*");
		assertFailing(aut::isNotOfTypeMaskedBy, STOPPING | RESOLVED
			| STARTING)
			.hasMessageMatching(
					"(?si).*not.*of one of types.*\\Q[416:RESOLVED | STARTING | STOPPING]\\E.*but was of type.*128:STARTING.*");

		setActual(new BundleEvent(UNINSTALLED, bundle, origin));

		assertPassing(aut::isNotOfTypeMaskedBy, RESOLVED | INSTALLED);
		assertPassing(aut::isNotOfTypeMaskedBy, RESOLVED);
		assertPassing(aut::isNotOfTypeMaskedBy, LAZY_ACTIVATION | STOPPING | RESOLVED);
		assertFailing(aut::isNotOfTypeMaskedBy,
			UNINSTALLED)
			.hasMessageMatching(
					"(?si).*not.*of one of types.*\\Q[16:UNINSTALLED]\\E.*but was of type.*16:UNINSTALLED.*");
		assertFailing(aut::isNotOfTypeMaskedBy, UNINSTALLED | STOPPING
			| LAZY_ACTIVATION)
			.hasMessageMatching(
					"(?si).*not.*of one of types.*\\Q[784:UNINSTALLED | STOPPING | LAZY_ACTIVATION]\\E.*but was of type.*16:UNINSTALLED.*");
		assertFailing(aut::isNotOfTypeMaskedBy, STOPPING | UNINSTALLED
			| STARTING)
			.hasMessageMatching(
					"(?si).*not.*of one of types.*\\Q[400:UNINSTALLED | STARTING | STOPPING]\\E.*but was of type.*16:UNINSTALLED.*");
	}

	@ParameterizedTest
	@ValueSource(ints = {
		-23, 0, 1024, 2048
	})
	public void isNotOfTypeMaskedBy_throwsIAE_forInvalidMask(int mask) {
		setActual(new BundleEvent(LAZY_ACTIVATION, bundle, origin));

		softly().assertThatThrownBy(() -> aut.isNotOfTypeMaskedBy(
			mask))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining(Integer.toString(mask));
	}

	public final static int UNKNOWN = 0x00000400;

	@ValueSource(ints = {
		INSTALLED, LAZY_ACTIVATION, RESOLVED, STARTED, STARTING, STOPPED, STOPPING, UNINSTALLED, UNRESOLVED, UPDATED,
		UNKNOWN
	})
	@Retention(RetentionPolicy.RUNTIME)
	static @interface TypeSource {}

	// Types in the order that they are listed in BundleEvent
	private static final int[]	TYPES			= IntStream
		.of(INSTALLED, LAZY_ACTIVATION, RESOLVED, STARTED, STARTING, STOPPED, STOPPING, UNINSTALLED, UNRESOLVED,
			UPDATED)
		.sorted()
		.toArray();

	private static final int	KNOWN_MASK		= IntStream.of(
		TYPES)
		.reduce((x, y) -> x | y)
		.getAsInt();
	private static final int	UNKNOWN_MASK	= ~KNOWN_MASK;

	private static String typeMaskToString(int type) {
		Stream<String> bits = IntStream.of(
			TYPES)
			.filter(x -> (x
				& type) != 0)
			.mapToObj(BundleEventAssertTest::typeToString);

		if ((type & UNKNOWN_MASK) != 0) {
			bits = Stream.concat(bits, Stream.of("UNKNOWN"));
		}
		return bits.collect(Collectors.joining(" | "));
	}

	private static String typeToString(int type) {
		switch (type) {
			case INSTALLED :
				return "INSTALLED";
			case LAZY_ACTIVATION :
				return "LAZY_ACTIVATION";
			case RESOLVED :
				return "RESOLVED";
			case STARTED :
				return "STARTED";
			case STARTING :
				return "STARTING";
			case STOPPED :
				return "STOPPED";
			case STOPPING :
				return "STOPPING";
			case UNINSTALLED :
				return "UNINSTALLED";
			case UNRESOLVED :
				return "UNRESOLVED";
			case UPDATED :
				return "UPDATED";
			default :
				return "UNKNOWN";
		}
	}

}
