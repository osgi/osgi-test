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

package org.osgi.test.assertj.monitoring.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.osgi.framework.AllServiceListener;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.FrameworkListener;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.SynchronousBundleListener;
import org.osgi.test.assertj.monitoring.TimedEvent;

interface EventRecording {

	Throwable throwable();

	boolean isTimedOut();

	List<TimedEvent<?>> timedEvents();

	@SuppressWarnings("unchecked")
	default <T> Stream<TimedEvent<T>> timedEventsStream(Class<T> clazz) {
		Stream<TimedEvent<T>> result = (Stream<TimedEvent<T>>) (Stream<?>) timedEvents().stream()
				.filter((te) -> clazz.isInstance(te.getEvent()));
		return result;
	}

	default <T> List<TimedEvent<T>> timedEvents(Class<T> clazz) {
		return timedEventsStream(clazz).collect(Collectors.toList());
	}

	default List<?> events() {
		return timedEvents().stream().map(TimedEvent::getEvent).collect(Collectors.toList());
	}

	default <T> List<T> events(Class<T> clazz) {
		return timedEventsStream(clazz).map(TimedEvent::getEvent).collect(Collectors.toList());
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
		private List<TimedEvent<?>> events;
		private boolean timedOut;
		private Throwable throwable = null;

		public EventsRecordingImpl(boolean timedOut, List<TimedEvent<?>> events, Throwable throwable) {
			this.throwable = throwable;
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

		@Override
		public Throwable throwable() {
			return throwable;
		}
	}

	static EventRecording record(BundleContext bc, ThrowingCallable action, Predicate<?> predicate, int timeout) {
		List<TimedEvent<?>> events = new ArrayList<>();
		List<TimedEvent<?>> syncList = Collections.synchronizedList(events);
		Throwable throwable = null;

		CountDownLatch latch = new CountDownLatch(1);

		BundleListener bListener = new SynchronousBundleListener() {
			@SuppressWarnings({ "unchecked", "rawtypes" })
			@Override
			public void bundleChanged(BundleEvent event) {

				BundleEvent clone = CloneUtil.clone(event);
				syncList.add(new TimedEventImpl<BundleEvent>(clone));

				Executors.newSingleThreadExecutor().execute(new Runnable() {
					@Override
					public void run() {
						if (((Predicate) predicate).test(clone)) {
							latch.countDown();
						}
					}
				});
			}
		};

		FrameworkListener fListener = new FrameworkListener() {
			@SuppressWarnings({ "unchecked", "rawtypes" })
			@Override
			public void frameworkEvent(FrameworkEvent event) {
				FrameworkEvent clone = CloneUtil.clone(event);
				syncList.add(new TimedEventImpl<FrameworkEvent>(clone));

				Executors.newSingleThreadExecutor().execute(new Runnable() {
					@Override
					public void run() {
						if (latch.getCount() > 0 && ((Predicate) predicate).test(clone)) {
							latch.countDown();
						}
					}
				});
			}
		};

		ServiceListener sListener = new AllServiceListener() {
			@SuppressWarnings({ "unchecked", "rawtypes" })
			@Override
			public void serviceChanged(ServiceEvent event) {
				ServiceEvent clone = CloneUtil.clone(event);
				syncList.add(new TimedEventImpl<ServiceEvent>(clone));
				Executors.newSingleThreadExecutor().execute(new Runnable() {
					@Override
					public void run() {
						if (latch.getCount() > 0 && ((Predicate) predicate).test(clone)) {
							latch.countDown();
						}
					}
				});
			}
		};

		bc.addFrameworkListener(fListener);
		bc.addBundleListener(bListener);
		bc.addServiceListener(sListener);
		try {
			action.call();
		} catch (Throwable e) {
			throwable = e;
		}
		boolean timedOut;
		try {
			timedOut = !latch.await(timeout, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			timedOut = true;
		}
		bc.removeServiceListener(sListener);
		bc.removeBundleListener(bListener);
		bc.removeFrameworkListener(fListener);

		List<TimedEvent<?>> objects = Collections.unmodifiableList(events);

		return new EventsRecordingImpl(timedOut, objects, throwable);
	}
}
