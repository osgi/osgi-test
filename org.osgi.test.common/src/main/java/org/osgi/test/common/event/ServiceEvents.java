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

public class ServiceEvents extends Events {

	private ServiceEvents() {}

	public static Predicate<Object> isServiceEvent() {
		return e -> e instanceof ServiceEvent;
	}

	public static Predicate<Object> isServiceEventAnd(Predicate<ServiceEvent> predicate) {
		return e -> isServiceEvent().test(e) && predicate.test((ServiceEvent) e);
	}

	// ServiceEvents
	public static Predicate<ServiceEvent> isType(final int eventTypeMask) {
		return e -> (e.getType() & eventTypeMask) != 0;
	}

	public static Predicate<ServiceEvent> isTypeRegistered() {
		return e -> isType(ServiceEvent.REGISTERED).test(e);
	}

	public static Predicate<ServiceEvent> isTypeModified() {
		return e -> isType(ServiceEvent.MODIFIED).test(e);
	}

	public static Predicate<ServiceEvent> isTypeModifiedEndmatch() {
		return e -> isType(ServiceEvent.MODIFIED_ENDMATCH).test(e);
	}

	public static Predicate<ServiceEvent> isTypeUnregistering() {
		return e -> isType(ServiceEvent.UNREGISTERING).test(e);
	}

	public static Predicate<ServiceEvent> hasObjectClass(final Class<?> objectClass) {

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

	public static Predicate<ServiceEvent> containServiceProperty(final String key, Object value) {
		return e -> containsServiceProperties(Dictionaries.dictionaryOf(key, value)).test(e);
	}

	public static Predicate<ServiceEvent> containsServiceProperties(Map<String, Object> map) {
		return e -> {
			ServiceReference<?> sr = e.getServiceReference();
			List<String> keys = Stream.of(sr.getPropertyKeys())
				.collect(Collectors.toList());
			for (Entry<String, Object> entry : map.entrySet()) {
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

	public static Predicate<ServiceEvent> containsServiceProperties(Dictionary<String, Object> dictionary) {
		return e -> containsServiceProperties(Dictionaries.asMap(dictionary)).test(e);
	}

	public static Predicate<ServiceEvent> matches(int eventTypeMask, final Class<?> objectClass) {
		return e -> isType(eventTypeMask).test(e) && (hasObjectClass(objectClass).test(e));
	}

	public static Predicate<ServiceEvent> matches(int eventTypeMask, final Class<?> objectClass,
		Map<String, Object> map) {
		return e -> isType(eventTypeMask).test(e) && (hasObjectClass(objectClass).test(e))
			&& containsServiceProperties(map).test(e);
	}

	public static Predicate<ServiceEvent> matches(int eventTypeMask, final Class<?> objectClass,
		Dictionary<String, Object> dictionary) {
		return e -> matches(eventTypeMask, objectClass, Dictionaries.asMap(dictionary)).test(e);
	}

	public static Predicate<ServiceEvent> isTypeRegistered(final Class<?> objectClass) {
		return matches(ServiceEvent.REGISTERED, objectClass);
	}

	public static Predicate<ServiceEvent> isTypeRegisteredWith(final Class<?> objectClass, Map<String, Object> map) {
		return matches(ServiceEvent.REGISTERED, objectClass, map);
	}

	public static Predicate<ServiceEvent> isTypeRegisteredWith(final Class<?> objectClass,
		Dictionary<String, Object> dictionary) {
		return matches(ServiceEvent.REGISTERED, objectClass, dictionary);
	}

	public static Predicate<ServiceEvent> isTypeUnregistering(final Class<?> objectClass) {
		return matches(ServiceEvent.UNREGISTERING, objectClass);
	}

	public static Predicate<ServiceEvent> isTypeModified(final Class<?> objectClass) {
		return matches(ServiceEvent.MODIFIED, objectClass);
	}

	public static Predicate<ServiceEvent> isTypeModifiedEndmatch(final Class<?> objectClass) {
		return matches(ServiceEvent.MODIFIED_ENDMATCH, objectClass);
	}
}
