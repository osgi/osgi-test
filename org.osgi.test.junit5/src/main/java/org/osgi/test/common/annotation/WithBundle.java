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

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.test.junit5.framework.FrameworkExtension;

/**
 * Enables the OSGi Service Component Runtime inside an embedded Framework
 * Example:
 *
 * <pre>
 * &#64;WithBundle("my.bundle.under.test")
 * class MyTests {
 *
 * }
 * </pre>
 */
@Inherited
@Target({
	ElementType.TYPE
})
@Retention(RUNTIME)
@ExtendWith(FrameworkExtension.class)
@Documented
@Repeatable(WithBundles.class)
public @interface WithBundle {
	/**
	 * @return the name of the bundle to include in the embedded framework
	 */
	String value();

	/**
	 * @return <code>true</code> if the bundle should be started or
	 *         <code>false</code> otherwise
	 */
	boolean start() default false;

	/**
	 * @return <code>true</code> if the bundle should use an isolated
	 *         classloader or <code>false</code> otherwise
	 */
	boolean isolated() default false;
}
