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

package org.osgi.test.junit5.context;

import static org.junit.jupiter.api.DynamicContainer.dynamicContainer;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.function.Executable;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.testkit.engine.Event;

/**
 * Transforms the output of an EngineTestKit set of events into a dynamic node
 * hierarchy.
 */
class DynamicNodeGenerator {
	final Map<TestDescriptor, Event> eventMap;

	DynamicNodeGenerator(Map<TestDescriptor, Event> eventMap) {
		this.eventMap = eventMap;
	}

	DynamicNode toNode(TestDescriptor descriptor) {
		String description = descriptor.getDisplayName();
		TestExecutionResult result = eventMap.get(descriptor)
			.getRequiredPayload(TestExecutionResult.class);
		Executable ex = result.getThrowable()
			.map(throwable -> (Executable) () -> {
				throw throwable;
			})
			.orElse(() -> {});
		if (!descriptor.isContainer()) {
			return dynamicTest(description, ex);
		}

		return dynamicContainer(description.replace("MultiLevelCleanupTest", "Class methods"),
			Stream.concat(Stream.of(dynamicTest("container", ex)), descriptor.getChildren()
				.stream()
				.map(this::toNode)));
	}
}
