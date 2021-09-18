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

package org.osgi.test.assertj.frameworkevent;

import static org.assertj.core.api.Assertions.allOf;
import static org.assertj.core.api.Assertions.not;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.Condition;
import org.assertj.core.api.ListAssert;
import org.assertj.core.api.ObjectAssert;
import org.assertj.core.condition.MappedCondition;
import org.assertj.core.condition.VerboseCondition;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.FrameworkEvent;
import org.osgi.test.assertj.bundle.BundleConditions;
import org.osgi.test.common.bitmaps.Bitmap;
import org.osgi.test.common.bitmaps.FrameworkEventType;

/**
 * A Utility-Class thats Provides public static methods to create
 * {@link FrameworkEventConditions} for an {@link FrameworkEvent}
 */
public final class FrameworkEventConditions {

	/**
	 * Creates a {@link Condition} to be met by an {@link FrameworkEvent}.
	 * Checking if a {@link Bundle} is <b>equal</b> the bundle that is
	 * associated with the event {@link BundleEvent}.
	 * <p>
	 * Example:
	 *
	 * <pre>
	 * List<FrameworkEvent> frameworkEvents = null;
	 *
	 * public static void example_bundleEquals(Bundle bundle) {
	 *
	 * 	assertThat(bundleEvents)// created an {@link ListAssert}
	 * 		.have(bundleEquals(bundle))
	 * 		.filteredOn(bundleEquals(bundle))
	 * 		.first()// map to {@link ObjectAssert}
	 * 		.has(bundleEquals(bundle));// used on {@link ObjectAssert}
	 * }
	 * </pre>
	 *
	 * @param bundle - the bundle that would be checked against the bundle of
	 *            the {@link FrameworkEvent}
	 * @return the Condition<br>
	 */
	public static Condition<FrameworkEvent> bundleEquals(Bundle bundle) {

		return MappedCondition.mappedCondition(FrameworkEvent::getBundle, BundleConditions.sameAs(bundle),
			"FrameworkEvent::getBundle");

	}

	/**
	 * Creates a {@link Condition} to be met by an {@link FrameworkEvent}.
	 * Checking if the bundle that is associated with the eventof the
	 * {@link FrameworkEvent} <b>is not null</b>.
	 *
	 * <pre>
	 * List<FrameworkEvent> frameworkEvents = null;
	 *
	 * public static void example_bundleIsNotNull() {
	 *
	 * 	assertThat(frameworkEvents)// created an {@link ListAssert}
	 * 		.have(bundleIsNotNull())
	 * 		.filteredOn(bundleIsNotNull())
	 * 		.first()// map to {@link ObjectAssert}
	 * 		.has(bundleIsNotNull());// used on {@link ObjectAssert}
	 * }
	 * </pre>
	 *
	 * @return the Condition
	 */
	public static Condition<FrameworkEvent> bundleIsNotNull() {
		return not(bundleIsNull());
	}

	/**
	 * Creates a {@link Condition} to be met by an {@link FrameworkEvent}.
	 * Checking if the bundle that is associated with the eventof the
	 * {@link FrameworkEvent} <b>is null</b>.
	 *
	 * <pre>
	 * List<FrameworkEvent> frameworkEvents = null;
	 *
	 * public static void example_bundleIsNull() {
	 *
	 * 	assertThat(frameworkEvents)// created an {@link ListAssert}
	 * 		.have(bundleIsNull())
	 * 		.filteredOn(bundleIsNull())
	 * 		.first()// map to {@link ObjectAssert}
	 * 		.has(bundleIsNull());// used on {@link ObjectAssert}
	 * }
	 * </pre>
	 *
	 * @return the Condition
	 */
	public static Condition<FrameworkEvent> bundleIsNull() {
		return VerboseCondition.verboseCondition((fe) -> fe.getBundle() == null, "bundle is <null>",
			(fe) -> " was <" + fe.getBundle() + ">".toString());
	}

