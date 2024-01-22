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
package org.osgi.test.junit5.cm.test.lifecycle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.osgi.service.cm.Configuration;
import org.osgi.test.common.annotation.Property;
import org.osgi.test.common.annotation.config.InjectConfiguration;
import org.osgi.test.common.annotation.config.WithConfiguration;
import org.osgi.test.junit5.cm.ConfigurationExtension;

// Use the direct extension to put the extension first in the list
@ExtendWith({
	ConfigAnnotationMutationLifecycleTest.TestBeforeExtension.class, ConfigurationExtension.class,
	ConfigAnnotationMutationLifecycleTest.TestAfterExtension.class
})
@WithConfiguration(pid = ConfigAnnotationMutationLifecycleTest.CONFIG, properties = @Property(key = "location", value = "ca"))
public class ConfigAnnotationMutationLifecycleTest {

	public static final String	CONFIG							= "config";

	public static final String	CLASS_ANNOTATION				= "ca";
	public static final String	BEFORE_ALL_INJECTED				= "bai";
	public static final String	BEFORE_ALL_INJECTED_MODIFIED	= "baim";
	public static final String	BEFORE_EACH_INJECTED			= "bei";
	public static final String	BEFORE_EACH_INJECTED_MODIFIED	= "beim";
	public static final String	TEST_ANNOTATION					= "ta";
	public static final String	TEST_INJECTED					= "ti";
	public static final String	TEST_INJECTED_MODIFIED			= "tim";

	@BeforeAll
	static void beforeAll(
		@InjectConfiguration(withConfig = @WithConfiguration(pid = CONFIG, properties = @Property(key = "location", value = BEFORE_ALL_INJECTED)))
		Configuration cm) throws IOException {

		assertEquals(BEFORE_ALL_INJECTED, cm.getProperties()
			.get("location"));

		Dictionary<String, Object> props = new Hashtable<>();
		props.put("location", BEFORE_ALL_INJECTED_MODIFIED);

		cm.update(props);

	}

	@BeforeEach
	void beforeEach(
		@InjectConfiguration(withConfig = @WithConfiguration(pid = CONFIG, properties = @Property(key = "location", value = BEFORE_EACH_INJECTED)))
		Configuration cm) throws IOException {

		assertEquals(BEFORE_EACH_INJECTED, cm.getProperties()
			.get("location"));

		Dictionary<String, Object> props = new Hashtable<>();
		props.put("location", BEFORE_EACH_INJECTED_MODIFIED);

		cm.update(props);

	}

	@WithConfiguration(pid = CONFIG, properties = @Property(key = "location", value = TEST_ANNOTATION))
	@Test
	void test(
		@InjectConfiguration(withConfig = @WithConfiguration(pid = CONFIG, properties = @Property(key = "location", value = TEST_INJECTED)))
		Configuration cm) throws IOException {

		assertEquals(TEST_INJECTED, cm.getProperties()
			.get("location"));

		Dictionary<String, Object> props = new Hashtable<>();
		props.put("location", TEST_INJECTED_MODIFIED);

		cm.update(props);

	}

	@AfterEach
	void afterEach(@InjectConfiguration(CONFIG)
	Configuration cm) throws IOException {
		assertEquals(BEFORE_EACH_INJECTED_MODIFIED, cm.getProperties()
			.get("location"));
	}

	@AfterAll
	static void afterAll(@InjectConfiguration(CONFIG)
	Configuration cm) throws IOException {
		assertEquals(BEFORE_ALL_INJECTED_MODIFIED, cm.getProperties()
			.get("location"));
	}

	public static abstract class AbstractTestExtension implements BeforeAllCallback, BeforeEachCallback,
		BeforeTestExecutionCallback, AfterTestExecutionCallback, AfterEachCallback, AfterAllCallback {

		protected Object getCurrentLocation(ExtensionContext extensionContext) throws Exception {
			return ConfigurationExtension.configurationAdmin(extensionContext)
				.listConfigurations("(service.pid=config)")[0].getProperties()
					.get("location");
		}
	}

	/**
	 * The before methods of this extension run before the
	 * {@link ConfigurationExtension} and so don't see its updates. The after
	 * methods of this extension run after the {@link ConfigurationExtension}
	 * and so *do* see its updates
	 *
	 * @author timothyjward
	 */
	public static class TestBeforeExtension extends AbstractTestExtension {

		// Runs before beforeAll setup so no config
		@Override
		public void beforeAll(ExtensionContext context) throws Exception {
			assertNull(ConfigurationExtension.configurationAdmin(context)
				.listConfigurations("(service.pid=config)"));
		}

		// Runs before beforeEach setup
		@Override
		public void beforeEach(ExtensionContext context) throws Exception {
			assertEquals(BEFORE_ALL_INJECTED_MODIFIED, getCurrentLocation(context));
		}

		// Runs before beforeTestExecution so sees results of after each but not
		// test setup
		@Override
		public void beforeTestExecution(ExtensionContext context) throws Exception {
			assertEquals(BEFORE_EACH_INJECTED_MODIFIED, getCurrentLocation(context));
		}

		// Runs after afterTestExecution so sees cleaned up config
		@Override
		public void afterTestExecution(ExtensionContext context) throws Exception {
			assertEquals(BEFORE_EACH_INJECTED_MODIFIED, getCurrentLocation(context));
		}

		// runs after afterEach so sees cleaned up config
		@Override
		public void afterEach(ExtensionContext context) throws Exception {
			assertEquals(BEFORE_ALL_INJECTED_MODIFIED, getCurrentLocation(context));
		}

		// runs after after all so sees no config
		@Override
		public void afterAll(ExtensionContext context) throws Exception {
			assertNull(ConfigurationExtension.configurationAdmin(context)
				.listConfigurations("(service.pid=config)"));
		}
	}

	public static class TestAfterExtension extends AbstractTestExtension {

		// Runs after beforeAll but before execution of setup method
		@Override
		public void beforeAll(ExtensionContext context) throws Exception {
			assertEquals(CLASS_ANNOTATION, getCurrentLocation(context));
		}

		// Runs after beforeEach setup but before execution of setup method
		@Override
		public void beforeEach(ExtensionContext context) throws Exception {
			assertEquals(BEFORE_ALL_INJECTED_MODIFIED, getCurrentLocation(context));
		}

		// Runs after beforeTestExecution but before the test
		@Override
		public void beforeTestExecution(ExtensionContext context) throws Exception {
			assertEquals(TEST_ANNOTATION, getCurrentLocation(context));
		}

		// Runs before afterTestExecution so no cleanup
		@Override
		public void afterTestExecution(ExtensionContext context) throws Exception {
			assertEquals(TEST_INJECTED_MODIFIED, getCurrentLocation(context));
		}

		// runs before afterEach so no cleanup of setup method
		@Override
		public void afterEach(ExtensionContext context) throws Exception {
			assertEquals(BEFORE_EACH_INJECTED_MODIFIED, getCurrentLocation(context));
		}

		// runs before afterAll so no cleanup of setup method
		@Override
		public void afterAll(ExtensionContext context) throws Exception {
			assertEquals(BEFORE_ALL_INJECTED_MODIFIED, getCurrentLocation(context));
		}
	}
}
