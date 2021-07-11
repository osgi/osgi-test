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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.test.assertj.servicereference.ServiceReferenceConditions;
import org.osgi.test.assertj.test.testutil.ConditionAssert;

class ServiceReferenceConditionsTest implements ConditionAssert {

	class A {}

	@SuppressWarnings("rawtypes")
	ServiceReference	otherServiceReference;

	@SuppressWarnings("rawtypes")
	ServiceReference	serviceReference;

	@BeforeEach
	private void beforEach() {
		serviceReference = mock(ServiceReference.class, "serviceReference");
		otherServiceReference = mock(ServiceReference.class, "otherServiceReference");
	}

	@SuppressWarnings("unchecked")
	@Test
	void serviceReferenceIsNotNull() throws Exception {

		when(serviceReference.getProperty(Constants.OBJECTCLASS)).thenReturn(new String[] {
			A.class.getName()
		});

		passingHas(ServiceReferenceConditions.objectClass(A.class), serviceReference);
		failingHas(ServiceReferenceConditions.objectClass(A.class), otherServiceReference)
			.hasMessageMatching((regex_startWith_Expecting + "has Objectclass .*"));

	}
}
