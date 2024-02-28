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

package org.osgi.test.common.annotation.config;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.service.cm.Configuration;
import org.osgi.test.common.annotation.Property;
import org.osgi.test.junit5.cm.ConfigurationExtension;

/*
 * Configure the Config-Admin using the annotation `@WithConfiguration`
 * Get or Create an new Configuration Object.
 *
 */
@Inherited
@Target({
	TYPE, METHOD
})
@Repeatable(WithConfigurations.class)
@Retention(RUNTIME)
@ExtendWith(ConfigurationExtension.class)
@Documented
public @interface WithConfiguration {

	/**
	 * The pid of the Configuration.<br>
	 *
	 * @return The pid
	 */
	String pid();

	/**
	 * The location of the Configuration.<br>
	 *
	 * @return The location
	 */
	String location() default (Property.NOT_SET);

	/**
	 * Indicate the properties that will be used to update the defined
	 * configuration.
	 * <p>
	 * When this annotation is used with
	 * {@link InjectConfiguration#withConfig()} then leaving the properties
	 * unset will result in the injection of a configuration that <em>has
	 * not</em> had {@link Configuration#update(java.util.Dictionary)} called.
	 * <p>
	 * When used as a direct annotation leaving the properties unset has the
	 * same effect as an empty array, and the configuration will be updated with
	 * empty properties.
	 *
	 * @return The Properties.
	 */
	Property[] properties() default {
		@Property(key = Property.NOT_SET)
	};

}
