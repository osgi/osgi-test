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

import static org.assertj.core.api.Assertions.not;

import java.util.Dictionary;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.Condition;
import org.assertj.core.api.ListAssert;
import org.assertj.core.api.ObjectAssert;
import org.assertj.core.condition.MappedCondition;
import org.assertj.core.condition.VerboseCondition;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceReference;
import org.osgi.test.assertj.servicereference.ServicePropertiesConditions;
import org.osgi.test.assertj.servicereference.ServiceReferenceConditions;
import org.osgi.test.common.bitmaps.Bitmap;
import org.osgi.test.common.bitmaps.ServiceEventType;
import org.osgi.test.common.dictionary.Dictionaries;

/**
 * A Utility-Class thats Provides public public static methods to create
 * {@link Condition}s for an {@link ServiceEvent}
 *
 * @since 1.1
 */
public final class ServiceEventConditions {

	/**
	 * Creates a {@link Condition} to be met by an {@link ServiceEvent}.
	 * Checking if a given type-mask <b>matches</b> the type and the given
	 * Class<?> objectClass matches a objectClass of the ServiceReference.
	 *
	 * <pre>
	 * List<ServiceEvent> serviceEvents = null;
	 *
	 * public public static void example_matches(int eventTypeMask, Class<?> objectClass) {
	 *
	 * 	assertThat(serviceEvents)// created an {@link ListAssert}
	 * 		.have(matches(eventTypeMask, objectClass))
	 * 		.filteredOn(matches(eventTypeMask, objectClass))
	 * 		.first()// map to {@link ObjectAssert}
	 * 		.has(matches(eventTypeMask, objectClass));// used on
	 * 	// {@link ObjectAssert}
	 * }
	 * </pre>
	 *
	 * @param eventTypeMask - the typeMask that would be checked against the
	 *            bundle type of the {@link ServiceEvent}
	 * @param objectClass - the objectClass that would be tested against the
	 *            ServiceReference
	 * @return the Condition
	 */
	public static Condition<ServiceEvent> matches(int eventTypeMask, final Class<?> objectClass) {
		Condition<ServiceEvent> cType = type(eventTypeMask);
		Condition<ServiceEvent> cObjectClass = serviceReferenceHas(ServiceReferenceConditions.objectClass(objectClass));
		return Assertions.allOf(cType, cObjectClass);
	}

	/**
	 * Creates a {@link Condition} to be met by an {@link ServiceEvent}.
	 * Checking if a given type-mask <b>matches</b> the type and the given
	 * String filter matches the ServiceReference.
	 *
	 * <pre>
	 * List<ServiceEvent> serviceEvents = null;
	 *
	 * public public static void example_matches(int eventTypeMask, String filter) {
	 *
	 * 	assertThat(serviceEvents)// created an {@link ListAssert}
	 * 		.have(matches(eventTypeMask, filter))
	 * 		.filteredOn(matches(eventTypeMask, filter))
	 * 		.first()// map to {@link ObjectAssert}
	 * 		.has(matches(eventTypeMask, filter));// used on
	 * 	// {@link ObjectAssert}
	 * }
	 * </pre>
	 *
	 * @param eventTypeMask - the typeMask that would be checked against the
	 *            bundle type of the {@link ServiceEvent}
	 * @param filter - the filter String would be tested against the
	 *            ServiceReference
	 * @return the Condition
	 */
	public static Condition<ServiceEvent> matches(int eventTypeMask, final String filter) throws InvalidSyntaxException {
		Condition<ServiceEvent> cType = type(eventTypeMask);
		Condition<ServiceEvent> cObjectClass = serviceReferenceHas(
			ServiceReferenceConditions.serviceReferenceMatch(filter));
		return Assertions.allOf(cType, cObjectClass);
	}

