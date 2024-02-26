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

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Inherited
@Target({
	ANNOTATION_TYPE
})
@Retention(RUNTIME)
@Documented
public @interface Property {

	public static final String NOT_SET = "org.osgi.test.common.annotation.notset";
	public enum Scalar {
		String,
		Integer,
		Long,
		Float,
		Double,
		Byte,
		Short,
		Character,
		Boolean
	}

	public enum Type {
		Scalar,
		Collection,
		Array,

		PrimitiveArray
	}

	/**
	 * Indicates the source of the configuration value
	 *
	 * @since 1.2
	 */
	public enum ValueSource {
		/** Use the {@link Property#value()} directly */
		Value,
		/**
		 * Use the first {@link Property#value()} as a key in the System
		 * Properties. If the key does not exist then either:
		 * <ul>
		 * <li>Use the second {@link Property#value()} as the value</li>
		 * <li>Throw an exception indicating that there is no property for the
		 * supplied key</li>
		 * </ul>
		 */
		SystemProperty,
		/**
		 * Use the first {@link Property#value()} as a key in the Environment If
		 * the key does not exist then either:
		 * <ul>
		 * <li>Use the second {@link Property#value()} as the value</li>
		 * <li>Throw an exception indicating that there is no property for the
		 * supplied variable name</li>
		 * </ul>
		 */
		EnvironmentVariable,
		/** Use the name of the test class */
		TestClass,
		/** Use the name of the test method */
		TestMethod,
		/** Use the test unique identifier */
		TestUniqueId
	}

	/**
	 * Used to provide an argument in the {@link Property#templateArguments()}.
	 * {@link ValueArgument} values are resolved in the same way as
	 * {@link Property} values but they are always of {@link Type#Scalar}
	 *
	 * @since 1.2
	 */
	public @interface ValueArgument {
		String[] value() default "";

		Scalar scalar() default Scalar.String;

		ValueSource source() default ValueSource.Value;
	}

	String key();

	String[] value() default "";

	Scalar scalar() default Scalar.String;

	Type type() default Type.Scalar;

	/**
	 * @return the source that should be used to obtain the value
	 * @since 1.2
	 */
	ValueSource source() default ValueSource.Value;

	/**
	 * If any template arguments are set then the resolved value of this
	 * {@link Property} annotation will be used as a template in
	 * {@link String#format} with the resolved {@link ValueArgument} values used
	 * as arguments.
	 * <p>
	 * Note that any defaulting or processing according to the {@link #source()}
	 * will happen <em>before</em> the formatting is applied, so template
	 * arguments cannot be used, for example, to change the name of a system
	 * property. Conversely, the scalar conversion will happen <em>after</em>
	 * the template is applied, meaning that numeric values can be assembled
	 * from multiple template arguments.
	 *
	 * @return the arguments that should be used with the template
	 * @since 1.2
	 */
	ValueArgument[] templateArguments() default {};

}
