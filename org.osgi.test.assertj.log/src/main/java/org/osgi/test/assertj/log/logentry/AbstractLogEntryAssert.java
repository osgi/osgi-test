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

package org.osgi.test.assertj.log.logentry;

import static org.assertj.core.api.InstanceOfAssertFactories.LONG;
import static org.assertj.core.api.InstanceOfAssertFactories.STRING;
import static org.assertj.core.api.InstanceOfAssertFactories.THROWABLE;
import static org.osgi.test.assertj.bundle.BundleAssert.BUNDLE;
import static org.osgi.test.assertj.date.Dates.LONG_AS_DATE;
import static org.osgi.test.assertj.servicereference.ServiceReferenceAssert.SERVICE_REFERENCE;

import java.util.Objects;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.AbstractDateAssert;
import org.assertj.core.api.AbstractLongAssert;
import org.assertj.core.api.AbstractStringAssert;
import org.assertj.core.api.AbstractThrowableAssert;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogEntry;
import org.osgi.service.log.LogLevel;
import org.osgi.test.assertj.bundle.AbstractBundleAssert;
import org.osgi.test.assertj.servicereference.AbstractServiceReferenceAssert;

public abstract class AbstractLogEntryAssert<SELF extends AbstractLogEntryAssert<SELF, ACTUAL>, ACTUAL extends LogEntry>
	extends AbstractAssert<SELF, ACTUAL> {

	protected AbstractLogEntryAssert(ACTUAL actual, Class<SELF> selfType) {
		super(actual, selfType);
	}

	public SELF hasBundle(Bundle expected) {
		isNotNull();
		if (!Objects.equals(actual.getBundle(), expected)) {
			throw failureWithActualExpected(actual.getBundle(), expected,
				"%nExpecting%n <%s>%nto have bundle source:%n <%s>%n but was:%n<%s>", actual, expected,
				actual.getBundle());
		}
		return myself;
	}

	public AbstractBundleAssert<?, ? extends Bundle> hasBundleThat() {
		return isNotNull().extracting(LogEntry::getBundle, BUNDLE)
			.as(actual + ".bundle");
	}

	public SELF hasServiceReference(ServiceReference<?> expected) {
		isNotNull();
		if (!Objects.equals(actual.getServiceReference(), expected)) {
			throw failureWithActualExpected(actual.getServiceReference(), expected,
				"%nExpecting%n <%s>%nto have service reference:%n <%s>%n but was:%n<%s>", actual, expected,
				actual.getServiceReference());
		}
		return myself;
	}

	public AbstractServiceReferenceAssert<?, ? extends ServiceReference<?>, ?> hasServiceReferenceThat() {
		return isNotNull().extracting(LogEntry::getServiceReference, SERVICE_REFERENCE)
			.as(actual + ".serviceReference");
	}

	public SELF hasMessage(String expected) {
		isNotNull();
		if (!Objects.equals(actual.getMessage(), expected)) {
			throw failureWithActualExpected(actual.getMessage(), expected,
				"%nExpecting%n  <%s>%nto have message:%n  <%s>%n but was:%n  <%s>", actual, expected,
				actual.getMessage());
		}
		return myself;
	}

	public AbstractStringAssert<?> hasMessageThat() {
		return isNotNull().extracting(LogEntry::getMessage, STRING)
			.as(actual + ".message");
	}

	public SELF hasException(Throwable expected) {
		isNotNull();
		if (!Objects.equals(actual.getException(), expected)) {
			throw failureWithActualExpected(actual.getException(), expected,
				"%nExpecting%n  <%s>%nto have exception:%n  <%s>%n but was:%n  <%s>", actual, expected,
				actual.getException());
		}
		return myself;
	}

	public AbstractThrowableAssert<?, ?> hasExceptionThat() {
		return isNotNull().extracting(LogEntry::getException, THROWABLE)
			.as(actual + ".exception");
	}

	public AbstractLongAssert<?> hasTimeLongThat() {
		return isNotNull().extracting(LogEntry::getTime, LONG)
			.as(actual + ".time");
	}

	public AbstractDateAssert<?> hasTimeDateThat() {
		return isNotNull().extracting(LogEntry::getTime, LONG_AS_DATE)
			.as(actual + ".time");
	}

	public SELF hasLogLevel(LogLevel expected) {
		isNotNull();
		if (!Objects.equals(actual.getLogLevel(), expected)) {
			throw failureWithActualExpected(actual.getLogLevel(), expected,
				"%nExpecting%n  <%s>%nto have log level:%n  <%s>%n but was:%n  <%s>", actual, expected,
				actual.getLogLevel());
		}
		return myself;
	}

	public SELF hasLoggerName(String expected) {
		isNotNull();
		if (!Objects.equals(actual.getLoggerName(), expected)) {
			throw failureWithActualExpected(actual.getLoggerName(), expected,
				"%nExpecting%n  <%s>%nto have logger name:%n  <%s>%n but was:%n  <%s>", actual, expected,
				actual.getLoggerName());
		}
		return myself;
	}

	public AbstractStringAssert<?> hasLoggerNameThat() {
		return isNotNull().extracting(LogEntry::getLoggerName, STRING)
			.as(actual + ".loggerName");
	}
}
