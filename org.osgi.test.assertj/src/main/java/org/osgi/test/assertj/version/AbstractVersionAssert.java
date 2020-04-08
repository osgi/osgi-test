/*
 * Copyright (c) OSGi Alliance (2019-2020). All Rights Reserved.
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
package org.osgi.test.assertj.version;

import static org.assertj.core.api.InstanceOfAssertFactories.INTEGER;
import static org.assertj.core.api.InstanceOfAssertFactories.STRING;

import java.util.Objects;

import org.assertj.core.api.AbstractComparableAssert;
import org.assertj.core.api.AbstractIntegerAssert;
import org.assertj.core.api.AbstractStringAssert;
import org.assertj.core.api.ComparableAssert;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.assertj.core.api.InstanceOfAssertFactory;
import org.assertj.core.api.ObjectAssert;
import org.osgi.framework.Version;
import org.osgi.framework.VersionRange;

public class AbstractVersionAssert<SELF extends AbstractVersionAssert<SELF, ACTUAL>, ACTUAL extends Version>
	extends AbstractComparableAssert<SELF, ACTUAL> implements ComparableAssert<SELF, ACTUAL> {

	protected AbstractVersionAssert(ACTUAL actual, Class<?> selfType) {
		super(actual, selfType);
	}

	private static final InstanceOfAssertFactory<Version, ObjectAssert<Version>> VERSION_OBJECT = InstanceOfAssertFactories
		.type(Version.class);

	// TODO: when AssertJ 3.16 becomes available, get rid of this method and
	// VERSION_OBJECT as it's only used for exposing extracting()
	ObjectAssert<ACTUAL> asObjectAssert() {
		@SuppressWarnings({
			"unchecked", "rawtypes"
		})
		ObjectAssert<ACTUAL> objectAssert = (ObjectAssert) asInstanceOf(VERSION_OBJECT);
		return objectAssert;
	}

	public AbstractIntegerAssert<?> hasMajorThat() {
		return isNotNull().asObjectAssert()
			.extracting(Version::getMajor, INTEGER);
	}

	public SELF hasMajor(int expected) {
		isNotNull();
		int a = actual.getMajor();
		if (expected != a) {
			failWithMessage("%nExpecting%n <%s>%nto have major version:%n  <%d>%n but it was:%n  <%d>", actual,
				expected, a);
		}
		return myself;
	}

	public AbstractIntegerAssert<?> hasMinorThat() {
		return isNotNull().asObjectAssert()
			.extracting(Version::getMinor, INTEGER);
	}

	public SELF hasMinor(int expected) {
		isNotNull();
		int a = actual.getMinor();
		if (expected != a) {
			failWithMessage("%nExpecting%n <%s>%nto have minor version:%n  <%d>%n but it was:%n  <%d>", actual,
				expected, a);
		}
		return myself;
	}

	public AbstractIntegerAssert<?> hasMicroThat() {
		return isNotNull().asObjectAssert()
			.extracting(Version::getMicro, INTEGER);
	}

	public SELF hasMicro(int expected) {
		isNotNull();
		int a = actual.getMicro();
		if (expected != a) {
			failWithMessage("%nExpecting%n <%s>%nto have micro version:%n <%d>%n but it was:%n <%d>", actual, expected,
				a);
		}
		return myself;
	}

	public AbstractStringAssert<?> hasQualifierThat() {
		return isNotNull().asObjectAssert()
			.extracting(Version::getQualifier, STRING);
	}

	public SELF hasQualifier(String expected) {
		isNotNull();
		String a = actual.getQualifier();
		if (!Objects.equals(a, expected)) {
			failWithMessage("%nExpecting%n <%s>%nto have qualifier:%n <%s>%n but it was:%n <%s>", actual, expected, a);
		}
		return myself;
	}

	public SELF isEmpty() {
		return isEqualTo(Version.emptyVersion);
	}

	public SELF isInRange(String range) {
		return isInRange(VersionRange.valueOf(range));
	}

	public SELF isInRange(VersionRange range) {
		isNotNull();
		if (!range.includes(actual)) {
			failWithMessage("%nExpecting%n <%s>%nto be in range:%n <%s>%n but it was not", actual, range);
		}
		return myself;
	}

	public SELF isNotInRange(String range) {
		return isNotInRange(VersionRange.valueOf(range));
	}

	public SELF isNotInRange(VersionRange range) {
		isNotNull();
		if (range.includes(actual)) {
			failWithMessage("%nExpecting%n <%s>%nto not be in range:%n <%s>%n but it was", actual, range);
		}
		return myself;
	}
}
