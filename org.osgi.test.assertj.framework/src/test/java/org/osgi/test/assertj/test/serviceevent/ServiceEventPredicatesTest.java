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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.Dictionary;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceReference;
import org.osgi.test.assertj.serviceevent.ServiceEventPredicates;
import org.osgi.test.common.dictionary.Dictionaries;

public class ServiceEventPredicatesTest {
	ServiceReference<?> sr = Mockito.mock(ServiceReference.class);

	@Test
	void testPredicateService() throws Exception {

		SoftAssertions softly = new SoftAssertions();

		//

		Dictionary<String, Object> dict = Dictionaries.dictionaryOf(Constants.OBJECTCLASS, new String[] {
			A.class.getName()
		}, "key1", 1, "key2", 2);

		Mockito.when(sr.getPropertyKeys())
			.thenReturn(Collections.list(dict.keys())
				.toArray(new String[3]));

		Mockito.when(sr.getProperty(Mockito.any(String.class)))
			.then(new Answer<Object>() {

				@Override
				public Object answer(InvocationOnMock invocation) throws Throwable {
					String key = invocation.getArgument(0);
					return dict.get(key);
				}
			});

		ServiceEvent event = new ServiceEvent(ServiceEvent.REGISTERED, sr);

		//
		softly.assertThat(ServiceEventPredicates.type(ServiceEvent.REGISTERED)
			.test(event))
			.isTrue();

		softly.assertThat(ServiceEventPredicates.type(ServiceEvent.MODIFIED_ENDMATCH)
			.test(event))
			.isFalse();
		//
		softly.assertThat(ServiceEventPredicates.typeModified()
			.test(new ServiceEvent(ServiceEvent.MODIFIED, sr)))
			.isTrue();

		softly.assertThat(ServiceEventPredicates.typeModified()
			.test(new ServiceEvent(ServiceEvent.REGISTERED, sr)))
			.isFalse();
		//
		softly.assertThat(ServiceEventPredicates.typeModifiedEndmatch()
			.test(new ServiceEvent(ServiceEvent.MODIFIED_ENDMATCH, sr)))
			.isTrue();

		softly.assertThat(ServiceEventPredicates.typeModifiedEndmatch()
			.test(new ServiceEvent(ServiceEvent.REGISTERED, sr)))
			.isFalse(); //
		softly.assertThat(ServiceEventPredicates.typeRegistered()
			.test(new ServiceEvent(ServiceEvent.REGISTERED, sr)))
			.isTrue();

		softly.assertThat(ServiceEventPredicates.typeRegistered()
			.test(new ServiceEvent(ServiceEvent.UNREGISTERING, sr)))
			.isFalse();

		//
		softly.assertThat(ServiceEventPredicates.typeUnregistering()
			.test(new ServiceEvent(ServiceEvent.UNREGISTERING, sr)))
			.isTrue();

		softly.assertThat(ServiceEventPredicates.typeUnregistering()
			.test(new ServiceEvent(ServiceEvent.REGISTERED, sr)))
			.isFalse();

		softly.assertThat(ServiceEventPredicates.containsServiceProperties(dict)
			.test(event))
			.isTrue();

		softly.assertThat(ServiceEventPredicates.containsServiceProperties(Dictionaries.asMap(dict))
			.test(event))
			.isTrue();

		softly.assertThat(ServiceEventPredicates.containServiceProperty("key1", 1)
			.test(event))
			.isTrue();

		softly.assertThat(ServiceEventPredicates.containServiceProperty("key1", 2)
			.test(event))
			.isFalse();

		softly.assertThat(ServiceEventPredicates.containServiceProperty("key3", 3)
			.test(event))
			.isFalse();

		softly.assertThat(ServiceEventPredicates.typeRegistered(A.class)
			.test(event))
			.isTrue();

		softly.assertThat(ServiceEventPredicates.objectClass(A.class)
			.test(event))
			.isTrue();

		softly.assertThat(ServiceEventPredicates.typeModified(A.class)
			.test(event))
			.isFalse();

		softly.assertThat(ServiceEventPredicates.matches(ServiceEvent.REGISTERED, A.class, dict)
			.test(event))
			.isTrue();

		softly.assertThat(ServiceEventPredicates.matches(ServiceEvent.REGISTERED, A.class, Dictionaries.dictionaryOf())
			.test(event))
			.isTrue();

		softly.assertAll();
	}

	interface A {}

	@Test
	void serviceEvent() throws Exception {
		assertThat(ServiceEventPredicates.serviceEvent()
			.test(new ServiceEvent(ServiceEvent.MODIFIED, sr))).isTrue();

		assertThat(ServiceEventPredicates.serviceEvent()
			.test("")).isFalse();

	}

	@Test
	void test_serviceEventAnd() throws Exception {

		AtomicBoolean flag = new AtomicBoolean(false);

		Predicate<ServiceEvent> p = new Predicate<ServiceEvent>() {

			@Override
			public boolean test(ServiceEvent t) {
				flag.set(true);
				return true;
			}
		};

		assertThat(ServiceEventPredicates.serviceEventAnd(p)
			.test(new ServiceEvent(ServiceEvent.MODIFIED, sr))).isTrue();

		assertThat(flag.get()).isTrue();
	}

}
