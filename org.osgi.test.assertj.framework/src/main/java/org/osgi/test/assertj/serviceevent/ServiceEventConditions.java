package org.osgi.test.assertj.serviceevent;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.Condition;
import org.osgi.framework.ServiceEvent;
import org.osgi.test.common.bitmaps.ServiceEventType;
import org.osgi.test.common.dictionary.Dictionaries;
import org.osgi.test.common.event.ServiceEvents;

public class ServiceEventConditions {

	public static Condition<ServiceEvent> isType(final int eventTypeMask) {
		return new Condition<ServiceEvent>(ServiceEvents.isType(eventTypeMask), "isType with mask '%s' (%s)",
			eventTypeMask, ServiceEventType.BITMAP.maskToString(eventTypeMask));
	}

	public static Condition<ServiceEvent> isTypeRegistered() {
		return isType(ServiceEvent.REGISTERED);
	}

	public static Condition<ServiceEvent> isTypeModified() {
		return isType(ServiceEvent.MODIFIED);
	}

	public static Condition<ServiceEvent> isTypeModifiedEndmatch() {
		return isType(ServiceEvent.MODIFIED_ENDMATCH);
	}

	public static Condition<ServiceEvent> isTypeUnregistering() {
		return isType(ServiceEvent.UNREGISTERING);
	}

	public static Condition<ServiceEvent> containsServiceProperties(Map<String, Object> map) {
		return

		new Condition<ServiceEvent>(ServiceEvents.containsServiceProperties(map), "contains ServiceProperties %s", map);
	}

	public static Condition<ServiceEvent> containsServiceProperties(Dictionary<String, Object> dictionary) {
		return containsServiceProperties(Dictionaries.asMap(dictionary));
	}

	public static Condition<ServiceEvent> hasObjectClass(final Class<?> objectClass) {

		return new Condition<ServiceEvent>(ServiceEvents.hasObjectClass(objectClass), "has Objectclass %s",
			objectClass.getName());
	}

	public static Condition<ServiceEvent> containServiceProperty(final String key, Object value) {
		return containsServiceProperties(Dictionaries.dictionaryOf(key, value));
	}

	public static Condition<ServiceEvent> matches(int eventTypeMask, final Class<?> objectClass,
		Map<String, Object> map) {

		return Assertions.allOf(isType(eventTypeMask), hasObjectClass(objectClass), containsServiceProperties(map));
	}

	public static Condition<ServiceEvent> matches(int eventTypeMask, final Class<?> objectClass,
		Dictionary<String, Object> dictionary) {
		return matches(eventTypeMask, objectClass, Dictionaries.asMap(dictionary));
	}

	public static Condition<ServiceEvent> matches(int eventTypeMask, final Class<?> objectClass) {
		return matches(eventTypeMask, objectClass, new HashMap<String, Object>());
	}

	public static Condition<ServiceEvent> isTypeRegistered(final Class<?> objectClass) {
		return matches(ServiceEvent.REGISTERED, objectClass);
	}

	public static Condition<ServiceEvent> isTypeRegisteredWith(final Class<?> objectClass, Map<String, Object> map) {
		return matches(ServiceEvent.REGISTERED, objectClass, map);
	}

	public static Condition<ServiceEvent> isTypeRegisteredWith(final Class<?> objectClass,
		Dictionary<String, Object> dictionary) {
		return matches(ServiceEvent.REGISTERED, objectClass, dictionary);
	}

	public static Condition<ServiceEvent> isTypeUnregistering(final Class<?> objectClass) {
		return matches(ServiceEvent.UNREGISTERING, objectClass);
	}

	public static Condition<ServiceEvent> isTypeModified(final Class<?> objectClass) {
		return matches(ServiceEvent.MODIFIED, objectClass);
	}

	public static Condition<ServiceEvent> isTypeModifiedEndmatch(final Class<?> objectClass) {
		return matches(ServiceEvent.MODIFIED_ENDMATCH, objectClass);
	}
}
