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

import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.ServiceEvent;
import org.osgi.test.assertj.monitoring.internal.MonitoringAssertionImpl;


/**
 * The Interface MonitoringAssert.
 */
public interface MonitoringAssertion {

	/**
	 * Creates a MonitoringAssert.
	 *
	 * @param execute - the action that will be invoked.
	 * @return the runtime monitoring assert
	 */
	static MonitoringAssertion executeAndObserve(ThrowingCallable execute) {
		Objects.requireNonNull(execute, "Runnable must exist");
		return new MonitoringAssertionImpl(execute);
	}

	/**
	 * Until any service event is fired.
	 *
	 * @return MonitoringAssertTimeoutStep - to define the
	 *         observation-timeout.
	 */
	MonitoringAssertionTimeoutStep untilAnyServiceEvent();

	/**
	 * Until a service event that matches the given predicate is fired.
	 *
	 * @param predicate the predicate that must match
	 * @return MonitoringAssertTimeoutStep - to define the
	 *         observation-timeout.
	 */
	MonitoringAssertionTimeoutStep untilServiceEvent(Predicate<ServiceEvent> predicate);

	/**
	 * Until a service event registered with the given class is fired.
	 *
	 * @param objectClass the objectClass
	 * @return MonitoringAssertTimeoutStep - to define the
	 *         observation-timeout.
	 */
	MonitoringAssertionTimeoutStep untilServiceEventRegistered(Class<?> objectClass);

	/**
	 * Until a service event registered with the given class and properties is
	 * fired.
	 *
	 * @param objectClass the objectClass
	 * @param map         the properties
	 * @return MonitoringAssertTimeoutStep - to define the
	 *         observation-timeout.
	 */
	MonitoringAssertionTimeoutStep untilServiceEventRegistered(Class<?> objectClass,
			Map<String, Object> properties);

	/**
	 * Until a service event unregistered with the given class is fired.
	 *
	 * @param objectClass the objectClass
	 * @return MonitoringAssertTimeoutStep - to define the
	 *         observation-timeout.
	 */
	MonitoringAssertionTimeoutStep untilServiceEventUnregistered(Class<?> objectClass);

	/**
	 * /** Until a service event unregistered with the given class and properties is
	 * fired.
	 *
	 * @param objectClass the objectClass
	 * @param map         the properties
	 * @return MonitoringAssertTimeoutStep - to define the
	 *         observation-timeout.
	 */
	MonitoringAssertionTimeoutStep untilServiceEventUnregistered(Class<?> objectClass,
			Map<String, Object> properties);

	/**
	 * Until a service event modified with the given class is fired.
	 *
	 * @param objectClass the objectClass
	 * @return MonitoringAssertTimeoutStep - to define the
	 *         observation-timeout.
	 */
	MonitoringAssertionTimeoutStep untilServiceEventModified(Class<?> objectClass);

	/**
	 * Until a service event modified with the given class and properties is fired.
	 *
	 * @param objectClass the objectClass
	 * @param map         the properties
	 * @return MonitoringAssertTimeoutStep - to define the
	 *         observation-timeout.
	 */
	MonitoringAssertionTimeoutStep untilServiceEventModified(Class<?> objectClass,
			Map<String, Object> properties);

	/**
	 * Until a service event modified-endmatch with the given class is fired.
	 *
	 * @param clazz the objectClass
	 * @return MonitoringAssertTimeoutStep - to define the
	 *         observation-timeout.
	 */
	MonitoringAssertionTimeoutStep untilServiceEventModifiedEndmatch(Class<?> objectClass);

	/**
	 * Until a service modified-endmatch with the given class and properties is
	 * fired.
	 *
	 * @param objectClass the objectClass
	 * @param properties  the properties
	 * @return MonitoringAssertTimeoutStep - to define the
	 *         observation-timeout.
	 */
	MonitoringAssertionTimeoutStep untilServiceEventModifiedEndmatch(Class<?> objectClass,
			Map<String, Object> map);

	/**
	 * Until no more service event within given time is fired.
	 *
	 * @param timeMs the time ms
	 * @return MonitoringAssertTimeoutStep - to define the
	 *         observation-timeout.
	 */
	MonitoringAssertionTimeoutStep untilNoMoreServiceEventWithin(long timeMs);

	/**
	 * Until no more service event within given time is fired.
	 *
	 * @param timeMs    the time ms
	 * @param predicate the predicate
	 * @return MonitoringAssertTimeoutStep - to define the
	 *         observation-timeout.
	 */
	MonitoringAssertionTimeoutStep untilNoMoreServiceEventWithin(long timeMs,
			Predicate<ServiceEvent> predicate);

	/**
	 * Until no more service event within given time is fired.
	 *
	 * @param millis      the time in milliseconds
	 * @param objectClass the objectClass
	 * @return MonitoringAssertTimeoutStep - to define the
	 *         observation-timeout.
	 */
	MonitoringAssertionTimeoutStep untilNoMoreServiceEventOfClassWithin(long millis, Class<?> objectClass);

