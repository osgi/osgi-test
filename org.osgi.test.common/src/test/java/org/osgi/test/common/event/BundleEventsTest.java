package org.osgi.test.common.event;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;

public class BundleEventsTest {

	@Test
	void testPredicateBundle() throws Exception {

		SoftAssertions softly = new SoftAssertions();

		//
		Bundle bundle = Mockito.mock(Bundle.class);
		BundleEvent event = new BundleEvent(BundleEvent.INSTALLED, bundle);

		//
		softly.assertThat(BundleEvents.isType(BundleEvent.INSTALLED)
			.test(event))
			.isTrue();

		softly.assertAll();
	}
}
