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

import java.util.Dictionary;

/**
 * The generic PropertiesConverter is unable to support all forms of property
 * conversion and should not be used. Use the JUnit 5 specific
 * {@link org.osgi.test.junit5.properties.PropertiesConverter} instead.
 */
@Deprecated
public class PropertiesConverter {

	@Deprecated
	public static Dictionary<String, Object> of(Property[] entrys) {
	    return org.osgi.test.junit5.properties.PropertiesConverter.of(null, entrys);
	}

}
