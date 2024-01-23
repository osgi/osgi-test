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

import java.io.IOException;
import java.util.Hashtable;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.osgi.service.cm.Configuration;
import org.osgi.test.common.annotation.Property;
import org.osgi.test.common.annotation.config.InjectConfiguration;
import org.osgi.test.common.annotation.config.WithConfiguration;
import org.osgi.test.junit5.cm.ConfigurationExtension;

// Use the direct extension to put the extension first in the list
@ExtendWith({
	ExternalConfigLifecycleTest.ConfigurationCreator.class, ConfigurationExtension.class
})
public class ExternalConfigLifecycleTest {

	public static final String	CONFIG							= "external.config";

	private static long			inititalChangeCount;

	private static int			updateCount	= 0;

	@BeforeAll
	static void beforeAll(@InjectConfiguration(CONFIG)
	Configuration cm) throws IOException {
		inititalChangeCount = cm.getChangeCount();
		updateCount = 0;
		checkExternalConfig(cm, updateCount);
	}

	private static void checkExternalConfig(Configuration cm, int expectedUpdates) {
		checkExternalConfig(cm, expectedUpdates, "bar");
	}

	private static void checkExternalConfig(Configuration cm, int expectedUpdates, String value) {
		assertEquals(value, cm.getProperties()
			.get("foo"));
		assertEquals(inititalChangeCount + expectedUpdates, cm.getChangeCount());
	}

	@BeforeEach
	void beforeEach(@InjectConfiguration(CONFIG)
	Configuration cm) throws IOException {
		checkExternalConfig(cm, updateCount);
	}

	@AfterEach
	void afterEach(@InjectConfiguration(CONFIG)
	Configuration cm) throws IOException {
		checkExternalConfig(cm, updateCount);
	}

	// No update should be no change
	@Test
	void testNoUpdate(@InjectConfiguration(CONFIG)
	Configuration cm) {
		checkExternalConfig(cm, updateCount);
	}

	// Updating the config - note that there will be another update to clear it
	@SuppressWarnings("serial")
	@Test
	void testUpdate(@InjectConfiguration(CONFIG)
	Configuration cm) throws IOException {
		cm.update(new Hashtable<String, Object>() {
			{
				put("foo", "foobar");
			}
		});
		checkExternalConfig(cm, ++updateCount, "foobar");
		updateCount++;
	}

	// Updating the config - note that there will be another update to clear it
	@WithConfiguration(pid = CONFIG, properties = @Property(key = "foo", value = "buzz"))
	@Test
	void testOverride(@InjectConfiguration(CONFIG)
	Configuration cm) throws IOException {
		checkExternalConfig(cm, ++updateCount, "buzz");
		updateCount++;
	}

	// Keeping the config the same should not trigger a bump
	@WithConfiguration(pid = CONFIG, properties = @Property(key = "foo", value = "bar"))
	@Test
	void testOverrideWithSame(@InjectConfiguration(CONFIG)
	Configuration cm) throws IOException {
		checkExternalConfig(cm, updateCount);
	}

	// Keeping the config the same should not trigger a bump
	@SuppressWarnings("serial")
	@WithConfiguration(pid = CONFIG, properties = @Property(key = "foo", value = "bar"))
	@Test
	void testOverrideWithSameThenChange(@InjectConfiguration(CONFIG)
	Configuration cm) throws IOException {
		checkExternalConfig(cm, updateCount);
		cm.update(new Hashtable<String, Object>() {
			{
				put("foo", "foobar");
			}
		});
		checkExternalConfig(cm, ++updateCount, "foobar");
		updateCount++;
	}

	public static class ConfigurationCreator implements BeforeAllCallback, AfterAllCallback {

		@SuppressWarnings("serial")
		@Override
		public void beforeAll(ExtensionContext context) throws Exception {
			ConfigurationExtension.configurationAdmin(context)
				.getConfiguration(CONFIG)
				.update(new Hashtable<String, Object>() {
					{
						put("foo", "bar");
					}
				});
		}

		// runs after after all so sees no config
		@Override
		public void afterAll(ExtensionContext context) throws Exception {
			ConfigurationExtension.configurationAdmin(context)
				.listConfigurations("(service.pid=" + CONFIG + ")")[0]
				.delete();
		}
	}
}
