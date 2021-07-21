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

package org.osgi.test.assertj.monitoring;

import java.util.List;
import java.util.Map;

import org.assertj.core.api.ClassBasedNavigableListAssert;
import org.assertj.core.api.Condition;
import org.assertj.core.api.DurationAssert;
import org.assertj.core.api.ListAssert;
import org.assertj.core.api.ThrowableAssert;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.test.assertj.bundleevent.BundleEventAssert;
import org.osgi.test.assertj.frameworkevent.FrameworkEventAssert;
import org.osgi.test.assertj.serviceevent.ServiceEventAssert;

/**
 * The Interface RuntimeMonitoringResultAssert.
 */
public interface MonitoringAssertionResult {

	/**
	 * Checks for at least N service event unregistering with the given parameter(s).
	 *
	 * @param n           the n
	 * @param objectClass the object class
	 * @return the runtime monitoring result assert
	 */
	MonitoringAssertionResult hasAtLeastNServiceEventUnregisteringWith(int n, Class<?> objectClass);

	/**
	 * Checks for at least N service event unregistering with the given parameter(s).
	 *
	 * @param n           the n
	 * @param objectClass the object class
	 * @param properties  the properties
	 * @return the runtime monitoring result assert
	 */
	MonitoringAssertionResult hasAtLeastNServiceEventUnregisteringWith(int n, Class<?> objectClass,
			Map<String, Object> properties);

	/**
	 * Checks for at least one service event modified with the given parameter(s).
	 *
	 * @param objectClass the object class
	 * @return the runtime monitoring result assert
	 */
	public MonitoringAssertionResult hasAtLeastOneServiceEventModifiedWith(final Class<?> objectClass);

	/**
	 * Checks for at least one service event modified with the given parameter(s).
	 *
	 * @param objectClass the object class
	 * @param properties  the properties
	 * @return the runtime monitoring result assert
	 */
	public MonitoringAssertionResult hasAtLeastOneServiceEventModifiedWith(final Class<?> objectClass,
			Map<String, Object> properties);

	/**
	 * Checks for at least one service event registered with the given parameter(s).
	 *
	 * @param objectClass the object class
	 * @return the runtime monitoring result assert
	 */
	public MonitoringAssertionResult hasAtLeastOneServiceEventRegisteredWith(final Class<?> objectClass);

	/**
	 * Checks for at least one service event registered with the given parameter(s).
	 *
	 * @param objectClass the object class
	 * @param properties  the properties
	 * @return the runtime monitoring result assert
	 */
	public MonitoringAssertionResult hasAtLeastOneServiceEventRegisteredWith(final Class<?> objectClass,
			Map<String, Object> properties);

	/**
	 * Checks for at least one service event unregistering with the given
	 * parameters.
	 *
	 * @param objectClass the object class
	 * @return the runtime monitoring result assert
	 */
	public MonitoringAssertionResult hasAtLeastOneServiceEventUnregisteringWith(final Class<?> objectClass);

	/**
	 * Checks for at least one service event unregistering with the given
	 * parameters.
	 *
	 * @param objectClass the object class
	 * @param properties  the properties
	 * @return the runtime monitoring result assert
	 */
	public MonitoringAssertionResult hasAtLeastOneServiceEventUnregisteringWith(final Class<?> objectClass,
			Map<String, Object> properties);

	/**
	 * Checks for at least one service event with the given parameter(s).
	 *
	 * @param eventTypeMask the event type mask
	 * @param objectClass   the object class
	 * @return the runtime monitoring result assert
	 */
	public MonitoringAssertionResult hasAtLeastOneServiceEventWith(int eventTypeMask, final Class<?> objectClass);

	/**
	 * Checks for at least one service event with the given parameter(s).
	 *
	 * @param eventTypeMask the event type mask
	 * @param objectClass   the object class
	 * @param properties    the properties
	 * @return the runtime monitoring result assert
	 */
	public MonitoringAssertionResult hasAtLeastOneServiceEventWith(int eventTypeMask, final Class<?> objectClass,
			Map<String, Object> properties);

	/**
	 * Checks for at most N service event unregistering with the given parameter(s).
	 *
	 * @param n           the n
	 * @param objectClass the object class
	 * @return the runtime monitoring result assert
	 */
	MonitoringAssertionResult hasAtMostNServiceEventUnregisteringWith(int n, Class<?> objectClass);

