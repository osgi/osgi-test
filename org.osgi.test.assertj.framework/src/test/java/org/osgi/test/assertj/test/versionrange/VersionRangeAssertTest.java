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

package org.osgi.test.assertj.test.versionrange;

import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.osgi.framework.Version;
import org.osgi.framework.VersionRange;
import org.osgi.test.assertj.test.testutil.AbstractAssertAndSAPTest;
import org.osgi.test.assertj.versionrange.VersionRangeAssert;
import org.osgi.test.assertj.versionrange.VersionRangeSoftAssertionsProvider;

public class VersionRangeAssertTest
	extends AbstractAssertAndSAPTest<VersionRangeAssert, VersionRange, VersionRangeSoftAssertionsProvider> {

	public VersionRangeAssertTest() {
		super(VersionRangeAssert::assertThat, VersionRangeSoftAssertionsProvider.class, VersionRange.class,
			() -> new VersionRange("[1.0,2.0)"));
	}

	@BeforeEach
	void beforeEach() {
		setActual(VersionRange.valueOf("[1.2.3.qualifier,4.5.6)"));
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"1.2.3.qualifier", "2.22.2", "3.33.4"
	})
	public void hasLeft(String left) {
		setActual(VersionRange.valueOf("[" + left + ",4.5.6)"));

		assertEqualityAssertion("lower bound", aut::hasLeft, Version.valueOf(left), Version.valueOf("3.7.6"));
		assertEqualityAssertion("lower bound", aut::hasLeft, left, "3.7.6");
	}

	@Test
	public void hasLeftThat() {
		assertChildAssertion("lower bound", aut::hasLeftThat, actual::getLeft);
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"[1.2.3.qualifier,4.5.6)", "[3.2.3.qualifier,4.5.6]", "[4.5.5,4.5.6)"
	})
	public void withClosedLeftBound(String range) {
		setActual(VersionRange.valueOf(range));
		softly().assertThatCode(() -> aut.hasLeftThat()
			.isClosed())
			.doesNotThrowAnyException();
		softly().assertThatCode(() -> aut.hasLeftThat()
			.isOpen())
			.isInstanceOf(AssertionError.class);

		assertPassing("isLeftClosed", x -> aut.isLeftClosed(), null);
		assertFailing("isLeftOpen", x -> aut.isLeftOpen(), null)
			.hasMessageMatching("(?si).*to have open lower bound.*but it was closed.*");
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"(1.2.3.qualifier,4.5.6)", "(3.2.3.qualifier,4.5.6]", "(4.5.5,4.5.6)"
	})
	public void withOpenLeftBound(String range) {
		setActual(VersionRange.valueOf(range));
		softly().assertThatCode(() -> aut.hasLeftThat()
			.isOpen())
			.doesNotThrowAnyException();
		softly().assertThatCode(() -> aut.hasLeftThat()
			.isClosed())
			.isInstanceOf(AssertionError.class);

		assertPassing("isLeftOpen", x -> aut.isLeftOpen(), null);
		assertFailing("isLeftClosed", x -> aut.isLeftClosed(), null)
			.hasMessageMatching("(?si).*to have closed lower bound.*but it was open.*");
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"4.5.6.qualifier", "4.5.6", "6.7.8"
	})
	public void hasRight(String right) {
		setActual(VersionRange.valueOf("[1.2.3," + right + ")"));

		assertEqualityAssertion("upper bound", aut::hasRight, Version.valueOf(right), Version.valueOf("3.7.6"));
		assertEqualityAssertion("upper bound", aut::hasRight, right, "3.7.6");
	}

	@Test
	public void hasRightThat() {
		assertChildAssertion("upper bound", aut::hasRightThat, actual::getRight);
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"[1.2.3.qualifier,4.5.6]", "(3.2.3.qualifier,4.5.6]", "(4.5.5,4.5.6]"
	})
	public void withClosedRightBound(String range) {
		setActual(VersionRange.valueOf(range));
		softly().assertThatCode(() -> aut.hasRightThat()
			.isClosed())
			.doesNotThrowAnyException();
		softly().assertThatCode(() -> aut.hasRightThat()
			.isOpen())
			.isInstanceOf(AssertionError.class);

		assertPassing("isRightClosed", x -> aut.isRightClosed(), null);
		assertFailing("isRightOpen", x -> aut.isRightOpen(), null)
			.hasMessageMatching("(?si).*to have open upper bound.*but it was closed.*");
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"(1.2.3.qualifier,4.5.6)", "[3.2.3.qualifier,4.5.6)", "(4.5.5,4.5.6)"
	})
	public void withOpenRightBound(String range) {
		setActual(VersionRange.valueOf(range));
		softly().assertThatCode(() -> aut.hasRightThat()
			.isOpen())
			.doesNotThrowAnyException();
		softly().assertThatCode(() -> aut.hasRightThat()
			.isClosed())
			.isInstanceOf(AssertionError.class);

		assertPassing("isRightOpen", x -> aut.isRightOpen(), null);
		assertFailing("isRightClosed", x -> aut.isRightClosed(), null)
			.hasMessageMatching("(?si).*to have closed upper bound.*but it was open.*");
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"1.2.3.qualifier", "3.4.5", "4.5.5.qualifier"
	})
	public void includes_with_versions_in_range(String version) {
		assertPassing("includes", aut::includes, Version.valueOf(version));
		assertPassing("includes", aut::includes, version);
		assertFailing("doesNotInclude - Version", aut::doesNotInclude, Version.valueOf(version))
			.hasMessageMatching("(?si).*to not include version.*<" + version + ">.*but it does");
		assertFailing("doesNotInclude - String", aut::doesNotInclude, version)
			.hasMessageMatching("(?si).*to not include version.*<" + version + ">.*but it does");
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"1.2.3", "1.0.0", "4.5.6"
	})
	public void includes_with_versions_out_of_range(String version) {
		assertPassing("doesNotInclude - Version", aut::doesNotInclude, Version.valueOf(version));
		assertPassing("doesNotInclude - String", aut::doesNotInclude, version);
		assertFailing("includes - Version", aut::includes, Version.valueOf(version))
			.hasMessageMatching("(?si).*to include version.*<" + version + ">.*but it does not");
		assertFailing("includes - String", aut::includes, version)
			.hasMessageMatching("(?si).*to include version.*<" + version + ">.*but it does not");
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"(1.1.1,1.1.1)", "[2.1.1,2.1.1)", "(1.3.4,1.3.4]"
	})
	public void isEmpty_with_empty(String range) {
		setActual(VersionRange.valueOf(range));
		assertPassing("isEmpty", x -> aut.isEmpty(), null);
		assertFailing("isNotEmpty", x -> aut.isNotEmpty(), null)
			.hasMessageMatching("(?si).*to be empty\\s+but it was not");
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"(1.1.1,1.1.2)", "[2.1.1,2.1.1]", "[0.3.4,1.3.4]"
	})
	public void isEmpty_with_non_empty(String range) {
		setActual(VersionRange.valueOf(range));
		assertPassing("isNotEmpty", x -> aut.isNotEmpty(), null);
		assertFailing("isEmpty", x -> aut.isEmpty(), null)
			.hasMessageMatching("(?si).*to be empty\\s+but it was not");
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"[1.1.1,1.1.1]", "[2.1.1,2.1.1]"
	})
	public void isExact_with_exact(String range) {
		setActual(VersionRange.valueOf(range));
		assertPassing("isExact", x -> aut.isExact(), null);
		assertFailing("isNotExact", x -> aut.isNotExact(), null)
			.hasMessageMatching("(?si).*to be exact\\s+but it was not");
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"(1.1.1,1.1.2)", "[2.1.1,2.1.1)", "[0.3.4,1.3.4]"
	})
	public void isExact_with_non_exact(String range) {
		setActual(VersionRange.valueOf(range));
		assertPassing("isNotExact", x -> aut.isNotExact(), null);
		assertFailing("isExact", x -> aut.isExact(), null).hasMessageMatching("(?si).*to be exact\\s+but it was not");
	}

	static Stream<Arguments> intersects_withIntersecting() {
		return Stream.of(Arguments.of(new Object[] {
			new String[] {
			"[1.0.0,2.0.0)", "(1.5.0,2.5.0)", "[1.6.1,1.7.1]"
			}
		}), Arguments.of(new Object[] {
			new String[] {
				"[1.0.0,2.0.0)", "[1.6.1,1.7.1]"
			}
		}));
	}

	@ParameterizedTest
	@MethodSource
	public void intersects_withIntersecting(String[] values) {
		VersionRange[] ranges = Stream.of(values)
			.map(VersionRange::valueOf)
			.toArray(VersionRange[]::new);

		VersionRange intersection = actual.intersection(ranges);
		String rangeString = Arrays.toString(ranges);
		assertPassing("intersects - String args", aut::intersects, values);
		assertPassing("intersects - VersionRange args", aut::intersects, ranges);
		assertFailing("doesNotIntersect - String args", aut::doesNotIntersect, values)
			.hasMessageMatching(String.format("(?si).*to not intersect any of%n <%s>%nbut it has intersection%n <%s>",
				Pattern.quote(rangeString), Pattern.quote(intersection.toString())));
		assertFailing("doesNotIntersect - VersionRange args", aut::doesNotIntersect, ranges)
			.hasMessageMatching(String.format(
			"(?si).*to not intersect any of%n <%s>%nbut it has intersection%n <%s>", Pattern.quote(rangeString),
			Pattern.quote(intersection.toString())));
	}

	static Stream<Arguments> intersects_withNonIntersecting() {
		return Stream.of(Arguments.of(new Object[] {
			new String[] {
				"[1.8.0,2.0.0)", "(1.5.0,2.5.0)", "[1.6.1,1.7.1]"
			}
		}), Arguments.of(new Object[] {
			new String[] {
				"[1.8.0,2.0.0)", "[1.6.1,1.7.1]"
			}
		}));
	}

	@ParameterizedTest
	@MethodSource
	public void intersects_withNonIntersecting(String[] values) {
		VersionRange[] ranges = Stream.of(values)
			.map(VersionRange::valueOf)
			.toArray(VersionRange[]::new);

		VersionRange intersection = actual.intersection(ranges);
		String rangeString = Arrays.toString(ranges);
		assertFailing("intersects - String args", aut::intersects, values)
			.hasMessageMatching(String.format("(?si).*to intersect all of%n <%s>%nbut it does not",
				Pattern.quote(rangeString)));
		assertFailing("intersects - VersionRange args", aut::intersects, ranges)
			.hasMessageMatching(
				String.format("(?si).*to intersect all of%n <%s>%nbut it does not", Pattern.quote(rangeString)));
		assertPassing("doesNotIntersect - String args", aut::doesNotIntersect, values);
		assertPassing("doesNotIntersect - VersionRange args", aut::doesNotIntersect, ranges);
	}
}
