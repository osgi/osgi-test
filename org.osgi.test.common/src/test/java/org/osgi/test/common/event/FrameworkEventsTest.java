package org.osgi.test.common.event;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkEvent;

public class FrameworkEventsTest {

	@Test
	void testPredicateFramework() throws Exception {

		SoftAssertions softly = new SoftAssertions();

		//
		Bundle bundle = Mockito.mock(Bundle.class);
		FrameworkEvent event = new FrameworkEvent(FrameworkEvent.STARTLEVEL_CHANGED, bundle, null);

		//
		softly.assertThat(FrameworkEvents.isType(FrameworkEvent.STARTLEVEL_CHANGED)
			.test(event))
			.isTrue();

		softly.assertAll();
	}
}
