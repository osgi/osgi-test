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

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicMarkableReference;
import java.util.function.Predicate;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceEvent;
import org.osgi.test.assertj.NotPartOfPR.Predicates;
import org.osgi.test.assertj.monitoring.MonitoringAssertion;
import org.osgi.test.assertj.monitoring.MonitoringAssertionTimeoutStep;

public class MonitoringAssertionImpl implements MonitoringAssertionTimeoutStep, MonitoringAssertion {

	private ThrowingCallable execute;

	private Predicate<?> innerPredicate = (se) -> false;

	public MonitoringAssertionImpl(ThrowingCallable execute) {
		this.execute = execute;

	}

	/**
	 * @param execute   - the action that will be invoked.
	 * @param predicate - Predicate that would be tested against the Events
	 * @param timeout   - the time in that the matches must happened
	 */
	private static MonitoringResultAssertionImpl call(ThrowingCallable execute, Predicate<?> predicate, int timeout) {
		BundleContext bc = FrameworkUtil.getBundle(MonitoringResultAssertionImpl.class).getBundleContext();

		EventRecording eventRecording = EventRecording.record(bc, execute, predicate, timeout);
		return new MonitoringResultAssertionImpl(eventRecording, MonitoringResultAssertionImpl.class);
	}

	@Override
	public MonitoringResultAssertionImpl assertWithTimeoutThat(int timeout) {
		return call(execute, innerPredicate, timeout);
	}

	@Override
	public MonitoringResultAssertionImpl assertThat() {
		return assertWithTimeoutThat(0);
	}

	@SuppressWarnings("unchecked")
	private static <T> Predicate<Object> hasTypeAnd(Class<T> clazz, Predicate<T> predicate) {
		return e -> clazz.isAssignableFrom(e.getClass()) && predicate.test((T) e);
	}

	@Override
	public MonitoringAssertionTimeoutStep untilAnyServiceEvent() {

		innerPredicate = hasTypeAnd(ServiceEvent.class, (se) -> true);
		return this;
	}

	@Override
	public MonitoringAssertionTimeoutStep untilServiceEvent(Predicate<ServiceEvent> predicate) {
		innerPredicate = hasTypeAnd(ServiceEvent.class, predicate);
		return this;
	}

	@Override
	public MonitoringAssertionTimeoutStep untilNoMoreServiceEventOfClassWithin(long millis, Class<?> clazz) {
		innerPredicate = hasTypeAnd(ServiceEvent.class, Predicates.ServiceEvents.hasObjectClass(clazz))
				.and(new NoEventWithinPredicate(millis));
		return this;
	}

	@Override
	public MonitoringAssertionTimeoutStep untilNoMoreServiceEventOfClassWithin(long millis, Class<?> clazz,
			Map<String, Object> map) {
		innerPredicate = hasTypeAnd(ServiceEvent.class, Predicates.ServiceEvents.matches(clazz, map))
				.and(new NoEventWithinPredicate(millis));
		return this;
	}

	@Override
	public MonitoringAssertionTimeoutStep untilServiceEventRegistered(Class<?> objectClazz) {
		innerPredicate = hasTypeAnd(ServiceEvent.class,
				Predicates.ServiceEvents.matches(ServiceEvent.REGISTERED, objectClazz));
		return this;
	}

	@Override
	public MonitoringAssertionTimeoutStep untilNoMoreServiceEventRegisteredWithin(long millis, Class<?> clazz) {
		innerPredicate = hasTypeAnd(ServiceEvent.class,
				Predicates.ServiceEvents.matches(ServiceEvent.REGISTERED, clazz))
						.and(new NoEventWithinPredicate(millis));
		return this;
	}

	@Override
	public MonitoringAssertionTimeoutStep untilServiceEventRegistered(Class<?> clazz, Map<String, Object> map) {
		innerPredicate = hasTypeAnd(ServiceEvent.class,
				Predicates.ServiceEvents.matches(ServiceEvent.REGISTERED, clazz, map));
		return this;
	}

	@Override
	public MonitoringAssertionTimeoutStep untilNoMoreServiceEventRegistersWithin(long millis, Class<?> clazz,
			Map<String, Object> map) {
		innerPredicate = hasTypeAnd(ServiceEvent.class,
				Predicates.ServiceEvents.matches(ServiceEvent.REGISTERED, clazz, map))
						.and(new NoEventWithinPredicate(millis));
		return this;
	}

	@Override
	public MonitoringAssertionTimeoutStep untilServiceEventUnregistered(Class<?> clazz) {
		innerPredicate = hasTypeAnd(ServiceEvent.class,
				Predicates.ServiceEvents.matches(ServiceEvent.UNREGISTERING, clazz));
		return this;
	}

	@Override
	public MonitoringAssertionTimeoutStep untilNoMoreServiceEventUnregistersWithin(long millis, Class<?> clazz) {
		innerPredicate = hasTypeAnd(ServiceEvent.class,
				Predicates.ServiceEvents.matches(ServiceEvent.UNREGISTERING, clazz))
						.and(new NoEventWithinPredicate(millis));
		return this;
	}

	@Override
	public MonitoringAssertionTimeoutStep untilServiceEventUnregistered(Class<?> clazz, Map<String, Object> map) {
		innerPredicate = hasTypeAnd(ServiceEvent.class,
				Predicates.ServiceEvents.matches(ServiceEvent.UNREGISTERING, clazz, map));
		return this;
	}

