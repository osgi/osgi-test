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
package org.osgi.test.junit5.cm.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.test.assertj.dictionary.DictionaryAssert;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.annotation.Property;
import org.osgi.test.common.annotation.config.WithConfiguration;
import org.osgi.test.common.annotation.config.WithFactoryConfiguration;
import org.osgi.test.junit5.cm.ConfigUtil;
import org.osgi.test.junit5.cm.ConfigurationExtension;

@ExtendWith(ConfigurationExtension.class)
public class AnnotationsInAnnotationTest {

	private static final String	KEY							= "key";
	public static final String	FACTORY_CONFIGURATION_PID	= "my.factory.configuration.pid";
	public static final String	CONFIGURATION_PID			= "my.configuration.pid";
	public static final String	NAME						= "name";

	@ConfigSetup_1
	@ConfigSetup_2
	@WithConfiguration(pid = CONFIGURATION_PID, properties = {
		@Property(key = KEY, value = ConfigSetup_A.VALUE)
	})
	@WithFactoryConfiguration(factoryPid = FACTORY_CONFIGURATION_PID, name = NAME, properties = {
		@Property(key = KEY, value = ConfigSetup_A.VALUE)
	})
	@Retention(RetentionPolicy.RUNTIME)
	public @interface ConfigSetup_A {
		public static final String VALUE = "A";

	}

	@WithConfiguration(pid = CONFIGURATION_PID, properties = {
		@Property(key = KEY, value = ConfigSetup_B.VALUE)
	})
	@WithFactoryConfiguration(factoryPid = FACTORY_CONFIGURATION_PID, name = NAME, properties = {
		@Property(key = KEY, value = ConfigSetup_B.VALUE)
	})
	@ConfigSetup_1
	@ConfigSetup_2
	@Retention(RetentionPolicy.RUNTIME)
	public @interface ConfigSetup_B {
		public static final String VALUE = "B";

	}

	@WithConfiguration(pid = CONFIGURATION_PID, properties = {
		@Property(key = KEY, value = ConfigSetup_1.VALUE)
	})
	@WithFactoryConfiguration(factoryPid = FACTORY_CONFIGURATION_PID, name = NAME, properties = {
		@Property(key = KEY, value = ConfigSetup_1.VALUE)
	})
	@Retention(RetentionPolicy.RUNTIME)
	public @interface ConfigSetup_1 {
		public static final String VALUE = "1";

	}

	@WithConfiguration(pid = CONFIGURATION_PID, properties = {
		@Property(key = KEY, value = ConfigSetup_2.VALUE_FIRST)
	})
	@WithFactoryConfiguration(factoryPid = FACTORY_CONFIGURATION_PID, name = NAME, properties = {
		@Property(key = KEY, value = ConfigSetup_2.VALUE_FIRST)
	})
	@WithConfiguration(pid = CONFIGURATION_PID, properties = {
		@Property(key = KEY, value = ConfigSetup_2.VALUE)
	})
	@WithFactoryConfiguration(factoryPid = FACTORY_CONFIGURATION_PID, name = NAME, properties = {
		@Property(key = KEY, value = ConfigSetup_2.VALUE)
	})
	@Retention(RetentionPolicy.RUNTIME)
	public @interface ConfigSetup_2 {
		public static final String	VALUE_FIRST	= "2_FIRST";
		public static final String	VALUE		= "2";

	}

	@InjectService
	ConfigurationAdmin ca;

	@Test
	@ConfigSetup_1
	void testSimple() throws Exception {

		Configuration c = ConfigUtil.getConfigsByServicePid(ca, FACTORY_CONFIGURATION_PID + "~" + NAME);
		assertThat(c).isNotNull();
		DictionaryAssert.assertThat(c.getProperties())
			.containsEntry(KEY, ConfigSetup_1.VALUE);

		Configuration cs2 = ConfigUtil.getConfigsByServicePid(ca, CONFIGURATION_PID);
		assertThat(cs2).isNotNull();
		DictionaryAssert.assertThat(cs2.getProperties())
			.containsEntry(KEY, ConfigSetup_1.VALUE);

	}

