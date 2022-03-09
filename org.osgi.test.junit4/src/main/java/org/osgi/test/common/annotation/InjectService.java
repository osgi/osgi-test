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

package org.osgi.test.common.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Inject OSGi services into test classes and methods.
 * <p>
 * Example:
 *
 * <pre>
 * // For JUnit5
 * &#64;ExtendWith(ServiceExtension.class)
 * class MyTests {
 * 	// For JUnit4
 * 	&#64;Rule
 * 	ServiceRule sr = new ServiceRule();
 *
 * 	&#64;InjectService
 * 	Foo foo;
 *
 * 	&#64;Test
 * 	public void test() {
 * 		// use foo
 * 	}
 * }
 * </pre>
 */
@Inherited
@Target({
	FIELD, PARAMETER
})
@Retention(RUNTIME)
@Documented
public @interface InjectService {

	static long DEFAULT_TIMEOUT = 200l;

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
	 * Indicate the number of services that are required to arrive within the
	 * specified by {@link #timeout()} before starting the test.
	 *
	 * @return The cardinality.
	 */
	int cardinality() default 1;

	/**
	 * Indicate require services must arrive within the specified timeout.
	 *
	 * @return The timeout.
	 */
	long timeout() default DEFAULT_TIMEOUT;

	/**
	 * Indicate to take the serviceClass from not from the parameter. Use
	 * `org.osgi.test.common.annotation.InjectService.AnyService` and the
	 * ParameterType or FieldType `java.lang.Object` for any Service.
	 *
	 * @return The service class.
	 */
	Class<?> service() default InjectService.class;

	public static final class AnyService {
		private AnyService() {}
	}

}