	/**
	 * Creates a {@link Condition} to be met by an {@link ServiceEvent}.
	 * Checking if a given type-mask <b>matches</b> the type and the given
	 * Class<?> objectClass matches a objectClass of the serviceReference and
	 * given dictionary's entries are contained in the serviceProperties of the
	 * ServiceReverence of the {@link ServiceEvent}
	 *
	 * <pre>
	 * List<ServiceEvent> serviceEvents = null;
	 *
	 * public public static void example_matches(int eventTypeMask, Class<?> objectClass,
	 * 	Dictionary<String, Object> dictionary) {
	 *
	 * 	assertThat(serviceEvents)// created an {@link ListAssert}
	 * 		.have(matches(eventTypeMask, objectClass, dictionary))
	 * 		.filteredOn(matches(eventTypeMask, objectClass, dictionary))
	 * 		.first()// map to {@link ObjectAssert}
	 * 		.has(matches(eventTypeMask, objectClass, dictionary));// used on
	 * 	// {@link ObjectAssert}
	 * }
	 * </pre>
	 *
	 * @param eventTypeMask - the typeMask that would be checked against the
	 *            bundle type of the {@link ServiceEvent}
	 * @param objectClass - the objectClass that would be tested against the
	 *            ServiceReference
	 * @param dictionary - the dictionary's entries that would be tested against
	 *            the ServiceReference serviceProperties
	 * @return the Condition
	 */
	public static Condition<ServiceEvent> matches(int eventTypeMask, final Class<?> objectClass,
		Dictionary<String, Object> dictionary) {
		return matches(eventTypeMask, objectClass, Dictionaries.asMap(dictionary));
	}

	/**
	 * Creates a {@link Condition} to be met by an {@link ServiceEvent}.
	 * Checking if a given type-mask <b>matches</b> the type and the given
	 * Class<?> objectClass matches a objectClass of the serviceReference and
	 * given Map's entries are contained in the serviceProperties of the
	 * ServiceReverence of the {@link ServiceEvent}
	 *
	 * <pre>
	 * List<ServiceEvent> serviceEvents = null;
	 *
	 * public public static void example_matches(int eventTypeMask, Class<?> objectClass, Map<String, Object> map) {
	 *
	 * 	assertThat(serviceEvents)// created an {@link ListAssert}
	 * 		.have(matches(eventTypeMask, objectClass, map))
	 * 		.filteredOn(matches(eventTypeMask, objectClass, map))
	 * 		.first()// map to {@link ObjectAssert}
	 * 		.has(matches(eventTypeMask, objectClass, map));
	 * 	// used on {@link ObjectAssert}
	 * }
	 * </pre>
	 *
	 * @param eventTypeMask - the typeMask that would be checked against the bundle
	 *            type of the {@link ServiceEvent}
	 * @param objectClass - the objectClass that would be tested against the
	 *            ServiceReference
	 * @param map - the maps entries that would be tested against the
	 *            ServiceReference serviceProperties
	 * @return the Condition
	 */
	public static Condition<ServiceEvent> matches(int eventTypeMask, final Class<?> objectClass, Map<String, Object> map) {
		Condition<ServiceEvent> cType = type(eventTypeMask);
		Condition<ServiceEvent> cObjectClass = serviceReferenceHas(ServiceReferenceConditions.objectClass(objectClass));
		Condition<ServiceEvent> cProperties = serviceReferenceHas(
			ServiceReferenceConditions.servicePropertiesHas(ServicePropertiesConditions.servicePropertiesContains(map)));
		return Assertions.allOf(cType, cObjectClass, cProperties);
	}

	/**
	 * Creates a {@link Condition} to be met by an {@link ServiceEvent}.
	 * Checking if the serviceReference of the {@link ServiceEvent}
	 * <b>equals</b> the given serviceReference.
	 *
	 * <pre>
	 * List<ServiceEvent> serviceEvent = null;
	 *
	 * public public static void example_serviceReferenceEquals(ServiceReference<?> serviceReference) {
	 *
	 * 	assertThat(serviceEvent)// created an {@link ListAssert}
	 * 		.have(serviceReferenceEquals(serviceReference))
	 * 		.filteredOn(serviceReferenceEquals(serviceReference))
	 * 		.first()// map to {@link ObjectAssert}
	 * 		.has(serviceReferenceEquals(serviceReference));// used on
	 * 	// {@link ObjectAssert}
	 * }
	 * </pre>
	 *
	 * @return the Condition
	 */
	public static Condition<ServiceEvent> serviceReferenceEquals(ServiceReference<?> serviceReference) {

		return MappedCondition.mappedCondition(ServiceEvent::getServiceReference,
			ServiceReferenceConditions.isEqualsTo(serviceReference), "ServiceEvent::getServiceReference");
	}

