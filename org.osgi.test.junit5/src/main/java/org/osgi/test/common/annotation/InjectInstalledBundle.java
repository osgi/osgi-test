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

import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.test.junit5.context.InstalledBundleExtension;

/**
 * Loads a Bundle from a given location and installs the Bundle.
 *
 * <pre>
 * &#64;InjectInstalledBundle
 * Bundle installedBundle;
 * </pre>
 */
@Inherited
@Target({
	FIELD, PARAMETER
})
@Retention(RUNTIME)
@ExtendWith(InstalledBundleExtension.class)
@Documented
public @interface InjectInstalledBundle {

	/**
	 * @return value that would be processed in condition to the @sourceType
	 *         bundle:<bsn> (if bsn is empty, resolves to the current bundle)
	 *         <br>
	 *         bundle:<bsn>/path/to.jar for an embedded bundle (again, if bsn is
	 *         empty, resolves to the current bundle).<br>
	 *         file:/path/to.jar
	 */
	String value();

	/**
	 * @return start if true, indicates to start the bundle
	 */
	boolean start() default false;

}
