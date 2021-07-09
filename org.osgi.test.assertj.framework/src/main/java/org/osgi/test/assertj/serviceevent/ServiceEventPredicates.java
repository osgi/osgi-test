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

package org.osgi.test.assertj.serviceevent;

import java.util.Dictionary;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.osgi.framework.Constants;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceReference;
import org.osgi.test.common.dictionary.Dictionaries;

/**
 * The Class ServiceEventPredicates.
 */
public class ServiceEventPredicates {

	private ServiceEventPredicates() {}
	/**
	 * Returns a predicate that tests if the object Service event.
	 *
	 * @return the predicate
	 */
	public static Predicate<Object> serviceEvent() {
		return e -> e instanceof ServiceEvent;
	}

	/**
	 * Returns a predicate that tests if the object matches the given predicate.
	 *
	 * @param predicate the predicate
	 * @return the predicate
	 */
	public static Predicate<Object> serviceEventAnd(Predicate<ServiceEvent> predicate) {
		return e -> serviceEvent().test(e) && predicate.test((ServiceEvent) e);
	}

	/**
	 * Returns a predicate that tests if the event-type matches the
	 * eventTypeMask.
	 *
	 * @param eventTypeMask the event type mask
	 * @return the predicate
	 */
	// ServiceEvents
	public static Predicate<ServiceEvent> type(final int eventTypeMask) {
		return e -> (e.getType() & eventTypeMask) != 0;
	}

	/**
	 * Returns a predicate that tests if the event-type is registered.
	 *
	 * @return the predicate
	 */
	public static Predicate<ServiceEvent> typeRegistered() {
		return e -> type(ServiceEvent.REGISTERED).test(e);
	}

	/**
	 * Returns a predicate that tests if the event-type is modified.
	 *
	 * @return the predicate
	 */
	public static Predicate<ServiceEvent> typeModified() {
		return e -> type(ServiceEvent.MODIFIED).test(e);
	}

	/**
	 * Returns a predicate that tests if the event-type is modified-endmatch.
	 *
	 * @return the predicate
	 */
	public static Predicate<ServiceEvent> typeModifiedEndmatch() {
		return e -> type(ServiceEvent.MODIFIED_ENDMATCH).test(e);
	}

	/**
	 * Returns a predicate that tests if the event-type is unregistering.
	 *
	 * @return the predicate
	 */
	public static Predicate<ServiceEvent> typeUnregistering() {
		return e -> type(ServiceEvent.UNREGISTERING).test(e);
	}

	/**
	 * Returns a predicate that tests if the Service of the event matches the
	 * objectClass.
	 *
	 * @param objectClass the objectClass
	 * @return the predicate
	 */
	public static Predicate<ServiceEvent> objectClass(final Class<?> objectClass) {

		return e -> {
			Object classes = e.getServiceReference()
				.getProperty(Constants.OBJECTCLASS);

			if (classes != null && classes instanceof String[]) {
				return Stream.of((String[]) classes)
					.filter(Objects::nonNull)
					.anyMatch(objectClass.getName()::equals);
			}
			return false;
		};

	}

	/**
	 * Returns a predicate that tests if the object Contain the service
	 * property.
	 *
	 * @param key the key
	 * @param value the value
	 * @return the predicate
	 */
	public static Predicate<ServiceEvent> containServiceProperty(final String key, Object value) {
		return e -> containsServiceProperties(Dictionaries.dictionaryOf(key, value)).test(e);
	}

	/**
	 * Returns a predicate that tests if the object Contains the service
	 * properties.
	 *
	 * @param map the map
	 * @return the predicate
	 */
	public static Predicate<ServiceEvent> containsServiceProperties(Map<String, ?> map) {
		return e -> {
			ServiceReference<?> sr = e.getServiceReference();
			List<String> keys = Stream.of(sr.getPropertyKeys())
				.collect(Collectors.toList());
			for (Entry<String, ?> entry : map.entrySet()) {
				if (!keys.contains(entry.getKey())) {
					return false;
				}
				if (!Objects.equals(sr.getProperty(entry.getKey()), entry.getValue())) {
					return false;
				}
			}
			return true;
		};
	}

