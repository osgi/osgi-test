package org.osgi.test.assertj.monitoring.internal.itest;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceRegistration;
import org.osgi.test.assertj.NotPartOfPR.Conditions.ServiceEventConditions;
import org.osgi.test.assertj.monitoring.MonitoringAssertion;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.dictionary.Dictionaries;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;

@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
public class ExampleITest {

	@InjectBundleContext
	BundleContext bc;

	@Test
	public void example1() {
		MonitoringAssertion.executeAndObserve(() -> {

			for (int i = 0; i < 5; i++) {
				bc.registerService(Serializable.class, "" + i, null);
				System.out.println("reg" + i);
				Thread.sleep(100);
			}

		})
			.untilNoMoreServiceEventWithin(10l)// main stop-condition criteria
			.assertWithTimeoutThat(3000)// timeout to granite a stop
			.isNotTimedOut()// check that NOT timed-out
			.hasAtLeastOneServiceEventRegisteredWith(Serializable.class);

	}

	@Test
	public void example2() throws InvalidSyntaxException {

		MonitoringAssertion.executeAndObserve(() -> {

			ServiceRegistration<A> sr = bc.registerService(A.class, new A() {},
				Dictionaries.dictionaryOf("k", "v0"));

			sr.setProperties(Dictionaries.dictionaryOf("k", "v1"));
			sr.setProperties(Dictionaries.dictionaryOf("k", "v2"));
			sr.unregister();

		})
			.untilNoMoreServiceEventWithin(100l)// main stop-condition criteria
			.assertWithTimeoutThat(3000)// timeout to granite a stop
			.hasNoThrowable()// not exception thrown while executed
			.isNotTimedOut()// check that NOT timed-out
			.hasServiceEventsInOrder(//
				listOf(ServiceEventConditions.matches(ServiceEvent.REGISTERED, "(k=v0)"), //
					ServiceEventConditions.typeModifiedAndObjectClass(A.class), //
					ServiceEventConditions.typeUnregisteringAndObjectClass(A.class)))//
			.hasServiceEventsInExactOrder(//
				listOf(ServiceEventConditions.typeRegisteredWith(A.class, mapOf("k", "v0")), //
					ServiceEventConditions.typeModifiedAndObjectClass(A.class), //
					ServiceEventConditions.typeModifiedAndObjectClass(A.class), //
					ServiceEventConditions.matches(ServiceEvent.UNREGISTERING, A.class, mapOf("k", "v2"))))//
			.hasAtLeastNServiceEventModifiedWith(2, A.class)//
			.hasAtMostNServiceEventModifiedWith(2, A.class)//
			.hasExactlyNServiceEventModifiedWith(2, A.class)//
			.hasExactlyOneServiceEventRegisteredWith(A.class)//
			.hasAtLeastOneServiceEventModifiedWith(A.class)//
			.hasAtLeastOneServiceEventUnregisteringWith(A.class)//
			.hasAtLeastOneServiceEventWith(ServiceEvent.REGISTERED, "(k=v0)")//
			.hasAtLeastOneServiceEventRegisteredWith(A.class, mapOf("k", "v0"))//
			.hasAtLeastOneServiceEventModifiedWith(A.class, mapOf("k", "v1"))//
			.hasAtLeastOneServiceEventUnregisteringWith(A.class, mapOf("k", "v2"))//
			.hasAtLeastOneServiceEventWith(ServiceEvent.REGISTERED, A.class)

			.hasNoServiceEventWith(ServiceEvent.REGISTERED, "(k=v4)");
		// hasServiceEvents;
	}

	private List<Condition<ServiceEvent>> listOf(Condition<ServiceEvent> e1, Condition<ServiceEvent> e2,
		Condition<ServiceEvent> e3) {
		List<Condition<ServiceEvent>> list = new ArrayList<>();
		list.add(e1);
		list.add(e2);
		list.add(e3);
		return list;
	}

	private List<Condition<ServiceEvent>> listOf(Condition<ServiceEvent> e1, Condition<ServiceEvent> e2,
		Condition<ServiceEvent> e3, Condition<ServiceEvent> e4) {
		List<Condition<ServiceEvent>> list = new ArrayList<>();
		list.add(e1);
		list.add(e2);
		list.add(e3);
		list.add(e4);
		return list;
	}

	private Map<String, Object> mapOf(String string, Object object) {
		HashMap<String, Object> map = new HashMap<>();
		map.put(string, object);
		return map;
	}

	class A {}

}