	/**
	 * Creates a {@link Condition} to be met by an {@link ServiceEvent}.
	 * Checking if the serviceReference of the {@link ServiceEvent}
	 * <b>matches</b> the given serviceReferenceCondition {@link Condition}.
	 *
	 * <pre>
	 * List<ServiceEvent> serviceEvent = null;
	 *
	 * public public static void example_serviceReferenceHas(Condition<ServiceReference<?>> serviceReferenceCondition) {
	 *
	 * 	assertThat(serviceEvent)// created an {@link ListAssert}
	 * 		.have(serviceReferenceHas(serviceReferenceCondition))
	 * 		.filteredOn(serviceReferenceHas(serviceReferenceCondition))
	 * 		.first()// map to {@link ObjectAssert}
	 * 		.has(serviceReferenceHas(serviceReferenceCondition));// used on
	 * 	// {@link ObjectAssert}
	 * }
	 * </pre>
	 *
	 * @return the Condition
	 */
	public static Condition<ServiceEvent> serviceReferenceHas(Condition<ServiceReference<?>> serviceReferenceCondition) {
		Condition<ServiceEvent> mc = MappedCondition.mappedCondition(ServiceEvent::getServiceReference,
			serviceReferenceCondition, "ServiceEvent to ServiceReference using ServiceEvent::getServiceReference");
		return Assertions.allOf(serviceReferenceIsNotNull(), mc);
	}

	/**
	 * Creates a {@link Condition} to be met by an {@link ServiceEvent}.
	 * Checking if the serviceReference of the {@link ServiceEvent} <b>is not
	 * null</b>.
	 *
	 * <pre>
	 * List<ServiceEvent> serviceEvent = null;
	 *
	 * public public static void example_serviceReferenceIsNull() {
	 *
	 * 	assertThat(serviceEvent)// created an {@link ListAssert}
	 * 		.have(serviceReferenceIsNotNull())
	 * 		.filteredOn(serviceReferenceIsNotNull())
	 * 		.first()// map to {@link ObjectAssert}
	 * 		.has(serviceReferenceIsNotNull());// used on {@link ObjectAssert}
	 * }
	 * </pre>
	 *
	 * @return the Condition
	 */
	public static Condition<ServiceEvent> serviceReferenceIsNotNull() {
		return not(serviceReferenceIsNull());
	}

	/**
	 * Creates a {@link Condition} to be met by an {@link ServiceEvent}.
	 * Checking if the serviceReference of the {@link ServiceEvent} <b>is
	 * null</b>.
	 *
	 * <pre>
	 * List<ServiceEvent> serviceEvent = null;
	 *
	 * public public static void example_serviceReferenceIsNull() {
	 *
	 * 	assertThat(serviceEvent)// created an {@link ListAssert}
	 * 		.have(serviceReferenceIsNull())
	 * 		.filteredOn(serviceReferenceIsNull())
	 * 		.first()// map to {@link ObjectAssert}
	 * 		.has(serviceReferenceIsNull());// used on {@link ObjectAssert}
	 * }
	 * </pre>
	 *
	 * @return the Condition
	 */
	public static Condition<ServiceEvent> serviceReferenceIsNull() {

		return VerboseCondition.verboseCondition((sericeEvent) -> sericeEvent.getServiceReference() == null,
			"serviceReference is <null>", (se) -> se.getServiceReference()
				.toString());

	}

	/**
	 * Creates a {@link Condition} to be met by an {@link ServiceEvent}.
	 * Checking if a given type-mask <b>matches</b> the type.
	 *
	 * <pre>
	 * List<ServiceEvent> serviceEvents = null;
	 *
	 * public public static void example_type(int typeMask) {
	 *
	 * 	assertThat(serviceEvents)// created an {@link ListAssert}
	 * 		.have(type(type))
	 * 		.filteredOn(type(typeMask))
	 * 		.first()// map to {@link ObjectAssert}
	 * 		.has(type(typeMask));// used on {@link ObjectAssert}
	 * }
	 * </pre>
	 *
	 * @param eventTypeMask - the typeMask that would be checked against the
	 *            bundle type of the {@link ServiceEvent}
	 * @return the Condition
	 */
	public static Condition<ServiceEvent> type(final int eventTypeMask) {

		return VerboseCondition.verboseCondition(
			(ServiceEvent se) -> Bitmap.typeMatchesMask(se.getType(), eventTypeMask),
			"type matches mask <" + ServiceEventType.BITMAP.maskToString(eventTypeMask) + ">", //
			se -> " but was <" + ServiceEventType.BITMAP.toString(se.getType()) + ">");
	}

}
