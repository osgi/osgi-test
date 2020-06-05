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
