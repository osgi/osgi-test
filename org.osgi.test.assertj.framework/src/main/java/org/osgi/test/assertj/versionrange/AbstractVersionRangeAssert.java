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

package org.osgi.test.assertj.versionrange;

import static org.osgi.framework.VersionRange.LEFT_CLOSED;
import static org.osgi.framework.VersionRange.LEFT_OPEN;
import static org.osgi.framework.VersionRange.RIGHT_CLOSED;
import static org.osgi.framework.VersionRange.RIGHT_OPEN;
import static org.osgi.test.assertj.versionrange.VersionBoundAssert.versionBoundAssertFactory;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

import org.assertj.core.api.AbstractAssert;
import org.osgi.framework.Version;
import org.osgi.framework.VersionRange;

public class AbstractVersionRangeAssert<SELF extends AbstractVersionRangeAssert<SELF, ACTUAL>, ACTUAL extends VersionRange>
	extends AbstractAssert<SELF, ACTUAL> {

	protected AbstractVersionRangeAssert(ACTUAL actual, Class<?> selfType) {
		super(actual, selfType);
	}

	public AbstractVersionBoundAssert<?, ?> hasLeftThat() {
		return isNotNull().extracting(VersionRange::getLeft,
			versionBoundAssertFactory(actual.getLeftType() == LEFT_OPEN));
	}

	public SELF hasLeft(String expected) {
		return hasLeft(Version.valueOf(expected));
	}

	public SELF hasLeft(Version expected) {
		isNotNull();
		Version a = actual.getLeft();
		if (!Objects.equals(expected, a)) {
			throw failureWithActualExpected(a, expected,
				"%nExpecting version range%n <%s>%nto have lower bound:%n <%s>%n but it was:%n <%s>", actual, expected,
				a);
		}
		return myself;
	}

	public SELF isLeftOpen() {
		isNotNull();
		if (actual.getLeftType() != LEFT_OPEN) {
			throw failure("%nExpecting version range%n <%s>%nto have open lower bound%n but it was closed", actual);
		}
		return myself;
	}

	public SELF isLeftClosed() {
		isNotNull();
		if (actual.getLeftType() != LEFT_CLOSED) {
			throw failure("%nExpecting version range%n <%s>%nto have closed lower bound%n but it was open", actual);
		}
		return myself;
	}

	public AbstractVersionBoundAssert<?, ?> hasRightThat() {
		return isNotNull().extracting(VersionRange::getRight,
			versionBoundAssertFactory(actual.getRightType() == RIGHT_OPEN));
	}

	public SELF hasRight(String expected) {
		return hasRight(Version.valueOf(expected));
	}

	public SELF hasRight(Version expected) {
		isNotNull();
		Version a = actual.getRight();
		if (!Objects.equals(expected, a)) {
			throw failureWithActualExpected(a, expected,
				"%nExpecting version range%n <%s>%nto have upper bound:%n <%s>%n but it was:%n <%s>", actual, expected,
				a);
		}
		return myself;
	}

	public SELF isRightOpen() {
		isNotNull();
		if (actual.getRightType() != RIGHT_OPEN) {
			throw failure("%nExpecting version range%n <%s>%nto have open upper bound%nbut it was closed", actual);
		}
		return myself;
	}

	public SELF isRightClosed() {
		isNotNull();
		if (actual.getRightType() != RIGHT_CLOSED) {
			throw failure("%nExpecting version range%n <%s>%nto have closed upper bound%nbut it was open", actual);
		}
		return myself;
	}

	public SELF includes(String version) {
		return includes(Version.valueOf(version));
	}

	public SELF includes(Version version) {
		isNotNull();
		if (!actual.includes(version)) {
			throw failure("%nExpecting version range%n <%s>%nto include version%n <%s>%nbut it does not", actual,
				version);
		}
		return myself;
	}

	public SELF doesNotInclude(String version) {
		return doesNotInclude(Version.valueOf(version));
	}

	public SELF doesNotInclude(Version version) {
		isNotNull();
		if (actual.includes(version)) {
			throw failure("%nExpecting version range%n <%s>%nto not include version%n <%s>%nbut it does", actual,
				version);
		}
		return myself;
	}

	public SELF isEmpty() {
		isNotNull();
		if (!actual.isEmpty()) {
			throw failure("%nExpecting version range%n <%s>%nto be empty%nbut it was not", actual);
		}
		return myself;
	}

	public SELF isNotEmpty() {
		isNotNull();
		if (actual.isEmpty()) {
			throw failure("%nExpecting version range%n <%s>%nto be empty%nbut it was not", actual);
		}
		return myself;
	}

	public SELF isExact() {
		isNotNull();
		if (!actual.isExact()) {
			throw failure("%nExpecting version range%n <%s>%nto be exact%nbut it was not", actual);
		}
		return myself;
	}

	public SELF isNotExact() {
		isNotNull();
		if (actual.isExact()) {
			throw failure("%nExpecting version range%n <%s>%nto be exact%nbut it was not", actual);
		}
		return myself;
	}

	public SELF intersects(String... version) {
		return intersects(Stream.of(version)
			.map(VersionRange::valueOf)
			.toArray(VersionRange[]::new));
	}

	public SELF intersects(VersionRange... versions) {
		isNotNull();
		if (actual.intersection(versions)
			.isEmpty()) {
			throw failure("%nExpecting version range%n <%s>%nto intersect all of%n <%s>%nbut it does not", actual,
				Arrays.toString(versions));
		}
		return myself;
	}

	public SELF doesNotIntersect(String... versions) {
		return doesNotIntersect(Stream.of(versions)
			.map(VersionRange::valueOf)
			.toArray(VersionRange[]::new));
	}

	public SELF doesNotIntersect(VersionRange... versions) {
		isNotNull();
		VersionRange intersection = actual.intersection(versions);
		if (!intersection
			.isEmpty()) {
			throw failure(
				"%nExpecting version range%n <%s>%nto not intersect any of%n <%s>%nbut it has intersection%n <%s>",
				actual, Arrays.toString(versions), intersection);
		}
		return myself;
	}
}
