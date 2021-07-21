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

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.ClassBasedNavigableListAssert;
import org.assertj.core.api.Condition;
import org.assertj.core.api.DurationAssert;
import org.assertj.core.api.ListAssert;
import org.assertj.core.api.ThrowableAssert;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.test.assertj.NotPartOfPR.Conditions;
import org.osgi.test.assertj.bundleevent.BundleEventAssert;
import org.osgi.test.assertj.frameworkevent.FrameworkEventAssert;
import org.osgi.test.assertj.monitoring.MonitoringAssertionResult;
import org.osgi.test.assertj.monitoring.TimedEvent;
import org.osgi.test.assertj.serviceevent.ServiceEventAssert;
import org.osgi.test.common.bitmaps.ServiceEventType;
import org.osgi.test.common.dictionary.Dictionaries;

abstract class AbstractMonitoringAssertion<SELF extends AbstractMonitoringAssertion<SELF, ACTUAL>, ACTUAL extends EventRecording>
		extends AbstractAssert<SELF, ACTUAL> implements MonitoringAssertionResult {

	protected AbstractMonitoringAssertion(ACTUAL actual, Class<SELF> selfType) {
		super(actual, selfType);
	}

	@Override
	public ThrowableAssert hasThrowableThat() {
		return new ThrowableAssert(actual.throwable());
	}

	@Override
	public SELF hasNoThrowable() {
		hasThrowableThat().isNull();
		return myself;
	}

	@Override
	public SELF isTimedOut() {
		isNotNull();
		if (!actual.isTimedOut()) {
			throw failure("%nExpecting%n  <%s>%nto be a timedOut, but it was not", actual);
		}
		return myself;
	}

	@Override
	public SELF isNotTimedOut() {
		isNotNull();
		if (actual.isTimedOut()) {
			throw failure("%nExpecting%n  <%s>%nto not be a timedOut, but it was", actual);
		}
		return myself;
	}

	@Override
	public <T> ListAssert<T> hasEventsThat(Class<T> clazz) {
		isNotNull();
		List<T> list = actual.events(clazz);

		return assertThat(list);
	}

	@Override
	public <T> ListAssert<TimedEvent<T>> hasTimedEventsThat(Class<T> clazz) {
		isNotNull();
		List<TimedEvent<T>> list = actual.timedEvents(clazz);
		return assertThat(list);
	}

	@Override
	public ListAssert<?> hasEventsThat() {
		return hasEventsThat(Object.class);
	}

	@Override
	public ListAssert<TimedEvent<Object>> hasTimedEventsThat() {
		return hasTimedEventsThat(Object.class);
	}

	@Override
	public DurationAssert hasDurationBetweenThat(int firstElementIndex, int secondElementIndex) {

		hasEventsThat().hasSizeGreaterThanOrEqualTo(firstElementIndex + 1).element(firstElementIndex).isNotNull();

		hasEventsThat().hasSizeGreaterThanOrEqualTo(secondElementIndex + 1).element(secondElementIndex).isNotNull();

		TimedEvent<?> timedFirst = actual.timedEvents().get(firstElementIndex);

		TimedEvent<?> timedSecond = actual.timedEvents().get(secondElementIndex);

		return createDurationAssert(timedFirst, timedSecond);
	}

	private DurationAssert createDurationAssert(TimedEvent<?> timedFirst, TimedEvent<?> timedSecond) {
		Duration actual = Duration.between(timedFirst.getInstant(), timedSecond.getInstant());
		return new DurationAssert(actual);
	}

	@Override
	public ClassBasedNavigableListAssert<?, List<? extends ServiceEvent>, ServiceEvent, ServiceEventAssert> hasServiceEventsThat() {
		isNotNull();
		List<ServiceEvent> list = actual.events(ServiceEvent.class);

		return assertThat(list, ServiceEventAssert.class);
	}

	@Override
	public SELF hasAtLeastOneServiceEventWith(int eventTypeMask, final Class<?> objectClass) {
		Condition<ServiceEvent> condition = Conditions.ServiceEventConditions.matches(eventTypeMask, objectClass);

		return hasAtLeastNServiceEventWithCondition(1, condition);

	}

	@Override
	public SELF hasAtLeastOneServiceEventWith(int eventTypeMask, final String filter) throws InvalidSyntaxException {
		Condition<ServiceEvent> condition = Conditions.ServiceEventConditions.matches(eventTypeMask, filter);

		return hasAtLeastNServiceEventWithCondition(1, condition);

	}

	@Override
	public SELF hasAtLeastOneServiceEventWith(int eventTypeMask, final Class<?> objectClass, Map<String, Object> map) {
		Condition<ServiceEvent> condition = Conditions.ServiceEventConditions.matches(eventTypeMask, objectClass, map);

		return hasAtLeastNServiceEventWithCondition(1, condition);

	}

	@Override
	public SELF hasAtLeastNServiceEventWith(int n, int eventTypeMask, final String filter)
			throws InvalidSyntaxException {
		Condition<ServiceEvent> condition = Conditions.ServiceEventConditions.matches(eventTypeMask, filter);

		return hasAtLeastNServiceEventWithCondition(n, condition);

	}

	@Override
	public SELF hasAtLeastNServiceEventWith(int n, int eventTypeMask, final Class<?> objectClass) {
		Condition<ServiceEvent> condition = Conditions.ServiceEventConditions.matches(eventTypeMask, objectClass);

		return hasAtLeastNServiceEventWithCondition(1, condition);

	}

	@Override
	public SELF hasAtLeastNServiceEventWith(int n, int eventTypeMask, final Class<?> objectClass,
			Map<String, Object> map) {
		Condition<ServiceEvent> condition = Conditions.ServiceEventConditions.matches(eventTypeMask, objectClass, map);

		return hasAtLeastNServiceEventWithCondition(1, condition);

	}

	@Override
	public SELF hasExactlyOneServiceEventWith(int eventTypeMask, final Class<?> objectClass) {
		Condition<ServiceEvent> condition = Conditions.ServiceEventConditions.matches(eventTypeMask, objectClass);

		return hasExactlyNServiceEventWithCondition(1, condition);

	}

	@Override
	public SELF hasExactlyOneServiceEventWith(int eventTypeMask, final String filter) throws InvalidSyntaxException {
		Condition<ServiceEvent> condition = Conditions.ServiceEventConditions.matches(eventTypeMask, filter);

		return hasExactlyNServiceEventWithCondition(1, condition);

	}

	@Override
	public SELF hasExactlyOneServiceEventWith(int eventTypeMask, final Class<?> objectClass, Map<String, Object> map) {
		Condition<ServiceEvent> condition = Conditions.ServiceEventConditions.matches(eventTypeMask, objectClass, map);

		return hasExactlyNServiceEventWithCondition(1, condition);

	}

	@Override
	public SELF hasExactlyNServiceEventWith(int n, int eventTypeMask, final Class<?> objectClass) {
		Condition<ServiceEvent> condition = Conditions.ServiceEventConditions.matches(eventTypeMask, objectClass);

		return hasExactlyNServiceEventWithCondition(n, condition);

	}

	@Override
	public SELF hasExactlyNServiceEventWith(int n, int eventTypeMask, final String filter)
			throws InvalidSyntaxException {
		Condition<ServiceEvent> condition = Conditions.ServiceEventConditions.matches(eventTypeMask, filter);

		return hasExactlyNServiceEventWithCondition(n, condition);

	}

	@Override
	public SELF hasExactlyNServiceEventWith(int n, int eventTypeMask, final Class<?> objectClass,
			Map<String, Object> map) {
		Condition<ServiceEvent> condition = Conditions.ServiceEventConditions.matches(eventTypeMask, objectClass, map);

		return hasExactlyNServiceEventWithCondition(n, condition);

	}

	@Override
	public SELF hasNoServiceEventWith(int eventTypeMask, final Class<?> objectClass, Map<String, Object> map) {
		hasServiceEventsThat().areNot(Conditions.ServiceEventConditions.matches(eventTypeMask, objectClass, map));
		return myself;
	}

	@Override
	public SELF hasNoServiceEventWith(int eventTypeMask, String filter) throws InvalidSyntaxException {
		hasServiceEventsThat().areNot(Conditions.ServiceEventConditions.matches(eventTypeMask, filter));

		return myself;
	}

	@Override
	public SELF hasNoServiceEvent() {
		hasServiceEventsThat().isEmpty();
		return myself;
	}

	@Override
	public ListAssert<TimedEvent<ServiceEvent>> hasTimedServiceEventsThat() {
		return hasTimedEventsThat(ServiceEvent.class);
	}

	@Override
	public ClassBasedNavigableListAssert<?, List<? extends FrameworkEvent>, FrameworkEvent, FrameworkEventAssert> hasFrameworkEventsThat() {
		isNotNull();
		List<FrameworkEvent> list = actual.events(FrameworkEvent.class);
		return assertThat(list, FrameworkEventAssert.class);
	}

	@Override
	public SELF hasNoFrameworkEvent() {
		hasFrameworkEventsThat().isEmpty();
		return myself;
	}

	@Override
	public ListAssert<TimedEvent<FrameworkEvent>> hasTimedFrameworkEventsThat() {
		return hasTimedEventsThat(FrameworkEvent.class);
	}

	@Override
	public ClassBasedNavigableListAssert<?, List<? extends BundleEvent>, BundleEvent, BundleEventAssert> hasBundleEventsThat() {
		isNotNull();
		List<BundleEvent> list = actual.events(BundleEvent.class);
		return assertThat(list, BundleEventAssert.class);
	}

	@Override
	public SELF hasNoBundleEvent() {
		hasBundleEventsThat().isEmpty();
		return myself;
	}

	@Override
	public ListAssert<TimedEvent<BundleEvent>> hasTimedBundleEventsThat() {
		isNotNull();
		return hasTimedEventsThat(BundleEvent.class);
	}

	@Override
	public SELF hasServiceEventsInExactOrder(List<Condition<ServiceEvent>> conditions) {
		return hasServiceEventsInOrder(true, conditions);
	}

	private SELF hasServiceEventsInOrder(boolean exact, List<Condition<ServiceEvent>> conditions) {
		isNotNull();

		StringBuilder sb = new StringBuilder("hasServiceEventsInOrder").append(System.lineSeparator());
		List<ServiceEvent> list = actual.events(ServiceEvent.class);
		boolean fail = false;
		int condCounter = 0;
		int eventCounter = 0;
		Condition<ServiceEvent> c = conditions.get(condCounter);
		for (ServiceEvent se : list) {
			sb = sb.append("ServiceEvent " + eventCounter++ + ":").append(System.lineSeparator()).append(" type: ")
					.append(ServiceEventType.BITMAP.maskToString(se.getType())).append(", ObjectClass: ")
					.append(se.getServiceReference()).append(System.lineSeparator()).append(" props: ")
				.append(se.getServiceReference() == null ? "[]" : Dictionaries.asDictionary(se.getServiceReference()))
					.append(System.lineSeparator());
			if (c.matches(se)) {
				sb = sb.append(c.conditionDescriptionWithStatus(se)).append(System.lineSeparator())
						.append(System.lineSeparator());
				condCounter++;
				if (condCounter >= conditions.size()) {
					break;
				}
				c = conditions.get(condCounter);
			} else {
				if (exact) {
					// service event does not match - fail
					sb = sb.append(c.conditionDescriptionWithStatus(se)).append(System.lineSeparator());
					condCounter++;
					fail = true;
					break;
				} else {
					// service event does not match - try next
				}
			}
		}
		if (condCounter < conditions.size()) {
			fail = true;
			sb = sb.append(System.lineSeparator()).append("Unhandled Conditions").append(System.lineSeparator());
			for (int i = condCounter; i < conditions.size(); i++) {
				sb = sb.append("- ").append(conditions.get(i)).append(System.lineSeparator());
			}
		}
		if (fail) {
			throw failure(sb.toString());
		}
		return myself;
	}

	@Override
	public SELF hasServiceEventsInOrder(List<Condition<ServiceEvent>> conditions) {

		return hasServiceEventsInOrder(false, conditions);
	}

	// unregistering

	@Override
	public SELF hasAtLeastOneServiceEventUnregisteringWith(final Class<?> objectClass) {

		Condition<ServiceEvent> condition = Conditions.ServiceEventConditions
				.typeUnregisteringAndObjectClass(objectClass);
		return hasAtLeastNServiceEventWithCondition(1, condition);

	}

	@Override
	public SELF hasAtLeastOneServiceEventUnregisteringWith(Class<?> objectClass, Map<String, Object> map) {

		Condition<ServiceEvent> condition = Conditions.ServiceEventConditions.matches(ServiceEvent.UNREGISTERING,
				objectClass, map);

		return hasAtLeastNServiceEventWithCondition(1, condition);

	}

	@Override
	public SELF hasAtLeastNServiceEventUnregisteringWith(int n, final Class<?> objectClass) {

		Condition<ServiceEvent> condition = Conditions.ServiceEventConditions
				.typeUnregisteringAndObjectClass(objectClass);

		return hasAtLeastNServiceEventWithCondition(n, condition);

	}

	@Override
	public SELF hasAtLeastNServiceEventUnregisteringWith(int n, Class<?> objectClass, Map<String, Object> map) {

		Condition<ServiceEvent> condition = Conditions.ServiceEventConditions.matches(ServiceEvent.UNREGISTERING,
				objectClass, map);

		return hasAtLeastNServiceEventWithCondition(n, condition);

	}

	@Override
	public SELF hasAtMostNServiceEventUnregisteringWith(int n, final Class<?> objectClass) {

		Condition<ServiceEvent> condition = Conditions.ServiceEventConditions
				.typeUnregisteringAndObjectClass(objectClass);

		return hasAtMostNServiceEventWithCondition(n, condition);

	}

	@Override
	public SELF hasAtMostNServiceEventUnregisteringWith(int n, Class<?> objectClass, Map<String, Object> map) {

		Condition<ServiceEvent> condition = Conditions.ServiceEventConditions.matches(ServiceEvent.UNREGISTERING,
				objectClass, map);

		return hasAtMostNServiceEventWithCondition(n, condition);

	}

	@Override
	public SELF hasExactlyOneServiceEventUnregisteringWith(final Class<?> objectClass) {

		Condition<ServiceEvent> condition = Conditions.ServiceEventConditions
				.typeUnregisteringAndObjectClass(objectClass);

		return hasExactlyNServiceEventWithCondition(1, condition);

	}

	@Override
	public SELF hasExactlyOneServiceEventUnregisteringWith(Class<?> objectClass, Map<String, Object> map) {

		Condition<ServiceEvent> condition = Conditions.ServiceEventConditions
				.typeUnregisteringAndObjectClass(objectClass);

		return hasExactlyNServiceEventWithCondition(1, condition);

	}

	@Override
	public SELF hasExactlyNServiceEventUnregisteringWith(int n, final Class<?> objectClass) {

		Condition<ServiceEvent> condition = Conditions.ServiceEventConditions
				.typeUnregisteringAndObjectClass(objectClass);

		return hasExactlyNServiceEventWithCondition(n, condition);

	}

	@Override
	public SELF hasExactlyNServiceEventUnregisteringWith(int n, Class<?> objectClass, Map<String, Object> map) {

		Condition<ServiceEvent> condition = Conditions.ServiceEventConditions.matches(ServiceEvent.UNREGISTERING,
				objectClass, map);

		return hasExactlyNServiceEventWithCondition(n, condition);

	}
	// Registered

	@Override
	public SELF hasAtLeastOneServiceEventRegisteredWith(final Class<?> objectClass) {

		Condition<ServiceEvent> condition = Conditions.ServiceEventConditions.typeRegisteredAndObjectClass(objectClass);

		return hasAtLeastNServiceEventWithCondition(1, condition);

	}

	@Override
	public SELF hasAtLeastOneServiceEventRegisteredWith(Class<?> objectClass, Map<String, Object> map) {

		Condition<ServiceEvent> condition = Conditions.ServiceEventConditions.matches(ServiceEvent.REGISTERED,
				objectClass, map);

		return hasAtLeastNServiceEventWithCondition(1, condition);

	}

	@Override
	public SELF hasAtLeastNServiceEventRegisteredWith(int n, final Class<?> objectClass) {

		Condition<ServiceEvent> condition = Conditions.ServiceEventConditions.typeRegisteredAndObjectClass(objectClass);

		return hasAtLeastNServiceEventWithCondition(n, condition);

	}

	@Override
	public SELF hasAtLeastNServiceEventRegisteredWith(int n, Class<?> objectClass, Map<String, Object> map) {

		Condition<ServiceEvent> condition = Conditions.ServiceEventConditions.matches(ServiceEvent.REGISTERED,
				objectClass, map);

		return hasAtLeastNServiceEventWithCondition(n, condition);

	}

	@Override
	public SELF hasAtMostNServiceEventRegisteredWith(int n, final Class<?> objectClass) {
		Condition<ServiceEvent> condition = Conditions.ServiceEventConditions.typeRegisteredAndObjectClass(objectClass);

		return hasAtMostNServiceEventWithCondition(n, condition);

	}

	@Override
	public SELF hasAtMostNServiceEventRegisteredWith(int n, Class<?> objectClass, Map<String, Object> map) {

		Condition<ServiceEvent> condition = Conditions.ServiceEventConditions.matches(ServiceEvent.REGISTERED,
				objectClass, map);

		return hasAtMostNServiceEventWithCondition(n, condition);

	}

	@Override
	public SELF hasExactlyOneServiceEventRegisteredWith(final Class<?> objectClass) {

		Condition<ServiceEvent> condition = Conditions.ServiceEventConditions.typeRegisteredAndObjectClass(objectClass);

		return hasExactlyNServiceEventWithCondition(1, condition);

	}

	@Override
	public SELF hasExactlyOneServiceEventRegisteredWith(Class<?> objectClass, Map<String, Object> map) {

		Condition<ServiceEvent> condition = Conditions.ServiceEventConditions.matches(ServiceEvent.REGISTERED,
				objectClass, map);

		return hasExactlyNServiceEventWithCondition(1, condition);

	}

	@Override

	public SELF hasExactlyNServiceEventRegisteredWith(int n, final Class<?> objectClass) {

		Condition<ServiceEvent> condition = Conditions.ServiceEventConditions.typeRegisteredAndObjectClass(objectClass);

		return hasExactlyNServiceEventWithCondition(n, condition);

	}

	@Override
	public SELF hasExactlyNServiceEventRegisteredWith(int n, Class<?> objectClass, Map<String, Object> map) {

		Condition<ServiceEvent> condition = Conditions.ServiceEventConditions.matches(ServiceEvent.REGISTERED,
				objectClass, map);

		return hasExactlyNServiceEventWithCondition(n, condition);

	}

	// Modified

	@Override
	public SELF hasAtLeastOneServiceEventModifiedWith(final Class<?> objectClass) {

		Condition<ServiceEvent> condition = Conditions.ServiceEventConditions.typeModifiedAndObjectClass(objectClass);

		return hasAtLeastNServiceEventWithCondition(1, condition);

	}

	@Override
	public SELF hasAtLeastOneServiceEventModifiedWith(Class<?> objectClass, Map<String, Object> map) {

		Condition<ServiceEvent> condition = Conditions.ServiceEventConditions.matches(ServiceEvent.MODIFIED,
				objectClass, map);

		return hasAtLeastNServiceEventWithCondition(1, condition);

	}

	@Override
	public SELF hasAtLeastNServiceEventModifiedWith(int n, final Class<?> objectClass) {

		Condition<ServiceEvent> condition = Conditions.ServiceEventConditions.typeModifiedAndObjectClass(objectClass);

		return hasAtLeastNServiceEventWithCondition(n, condition);

	}

	@Override

	public SELF hasAtLeastNServiceEventModifiedWith(int n, Class<?> objectClass, Map<String, Object> map) {

		Condition<ServiceEvent> condition = Conditions.ServiceEventConditions.matches(ServiceEvent.MODIFIED,
				objectClass, map);

		return hasAtLeastNServiceEventWithCondition(n, condition);

	}

	@Override

	public SELF hasAtMostNServiceEventModifiedWith(int n, final Class<?> objectClass) {

		Condition<ServiceEvent> condition = Conditions.ServiceEventConditions.typeModifiedAndObjectClass(objectClass);

		return hasAtMostNServiceEventWithCondition(n, condition);

	}

	@Override
	public SELF hasAtMostNServiceEventModifiedWith(int n, Class<?> objectClass, Map<String, Object> map) {

		Condition<ServiceEvent> condition = Conditions.ServiceEventConditions.matches(ServiceEvent.MODIFIED,
				objectClass, map);

		return hasAtMostNServiceEventWithCondition(n, condition);

	}

	@Override
	public SELF hasExactlyOneServiceEventModifiedWith(final Class<?> objectClass) {

		Condition<ServiceEvent> condition = Conditions.ServiceEventConditions.typeModifiedAndObjectClass(objectClass);
		return hasExactlyNServiceEventWithCondition(1, condition);

	}

	@Override
	public SELF hasExactlyOneServiceEventModifiedWith(Class<?> objectClass, Map<String, Object> map) {

		Condition<ServiceEvent> condition = Conditions.ServiceEventConditions.matches(ServiceEvent.MODIFIED,
				objectClass, map);

		return hasExactlyNServiceEventWithCondition(1, condition);

	}

	@Override

	public SELF hasExactlyNServiceEventModifiedWith(int n, final Class<?> objectClass) {
		Condition<ServiceEvent> condition = Conditions.ServiceEventConditions.typeModifiedAndObjectClass(objectClass);

		return hasExactlyNServiceEventWithCondition(n, condition);

	}

	@Override
	public SELF hasExactlyNServiceEventModifiedWith(int n, Class<?> objectClass, Map<String, Object> map) {
		Condition<ServiceEvent> condition = Conditions.ServiceEventConditions.matches(ServiceEvent.MODIFIED,
				objectClass, map);

		return hasExactlyNServiceEventWithCondition(n, condition);

	}

	@Override
	public SELF hasExactlyNServiceEventWithCondition(int n, Condition<ServiceEvent> condition) {

		Map<ServiceEvent, String> result = matchServiceEvent(condition);

		long countPassed = countPassed(result);

		if (n != countPassed) {
			String failMessage = doMessage(n, countPassed, "exactly", result);
			failWithMessage(failMessage);
		}

		return myself;
	}

	@Override
	public SELF hasAtLeastNServiceEventWithCondition(int n, Condition<ServiceEvent> condition) {

		Map<ServiceEvent, String> result = matchServiceEvent(condition);

		long countPassed = countPassed(result);

		if (n > countPassed) {
			String failMessage = doMessage(n, countPassed, "at least", result);
			failWithMessage(failMessage);
		}

		return myself;
	}

	@Override
	public SELF hasAtMostNServiceEventWithCondition(int n, Condition<ServiceEvent> condition) {

		Map<ServiceEvent, String> result = matchServiceEvent(condition);

		long countPassed = countPassed(result);

		if (n < countPassed) {
			String failMessage = doMessage(n, countPassed, "at most", result);
			failWithMessage(failMessage);
		}

		return myself;
	}

	private String doMessage(int expectedCount, long wasCount, String compareText, Map<ServiceEvent, String> result) {
		StringBuilder sb = new StringBuilder();
		sb.append(System.lineSeparator());
		sb.append("Expected ").append(compareText).append(" <").append(expectedCount).append("> match(es) but was <")
				.append(wasCount).append(">.").append(System.lineSeparator()).append(System.lineSeparator());

		sb.append("Passed: ")
			.append(System.lineSeparator());

		List<Entry<ServiceEvent, String>> eventsPassed = result.entrySet().stream().filter(e -> e.getValue() == null)
				.collect(Collectors.toList());
		if (eventsPassed.isEmpty()) {
			sb.append("-none-").append(System.lineSeparator());

		}
		AtomicInteger i = new AtomicInteger();
		eventsPassed.forEach(e -> {
			ServiceEvent se = e.getKey();
			sb.append("-Element ")
				.append(i.getAndIncrement())
				.append(":")
				.append(System.lineSeparator())
				.append(seToString(se))
				.append(System.lineSeparator())
				.append(System.lineSeparator());

		});
		sb.append(System.lineSeparator())
			.append("Not Passed:")
			.append(System.lineSeparator());

		List<Entry<ServiceEvent, String>> eventsNotPassed = result.entrySet().stream().filter(e -> e.getValue() != null)
				.collect(Collectors.toList());

		if (eventsNotPassed.isEmpty()) {
			sb.append("-none-").append(System.lineSeparator());

		}
		AtomicInteger i2 = new AtomicInteger();
		eventsNotPassed.forEach(e -> {
			ServiceEvent se = e.getKey();
			sb.append("-Element ")
				.append(i2.getAndIncrement())
				.append(":")
				.append(System.lineSeparator())
				.append(seToString(se))
				.append(System.lineSeparator());

			sb.append("-Result:")
				.append(System.lineSeparator());
			sb.append(e.getValue());
			sb.append(System.lineSeparator()).append(System.lineSeparator());

		});
		return sb.toString();
	}

	private String seToString(ServiceEvent se) {
		return new StringBuilder().append("  ServiceEvent: type <")
				.append(ServiceEventType.BITMAP.maskToString(se.getType())).append(">").append(", SR-objectClass: ")
				.append(Arrays.toString((String[]) Dictionaries.asDictionary(se.getServiceReference()).get("objectClass")))
			.append(System.lineSeparator())
			.append("  SR-Properties: ")
			.append(Dictionaries.toString(Dictionaries.asDictionary(se.getServiceReference()))
				.toString())
			.toString();

	}

	private static long countPassed(Map<?, String> result) {
		return result.values().stream().filter(Objects::isNull).count();
	}

	Map<ServiceEvent, String> matchServiceEvent(Condition<ServiceEvent> c) {
		List<ServiceEvent> list = actual.events(ServiceEvent.class);

		Map<ServiceEvent, String> map = new HashMap<>();
		for (ServiceEvent serviceEvent : list) {

			String message = null;
			if (!c.matches(serviceEvent)) {
				message = c.conditionDescriptionWithStatus(serviceEvent).toString();
			}

			map.put(serviceEvent, message);
		}
		return map;
	}
}