	/**
	 * Creates a {@link Condition} to be met by an {@link FrameworkEvent}.
	 * Checking if a given type-mask <b>matches</b> the type <i>and</i> the
	 * given {@link Bundle} <b>equals</b> the Bundle and the throwable is an
	 * instanceof the throwableClass.
	 *
	 * <pre>
	 * List<FrameworkEvent> frameworkEvents = null;
	 *
	 * public static void example_matches(int typeMask, Bundle bundle, Class<? extends Throwable> throwableClass) {
	 *
	 * 	assertThat(frameworkEvents)// created an {@link ListAssert}
	 * 		.have(matches(typeMask, bundle, throwableClass))
	 * 		.filteredOn(matches(typeMask, bundle, throwableClass))
	 * 		.first()// map to {@link ObjectAssert}
	 * 		.has(matches(typeMask, bundle, throwableClass));// used on {@link
	 * 	// ObjectAssert}
	 * }
	 * </pre>
	 *
	 * @param eventTypeMask - the typeMask that would be checked against the
	 *            bundle type of the {@link FrameworkEvent}
	 * @param bundle - the bundle that would be checked against the bundle of
	 *            the {@link FrameworkEvent}
	 * @param throwableClass - the Class that would be checked against the
	 *            throwable of the {@link FrameworkEvent}
	 * @return the Condition
	 */
	public static Condition<FrameworkEvent> matches(final int eventTypeMask, Bundle bundle,
		final Class<? extends Throwable> throwableClass) {
		return Assertions.allOf(type(eventTypeMask), bundleEquals(bundle), throwableOfClass(throwableClass));
	}

	/**
	 * Creates a {@link Condition} to be met by an {@link FrameworkEvent}.
	 * Checking if a given type-mask <b>matches</b> the type <i>and</i> the
	 * throwable is an instanceof the throwableClass.
	 *
	 * <pre>
	 * List<FrameworkEvent> frameworkEvents = null;
	 *
	 * public static void example_matches(int typeMask, Class<? extends Throwable> throwableClass) {
	 *
	 * 	assertThat(frameworkEvents)// created an {@link ListAssert}
	 * 		.have(matches(typeMask, throwableClass))
	 * 		.filteredOn(matches(typeMask, throwableClass))
	 * 		.first()// map to
	 * 				// {@link
	 * 				// ObjectAssert}
	 * 		.has(matches(typeMask, throwableClass));// used on {@link
	 * 	// ObjectAssert}
	 * }
	 * </pre>
	 *
	 * @param eventTypeMask - the typeMask that would be checked against the
	 *            bundle type of the {@link FrameworkEvent}
	 * @param throwableClass - the Class that would be checked against the
	 *            throwable of the {@link FrameworkEvent}
	 * @return the Condition
	 */
	public static Condition<FrameworkEvent> matches(final int eventTypeMask, final Class<? extends Throwable> throwableClass) {
		return Assertions.allOf(type(eventTypeMask), throwableOfClass(throwableClass));
	}

	/**
	 * Creates a {@link Condition} to be met by an {@link FrameworkEvent}.
	 * Checking if a throwable is not null.
	 *
	 * <pre>
	 * List<FrameworkEvent> frameworkEvents = null;
	 *
	 * public static void example_throwableIsNotNull() {
	 *
	 * 	assertThat(frameworkEvents)// created an {@link ListAssert}
	 * 		.have(throwableIsNotNull())
	 * 		.filteredOn(throwableIsNotNull())
	 * 		.first()// map to {@link ObjectAssert}
	 * 		.has(throwableIsNotNull());// used on {@link
	 * 	// ObjectAssert}
	 * }
	 * </pre>
	 *
	 * @return the Condition
	 */
	public static Condition<FrameworkEvent> throwableIsNotNull() {
		return not(throwableIsNull());
	}

	/**
	 * Creates a {@link Condition} to be met by an {@link FrameworkEvent}.
	 * Checking if a throwable is null.
	 *
	 * <pre>
	 * List<FrameworkEvent> frameworkEvents = null;
	 *
	 * public static void example_throwableIsNull() {
	 *
	 * 	assertThat(frameworkEvents)// created an {@link ListAssert}
	 * 		.have(throwableIsNull())
	 * 		.filteredOn(throwableIsNull())
	 * 		.first()// map to {@link ObjectAssert}
	 * 		.has(throwableIsNull());// used on {@link
	 * 	// ObjectAssert}
	 * }
	 * </pre>
	 *
	 * @return the Condition
	 */
	public static Condition<FrameworkEvent> throwableIsNull() {
		return VerboseCondition.verboseCondition((fe) -> fe.getThrowable() == null, "throwable is <null>",
			(fe) -> fe.getThrowable()
				.toString());
	}

