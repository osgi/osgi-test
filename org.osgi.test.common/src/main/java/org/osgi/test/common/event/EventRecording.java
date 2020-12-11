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
package org.osgi.test.common.event;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.FrameworkListener;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;

public interface EventRecording {
	boolean isTimedOut();

	List<TimedEvent<?>> timedEvents();

	@SuppressWarnings("unchecked")
	default <T> List<TimedEvent<T>> timedEvents(Class<T> clazz) {
		return timedEvents().stream()
			.filter((te) -> clazz.isInstance(te.getEvent()))
			.map((te) -> (TimedEvent<T>) te)
			.collect(Collectors.toList());
	}

	default List<?> events() {
		return timedEvents().stream()
			.map(TimedEvent::getEvent)
			.collect(Collectors.toList());
	}

	default <T> List<T> events(Class<T> clazz) {
		return timedEvents(clazz).stream()
			.map(TimedEvent::getEvent)
			.collect(Collectors.toList());
	}

	default List<ServiceEvent> serviceEvents() {
		return events(ServiceEvent.class);
	}

	default List<TimedEvent<ServiceEvent>> timedServiceEvents() {
		return timedEvents(ServiceEvent.class);
	}

	default List<BundleEvent> bundleEvents() {

		return events(BundleEvent.class);
	}

	default List<TimedEvent<BundleEvent>> timedBundleEvents() {
		return timedEvents(BundleEvent.class);
	}

	default List<FrameworkEvent> frameworkEvents() {
		return events(FrameworkEvent.class);
	}

	default List<TimedEvent<FrameworkEvent>> timedFrameworkEvents() {
		return timedEvents(FrameworkEvent.class);
	}

	static class EventsRecordingImpl implements EventRecording {
		private List<TimedEvent<?>>	events;
		private boolean				timedOut;

		private EventsRecordingImpl() {}

		public EventsRecordingImpl(boolean timedOut, List<TimedEvent<?>> events) {
			this();
			this.timedOut = timedOut;
			this.events = events;
		}

		@Override
		public boolean isTimedOut() {
			return timedOut;
		}

		@Override
		public List<TimedEvent<?>> timedEvents() {
			return events;
		}

		@Override
		public String toString() {
			return "EventsSnapshotImpl [events=" + events + ", timedOut=" + timedOut + "]";
		}

	}

	static class TimedEvent<T> {
		private T event;

		public T getEvent() {
			return event;
		}

		public Instant getInstant() {
			return instant;
		}

		private Instant instant = Instant.now();

		public TimedEvent(T event) {
			this.event = event;
		}

		@Override
		public String toString() {
			return "TimedEvent [event=" + event + ", instant=" + instant + "]";
		}

	}

	static EventRecording recordEvents(BundleContext bc, Runnable callable, Predicate<Object> predicate, int timeout)
		throws Exception {
		List<TimedEvent<?>> events = Collections.synchronizedList(new ArrayList<>());
		CountDownLatch latch = new CountDownLatch(1);

		BundleListener bListener = new BundleListener() {

			@Override
			public void bundleChanged(BundleEvent event) {
				events.add(new TimedEvent<BundleEvent>(CloneUtil.clone(event)));
				if (predicate.test(event)) {
					latch.countDown();
				}

			}
		};

		FrameworkListener fListener = new FrameworkListener() {

			@Override
			public void frameworkEvent(FrameworkEvent event) {
				events.add(new TimedEvent<FrameworkEvent>(CloneUtil.clone(event)));
				if (predicate.test(event)) {
					latch.countDown();
				}
			}
		};

		ServiceListener sListener = new ServiceListener() {
			@Override
			public void serviceChanged(ServiceEvent event) {
				events.add(new TimedEvent<ServiceEvent>(CloneUtil.clone(event)));
				if (predicate.test(event)) {
					latch.countDown();
				}
			}
		};

		bc.addFrameworkListener(fListener);
		bc.addBundleListener(bListener);
		bc.addServiceListener(sListener);
		callable.run();
		boolean timedOut = !latch.await(timeout, TimeUnit.MILLISECONDS);
		bc.removeServiceListener(sListener);
		bc.removeBundleListener(bListener);
		bc.removeFrameworkListener(fListener);

		List<TimedEvent<?>> objects = Collections.unmodifiableList(events);

		return new EventsRecordingImpl(timedOut, objects);
	}
}
