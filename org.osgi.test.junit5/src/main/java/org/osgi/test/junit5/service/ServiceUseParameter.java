/*
 * Copyright (c) OSGi Alliance (2019-2020). All Rights Reserved.
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
package org.osgi.test.junit5.service;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Inject OSGi services into test classes and methods.
 * <p>
 * Example:
 *
 * <pre>
 * &#64;ExtendWith(ServiceUseExtension.class)
 * class MyTests {
 * 	// reused by all tests if static, otherwise injected per test
 * 	&#64;ServiceUseParameter
 * 	static Foo foo;
 *
 * 	// OR
 *
 * 	&#64;Test
 * 	public void otherTest(&#64;ServiceUseParameter Foo foo) {
 * 		//
 * 	}
 * }
 * </pre>
 */
@Target({
	FIELD, PARAMETER
})
@Retention(RUNTIME)
@Documented
public @interface ServiceUseParameter {
}