	/**
	 * Creates a {@link Condition} to be met by an {@link FrameworkEvent}.
	 * Checking if a throwable is an instanceof the throwableClass.
	 *
	 * <pre>
	 * List<FrameworkEvent> frameworkEvents = null;
	 *
	 * public static void example_throwableOfClass(Class<? extends Throwable> throwableClass) {
	 *
	 * 	assertThat(frameworkEvents)// created an {@link ListAssert}
	 * 		.have(throwableOfClass(throwableClass))
	 * 		.filteredOn(throwableOfClass(throwableClass))
	 * 		.first()// map to
	 * 				// {@link
	 * 				// ObjectAssert}
	 * 		.has(throwableOfClass(throwableClass));// used on {@link
	 * 	// ObjectAssert}
	 * }
	 * </pre>
	 *
	 * @param throwableClass - the Class that would be checked against the
	 *            throwable of the {@link FrameworkEvent}
	 * @return the Condition
	 */
	public static Condition<FrameworkEvent> throwableOfClass(final Class<? extends Throwable> throwableClass) {

		Condition<FrameworkEvent> cond = VerboseCondition.verboseCondition((fe) -> fe.getThrowable()
			.getClass()
			.isAssignableFrom(throwableClass), "throwable of class",
			fe -> fe.getThrowable()
				.toString());

		return allOf(throwableIsNotNull(), cond);//

	}

	/**
	 * Creates a {@link Condition} to be met by an {@link FrameworkEvent}.
	 * Checking if a given type-mask <b>matches</b> the type.
	 *
	 * <pre>
	 * List<FrameworkEvent> frameworkEvents = null;
	 *
	 * public static void example_type(int typeMask) {
	 *
	 * 	assertThat(frameworkEvents)// created an {@link ListAssert}
	 * 		.have(type(type))
	 * 		.filteredOn(type(typeMask))
	 * 		.first()// map to {@link ObjectAssert}
	 * 		.has(type(typeMask));// used on {@link
	 * 	// ObjectAssert}
	 * }
	 * </pre>
	 *
	 * @param eventTypeMask - the typeMask that would be checked against the
	 *            bundle type of the {@link FrameworkEvent}
	 * @return the Condition
	 */
	public static Condition<FrameworkEvent> type(final int eventTypeMask) {

		return VerboseCondition.verboseCondition(
			(FrameworkEvent fe) -> Bitmap.typeMatchesMask(fe.getType(), eventTypeMask),
			"type matches mask <" + FrameworkEventType.BITMAP.maskToString(eventTypeMask) + ">", //
			fe -> " was <" + FrameworkEventType.BITMAP.toString(fe.getType()) + ">");

	}

	/**
	 * Creates a {@link Condition} to be met by an {@link FrameworkEvent}.
	 * Checking if a given type-mask <b>matches</b> the type <i>and</i> the
	 * given {@link Bundle} <b>equals</b> the Bundle.
	 *
	 * <pre>
	 * List<FrameworkEvent> frameworkEvents = null;
	 *
	 * public static void example_typeAndBundle(int typeMask, Bundle bundle) {
	 *
	 * 	assertThat(frameworkEvents)// created an {@link ListAssert}
	 * 		.have(typeAndBundle(typeMask, bundles))
	 * 		.filteredOn(typeAndBundle(typeMask, bundle))
	 * 		.first()// map to
	 * 				// {@link
	 * 				// ObjectAssert}
	 * 		.has(typeAndBundle(typeMask, bundle));// used on {@link
	 * 	// ObjectAssert}
	 * }
	 * </pre>
	 *
	 * @param eventTypeMask - the typeMask that would be checked against the
	 *            bundle type of the {@link FrameworkEvent}
	 * @param bundle - the bundle that would be checked against the bundle of
	 *            the {@link FrameworkEvent}
	 * @return the Condition
	 */
	public static Condition<FrameworkEvent> typeAndBundle(final int eventTypeMask, Bundle bundle) {
		return Assertions.allOf(type(eventTypeMask), bundleEquals(bundle));
	}

}