	@Test
	@ConfigSetup_1
	@ConfigSetup_2
	void testOrderLatest() throws Exception {

		Configuration c = ConfigUtil.getConfigsByServicePid(ca, FACTORY_CONFIGURATION_PID + "~" + NAME);
		assertThat(c).isNotNull();
		DictionaryAssert.assertThat(c.getProperties())
			.containsEntry(KEY, ConfigSetup_2.VALUE);

		Configuration cs2 = ConfigUtil.getConfigsByServicePid(ca, CONFIGURATION_PID);
		assertThat(cs2).isNotNull();
		DictionaryAssert.assertThat(cs2.getProperties())
			.containsEntry(KEY, ConfigSetup_2.VALUE);

	}

	@Test
	@ConfigSetup_A
	void testOrderUpperLatest_A() throws Exception {

		Configuration c = ConfigUtil.getConfigsByServicePid(ca, FACTORY_CONFIGURATION_PID + "~" + NAME);
		assertThat(c).isNotNull();
		DictionaryAssert.assertThat(c.getProperties())
			.containsEntry(KEY, ConfigSetup_A.VALUE);

		Configuration cs2 = ConfigUtil.getConfigsByServicePid(ca, CONFIGURATION_PID);
		assertThat(cs2).isNotNull();
		DictionaryAssert.assertThat(cs2.getProperties())
			.containsEntry(KEY, ConfigSetup_A.VALUE);

	}

	@Test
	@ConfigSetup_B
	void testOrderUpperLatest_B() throws Exception {

		Configuration c = ConfigUtil.getConfigsByServicePid(ca, FACTORY_CONFIGURATION_PID + "~" + NAME);
		assertThat(c).isNotNull();
		DictionaryAssert.assertThat(c.getProperties())
			.containsEntry(KEY, ConfigSetup_2.VALUE);

		Configuration cs2 = ConfigUtil.getConfigsByServicePid(ca, CONFIGURATION_PID);
		assertThat(cs2).isNotNull();
		DictionaryAssert.assertThat(cs2.getProperties())
			.containsEntry(KEY, ConfigSetup_2.VALUE);

	}

	@Test
	@ConfigSetup_B
	@ConfigSetup_1
	void testOrderUpperLatest() throws Exception {

		Configuration c = ConfigUtil.getConfigsByServicePid(ca, FACTORY_CONFIGURATION_PID + "~" + NAME);
		assertThat(c).isNotNull();
		DictionaryAssert.assertThat(c.getProperties())
			.containsEntry(KEY, ConfigSetup_1.VALUE);

		Configuration cs2 = ConfigUtil.getConfigsByServicePid(ca, CONFIGURATION_PID);
		assertThat(cs2).isNotNull();
		DictionaryAssert.assertThat(cs2.getProperties())
			.containsEntry(KEY, ConfigSetup_1.VALUE);

	}

	@ConfigSetup_B
	@ConfigSetup_1 // is part of B but not on last position
	@Retention(RetentionPolicy.RUNTIME)
	public @interface ConfigSetup_X {

	}

	@Test
	@ConfigSetup_X
	void testOrderLatestInsideAnnotations() throws Exception {

		Configuration c = ConfigUtil.getConfigsByServicePid(ca, FACTORY_CONFIGURATION_PID + "~" + NAME);
		assertThat(c).isNotNull();
		DictionaryAssert.assertThat(c.getProperties())
			.containsEntry(KEY, ConfigSetup_1.VALUE);

		Configuration cs2 = ConfigUtil.getConfigsByServicePid(ca, CONFIGURATION_PID);
		assertThat(cs2).isNotNull();
		DictionaryAssert.assertThat(cs2.getProperties())
			.containsEntry(KEY, ConfigSetup_1.VALUE);

	}

	@ConfigSetup_1
	@ConfigSetup_Loop1
	@Retention(RetentionPolicy.RUNTIME)
	public @interface ConfigSetup_Loop1 {

	}