	@Override
	public MonitoringAssertionTimeoutStep untilNoMoreServiceEventUnregistersWithin(long millis, Class<?> clazz,
			Map<String, Object> map) {
		innerPredicate = hasTypeAnd(ServiceEvent.class,
				Predicates.ServiceEvents.matches(ServiceEvent.UNREGISTERING, clazz, map))
						.and(new NoEventWithinPredicate(millis));
		return this;
	}

	@Override
	public MonitoringAssertionTimeoutStep untilServiceEventModified(Class<?> clazz) {
		innerPredicate = hasTypeAnd(ServiceEvent.class,
				Predicates.ServiceEvents.matches(ServiceEvent.MODIFIED, clazz));
		return this;
	}

	@Override
	public MonitoringAssertionTimeoutStep untilNoMoreServiceEventModifiedWithin(long millis, Class<?> clazz) {
		innerPredicate = hasTypeAnd(ServiceEvent.class,
				Predicates.ServiceEvents.matches(ServiceEvent.MODIFIED, clazz))
						.and(new NoEventWithinPredicate(millis));
		return this;
	}

	@Override
	public MonitoringAssertionTimeoutStep untilServiceEventModified(Class<?> clazz, Map<String, Object> map) {
		innerPredicate = hasTypeAnd(ServiceEvent.class,
				Predicates.ServiceEvents.matches(ServiceEvent.MODIFIED, clazz, map));
		return this;
	}

	@Override
	public MonitoringAssertionTimeoutStep untilNoMoreServiceEventModifiedWithin(long millis, Class<?> clazz,
			Map<String, Object> map) {
		innerPredicate = hasTypeAnd(ServiceEvent.class,
				Predicates.ServiceEvents.matches(ServiceEvent.MODIFIED, clazz, map))
						.and(new NoEventWithinPredicate(millis));
		return this;
	}

	@Override
	public MonitoringAssertionTimeoutStep untilServiceEventModifiedEndmatch(Class<?> clazz) {
		innerPredicate = hasTypeAnd(ServiceEvent.class,
				Predicates.ServiceEvents.matches(ServiceEvent.MODIFIED_ENDMATCH, clazz));
		return this;
	}

	@Override
	public MonitoringAssertionTimeoutStep untilNoMoreServiceEventModifiedEndmatchWithin(long millis, Class<?> clazz) {
		innerPredicate = hasTypeAnd(ServiceEvent.class,
				Predicates.ServiceEvents.matches(ServiceEvent.MODIFIED_ENDMATCH, clazz))
						.and(new NoEventWithinPredicate(millis));
		return this;
	}

	@Override
	public MonitoringAssertionTimeoutStep untilServiceEventModifiedEndmatch(Class<?> clazz, Map<String, Object> map) {
		innerPredicate = hasTypeAnd(ServiceEvent.class,
				Predicates.ServiceEvents.matches(ServiceEvent.MODIFIED_ENDMATCH, clazz, map));
		return this;
	}

	@Override
	public MonitoringAssertionTimeoutStep untilNoMoreServiceEventModifiedEndmatchWithin(long millis, Class<?> clazz,
			Map<String, Object> map) {
		innerPredicate = hasTypeAnd(ServiceEvent.class,
				Predicates.ServiceEvents.matches(ServiceEvent.MODIFIED_ENDMATCH, clazz, map))
						.and(new NoEventWithinPredicate(millis));
		return this;
	}

	@Override
	public MonitoringAssertionTimeoutStep untilBundleEvent(Predicate<BundleEvent> predicate) {
		innerPredicate = hasTypeAnd(BundleEvent.class, predicate);
		return this;
	}

	@Override
	public MonitoringAssertionTimeoutStep untilFrameworkEvent(Predicate<FrameworkEvent> predicate) {
		innerPredicate = hasTypeAnd(FrameworkEvent.class, predicate);
		return this;
	}

	@Override
	public MonitoringAssertionTimeoutStep untilNoMoreServiceEventWithin(long timeMs) {

		return untilNoMoreServiceEventWithin(timeMs, (se) -> true);
	}

	@Override
	public MonitoringAssertionTimeoutStep untilNoMoreServiceEventWithin(long timeMs, Predicate<ServiceEvent> predicate) {

		innerPredicate = hasTypeAnd(ServiceEvent.class, predicate).and(new NoEventWithinPredicate(timeMs));
		return this;
	}

	private class NoEventWithinPredicate implements Predicate<Object> {

		private Runner runner;

		public NoEventWithinPredicate(long timeMs) {

			runner = new Runner(timeMs);
		}

		@Override
		public boolean test(Object t) {

			try {
				return runner.await();
			} catch (InterruptedException e) {
				e.printStackTrace();
				return false;
			}

		}
	}

	private class Runner {
		AtomicMarkableReference<CountDownLatch> atomic = new AtomicMarkableReference<CountDownLatch>(null, false);

		private long timeout;

		Runner(long timeout) {
			this.timeout = timeout;
		}

		public boolean await() throws InterruptedException {

			if (atomic.getReference() != null) {
				atomic.getReference().countDown();
			}

			CountDownLatch latch = new CountDownLatch(1);
			atomic.set(latch, true);

			boolean b = !latch.await(timeout, TimeUnit.MILLISECONDS);
			return b && latch.getCount() > 0;
		}
	}

}