	/**
	 * Checks for at most N service event unregistering with the given parameter(s).
	 *
	 * @param n           the n
	 * @param objectClass the object class
	 * @param properties  the properties
	 * @return the runtime monitoring result assert
	 */
	MonitoringAssertionResult hasAtMostNServiceEventUnregisteringWith(int n, Class<?> objectClass,
			Map<String, Object> properties);

	/**
	 * Checks for bundle events that.
	 *
	 * @return the class based navigable list assert<?, list<? extends bundle
	 *         event>, bundle event, bundle event assert>
	 */
	public ClassBasedNavigableListAssert<?, List<? extends BundleEvent>, BundleEvent, BundleEventAssert> hasBundleEventsThat();

	/**
	 * Checks for duration between that.
	 *
	 * @param firstElementIndex  the first element index
	 * @param secondElementIndex the second element index
	 * @return the duration assert
	 */
	public DurationAssert hasDurationBetweenThat(int firstElementIndex, int secondElementIndex);

	/**
	 * Checks for events that.
	 *
	 * @return the list assert
	 */
	public ListAssert<?> hasEventsThat();

	/**
	 * Checks for events that.
	 *
	 * @param <T>   the generic type
	 * @param clazz the clazz
	 * @return the list assert
	 */
	public <T> ListAssert<T> hasEventsThat(Class<T> clazz);

	/**
	 * Checks for exactly N service event unregistering with the given parameter(s).
	 *
	 * @param n           the n
	 * @param objectClass the object class
	 * @return the runtime monitoring result assert
	 */
	MonitoringAssertionResult hasExactlyNServiceEventUnregisteringWith(int n, Class<?> objectClass);

	/**
	 * Checks for exactly N service event unregistering with the given parameter(s).
	 *
	 * @param n           the n
	 * @param objectClass the object class
	 * @param properties  the properties
	 * @return the runtime monitoring result assert
	 */
	MonitoringAssertionResult hasExactlyNServiceEventUnregisteringWith(int n, Class<?> objectClass,
			Map<String, Object> properties);

	/**
	 * Checks for exactly one service event unregistering with the given parameter(s).
	 *
	 * @param objectClass the object class
	 * @return the runtime monitoring result assert
	 */
	MonitoringAssertionResult hasExactlyOneServiceEventUnregisteringWith(Class<?> objectClass);

	/**
	 * Checks for exactly one service event unregistering with the given parameter(s).
	 *
	 * @param objectClass the object class
	 * @param properties  the properties
	 * @return the runtime monitoring result assert
	 */
	MonitoringAssertionResult hasExactlyOneServiceEventUnregisteringWith(Class<?> objectClass,
			Map<String, Object> properties);

	/**
	 * Checks for exactly one service event with the given parameter(s).
	 *
	 * @param eventTypeMask the event type mask
	 * @param objectClass   the object class
	 * @return the runtime monitoring result assert
	 */
	public MonitoringAssertionResult hasExactlyOneServiceEventWith(int eventTypeMask, final Class<?> objectClass);

	/**
	 * Checks for exactly one service event with the given parameter(s).
	 *
	 * @param eventTypeMask the event type mask
	 * @param objectClass   the object class
	 * @param properties    the properties
	 * @return the runtime monitoring result assert
	 */
	public MonitoringAssertionResult hasExactlyOneServiceEventWith(int eventTypeMask, final Class<?> objectClass,
			Map<String, Object> properties);

	/**
	 * Checks for framework events that.
	 *
	 * @return the class based navigable list assert<?, list<? extends framework
	 *         event>, framework event, framework event assert>
	 */
	public ClassBasedNavigableListAssert<?, List<? extends FrameworkEvent>, FrameworkEvent, FrameworkEventAssert> hasFrameworkEventsThat();

	/**
	 * Checks for no bundle event.
	 *
	 * @return the runtime monitoring result assert
	 */
	public MonitoringAssertionResult hasNoBundleEvent();

	/**
	 * Checks for no framework event.
	 *
	 * @return the runtime monitoring result assert
	 */
	public MonitoringAssertionResult hasNoFrameworkEvent();

