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

import static org.mockito.Mockito.mock;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceReference;
import org.osgi.test.assertj.testutil.AbstractAssertTest;
import org.osgi.test.common.event.EventRecording;
import org.osgi.test.common.event.EventRecording.TimedEvent;

public class EventRecordingAssertTest extends AbstractAssertTest<EventRecordingAssert, EventRecording> {

	public EventRecordingAssertTest() {
		super(EventRecordingAssert::assertThat);
	}

	private EventRecording		recording;
	private Bundle				b	= mock(Bundle.class);

	private ServiceReference<?>	sr	= mock(ServiceReference.class);

	TimedEvent<FrameworkEvent>	tfe	= new TimedEvent<FrameworkEvent>(new FrameworkEvent(FrameworkEvent.INFO, b, null));
	TimedEvent<BundleEvent>		tbe	= new TimedEvent<BundleEvent>(new BundleEvent(BundleEvent.STARTED, b, b));
	TimedEvent<ServiceEvent>	tse	= new TimedEvent<ServiceEvent>(new ServiceEvent(ServiceEvent.REGISTERED, sr));

	@Test
	public void isTimedOut() throws Exception {
		recording = recordings(false, tfe);

		setActual(recording);
		assertPassing("isNot", x -> aut.isNotTimedOut(), null);
		assertFailing("is", x -> aut.isTimedOut(), null)
			.hasMessageMatching("(?si).*to be a timedOut.*but it was not.*");

		recording = recordings(true, tfe);

		setActual(recording);
		assertPassing("is", x -> aut.isTimedOut(), null);
		assertFailing("isNot", x -> aut.isNotTimedOut(), null)
			.hasMessageMatching("(?si).*not.*be a timedOut.*but it was.*");
	}

	private static EventRecording recordings(boolean timedOut, TimedEvent<?>... events) {

		return new EventRecording() {

			@Override
			public boolean isTimedOut() {
				return timedOut;
			}

			@Override
			public List<TimedEvent<?>> timedEvents() {
				return Arrays.asList(events);
			}
		};
	}

	class A {

	}
}
