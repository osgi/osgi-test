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

import java.util.Arrays;
import java.util.Collection;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.service.cm.Configuration;
import org.osgi.test.assertj.dictionary.DictionaryAssert;
import org.osgi.test.common.annotation.config.ConfigEntry;
import org.osgi.test.common.annotation.config.ConfigEntry.Scalar;
import org.osgi.test.common.annotation.config.ConfigEntry.Type;
import org.osgi.test.common.annotation.config.InjectConfiguration;
import org.osgi.test.common.annotation.config.WithConfiguration;
import org.osgi.test.common.annotation.config.WithFactoryConfiguration;

@ExtendWith(ConfigurationExtension.class)
public class InjectWith {

	@Test
	public void test1(@InjectConfiguration(withConfig = @WithConfiguration(pid = "pid", properties = {//
		@ConfigEntry(key = "Scalar", type = Type.Scalar, scalar = Scalar.Integer, value = "1"), //
		@ConfigEntry(key = "PrimitiveArray", type = Type.PrimitiveArray, scalar = Scalar.Integer, value = {
			"1", "2"
		}), //
		@ConfigEntry(key = "Array", type = Type.Array, scalar = Scalar.Integer, value = {
			"1", "2"
		}), //
		@ConfigEntry(key = "Collection", type = Type.Collection, scalar = Scalar.Integer, value = {
			"1", "2"
		})//
	})) Configuration c) throws Exception {

		Assertions.assertThat(c)
			.isNotNull();

		DictionaryAssert.assertThat(c.getProperties())
			.extracting("PrimitiveArray")
			.isInstanceOf(int[].class)
			.isEqualTo(new int[] {
				1, 2
			});

		DictionaryAssert.assertThat(c.getProperties())
			.extracting("Array")
			.isInstanceOf(Integer[].class)
			.isEqualTo(new Integer[] {
				1, 2
			});

		DictionaryAssert.assertThat(c.getProperties())
			.extracting("Collection")
			.isInstanceOf(Collection.class)
			.isEqualTo(Arrays.asList(1, 2));

		DictionaryAssert.assertThat(c.getProperties())
			.extracting("Scalar")
			.isInstanceOf(Integer.class)
			.isEqualTo(1);

	}

	@Test
	public void test2(
		@InjectConfiguration(withFactoryConfig = @WithFactoryConfiguration(factoryPid = "pid2", name = "name")) Configuration c)
		throws Exception {
		Assertions.assertThat(c)
			.isNotNull();
	}

}
