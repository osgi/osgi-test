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

package org.osgi.test.assertj.test.serviceevent;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.assertj.core.api.Condition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceReference;
import org.osgi.test.assertj.serviceevent.ServiceEventConditions;
import org.osgi.test.assertj.servicereference.ServiceReferenceConditions;
import org.osgi.test.assertj.test.testutil.ConditionAssert;
import org.osgi.test.common.bitmaps.ServiceEventType;

class ServiceEventConditionsTest implements ConditionAssert {

	@SuppressWarnings("rawtypes")
	ServiceReference	otherServiceReference;

	ServiceEvent		serviceEvent;

	@SuppressWarnings("rawtypes")
	ServiceReference	serviceReference;

	@BeforeEach
	private void beforEach() {
		serviceEvent = mock(ServiceEvent.class, "theServiceEvent");
		serviceReference = mock(ServiceReference.class, "serviceReference");
		otherServiceReference = mock(ServiceReference.class, "otherServiceReference");

	}

	@SuppressWarnings("unchecked")
	@Test
	void serviceReferenceEquals() throws Exception {

		when(serviceEvent.getServiceReference()).thenReturn(serviceReference);
		passingHas(ServiceEventConditions.serviceReferenceEquals(serviceReference), serviceEvent);

		failingHas(ServiceEventConditions.serviceReferenceEquals(otherServiceReference), serviceEvent,
			"serviceReference equals");
	}

	@SuppressWarnings("unchecked")
	@Test
	void serviceReferenceHas() throws Exception {
		Condition<ServiceReference<?>> c = ServiceReferenceConditions.isEqualsTo(mock(ServiceReference.class));

		when(serviceEvent.getServiceReference()).thenReturn(serviceReference);
		failingHas(ServiceEventConditions.serviceReferenceHas(c), serviceEvent, "serviceReference equals");

		c = ServiceReferenceConditions.isEqualsTo(serviceReference);
		passingHas(ServiceEventConditions.serviceReferenceHas(c), serviceEvent);

	}

	@SuppressWarnings("unchecked")
	@Test
	void serviceReferenceIsNotNull() throws Exception {

		failingHas(ServiceEventConditions.serviceReferenceIsNotNull(), serviceEvent, "not.*serviceReference is <null>");

		when(serviceEvent.getServiceReference()).thenReturn(serviceReference);
		passingHas(ServiceEventConditions.serviceReferenceIsNotNull(), serviceEvent);
	}

	@SuppressWarnings("unchecked")
	@Test
	void serviceReferenceIsNull() throws Exception {

		passingHas(ServiceEventConditions.serviceReferenceIsNull(), serviceEvent);

		when(serviceEvent.getServiceReference()).thenReturn(serviceReference);
		failingHas(ServiceEventConditions.serviceReferenceIsNull(), serviceEvent, "serviceReference is <null>");
	}

	@Test
	void type() throws Exception {

		when(serviceEvent.getServiceReference()).thenReturn(serviceReference);
		when(serviceReference.getProperty("objectClass")).thenReturn(new String[] {
			A.class.getName()
		});
		when(serviceReference.getProperty("k")).thenReturn("v");
		when(serviceReference.getPropertyKeys()).thenReturn(new String[] {
			"k", "objectClass"
		});

		when(serviceEvent.getType()).thenReturn(ServiceEvent.MODIFIED);
		passingHas(ServiceEventConditions.type(ServiceEvent.MODIFIED), serviceEvent);

		passingHas(ServiceEventConditions.matches(ServiceEvent.MODIFIED, A.class), serviceEvent);
		failingHas(ServiceEventConditions.matches(ServiceEvent.UNREGISTERING, A.class), serviceEvent);
		failingHas(ServiceEventConditions.matches(ServiceEvent.MODIFIED, B.class), serviceEvent);

		passingHas(ServiceEventConditions.matches(ServiceEvent.MODIFIED, "(k=v)"), serviceEvent);
		failingHas(ServiceEventConditions.matches(ServiceEvent.UNREGISTERING, "(k=v)"), serviceEvent);
		failingHas(ServiceEventConditions.matches(ServiceEvent.MODIFIED, "(k=fail)"), serviceEvent);

		Map<String, Object> map = new HashMap<>();
		map.put("k", "v");
		passingHas(ServiceEventConditions.matches(ServiceEvent.MODIFIED, A.class, map), serviceEvent);
		failingHas(ServiceEventConditions.matches(ServiceEvent.UNREGISTERING, A.class, map), serviceEvent);
		failingHas(ServiceEventConditions.matches(ServiceEvent.MODIFIED, B.class, map), serviceEvent);

		map.put("kk", "vv");
		failingHas(ServiceEventConditions.matches(ServiceEvent.MODIFIED, A.class, map), serviceEvent);

		when(serviceEvent.getType()).thenReturn(ServiceEvent.MODIFIED_ENDMATCH);
		failingHas(ServiceEventConditions.type(ServiceEvent.MODIFIED), serviceEvent, "type matches mask <%s>",
			ServiceEventType.BITMAP.maskToString(ServiceEvent.MODIFIED));

	}

	class A {

	}

	class B {

	}
}
