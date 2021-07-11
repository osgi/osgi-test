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

package org.osgi.test.assertj.servicereference;

import java.util.Dictionary;
import java.util.Objects;
import java.util.stream.Stream;

import org.assertj.core.api.Condition;
import org.assertj.core.api.ListAssert;
import org.assertj.core.api.ObjectAssert;
import org.assertj.core.condition.MappedCondition;
import org.assertj.core.condition.VerboseCondition;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.test.common.dictionary.Dictionaries;

/**
 * A Utility-Class thats Provides static methods to create {@link Condition}s
 * for an {@link ServiceReference}
 */
public interface ServiceReferenceConditions {

	static Condition<ServiceReference<?>> serviceReferenceMatch(String filter) throws InvalidSyntaxException {
		Filter f = FrameworkUtil.createFilter(filter);

		return new Condition<ServiceReference<?>>(sr -> {
			return f.match(sr);

		}, "machts filter %s", filter);

	}

	/**
	 * Creates a {@link Condition} to be met by an {@link ServiceReference}.
	 * Checking if a {@link ServiceReference} is <b>equal</b> an other
	 * ServiceReference.
	 * <p>
	 * Example:
	 *
	 * <pre>
	 * List<ServiceReference> serviceReferences = null;
	 *
	 * static void sameAs(ServiceReference serviceReference) {
	 *
	 * 	assertThat(serviceReferences)// created an {@link ListAssert}
	 * 		.have(sameAs(serviceReference))
	 * 		.filteredOn(sameAs(serviceReference))
	 * 		.first()// map to {@link
	 * 				// ObjectAssert}
	 * 		.is(sameAs(serviceReference));// used on {@link ObjectAssert}
	 * }
	 * </pre>
	 *
	 * @param serviceReferences - the expected serviceReferences that would be
	 *            checked against other {@link ServiceReference}s
	 * @return the Condition<br>
	 */
	static Condition<ServiceReference<?>> sameAs(ServiceReference<?> serviceReference) {
		Condition<ServiceReference<?>> c = VerboseCondition.verboseCondition(sr -> sr.equals(serviceReference),
			"serviceReference equals", ServiceReference::toString);
		return c;
	}

	/**
	 * Creates a {@link Condition} to be met by an {@link ServiceReference}.
	 * Checking if type of {@link ServiceReference} <b>matches</b> the given
	 * Class<?> objectClass matches a objectClass of the ServiceReference.
	 *
	 * <pre>
	 * List<ServiceReference<?>> serviceEvents = null;
	 *
	 * static void example_objectClass(Class<?> objectClass) {
	 *
	 * 	assertThat(serviceEvents)// created an {@link ListAssert}
	 * 		.have(objectClass(objectClass))
	 * 		.filteredOn(objectClass(objectClass))
	 * 		.first()// map to {@link // ObjectAssert}
	 * 		.has(objectClass(objectClass));// used on {@link
	 * 	// ObjectAssert}
	 * }
	 * </pre>
	 *
	 * @param objectClass - the objectClass that would be tested against the
	 *            ServiceReference
	 * @return the Condition
	 */

	static Condition<ServiceReference<?>> objectClass(final Class<?> objectClass) {
		return new Condition<ServiceReference<?>>(sr -> {
			Object classes = sr.getProperty(Constants.OBJECTCLASS);
			if (classes != null && classes instanceof String[]) {
				return Stream.of((String[]) classes)
					.filter(Objects::nonNull)
					.anyMatch(objectClass.getName()::equals);
			}
			return false;
		}, "has Objectclass %s", objectClass.getName());
	}

	/**
	 * Creates a {@link Condition} to be met by an {@link ServiceReference}.
	 * Checking if the serviceProperties of the {@link ServiceReference}
	 * <b>matches</b> the given Condition<Dictionary<String, Object>>.
	 *
	 * <pre>
	 * List<ServiceReference> serviceReference = null;
	 *
	 * static void servicePropertiesHas(Condition<Dictionary<String,Object>> condition)) {
	 *
	 * assertThat(serviceReference)// created an {@link ListAssert}
	 *   .have(servicePropertiesHas(condition))
	 *   .filteredOn(servicePropertiesHas(condition))
	 *   .first()// map to {@link ObjectAssert}
	 *   .has(servicePropertiesHas(condition));// used on {@link
	 ObjectAssert}
	 * }
	 * </pre>
	 *
	 * @return the Condition
	 */
	static Condition<ServiceReference<?>> servicePropertiesHas(Condition<Dictionary<String, Object>> condition) {
		return MappedCondition.mappedCondition(sr -> Dictionaries.asDictionary(sr), condition,
			"ServiceReference to Dictionary");
	}

	/**
	 * Creates a {@link Condition} to be met by an {@link ServiceReference}.
	 * Checking if the serviceProperties of the {@link ServiceReference} <b>is
	 * not null</b>.
	 *
	 * <pre>
	 * List<ServiceReference> serviceReference = null;
	 *
	 * static void example_servicePropertiesIsNotNull() {
	 *
	 * 	assertThat(serviceReference)// created an {@link ListAssert}
	 * 		.have(servicePropertiesIsNotNull())
	 * 		.filteredOn(servicePropertiesIsNotNull())
	 * 		.first()// map to {@link
	 * 				// ObjectAssert}
	 * 		.has(servicePropertiesIsNotNull());// used on {@link
	 * 											// ObjectAssert}
	 * }
	 * </pre>
	 *
	 * @return the Condition
	 */

	/**
	 * TODO: with switch to osgi.core R7 static Condition<ServiceReference<?>>
	 * servicePropertiesIsNotNull() { return not(servicePropertiesIsNull()); }
	 */

	/**
	 * Creates a {@link Condition} to be met by an {@link ServiceReference}.
	 * Checking if the serviceProperties of the {@link ServiceReference} <b>is
	 * null</b>.
	 *
	 * <pre>
	 * List<ServiceReference> serviceReference = null;
	 *
	 * static void example_servicePropertiesIsNull() {
	 *
	 * 	assertThat(serviceReference)// created an {@link ListAssert}
	 * 		.have(servicePropertiesIsNull())
	 * 		.filteredOn(servicePropertiesIsNull())
	 * 		.first()// map to {@link
	 * 				// ObjectAssert}
	 * 		.has(servicePropertiesIsNull());// used on {@link ObjectAssert}
	 * }
	 * </pre>
	 *
	 * @return the Condition
	 */
	/**
	 * TODO: with switch to osgi.core R7 static Condition<ServiceReference<?>>
	 * servicePropertiesIsNull() { return
	 * Descriptive.descriptive((Dictionary<String, Object>) null, (sr, d) ->
	 * Dictionaries.asDictionary(sr) == d, "serviceProperties is"); }
	 */
}