	/**
	 * Checks for no service event.
	 *
	 * @return the runtime monitoring result assert
	 */
	public MonitoringAssertionResult hasNoServiceEvent();

	/**
	 * Checks for no service event with the given parameter(s).
	 *
	 * @param eventTypeMask the event type mask
	 * @param objectClass   the object class
	 * @param properties    the properties
	 * @return the runtime monitoring result assert
	 */
	public MonitoringAssertionResult hasNoServiceEventWith(int eventTypeMask, final Class<?> objectClass,
			Map<String, Object> properties);

	/**
	 * Checks for no throwable.
	 *
	 * @return the runtime monitoring result assert
	 */
	public MonitoringAssertionResult hasNoThrowable();

	/**
	 * Checks for service events in exact order.
	 *
	 * @param conditions the conditions
	 * @return the runtime monitoring result assert
	 */
	public MonitoringAssertionResult hasServiceEventsInExactOrder(List<Condition<ServiceEvent>> conditions);

	/**
	 * Checks for service events in order.
	 *
	 * @param conditions the conditions
	 * @return the runtime monitoring result assert
	 */
	public MonitoringAssertionResult hasServiceEventsInOrder(List<Condition<ServiceEvent>> conditions);

	/**
	 * Checks for service events that.
	 *
	 * @return the class based navigable list assert<?, list<? extends service
	 *         event>, service event, service event assert>
	 */
	public ClassBasedNavigableListAssert<?, List<? extends ServiceEvent>, ServiceEvent, ServiceEventAssert> hasServiceEventsThat();

	/**
	 * Checks for throwable that.
	 *
	 * @return the throwable assert
	 */
	public ThrowableAssert hasThrowableThat();

	/**
	 * Checks for timed bundle events that.
	 *
	 * @return the list assert
	 */
	public ListAssert<TimedEvent<BundleEvent>> hasTimedBundleEventsThat();

	/**
	 * Checks for timed events that.
	 *
	 * @return the list assert
	 */
	public ListAssert<TimedEvent<Object>> hasTimedEventsThat();

	/**
	 * Checks for timed events that.
	 *
	 * @param <T>   the generic type
	 * @param clazz the clazz
	 * @return the list assert
	 */
	public <T> ListAssert<TimedEvent<T>> hasTimedEventsThat(Class<T> clazz);

	/**
	 * Checks for timed framework events that.
	 *
	 * @return the list assert
	 */
	public ListAssert<TimedEvent<FrameworkEvent>> hasTimedFrameworkEventsThat();

	/**
	 * Checks for timed service events that.
	 *
	 * @return the list assert
	 */
	public ListAssert<TimedEvent<ServiceEvent>> hasTimedServiceEventsThat();

	/**
	 * Checks if is not timed out.
	 *
	 * @return the runtime monitoring result assert
	 */
	public MonitoringAssertionResult isNotTimedOut();

	/**
	 * Checks if is timed out.
	 *
	 * @return the runtime monitoring result assert
	 */
	public MonitoringAssertionResult isTimedOut();

	/**
	 * Checks for at least N service event with the given parameter(s).
	 *
	 * @param n             the n
	 * @param eventTypeMask the event type mask
	 * @param objectClass   the object class
	 * @return the runtime monitoring result assert
	 */
	MonitoringAssertionResult hasAtLeastNServiceEventWith(int n, int eventTypeMask, Class<?> objectClass);

	/**
	 * Checks for at least N service event with the given parameter(s).
	 *
	 * @param n             the n
	 * @param eventTypeMask the event type mask
	 * @param objectClass   the object class
	 * @param properties    the properties
	 * @return the runtime monitoring result assert
	 */
	MonitoringAssertionResult hasAtLeastNServiceEventWith(int n, int eventTypeMask, Class<?> objectClass,
			Map<String, Object> properties);

	/**
	 * Checks for exactly N service event with the given parameter(s).
	 *
	 * @param n             the n
	 * @param eventTypeMask the event type mask
	 * @param objectClass   the object class
	 * @return the runtime monitoring result assert
	 */
	MonitoringAssertionResult hasExactlyNServiceEventWith(int n, int eventTypeMask, Class<?> objectClass);

