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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.ClassBasedNavigableListAssert;
import org.assertj.core.api.ListAssert;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.ServiceEvent;
import org.osgi.test.assertj.bundleevent.BundleEventAssert;
import org.osgi.test.assertj.frameworkevent.FrameworkEventAssert;
import org.osgi.test.assertj.serviceevent.ServiceEventAssert;
import org.osgi.test.common.event.EventRecording;
import org.osgi.test.common.event.EventRecording.TimedEvent;

public abstract class AbstractEventRecordingAssert<SELF extends AbstractEventRecordingAssert<SELF, ACTUAL>, ACTUAL extends EventRecording>
	extends AbstractAssert<SELF, ACTUAL> {

	protected AbstractEventRecordingAssert(ACTUAL actual, Class<SELF> selfType) {
		super(actual, selfType);
	}

	public SELF isTimedOut() {
		isNotNull();
		if (!actual.isTimedOut()) {
			throw failure("%nExpecting%n  <%s>%nto be a timedOut, but it was not", actual);
		}
		return myself;
	}

	public SELF isNotTimedOut() {
		isNotNull();
		if (actual.isTimedOut()) {
			throw failure("%nExpecting%n  <%s>%nto not be a timedOut, but it was", actual);
		}
		return myself;
	}

	public <T> ListAssert<T> hasEventsThat(Class<T> clazz) {
		isNotNull();
		List<T> list = actual.events(clazz);
		return new ListAssert<T>(list);
	}

	public <T> ListAssert<TimedEvent<T>> hasTimedEventsThat(Class<T> clazz) {
		isNotNull();
		List<TimedEvent<T>> list = actual.timedEvents(clazz);
		return new ListAssert<TimedEvent<T>>(list);
	}

	public ListAssert<?> hasEventsThat() {
		return hasEventsThat(Object.class);
	}

	public ListAssert<TimedEvent<Object>> hasTimedEventsThat() {
		return hasTimedEventsThat(Object.class);
	}

	public ClassBasedNavigableListAssert<?, List<? extends ServiceEvent>, ServiceEvent, ServiceEventAssert> hasServiceEventsThat() {
		isNotNull();
		List<ServiceEvent> list = actual.events(ServiceEvent.class);
		return assertThat(list, ServiceEventAssert.class);
	}

	public ListAssert<TimedEvent<ServiceEvent>> hasTimedServiceEventsThat() {
		return hasTimedEventsThat(ServiceEvent.class);
	}

	public ClassBasedNavigableListAssert<?, List<? extends FrameworkEvent>, FrameworkEvent, FrameworkEventAssert> hasFrameworkEventsThat() {
		isNotNull();
		List<FrameworkEvent> list = actual.events(FrameworkEvent.class);
		return assertThat(list, FrameworkEventAssert.class);
	}

	public ListAssert<TimedEvent<FrameworkEvent>> hasTimedFrameworkEventsThat() {
		return hasTimedEventsThat(FrameworkEvent.class);
	}

	public ClassBasedNavigableListAssert<?, List<? extends BundleEvent>, BundleEvent, BundleEventAssert> hasBundleEventsThat() {
		isNotNull();
		List<BundleEvent> list = actual.events(BundleEvent.class);
		return assertThat(list, BundleEventAssert.class);
	}

	public ListAssert<TimedEvent<BundleEvent>> hasTimedBundleEventsThat() {
		isNotNull();
		return hasTimedEventsThat(BundleEvent.class);
	}

}
