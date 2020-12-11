package org.osgi.test.common.event;

import java.util.Collections;
import java.util.Dictionary;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceReference;
import org.osgi.test.common.dictionary.Dictionaries;

public class ServiceEventsTest {

	@Test
	void testPredicateService() throws Exception {

		SoftAssertions softly = new SoftAssertions();

		//

		Dictionary<String, Object> dict = Dictionaries.dictionaryOf(Constants.OBJECTCLASS, new String[] {
			A.class.getName()
		}, "key1", 1, "key2", 2);

		ServiceReference<?> sr = Mockito.mock(ServiceReference.class);
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
		softly.assertThat(ServiceEvents.isType(ServiceEvent.REGISTERED)
			.test(event))
			.isTrue();

		softly.assertThat(ServiceEvents.containsServiceProperties(dict)
			.test(event))
			.isTrue();

		softly.assertThat(ServiceEvents.containsServiceProperties(Dictionaries.asMap(dict))
			.test(event))
			.isTrue();

		softly.assertThat(ServiceEvents.containServiceProperty("key1", 1)
			.test(event))
			.isTrue();

		softly.assertThat(ServiceEvents.containServiceProperty("key1", 2)
			.test(event))
			.isFalse();

		softly.assertThat(ServiceEvents.containServiceProperty("key3", 3)
			.test(event))
			.isFalse();

		softly.assertThat(ServiceEvents.isTypeRegistered(A.class)
			.test(event))
			.isTrue();

		softly.assertThat(ServiceEvents.hasObjectClass(A.class)
			.test(event))
			.isTrue();

		softly.assertThat(ServiceEvents.isTypeModified(A.class)
			.test(event))
			.isFalse();

		softly.assertThat(ServiceEvents.matches(ServiceEvent.REGISTERED, A.class, dict)
			.test(event))
			.isTrue();

		softly.assertThat(ServiceEvents.matches(ServiceEvent.REGISTERED, A.class, Dictionaries.dictionaryOf())
			.test(event))
			.isTrue();

		softly.assertAll();
	}

	interface A {}
}
