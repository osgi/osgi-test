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

package org.osgi.test.junit5.testutils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.testkit.engine.EventType.FINISHED;

import java.lang.reflect.Modifier;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.assertj.core.api.AbstractThrowableAssert;
import org.junit.jupiter.engine.JupiterTestEngine;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.testkit.engine.EngineTestKit;
import org.junit.platform.testkit.engine.Event;

public class TestKitUtils {

	private TestKitUtils() {}

	public static void checkClass(Class<?> testClass) {
		// This is to protect against developer slip-ups that can be costly...
		if (!Modifier.isStatic(testClass.getModifiers())) {
			throw new IllegalStateException(
				"Test class is not static: "
					+ testClass);
		}
	}

	public static AbstractThrowableAssert<?, ? extends Throwable> assertThatTest(Class<?> testClass) {
		checkClass(testClass);

		Logger logger = Logger.getLogger("org.junit.jupiter");
		Level oldLevel = logger.getLevel();
		try {
			// Suppress log output while the testkit is running (see issue
			// #133).
			logger.setLevel(Level.OFF);
			Event testEvent = EngineTestKit.engine(new JupiterTestEngine())
				.selectors(selectClass(testClass))
				.execute()
				.allEvents()
				// .debug(System.err)
				.filter(event -> event.getType()
					.equals(FINISHED))
				.findAny()
				.orElseThrow(() -> new IllegalStateException("Test failed to run at all"));

			TestExecutionResult result = testEvent.getPayload(TestExecutionResult.class)
				.orElseThrow(() -> new IllegalStateException("Test result payload missing"));

			return assertThat(result.getThrowable()
				.orElse(null));
		} finally {
			// Restore the filter to what it was so that we do not interfere
			// with the parent test
			logger.setLevel(oldLevel);
		}
	}
}
