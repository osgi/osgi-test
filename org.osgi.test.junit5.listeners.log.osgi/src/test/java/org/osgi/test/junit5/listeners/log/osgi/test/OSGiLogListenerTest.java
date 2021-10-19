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

package org.osgi.test.junit5.listeners.log.osgi.test;

import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.InjectSoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;
import org.opentest4j.TestAbortedException;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.log.LogEntry;
import org.osgi.service.log.LogLevel;
import org.osgi.service.log.LogListener;
import org.osgi.service.log.LogReaderService;
import org.osgi.test.assertj.log.logentry.LogEntryAssert;
import org.osgi.test.assertj.log.logentry.LogEntrySoftAssertionsProvider;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.annotation.InjectInstalledBundle;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.service.ServiceAware;

@ExtendWith(SoftAssertionsExtension.class)
public class OSGiLogListenerTest {

	static class AllSoftAssertions extends SoftAssertions implements LogEntrySoftAssertionsProvider {}

	static final String	LOGGER	= "org.osgi.test.junit5.listeners.log.osgi.OSGiLogListener";

	@InjectBundleContext
	BundleContext		bc;

	@InjectSoftAssertions
	AllSoftAssertions	softly;

	static class LogRecorder implements LogListener {

		List<LogEntry>			entries	= new ArrayList<>();

		volatile CountDownLatch	flag	= new CountDownLatch(1);

		@Override
		public void logged(LogEntry entry) {
			if (entry.getLoggerName()
				.equals(LOGGER)) {
				entries.add(entry);
				flag.countDown();
			}
		}

		void waitForLog() throws InterruptedException {
			flag.await(1000, TimeUnit.MILLISECONDS);
			flag = new CountDownLatch(1);
		}
	}

	@InjectInstalledBundle(value = "org.osgi.test.junit5.listeners.log.osgi.jar", start = true)
	Bundle								b;

	LogRecorder							recorder;
	@InjectService(cardinality = 0)
	ServiceAware<TestExecutionListener>	osgiLogAware;

	TestExecutionListener				osgiLog;

	@InjectService
	LogReaderService					lrs;

	TestIdentifier						id;
	TestPlan							plan;

	@BeforeEach
	void beforeEach() throws Exception {
		osgiLog = osgiLogAware.waitForService(1000);
		recorder = new LogRecorder();
		lrs.addLogListener(recorder);

		TestPlan plan;

		UniqueId uid = UniqueId.parse("[engine:jupiter-engine]/[class:MyClass]/[method:myMethod]");
		AbstractTestDescriptor td = new AbstractTestDescriptor(uid, "My class method") {
			@Override
			public Type getType() {
				return Type.TEST;
			}
		};
		UniqueId eid = UniqueId.parse("[engine:jupiter-engine]");
		AbstractTestDescriptor ed = new AbstractTestDescriptor(eid, "Jupiter Test Engine") {
			@Override
			public Type getType() {
				return Type.CONTAINER;
			}
		};

		td.setParent(ed);

		plan = mock(TestPlan.class);

		id = TestIdentifier.from(td);
		osgiLog.testPlanExecutionStarted(plan);
		recorder.waitForLog();
		softly.assertThat(getLastEntry())
			.hasLogLevel(LogLevel.DEBUG)
			.hasException(null)
			.hasMessage("Test plan started");
	}

	LogEntry getLastEntry() {
		return recorder.entries.get(recorder.entries.size() - 1);
	}

	@AfterEach
	void afterEach() throws Exception {
		osgiLog.testPlanExecutionFinished(plan);
		recorder.waitForLog();
		lrs.removeLogListener(recorder);
		LogEntryAssert.assertThat(getLastEntry())
			.hasLogLevel(LogLevel.DEBUG)
			.hasException(null)
			.hasMessage("Test plan finished");
	}

	@Test
	void dynamicTestRegistered() throws Exception {
		osgiLog.dynamicTestRegistered(id);
		recorder.waitForLog();
		softly.assertThat(getLastEntry())
			.hasLogLevel(LogLevel.DEBUG)
			.hasException(null)
			.hasMessageThat()
			.matches("Test registered:\\s+\\Q" + id.getDisplayName() + "\\E");
	}

	@Test
	void executionFinished_successfulTest() throws Exception {
		osgiLog.executionFinished(id, TestExecutionResult.successful());
		recorder.waitForLog();
		softly.assertThat(getLastEntry())
			.hasLogLevel(LogLevel.INFO)
			.hasException(null)
			.hasMessageThat()
			.matches("Test passed:\\s+\\Q" + id.getDisplayName() + "\\E");
	}

	@Test
	void executionFinished_failedTest_withException() throws Exception {
		Exception e = new RuntimeException();
		osgiLog.executionFinished(id, TestExecutionResult.failed(e));
		recorder.waitForLog();
		softly.assertThat(getLastEntry())
			.hasLogLevel(LogLevel.ERROR)
			.hasException(e)
			.hasMessageThat()
			.matches("Test failed:\\s+\\Q" + id.getDisplayName() + "\\E, reason: \\Q" + e + "\\E");
	}

	@Test
	void executionFinished_failedTest_noException() throws Exception {
		osgiLog.executionFinished(id, TestExecutionResult.failed(null));
		recorder.waitForLog();
		softly.assertThat(getLastEntry())
			.hasLogLevel(LogLevel.ERROR)
			.hasException(null)
			.hasMessageThat()
			.matches("Test failed:\\s+\\Q" + id.getDisplayName() + "\\E");
	}

	@Test
	void executionFinished_abortedTest_withException() throws Exception {
		Exception e = new TestAbortedException();
		osgiLog.executionFinished(id, TestExecutionResult.aborted(e));
		recorder.waitForLog();
		softly.assertThat(getLastEntry())
			.hasLogLevel(LogLevel.WARN)
			.hasException(e)
			.hasMessageThat()
			.matches("Test aborted:\\s+\\Q" + id.getDisplayName() + "\\E, reason: \\Q" + e + "\\E");
	}

	@Test
	void executionFinished_abortedTest_noException() throws Exception {
		osgiLog.executionFinished(id, TestExecutionResult.aborted(null));
		recorder.waitForLog();
		softly.assertThat(getLastEntry())
			.hasLogLevel(LogLevel.WARN)
			.hasException(null)
			.hasMessageThat()
			.matches("Test aborted:\\s+\\Q" + id.getDisplayName() + "\\E");
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"reason 1", "reason2", "third one"
	})
	void executionSkipped(String reason) throws Exception {
		osgiLog.executionSkipped(id, reason);
		recorder.waitForLog();
		softly.assertThat(getLastEntry())
			.hasLogLevel(LogLevel.WARN)
			.hasException(null)
			.hasMessageThat()
			.matches("Test skipped:\\s+\\Q" + id.getDisplayName() + "\\E, reason: \\Q" + reason + "\\E");
	}

	@Test
	void executionStarted() throws Exception {
		osgiLog.executionStarted(id);
		recorder.waitForLog();
		softly.assertThat(getLastEntry())
			.hasLogLevel(LogLevel.DEBUG)
			.hasException(null)
			.hasMessageThat()
			.matches("Test started:\\s+\\Q" + id.getDisplayName() + "\\E");
	}
}