	/**
	 * Until no more service event within given time is fired.
	 *
	 * @param millis      the time in milliseconds
	 * @param objectClass the objectClass
	 * @param properties  the properties
	 * @return MonitoringAssertTimeoutStep - to define the
	 *         observation-timeout.
	 */
	MonitoringAssertionTimeoutStep untilNoMoreServiceEventOfClassWithin(long millis, Class<?> objectClass,
			Map<String, Object> properties);

	/**
	 * Until no more service event within given time is fired.
	 *
	 * @param millis      the time in milliseconds
	 * @param objectClass the objectClass
	 * @return MonitoringAssertTimeoutStep - to define the
	 *         observation-timeout.
	 */
	MonitoringAssertionTimeoutStep untilNoMoreServiceEventRegisteredWithin(long millis,
			Class<?> objectClass);

	/**
	 * Until no more service event within given time is fired. *
	 *
	 * @param millis      the time in milliseconds
	 * @param objectClass the objectClass
	 * @param properties  the properties
	 * @return MonitoringAssertTimeoutStep - to define the
	 *         observation-timeout.
	 */
	MonitoringAssertionTimeoutStep untilNoMoreServiceEventRegistersWithin(long millis, Class<?> objectClass,
			Map<String, Object> properties);

	/**
	 * Until no more service event within given time is fired.
	 *
	 * @param millis      the time in milliseconds
	 * @param objectClass the objectClass
	 * @return MonitoringAssertTimeoutStep - to define the
	 *         observation-timeout.
	 */
	MonitoringAssertionTimeoutStep untilNoMoreServiceEventUnregistersWithin(long millis,
			Class<?> objectClass);

	/**
	 * Until no more service event within given time is fired.
	 *
	 * @param millis      the time in milliseconds
	 * @param objectClass the objectClass
	 * @param properties  the properties
	 * @return MonitoringAssertTimeoutStep - to define the
	 *         observation-timeout.
	 */
	MonitoringAssertionTimeoutStep untilNoMoreServiceEventUnregistersWithin(long millis,
			Class<?> objectClass, Map<String, Object> properties);

	/**
	 * Until no more service event within given time is fired.
	 *
	 * @param millis      the time in milliseconds
	 * @param objectClass the objectClass
	 * @return MonitoringAssertTimeoutStep - to define the
	 *         observation-timeout.
	 */
	MonitoringAssertionTimeoutStep untilNoMoreServiceEventModifiedWithin(long millis, Class<?> objectClass);

	/**
	 * Until no more service event within given time is fired.
	 *
	 * @param millis      the time in milliseconds
	 * @param objectClass the objectClass
	 * @param properties  the properties
	 * @return MonitoringAssertTimeoutStep - to define the
	 *         observation-timeout.
	 */
	MonitoringAssertionTimeoutStep untilNoMoreServiceEventModifiedWithin(long millis, Class<?> objectClass,
			Map<String, Object> properties);

	/**
	 * Until no more service event within given time is fired.
	 *
	 * @param millis      the time in milliseconds
	 * @param objectClass the objectClass
	 * @return MonitoringAssertTimeoutStep - to define the
	 *         observation-timeout.
	 */
	MonitoringAssertionTimeoutStep untilNoMoreServiceEventModifiedEndmatchWithin(long millis,
			Class<?> objectClass);

	/**
	 * Until no more service event within given time is fired.
	 *
	 * @param millis      the time in milliseconds
	 * @param objectClass the objectClass
	 * @param properties  the properties
	 * @return MonitoringAssertTimeoutStep - to define the
	 *         observation-timeout.
	 */
	MonitoringAssertionTimeoutStep untilNoMoreServiceEventModifiedEndmatchWithin(long millis,
			Class<?> objectClass, Map<String, Object> map);

	/**
	 * Until a bundle event matching the given predicate is fired.
	 *
	 * @param predicate the predicate
	 * @return MonitoringAssertTimeoutStep - to define the
	 *         observation-timeout.
	 */
	MonitoringAssertionTimeoutStep untilBundleEvent(Predicate<BundleEvent> predicate);

	/**
	 * Until a framework event matching the given predicate is fired.
	 *
	 * @param predicate the predicate
	 * @return MonitoringAssertTimeoutStep - to define the
	 *         observation-timeout.
	 */
	MonitoringAssertionTimeoutStep untilFrameworkEvent(Predicate<FrameworkEvent> predicate);

	/**
	 * will stop observation of the events direct after executing the action.
	 *
	 * @return the runtime monitoring result assert
	 */
	MonitoringAssertionResult assertThat();

	/**
	 * will stop observation of the events at least after the given timeout.
	 *
	 * @param timeout - the maximal time in that actions will be observed. Could be
	 *                lower if an other `until`-Condition is set.
	 * @return the runtime monitoring result assert
	 */
	MonitoringAssertionResult assertWithTimeoutThat(int timeout);

}
