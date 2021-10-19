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

package org.osgi.test.junit5.listeners.log.osgi;

import java.util.Optional;

import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.TestExecutionResult.Status;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;
import org.osgi.service.log.Logger;
import org.osgi.service.log.LoggerFactory;

public class OSGiLogListener implements TestExecutionListener {

	TestPlan		testPlan;

	final Logger	logger;

	public OSGiLogListener(LoggerFactory logger) {
		this.logger = logger.getLogger(getClass());
	}

	void indentedName(StringBuilder msg, TestIdentifier testIdentifier) {
		int depth = 0;
		Optional<TestIdentifier> current = testPlan.getParent(testIdentifier);
		if (current.isPresent()) {
			current = testPlan.getParent(testIdentifier);
			while (current.isPresent()) {
				msg.append("  ");
				current = testPlan.getParent(current.get());
			}
		}
		msg.append(testIdentifier.getDisplayName());
	}

	String msg(CharSequence prefix, TestIdentifier id) {
		StringBuilder msg = new StringBuilder(256);
		msg.append(prefix);
		indentedName(msg, id);
		return msg.toString();
	}

	String msg(CharSequence prefix, TestIdentifier id, String reason) {
		StringBuilder msg = new StringBuilder(256);
		msg.append(prefix);
		indentedName(msg, id);
		msg.append(", reason: ");
		msg.append(reason);
		return msg.toString();
	}

	String msg(CharSequence prefix, TestIdentifier id, TestExecutionResult result) {
		StringBuilder msg = new StringBuilder(256);
		msg.append(prefix);
		indentedName(msg, id);
		if (result.getThrowable()
			.isPresent()) {
			msg.append(", reason: ");
			msg.append(result.getThrowable()
				.get());
		}
		return msg.toString();
	}

	@Override
	public void dynamicTestRegistered(TestIdentifier testIdentifier) {
		logger.debug(l -> l.debug(msg("Test registered: ", testIdentifier)));
	}

	@Override
	public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
		final Status status = testExecutionResult.getStatus();
		switch (status) {
			case ABORTED :
				logger.warn(l -> {
					if (testExecutionResult.getThrowable()
						.isPresent()) {
						l.warn(msg("Test aborted:    ", testIdentifier, testExecutionResult),
							testExecutionResult.getThrowable()
								.get());
					} else {
						l.warn(msg("Test aborted:    ", testIdentifier, testExecutionResult));
					}
				});
				break;
			case FAILED :
				logger.error(l -> {
					if (testExecutionResult.getThrowable()
						.isPresent()) {
						l.error(msg("Test failed:     ", testIdentifier, testExecutionResult),
							testExecutionResult.getThrowable()
								.get());
					} else {
						l.error(msg("Test failed:     ", testIdentifier, testExecutionResult));
					}
				});
				break;
			case SUCCESSFUL :
				logger.info(l -> l.info(msg("Test passed:     ", testIdentifier)));
				break;
		}
	}

	@Override
	public void executionSkipped(TestIdentifier testIdentifier, String reason) {
		logger.warn(l -> l.warn(msg("Test skipped:    ", testIdentifier, reason)));
	}

	@Override
	public void executionStarted(TestIdentifier testIdentifier) {
		logger.debug(l -> l.debug(msg("Test started:    ", testIdentifier)));
	}

	@Override
	public void testPlanExecutionFinished(TestPlan testPlan) {
		logger.debug("Test plan finished");
		this.testPlan = null;
	}

	@Override
	public void testPlanExecutionStarted(TestPlan testPlan) {
		logger.debug("Test plan started");
		this.testPlan = testPlan;
	}
}
