/*
 * Copyright (c) OSGi Alliance (2020). All Rights Reserved.
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
package org.osgi.test.junit5.cm;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.test.assertj.dictionary.DictionaryAssert;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.annotation.config.ConfigEntry;
import org.osgi.test.common.annotation.config.InjectConfiguration;
import org.osgi.test.common.annotation.config.WithConfiguration;
import org.osgi.test.common.annotation.config.WithFactoryConfiguration;
import org.osgi.test.junit5.service.ServiceExtension;

@ExtendWith({
	ConfigurationExtension.class, ServiceExtension.class
})
@WithConfiguration(pid = ConfigAnnotationTest.NONSTATIC_CONFIGURATION_PID)
public class ConfigAnnotationTest {

	public static final String	FACTORY_CONFIGURATION_PID	= "my.factory.configuration.pid";
	public static final String	NONSTATIC_CONFIGURATION_PID	= "nonstatic.configuration.pid";

	@InjectService
	ConfigurationAdmin			ca;



	// START TESTS
	@InjectConfiguration(NONSTATIC_CONFIGURATION_PID)
	Configuration nonStaticConfiguration;

	@Test
	public void testFieldConfiguration() throws Exception {

		Configuration cs = ConfigUtil.getConfigsByServicePid(ca, NONSTATIC_CONFIGURATION_PID);

		assertThat(cs).isEqualTo(nonStaticConfiguration);

		DictionaryAssert.assertThat(cs.getProperties())
			.containsExactlyInAnyOrderEntriesOf(nonStaticConfiguration.getProperties());

	}

	static final String PARAM_PID = "param.pid";

	@Test
	@WithConfiguration(pid = PARAM_PID, properties = {
		@ConfigEntry(key = "bar", value = "foo")
	})
	public void testParameterConfiguration(@InjectService ConfigurationAdmin ca,
		@InjectConfiguration(PARAM_PID) Configuration configuration) throws Exception {

		Configuration cs = ConfigUtil.getConfigsByServicePid(ca, PARAM_PID);
		assertThat(cs).isEqualTo(configuration);

		DictionaryAssert.assertThat(cs.getProperties())
			.containsExactlyInAnyOrderEntriesOf(configuration.getProperties());

		DictionaryAssert.assertThat(configuration.getProperties())
			.doesNotContainKey("foo")
			.containsEntry("bar", "foo");

	}

	@Test
	@WithConfiguration(pid = PARAM_PID, properties = {
		@ConfigEntry(key = "foo", value = "bar")
	})
	public void testParameterConfiguration2(@InjectConfiguration(PARAM_PID) Configuration configuration)
		throws Exception {

		Configuration cs = ConfigUtil.getConfigsByServicePid(ca, PARAM_PID);
		assertThat(cs).isEqualTo(configuration);

		DictionaryAssert.assertThat(cs.getProperties())
			.containsExactlyInAnyOrderEntriesOf(configuration.getProperties());

		DictionaryAssert.assertThat(configuration.getProperties())
			.doesNotContainKey("bar")
			.containsEntry("foo", "bar");

	}

	@Test
	@WithConfiguration(pid = PARAM_PID)
	public void testParameterConfiguration3(@InjectConfiguration(PARAM_PID) Configuration configuration)
		throws Exception {

		Configuration cs = ConfigUtil.getConfigsByServicePid(ca, PARAM_PID);

		DictionaryAssert.assertThat(configuration.getProperties())
			.doesNotContainKey("foo")
			.doesNotContainKey("bar");

	}

	static final String METHOD_PID = "method.pid";

	@Test
	@WithConfiguration(pid = METHOD_PID, properties = {
		@ConfigEntry(key = "foo", value = "bar")
	})
	public void testMethodConfiguration() throws Exception {

		Configuration cs = ConfigUtil.getConfigsByServicePid(ca, METHOD_PID);
		assertThat(cs).isNotNull();

		DictionaryAssert.assertThat(cs.getProperties())
			.containsEntry("foo", "bar");
	}

	@Test
	@WithConfiguration(pid = FACTORY_CONFIGURATION_PID + "~" + "factory.name", properties = {
		@ConfigEntry(key = "foo", value = "bar")
	})
	public void testMethodConfigurationFactory() throws Exception {

		Configuration cs = ConfigUtil.getConfigsByServicePid(ca, FACTORY_CONFIGURATION_PID + "~factory.name");
		assertThat(cs).isNotNull();

		DictionaryAssert.assertThat(cs.getProperties())
			.containsEntry("foo", "bar");

	}

	@Test
	@WithFactoryConfiguration(factoryPid = FACTORY_CONFIGURATION_PID, name = "factory.name2", properties = {
		@ConfigEntry(key = "foo", value = "bar")
	})
	public void testMethodConfigurationFactoryCreate() throws Exception {

		Configuration cs = ConfigUtil.getConfigsByServicePid(ca, FACTORY_CONFIGURATION_PID + "~factory.name2");
		assertThat(cs).isNotNull();

		DictionaryAssert.assertThat(cs.getProperties())
			.containsEntry("foo", "bar");

	}

}
