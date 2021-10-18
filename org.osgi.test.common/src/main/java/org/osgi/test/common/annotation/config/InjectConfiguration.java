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

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.test.common.annotation.Property;
import org.osgi.test.junit5.cm.ConfigurationExtension;

/*
 * Reads existing a Configuration from the Config-Admin and injects it into Field or Parameter
 * Inject the `org.osgi.service.cm.Configuration`, `Map`, `Dictionary` or an `Optional<Configuration>`.
 *
 * Only one of the Fields `value`, `withConfig` or `withFactoryConfig` could be used.
 */
@Inherited
@Target({
	PARAMETER, FIELD
})

@Retention(RUNTIME)
@ExtendWith(ConfigurationExtension.class)
@Documented
public @interface InjectConfiguration {

	static long DEFAULT_TIMEOUT = 200l;

	/**
	 * The pid of the Configuration.
	 *
	 * @return The pid
	 */
	String value() default Property.NOT_SET;

	/**
	 * The Configuration that would be created/updated before injection.
	 *
	 * @return The withConfiguration
	 */
	WithConfiguration withConfig() default @WithConfiguration(pid = Property.NOT_SET);

	/**
	 * The FactoryConfiguration that would be created/updated before injection.
	 *
	 * @return The withFactoryConfiguration
	 */
	WithFactoryConfiguration withFactoryConfig() default @WithFactoryConfiguration(factoryPid = Property.NOT_SET, name = Property.NOT_SET);

	/**
	 * Indicate require Configuration must arrive within the specified timeout.
	 *
	 * @return The timeout.
	 */
	long timeout() default DEFAULT_TIMEOUT;
}