	/**
	 * Checks for exactly N service event with the given parameter(s).
	 *
	 * @param n             the n
	 * @param eventTypeMask the event type mask
	 * @param objectClass   the object class
	 * @param properties    the properties
	 * @return the runtime monitoring result assert
	 */
	MonitoringAssertionResult hasExactlyNServiceEventWith(int n, int eventTypeMask, Class<?> objectClass,
			Map<String, Object> properties);

	/**
	 * Checks for at least N service event registered with the given parameter(s).
	 *
	 * @param n           the n
	 * @param objectClass the object class
	 * @return the runtime monitoring result assert
	 */
	MonitoringAssertionResult hasAtLeastNServiceEventRegisteredWith(int n, Class<?> objectClass);

	/**
	 * Checks for at least N service event registered with the given parameter(s).
	 *
	 * @param n           the n
	 * @param objectClass the object class
	 * @param properties  the properties
	 * @return the runtime monitoring result assert
	 */
	MonitoringAssertionResult hasAtLeastNServiceEventRegisteredWith(int n, Class<?> objectClass,
			Map<String, Object> properties);

	/**
	 * Checks for at most N service event registered with the given parameter(s).
	 *
	 * @param n           the n
	 * @param objectClass the object class
	 * @return the runtime monitoring result assert
	 */
	MonitoringAssertionResult hasAtMostNServiceEventRegisteredWith(int n, Class<?> objectClass);

	/**
	 * Checks for at most N service event registered with the given parameter(s).
	 *
	 * @param n           the n
	 * @param objectClass the object class
	 * @param properties  the properties
	 * @return the runtime monitoring result assert
	 */
	MonitoringAssertionResult hasAtMostNServiceEventRegisteredWith(int n, Class<?> objectClass,
			Map<String, Object> properties);

	/**
	 * Checks for exactly one service event registered with the given parameter(s).
	 *
	 * @param objectClass the object class
	 * @return the runtime monitoring result assert
	 */
	MonitoringAssertionResult hasExactlyOneServiceEventRegisteredWith(Class<?> objectClass);

	/**
	 * Checks for exactly one service event registered with the given parameter(s).
	 *
	 * @param objectClass the object class
	 * @param properties  the properties
	 * @return the runtime monitoring result assert
	 */
	MonitoringAssertionResult hasExactlyOneServiceEventRegisteredWith(Class<?> objectClass,
			Map<String, Object> properties);

	/**
	 * Checks for exactly N service event registered with the given parameter(s).
	 *
	 * @param n           the n
	 * @param objectClass the object class
	 * @return the runtime monitoring result assert
	 */
	MonitoringAssertionResult hasExactlyNServiceEventRegisteredWith(int n, Class<?> objectClass);

	/**
	 * Checks for exactly N service event registered with the given parameter(s).
	 *
	 * @param n           the n
	 * @param objectClass the object class
	 * @param properties  the properties
	 * @return the runtime monitoring result assert
	 */
	MonitoringAssertionResult hasExactlyNServiceEventRegisteredWith(int n, Class<?> objectClass,
			Map<String, Object> properties);

	/**
	 * Checks for at least N service event modified with the given parameter(s).
	 *
	 * @param n           the n
	 * @param objectClass the object class
	 * @return the runtime monitoring result assert
	 */
	MonitoringAssertionResult hasAtLeastNServiceEventModifiedWith(int n, Class<?> objectClass);

	/**
	 * Checks for at least N service event modified with the given parameter(s).
	 *
	 * @param n           the n
	 * @param objectClass the object class
	 * @param properties  the properties
	 * @return the runtime monitoring result assert
	 */
	MonitoringAssertionResult hasAtLeastNServiceEventModifiedWith(int n, Class<?> objectClass,
			Map<String, Object> properties);

	/**
	 * Checks for at most N service event modified with the given parameter(s).
	 *
	 * @param n           the n
	 * @param objectClass the object class
	 * @return the runtime monitoring result assert
	 */
	MonitoringAssertionResult hasAtMostNServiceEventModifiedWith(int n, Class<?> objectClass);

	/**
	 * Checks for at most N service event modified with the given parameter(s).
	 *
	 * @param n           the n
	 * @param objectClass the object class
	 * @param properties  the properties
	 * @return the runtime monitoring result assert
	 */
	MonitoringAssertionResult hasAtMostNServiceEventModifiedWith(int n, Class<?> objectClass,
			Map<String, Object> properties);