	/**
	 * Returns a predicate that tests if the object Contains the service
	 * properties.
	 *
	 * @param properties the properties
	 * @return the predicate
	 */
	public static Predicate<ServiceEvent> containsServiceProperties(Dictionary<String, ?> properties) {
		return e -> containsServiceProperties(Dictionaries.asMap(properties)).test(e);
	}

	/**
	 * Returns a predicate that tests if the object Matches matches the given
	 * parameters.
	 *
	 * @param eventTypeMask the event type mask
	 * @param objectClass the objectClass
	 * @return the predicate
	 */
	public static Predicate<ServiceEvent> matches(int eventTypeMask, final Class<?> objectClass) {
		return e -> type(eventTypeMask).test(e) && (objectClass(objectClass).test(e));
	}

	/**
	 * Returns a predicate that tests if the object matches the given
	 * parameters.
	 *
	 * @param eventTypeMask the event type mask
	 * @param objectClass the objectClass
	 * @param properties the properties
	 * @return the predicate
	 */
	public static Predicate<ServiceEvent> matches(int eventTypeMask, final Class<?> objectClass,
		Map<String, ?> properties) {
		return e -> type(eventTypeMask).test(e) && (objectClass(objectClass).test(e))
			&& containsServiceProperties(properties).test(e);
	}

	/**
	 * Returns a predicate that tests if the object Matches the given
	 * parameters.
	 *
	 * @param eventTypeMask the event type mask
	 * @param objectClass the objectClass
	 * @param properties the properties
	 * @return the predicate
	 */
	public static Predicate<ServiceEvent> matches(int eventTypeMask, final Class<?> objectClass,
		Dictionary<String, ?> properties) {
		return e -> matches(eventTypeMask, objectClass, Dictionaries.asMap(properties)).test(e);
	}

	/**
	 * Returns a predicate that tests if the event-type is registered and
	 * matches the given objectClass.
	 *
	 * @param objectClass the objectClass
	 * @return the predicate
	 */
	public static Predicate<ServiceEvent> typeRegistered(final Class<?> objectClass) {
		return matches(ServiceEvent.REGISTERED, objectClass);
	}

	/**
	 * Returns a predicate that tests if the event-type is registered with the
	 * given objectClass and properties.
	 *
	 * @param objectClass the objectClass
	 * @param properties the properties
	 * @return the predicate
	 */
	public static Predicate<ServiceEvent> typeRegisteredWith(final Class<?> objectClass, Map<String, ?> properties) {
		return matches(ServiceEvent.REGISTERED, objectClass, properties);
	}

	/**
	 * Returns a predicate that tests if the event-type is registered with the
	 * given objectClass and properties.
	 *
	 * @param objectClass the objectClass
	 * @param dictionary the dictionary
	 * @return the predicate
	 */
	public static Predicate<ServiceEvent> typeRegisteredWith(final Class<?> objectClass,
		Dictionary<String, ?> dictionary) {
		return matches(ServiceEvent.REGISTERED, objectClass, dictionary);
	}

	/**
	 * Returns a predicate that tests if the event-type is unregistering and
	 * matches the given objectClass.
	 *
	 * @param objectClass the objectClass
	 * @return the predicate
	 */
	public static Predicate<ServiceEvent> typeUnregistering(final Class<?> objectClass) {
		return matches(ServiceEvent.UNREGISTERING, objectClass);
	}

	/**
	 * Returns a predicate that tests if the event-type is modified and matches
	 * the given objectClass.
	 *
	 * @param objectClass the objectClass
	 * @return the predicate
	 */
	public static Predicate<ServiceEvent> typeModified(final Class<?> objectClass) {
		return matches(ServiceEvent.MODIFIED, objectClass);
	}

	/**
	 * Returns a predicate that tests if the event-type is modified endmatch and
	 * matches the given objectClass.
	 *
	 * @param objectClass the objectClass
	 * @return the predicate
	 */
	public static Predicate<ServiceEvent> typeModifiedEndmatch(final Class<?> objectClass) {
		return matches(ServiceEvent.MODIFIED_ENDMATCH, objectClass);
	}
}
