/*
 * Copyright (c) OSGi Alliance (2020). All Rights Reserved.
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

package org.osgi.test.assertj.event;

import static org.osgi.framework.ServiceEvent.REGISTERED;
import static org.osgi.test.assertj.serviceevent.ServiceEventConditions.containServiceProperty;
import static org.osgi.test.assertj.serviceevent.ServiceEventConditions.containsServiceProperties;
import static org.osgi.test.assertj.serviceevent.ServiceEventConditions.hasObjectClass;
import static org.osgi.test.assertj.serviceevent.ServiceEventConditions.isTypeModified;
import static org.osgi.test.assertj.serviceevent.ServiceEventConditions.isTypeModifiedEndmatch;
import static org.osgi.test.assertj.serviceevent.ServiceEventConditions.isTypeRegistered;
import static org.osgi.test.assertj.serviceevent.ServiceEventConditions.isTypeUnregistering;
import static org.osgi.test.assertj.serviceevent.ServiceEventConditions.matches;
import static org.osgi.test.common.dictionary.Dictionaries.dictionaryOf;

import org.junit.jupiter.api.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;
import org.osgi.test.common.event.ServiceEvents;

public class EventRecordingAssertIntegrationTest {

	BundleContext	bc	= FrameworkUtil.getBundle(EventRecordingAssertIntegrationTest.class)
		.getBundleContext();
	String			k1	= "key1";
	String			v1	= "value1";

	String			k2	= "key2";
	String			v2	= "value2";

	@Test
	void exampleIntegrationTest() throws Exception {

		// Setup assert
		EventRecordingAssert eAssert = EventRecordingAssert.assertThatServiceEvent(() -> {
			ServiceRegistration<A> reg = null;
			reg = bc.registerService(A.class, new A() {}, dictionaryOf(k1, v1));
			reg.setProperties(dictionaryOf(k1, v1, k2, v2));
			reg.unregister();
		}, ServiceEvents.isTypeUnregistering(A.class), 20);

		// check whether the Predicate matches or the timeout
		eAssert.isNotTimedOut();

		// get ListAsserts and check them
		eAssert.hasEventsThat()
			.isNotEmpty();

		eAssert.hasFrameworkEventsThat()
			.isEmpty();

		eAssert.hasBundleEventsThat()
			.isEmpty();

		// ListAsserts in combination with Conditions
		eAssert.hasServiceEventsThat()
			.areAtLeast(1, isTypeModified())
			.are(hasObjectClass(A.class))
			.areExactly(3, hasObjectClass(A.class))
			.areNot(isTypeModifiedEndmatch())
			.first()
			.isOfType(REGISTERED);

		eAssert.hasServiceEventsThat()
			.isNotEmpty()
			.hasSize(3);

		eAssert.hasServiceEventsThat()
			.element(0)
			.is(isTypeRegistered())
			.is(hasObjectClass(A.class))
			.is(isTypeRegistered(A.class))
			.is(containServiceProperty(k1, v1))
			.is(matches(REGISTERED, A.class, dictionaryOf(k1, v1)));

		eAssert.hasServiceEventsThat()
			.element(1)// ServiceEventAssert
			.is(isTypeModified(A.class))
			.is(containsServiceProperties(dictionaryOf(k1, v1, k2, v2)));

		// TODO: Extra PR
		// .hasServiceReferenceThat()// ServiceReferenceAssert
		// .hasServicePropertiesThat()// DictionaryAssert
		// .containsEntry(k1, v1);

		eAssert.hasServiceEventsThat()
			.element(2)
			.is(isTypeUnregistering(A.class));
	}

	@Test
	void testClone() throws Exception {

		// Setup assert
		EventRecordingAssert.assertThat(() -> {
			ServiceRegistration<A> reg = null;
			reg = bc.registerService(A.class, new A() {}, dictionaryOf(k1, v1));
			reg.setProperties(dictionaryOf(k1, v1, k2, v2));
			reg.unregister();
		})
			.hasServiceEventsThat()
			.first()
			.isNot(containServiceProperty(k2, v2));
	}

	class A {

	}
}