	/**
	 * Checks for exactly one service event modified with the given parameter(s).
	 *
	 * @param objectClass the object class
	 * @return the runtime monitoring result assert
	 */
	MonitoringAssertionResult hasExactlyOneServiceEventModifiedWith(Class<?> objectClass);

	/**
	 * Checks for exactly one service event modified with the given parameter(s).
	 *
	 * @param objectClass the object class
	 * @param properties  the properties
	 * @return the runtime monitoring result assert
	 */
	MonitoringAssertionResult hasExactlyOneServiceEventModifiedWith(Class<?> objectClass,
			Map<String, Object> properties);

	/**
	 * Checks for exactly N service event modified with the given parameter(s).
	 *
	 * @param n           the n
	 * @param objectClass the object class
	 * @return the runtime monitoring result assert
	 */
	MonitoringAssertionResult hasExactlyNServiceEventModifiedWith(int n, Class<?> objectClass);

	/**
	 * Checks for exactly N service event modified with the given parameter(s).
	 *
	 * @param n           the n
	 * @param objectClass the object class
	 * @param properties  the properties
	 * @return the runtime monitoring result assert
	 */
	MonitoringAssertionResult hasExactlyNServiceEventModifiedWith(int n, Class<?> objectClass,
			Map<String, Object> properties);

	/**
	 * Checks for no service event with the given parameter(s).
	 *
	 * @param eventTypeMask the event type mask
	 * @param filter        the filter
	 * @return the runtime monitoring result assert
	 * @throws InvalidSyntaxException the invalid syntax exception
	 */
	MonitoringAssertionResult hasNoServiceEventWith(int eventTypeMask, String filter) throws InvalidSyntaxException;

	/**
	 * Checks for exactly N service event with the given parameter(s).
	 *
	 * @param n             the n
	 * @param eventTypeMask the event type mask
	 * @param filter        the filter
	 * @return the runtime monitoring result assert
	 * @throws InvalidSyntaxException the invalid syntax exception
	 */
	MonitoringAssertionResult hasExactlyNServiceEventWith(int n, int eventTypeMask, String filter)
			throws InvalidSyntaxException;

	/**
	 * Checks for exactly one service event with the given parameter(s).
	 *
	 * @param eventTypeMask the event type mask
	 * @param filter        the filter
	 * @return the runtime monitoring result assert
	 * @throws InvalidSyntaxException the invalid syntax exception
	 */
	MonitoringAssertionResult hasExactlyOneServiceEventWith(int eventTypeMask, String filter)
			throws InvalidSyntaxException;

	/**
	 * Checks for at least N service event with the given parameter(s).
	 *
	 * @param n             the n
	 * @param eventTypeMask the event type mask
	 * @param filter        the filter
	 * @return the runtime monitoring result assert
	 * @throws InvalidSyntaxException the invalid syntax exception
	 */
	MonitoringAssertionResult hasAtLeastNServiceEventWith(int n, int eventTypeMask, String filter)
			throws InvalidSyntaxException;

	/**
	 * Checks for at least one service event with the given parameter(s).
	 *
	 * @param eventTypeMask the event type mask
	 * @param filter        the filter
	 * @return the runtime monitoring result assert
	 * @throws InvalidSyntaxException the invalid syntax exception
	 */
	MonitoringAssertionResult hasAtLeastOneServiceEventWith(int eventTypeMask, String filter)
			throws InvalidSyntaxException;

	/**
	 * Checks for at least N service event with condition.
	 *
	 * @param n         the n
	 * @param condition the condition
	 * @return the runtime monitoring result assert
	 */
	MonitoringAssertionResult hasAtLeastNServiceEventWithCondition(int n, Condition<ServiceEvent> condition);

	/**
	 * Checks for at most N service event with condition.
	 *
	 * @param n         the n
	 * @param condition the condition
	 * @return the runtime monitoring result assert
	 */
	MonitoringAssertionResult hasAtMostNServiceEventWithCondition(int n, Condition<ServiceEvent> condition);

	/**
	 * Checks for exactly N service event with condition.
	 *
	 * @param n         the n
	 * @param condition the condition
	 * @return the runtime monitoring result assert
	 */
	MonitoringAssertionResult hasExactlyNServiceEventWithCondition(int n, Condition<ServiceEvent> condition);

}
