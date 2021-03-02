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

import org.osgi.test.common.install.BundleInstaller;

/**
 * Inject {@link BundleInstaller} into test classes and methods.
 * <p>
 * The {@link BundleInstaller} implementation provided by this rule simplifies
 * installation of embedded bundles.
 * <p>
 * Example:
 *
 * <pre>
 * // For JUnit5
 * &#64;ExtendWith(BundleContextExtension.class)
 * class MyTests {
 * 	// For JUnit4
 * 	&#64;Rule
 * 	BundleContextRule bcr = new BundleContextRule();
 *
 * 	&#64;InjectBundleInstaller
 * 	BundleInstaller bundleInstaller;
 *
 * 	&#64;Test
 * 	public void test() {
 * 		// use bundleInstaller
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
public @interface InjectBundleInstaller {}
