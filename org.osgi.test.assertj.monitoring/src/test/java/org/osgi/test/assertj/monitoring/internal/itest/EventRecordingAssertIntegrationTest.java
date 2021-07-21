package org.osgi.test.assertj.monitoring.internal.itest;

import org.junit.jupiter.api.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceRegistration;
import org.osgi.test.assertj.monitoring.MonitoringAssertion;
import org.osgi.test.assertj.monitoring.MonitoringAssertionResult;
import org.osgi.test.common.dictionary.Dictionaries;

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

		MonitoringAssertionResult assertionResult = MonitoringAssertion.executeAndObserve(() -> {
			ServiceRegistration<A> reg = null;
			reg = bc.registerService(A.class, new A() {}, Dictionaries.dictionaryOf(k1, v1));
			reg.setProperties(Dictionaries.dictionaryOf(k1, v1, k2, v2));
			reg.unregister();
		})
			.untilServiceEvent((e) -> e.getType() == ServiceEvent.UNREGISTERING)
			.assertWithTimeoutThat(1000);

		// check whether the Predicate matches or the timeout
		assertionResult.isNotTimedOut();

		// get ListAsserts and check them
		assertionResult.hasEventsThat()
			.isNotEmpty();

		assertionResult.hasTimedEventsThat()
			.isNotEmpty();

		assertionResult.hasFrameworkEventsThat()
			.isEmpty();

		assertionResult.hasTimedFrameworkEventsThat()
			.isEmpty();

		assertionResult.hasBundleEventsThat()
			.isEmpty();

		assertionResult.hasTimedBundleEventsThat()
			.isEmpty();

		// ListAsserts in combination with Conditions
		assertionResult.hasServiceEventsThat()
			.isNotEmpty()
			.hasSize(3);

		assertionResult.hasServiceEventsThat()
			.element(0)
			.isNotNull();

		assertionResult.hasTimedServiceEventsThat()
			.isNotEmpty()
			.hasSize(3);

		assertionResult.hasServiceEventsThat()
			.first()
			.isOfType(ServiceEvent.REGISTERED);

		assertionResult.hasServiceEventsThat()
			.element(1)
			.isOfType(ServiceEvent.MODIFIED);

		assertionResult.hasServiceEventsThat()
			.element(2)
			.isOfType(ServiceEvent.UNREGISTERING);
	}

	class A {}
}
