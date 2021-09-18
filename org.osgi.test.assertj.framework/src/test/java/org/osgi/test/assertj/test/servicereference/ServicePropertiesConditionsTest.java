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

package org.osgi.test.assertj.test.servicereference;

import org.junit.jupiter.api.Test;
import org.osgi.test.assertj.servicereference.ServicePropertiesConditions;
import org.osgi.test.assertj.test.testutil.ConditionAssert;
import org.osgi.test.common.dictionary.Dictionaries;

class ServicePropertiesConditionsTest implements ConditionAssert {
	String	k1	= "k1";
	String	k2	= "k2";
	String	v1	= "v1";
	String	v2	= "v2";

	@Test
	void serviceProperties() throws Exception {
		passingHas(ServicePropertiesConditions.servicePropertiesContains(Dictionaries.dictionaryOf(k1, v1)),
			Dictionaries.dictionaryOf(k1, v1));
		passingHas(
			ServicePropertiesConditions.servicePropertiesContains(Dictionaries.asMap(Dictionaries.dictionaryOf(k1, v1))),
			Dictionaries.dictionaryOf(k1, v1));



		failingHas(ServicePropertiesConditions.servicePropertiesContains(Dictionaries.dictionaryOf(k2, v2)),
			Dictionaries.dictionaryOf(k1, v1));
		failingHas(
			ServicePropertiesConditions.servicePropertiesContains(Dictionaries.asMap(Dictionaries.dictionaryOf(k2, v2))),
			Dictionaries.dictionaryOf(k1, v1));
		failingHas(
			ServicePropertiesConditions.servicePropertiesContains(Dictionaries.asMap(Dictionaries.dictionaryOf(k2, v2))),
			Dictionaries.dictionaryOf(k1, "z"));

		passingHas(ServicePropertiesConditions.servicePropertyContains(k1, v1), Dictionaries.dictionaryOf(k1, v1));
		failingHas(ServicePropertiesConditions.servicePropertyContains(k1, "Z"), Dictionaries.dictionaryOf(k1, v1));

		passingHas(ServicePropertiesConditions.servicePropertiesMatch("(k1=v1)"), Dictionaries.dictionaryOf(k1, v1));
		failingHas(ServicePropertiesConditions.servicePropertiesMatch("(k1=v1)"), Dictionaries.dictionaryOf(k1, v2));
	}

}
