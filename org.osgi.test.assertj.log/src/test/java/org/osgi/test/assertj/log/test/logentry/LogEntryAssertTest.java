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

package org.osgi.test.assertj.log.test.logentry;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogEntry;
import org.osgi.service.log.LogLevel;
import org.osgi.test.assertj.log.logentry.LogEntryAssert;
import org.osgi.test.assertj.log.test.testutil.AbstractAssertTest;

class LogEntryAssertTest extends AbstractAssertTest<LogEntryAssert, LogEntry> {

	LogEntryAssertTest() {
		super(LogEntryAssert::assertThat);
	}

	Bundle		bundle;
	ServiceReference<?>	ref;
	LogEntry	entry;
	LogEntry	otherEntry;

	@SuppressWarnings({
		"rawtypes", "unchecked"
	})
	@BeforeEach
	void setUp() {
		bundle = mock(Bundle.class);
		ref = mock(ServiceReference.class);
		setActual(mock(LogEntry.class));
		when(actual.getBundle()).thenReturn(bundle);
		when(actual.getServiceReference()).then(invocation -> {
			return ref;
		});
	}

	@Test
	void hasBundle() {
		Bundle otherBundle = mock(Bundle.class);
		assertEqualityAssertion("bundle", aut::hasBundle, bundle, otherBundle);
	}

	@Test
	void hasBundleThat() {
		assertChildAssertion("bundle", aut::hasBundleThat, actual::getBundle);
	}

	@Test
	void hasServiceReference() {
		ServiceReference<?> otherRef = mock(ServiceReference.class);
		assertEqualityAssertion("service reference", aut::hasServiceReference, ref, otherRef);
	}

	@Test
	void hasServiceReferenceThat() {
		assertChildAssertion("service reference", aut::hasServiceReferenceThat, actual::getServiceReference);
	}

	@Test
	public void hasMessage() {
		when(actual.getMessage()).thenReturn("a message");

		assertEqualityAssertion("message", aut::hasMessage, "a message", "some other message");

		when(actual.getMessage()).thenReturn(null);

		assertEqualityAssertion("message", aut::hasMessage, null, "some other message");
	}

	@Test
	public void hasMessageThat() {
		when(actual.getMessage()).thenReturn("a message");

		assertChildAssertion("message", aut::hasMessageThat, actual::getMessage);
	}

	@Test
	public void hasException() {
		RuntimeException e = new RuntimeException("error");
		when(actual.getException()).thenReturn(e);

		assertEqualityAssertion("exception", aut::hasException, e, new RuntimeException());

		when(actual.getException()).thenReturn(null);

		assertEqualityAssertion("exception", aut::hasException, null, e);
	}

	@Test
	public void hasExceptionThat() {
		when(actual.getException()).thenReturn(new RuntimeException("a message"));

		assertChildAssertion("exception", aut::hasExceptionThat, actual::getException);
	}

	@Test
	public void hasTimeLongThat() {
		when(actual.getTime()).thenReturn(10L);

		assertChildAssertion("time", aut::hasTimeLongThat, actual::getTime);
	}

	@Test
	public void hasTimeDateThat() {
		when(actual.getTime()).thenReturn(10L);

		assertChildAssertion("time", aut::hasTimeDateThat, () -> new Date(actual.getTime()));
	}

	@Test
	public void hasLogLevel() {
		when(actual.getLogLevel()).thenReturn(LogLevel.ERROR);

		assertEqualityAssertion("log level", aut::hasLogLevel, LogLevel.ERROR, LogLevel.DEBUG);

		when(actual.getLogLevel()).thenReturn(null);

		assertEqualityAssertion("log level", aut::hasLogLevel, null, LogLevel.DEBUG);
	}

	@Test
	public void hasLoggerName() {
		when(actual.getLoggerName()).thenReturn("my.name");

		assertEqualityAssertion("logger name", aut::hasLoggerName, "my.name", "some.other.name");

		when(actual.getLoggerName()).thenReturn(null);

		assertEqualityAssertion("logger name", aut::hasLoggerName, null, "some.other.name");
	}

	@Test
	public void hasLoggerNameThat() {
		when(actual.getLoggerName()).thenReturn("my.name");

		assertChildAssertion("logger name", aut::hasLoggerNameThat, actual::getLoggerName);
	}
}
