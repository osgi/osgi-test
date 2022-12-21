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
package org.osgi.test.junit5.service;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.params.provider.ArgumentsSource;

@Target({
	ElementType.ANNOTATION_TYPE, ElementType.METHOD
})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ArgumentsSource(ServiceArgumentsProvider.class)
public @interface ServiceSource {

	static long DEFAULT_TIMEOUT = 200l;

	Class<?> serviceType() default Object.class;

	/**
	 * Filter string used to target more specific services using the
	 * {@code String.format} pattern. Must use valid OSGi filter syntax.
	 *
	 * @return The filter string.
	 */
	String filter() default "";

	/**
	 * Optional arguments to the format string provided by {@link #filter()}.
	 *
	 * @return The filter arguments.
	 */
	String[] filterArguments() default {};

	/**
	 * Indicate require services must arrive within the specified timeout.
	 *
	 * @return The timeout.
	 */
	long timeout() default DEFAULT_TIMEOUT;

	/**
	 * Indicate the number of services that are required to arrive within the
	 * specified {@link #timeout()} before starting the test.
	 * The default value is 1.
	 *
	 * @return The cardinality.
	 * @since 1.2
	 */
	int cardinality() default 1;
}