	@Test
	@ConfigSetup_Loop1
	void testLoopInAnn1() throws Exception {

		Configuration c = ConfigUtil.getConfigsByServicePid(ca, FACTORY_CONFIGURATION_PID + "~" + NAME);
		assertThat(c).isNotNull();
		DictionaryAssert.assertThat(c.getProperties())
			.containsEntry(KEY, ConfigSetup_1.VALUE);

		Configuration cs2 = ConfigUtil.getConfigsByServicePid(ca, CONFIGURATION_PID);
		assertThat(cs2).isNotNull();
		DictionaryAssert.assertThat(cs2.getProperties())
			.containsEntry(KEY, ConfigSetup_1.VALUE);

	}

	@Test
	@ConfigSetup_Loop2
	void testLoopInAnn2() throws Exception {

		Configuration c = ConfigUtil.getConfigsByServicePid(ca, FACTORY_CONFIGURATION_PID + "~" + NAME);
		assertThat(c).isNotNull();
		DictionaryAssert.assertThat(c.getProperties())
			.containsEntry(KEY, ConfigSetup_1.VALUE);

		Configuration cs2 = ConfigUtil.getConfigsByServicePid(ca, CONFIGURATION_PID);
		assertThat(cs2).isNotNull();
		DictionaryAssert.assertThat(cs2.getProperties())
			.containsEntry(KEY, ConfigSetup_1.VALUE);

	}

	@ConfigSetup_Loop2
	@ConfigSetup_1
	@Retention(RetentionPolicy.RUNTIME)
	public @interface ConfigSetup_Loop2 {

	}

	@Test
	@ConfigSetup_Loop4
	void testLoopInAnn4() throws Exception {

		Configuration c = ConfigUtil.getConfigsByServicePid(ca, FACTORY_CONFIGURATION_PID + "~" + NAME);
		assertThat(c).isNotNull();
		DictionaryAssert.assertThat(c.getProperties())
			.containsEntry(KEY, ConfigSetup_1.VALUE);

		Configuration cs2 = ConfigUtil.getConfigsByServicePid(ca, CONFIGURATION_PID);
		assertThat(cs2).isNotNull();
		DictionaryAssert.assertThat(cs2.getProperties())
			.containsEntry(KEY, ConfigSetup_1.VALUE);

	}

	@Test
	@ConfigSetup_Loop5
	void testLoopInAnn5() throws Exception {

		Configuration c = ConfigUtil.getConfigsByServicePid(ca, FACTORY_CONFIGURATION_PID + "~" + NAME);
		assertThat(c).isNotNull();
		DictionaryAssert.assertThat(c.getProperties())
			.containsEntry(KEY, ConfigSetup_1.VALUE);

		Configuration cs2 = ConfigUtil.getConfigsByServicePid(ca, CONFIGURATION_PID);
		assertThat(cs2).isNotNull();
		DictionaryAssert.assertThat(cs2.getProperties())
			.containsEntry(KEY, ConfigSetup_1.VALUE);

	}

	@Test
	@ConfigSetup_Loop6
	void testLoopInAnn6() throws Exception {

		Configuration c = ConfigUtil.getConfigsByServicePid(ca, FACTORY_CONFIGURATION_PID + "~" + NAME);
		assertThat(c).isNotNull();
		DictionaryAssert.assertThat(c.getProperties())
			.containsEntry(KEY, ConfigSetup_1.VALUE);

		Configuration cs2 = ConfigUtil.getConfigsByServicePid(ca, CONFIGURATION_PID);
		assertThat(cs2).isNotNull();
		DictionaryAssert.assertThat(cs2.getProperties())
			.containsEntry(KEY, ConfigSetup_1.VALUE);

	}

	@ConfigSetup_Loop5
	@Retention(RetentionPolicy.RUNTIME)
	public @interface ConfigSetup_Loop4 {

	}

	@ConfigSetup_Loop6
	@Retention(RetentionPolicy.RUNTIME)
	public @interface ConfigSetup_Loop5 {

	}

	@Retention(RetentionPolicy.RUNTIME)
	@ConfigSetup_Loop4
	@ConfigSetup_1
	public @interface ConfigSetup_Loop6 {

	}
}
