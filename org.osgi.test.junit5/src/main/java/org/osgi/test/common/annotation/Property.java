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

	String key();

	String[] value() default "";

	Scalar scalar() default Scalar.String;

	Type type() default Type.Scalar;

	String systemProperty() default NOT_SET;

}
