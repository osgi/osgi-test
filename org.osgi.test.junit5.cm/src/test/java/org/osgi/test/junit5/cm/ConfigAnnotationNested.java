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

import java.util.Dictionary;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.service.cm.Configuration;
import org.osgi.test.common.annotation.config.InjectConfiguration;
import org.osgi.test.common.annotation.config.WithConfiguration;
import org.osgi.test.common.dictionary.Dictionaries;

@ExtendWith(ConfigurationExtension.class)
@WithConfiguration(pid = "class")
@TestMethodOrder(OrderAnnotation.class)
public class ConfigAnnotationNested {

	@Test
	@WithConfiguration(pid = "class.method1")
	public void test_class_method1(@InjectConfiguration("class") Configuration c_class,
		@InjectConfiguration("class.method1") Configuration c_class_Method1,
		@InjectConfiguration("class.inner1") Configuration c_class_Inner1,
		@InjectConfiguration("class.inner1.method1") Configuration c_class_Inner1_Method1,
		@InjectConfiguration("class.inner1.method2") Configuration c_class_Inner1_Method2,
		@InjectConfiguration("class.inner2") Configuration c_class_Inner2,
		@InjectConfiguration("class.inner2.method1") Configuration c_class_Inner2_Method1) throws Exception {

		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(c_class)
			.isNotNull();
		softly.assertThat(c_class_Method1)
			.isNotNull();
		softly.assertThat(c_class_Inner1)
			.isNull();
		softly.assertThat(c_class_Inner1_Method1)
			.isNull();
		softly.assertThat(c_class_Inner1_Method2)
			.isNull();
		softly.assertThat(c_class_Inner2)
			.isNull();
		softly.assertThat(c_class_Inner2_Method1)
			.isNull();
		softly.assertAll();
	}

	@WithConfiguration(pid = "class.inner1")
	@Nested
	class Inner1 {
		private static final String KEY = "k";

		@Test
		@Order(1)
		@WithConfiguration(pid = "class.inner1.method1")
		public void test_class_inner1_method1(@InjectConfiguration("class") Configuration c_class,
			@InjectConfiguration("class.method1") Configuration c_class_Method1,
			@InjectConfiguration("class.inner1") Configuration c_class_Inner1,
			@InjectConfiguration("class.inner1.method1") Configuration c_class_Inner1_Method1,
			@InjectConfiguration("class.inner1.method2") Configuration c_class_Inner1_Method2,
			@InjectConfiguration("class.inner2") Configuration c_class_Inner2,
			@InjectConfiguration("class.inner2.method1") Configuration c_class_Inner2_Method1) throws Exception {

			SoftAssertions softly = new SoftAssertions();

			softly.assertThat(c_class)
				.isNotNull();
			softly.assertThat(c_class_Method1)
				.isNull();
			softly.assertThat(c_class_Inner1)
				.isNotNull();
			softly.assertThat(c_class_Inner1_Method1)
				.isNotNull();
			softly.assertThat(c_class_Inner1_Method2)
				.isNull();
			softly.assertThat(c_class_Inner2)
				.isNull();
			softly.assertThat(c_class_Inner2_Method1)
				.isNull();
			softly.assertAll();
			c_class_Inner1.update(Dictionaries.dictionaryOf(KEY, "any"));

		}

		@Test
		@Order(2)
		@WithConfiguration(pid = "class.inner1.method2")
		public void test_class_inner1_method2(@InjectConfiguration("class") Configuration c_class,
			@InjectConfiguration("class.method1") Configuration c_class_Method1,
			@InjectConfiguration("class.inner1") Configuration c_class_Inner1,
			@InjectConfiguration("class.inner1.method1") Configuration c_class_Inner1_Method1,
			@InjectConfiguration("class.inner1.method2") Configuration c_class_Inner1_Method2,
			@InjectConfiguration("class.inner2") Configuration c_class_Inner2,
			@InjectConfiguration("class.inner2.method1") Configuration c_class_Inner2_Method1) throws Exception {

			SoftAssertions softly = new SoftAssertions();

			softly.assertThat(c_class)
				.isNotNull();
			softly.assertThat(c_class_Method1)
				.isNull();
			softly.assertThat(c_class_Inner1)
				.isNotNull();
			softly.assertThat(c_class_Inner1_Method1)
				.isNull();
			softly.assertThat(c_class_Inner1_Method2)
				.isNotNull();
			softly.assertThat(c_class_Inner2)
				.isNull();
			softly.assertThat(c_class_Inner2_Method1)
				.isNull();
			Dictionary<String, Object> properties = c_class_Inner1.getProperties();
			softly.assertThat(Dictionaries.asMap(properties))
				.doesNotContainKey(KEY);
			softly.assertAll();
		}
	}

	@WithConfiguration(pid = "class.inner2")
	@Nested
	class Inner2 {

		@Test
		@WithConfiguration(pid = "class.inner2.method1")
		public void test_class_inner2_method1(@InjectConfiguration("class") Configuration c_class,
			@InjectConfiguration("class.method1") Configuration c_class_Method1,
			@InjectConfiguration("class.inner1") Configuration c_class_Inner1,
			@InjectConfiguration("class.inner1.method1") Configuration c_class_Inner1_Method1,
			@InjectConfiguration("class.inner1.method2") Configuration c_class_Inner1_Method2,
			@InjectConfiguration("class.inner2") Configuration c_class_Inner2,
			@InjectConfiguration("class.inner2.method1") Configuration c_class_Inner2_Method1) throws Exception {

			SoftAssertions softly = new SoftAssertions();

			softly.assertThat(c_class)
				.isNotNull();
			softly.assertThat(c_class_Method1)
				.isNull();
			softly.assertThat(c_class_Inner1)
				.isNull();
			softly.assertThat(c_class_Inner1_Method1)
				.isNull();
			softly.assertThat(c_class_Inner1_Method2)
				.isNull();
			softly.assertThat(c_class_Inner2)
				.isNotNull();
			softly.assertThat(c_class_Inner2_Method1)
				.isNotNull();

			softly.assertAll();
		}
	}
}
