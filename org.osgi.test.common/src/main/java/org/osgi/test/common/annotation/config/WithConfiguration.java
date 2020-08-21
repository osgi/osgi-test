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

package org.osgi.test.common.annotation.config;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

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
@Documented

public @interface WithConfiguration {

	public static final String NOT_SET = "org.osgi.test.common.annotation.config.notset";

	/**
	 * The pid of the Configuration.<br>
	 *
	 * @return The pid
	 */
	String pid();


	/**
	 * Indicate the properties, that will be updated (if set) after selecting a
	 * Configuration. If empty no update will be done.
	 *
	 * @return The Properties.
	 */
	ConfigEntry[] properties() default {
		@ConfigEntry(key = NOT_SET)
	};

}
