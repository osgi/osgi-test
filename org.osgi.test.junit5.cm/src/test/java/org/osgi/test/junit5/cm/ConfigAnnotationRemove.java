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
package org.osgi.test.junit5.cm;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.service.cm.Configuration;
import org.osgi.test.common.annotation.config.InjectConfiguration;
import org.osgi.test.common.annotation.config.WithConfiguration;
import org.osgi.test.common.annotation.config.WithFactoryConfiguration;

@ExtendWith(ConfigurationExtension.class)
@TestMethodOrder(OrderAnnotation.class)
@WithFactoryConfiguration(factoryPid = ConfigAnnotationRemove.MY_FACTORY_PID, name = ConfigAnnotationRemove.MY_FACTORY_NAME)
@WithConfiguration(pid = ConfigAnnotationRemove.MY_PID)
public class ConfigAnnotationRemove {

	static final String	MY_FACTORY_NAME	= "my-factory-name";
	static final String	MY_FACTORY_PID	= "myFactory.pid";
	static final String	MY_PID			= "my.pid";

	@Test
	@Order(1)
	public void test_remove(@InjectConfiguration(MY_PID) Configuration c) throws Exception {
		Assertions.assertThat(c)
			.isNotNull();
		c.delete();
	}

	@Test
	@Order(2)
	public void test_exist(@InjectConfiguration(MY_PID) Configuration c) throws Exception {
		Assertions.assertThat(c)
			.isNotNull();
	}

	@Test
	@Order(3)
	public void test_factory_remove(@InjectConfiguration(MY_FACTORY_PID + "~" + MY_FACTORY_NAME) Configuration c)
		throws Exception {
		Assertions.assertThat(c)
			.isNotNull();
		c.delete();

	}

	@Test
	@Order(4)
	public void test_factory_exist(@InjectConfiguration(MY_FACTORY_PID + "~" + MY_FACTORY_NAME) Configuration c)
		throws Exception {
		Assertions.assertThat(c)
			.isNotNull();
	}
}
