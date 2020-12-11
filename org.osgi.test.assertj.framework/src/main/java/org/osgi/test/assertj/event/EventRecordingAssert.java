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

import java.util.function.Predicate;

import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceEvent;
import org.osgi.test.common.event.BundleEvents;
import org.osgi.test.common.event.EventRecording;
import org.osgi.test.common.event.FrameworkEvents;
import org.osgi.test.common.event.ServiceEvents;

public class EventRecordingAssert extends AbstractEventRecordingAssert<EventRecordingAssert, EventRecording> {

	public EventRecordingAssert(EventRecording actual, Class<EventRecordingAssert> selfType) {
		super(actual, selfType);
	}

	public static EventRecordingAssert assertThat(EventRecording actual) {
		return new EventRecordingAssert(actual, EventRecordingAssert.class);
	}

	/**
	 * @param execute - the action that will be invoked.
	 * @param predicate - Predicate that would be tested against the
	 *            ServiceEvents
	 * @param timeout - the time in that the matches must happened
	 */
	public static EventRecordingAssert assertThatServiceEvent(Runnable execute,
		Predicate<ServiceEvent> predicate, int timeout) throws Exception {
		return assertThat(execute, ServiceEvents.isServiceEventAnd(predicate), timeout);
	}

	/**
	 * @param execute - the action that will be invoked.
	 * @param predicate - Predicate that would be tested against the
	 *            BundleEvents
	 * @param timeout - the time in that the matches must happened
	 */
	public static EventRecordingAssert assertThatBundleEvent(Runnable execute, Predicate<BundleEvent> predicate,
		int timeout) throws Exception {
		return assertThat(execute, BundleEvents.isBundleEventAnd(predicate), timeout);
	}

	/**
	 * @param execute - the action that will be invoked.
	 * @param predicate - Predicate that would be tested against the
	 *            FrameworkEvents
	 * @param timeout - the time in that the matches must happened
	 */
	public static EventRecordingAssert assertThatFrameworkEvent(Runnable execute,
		Predicate<FrameworkEvent> predicate, int timeout) throws Exception {
		return assertThat(execute, FrameworkEvents.isFrameworkEventAnd(predicate), timeout);
	}

	/**
	 * @param execute - the action that will be invoked.
	 * @param predicate - Predicate that would be tested against the Events
	 * @param timeout - the time in that the matches must happened
	 */
	public static EventRecordingAssert assertThat(Runnable execute, Predicate<Object> predicate, int timeout)
		throws Exception {
		BundleContext bc = FrameworkUtil.getBundle(EventRecordingAssert.class)
			.getBundleContext();

		EventRecording awaitResult = EventRecording.recordEvents(bc, execute, predicate, timeout);
		return new EventRecordingAssert(awaitResult, EventRecordingAssert.class);
	}

	/**
	 * @param execute - the action that will be invoked.
	 * @param timeout - the time in that the matches must happened
	 */
	public static EventRecordingAssert assertThat(Runnable execute, int timeout) throws Exception {
		return assertThat(execute, (se) -> false, timeout);
	}

	/**
	 * @param execute - the action that will be invoked.
	 */
	public static EventRecordingAssert assertThat(Runnable execute) throws Exception {
		return assertThat(execute, 200);
	}
}
