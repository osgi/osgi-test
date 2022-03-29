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

package org.osgi.test.assertj.bundleevent;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.Condition;
import org.assertj.core.api.ListAssert;
import org.assertj.core.api.ObjectAssert;
import org.assertj.core.condition.MappedCondition;
import org.assertj.core.condition.VerboseCondition;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;
import org.osgi.test.assertj.bundle.BundleConditions;
import org.osgi.test.common.bitmaps.Bitmap;
import org.osgi.test.common.bitmaps.BundleEventType;

/**
 * A Utility-Class thats Provides public static methods to create
 * {@link Condition}s for an {@link BundleEvent}
 *
 * @since 1.1
 */
public final class BundleEventConditions {

	/**
	 * Creates a {@link Condition} to be met by an {@link BundleEvent}. Checking
	 * if a {@link Bundle} is <b>equal</b> the bundle of the {@link BundleEvent}
	 * that had a change occur in its lifecycle.
	 * <p>
	 * Example:
	 *
	 * <pre>
	 * List<BundleEvent> bundleEvents = null;
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
	 *            the {@link BundleEvent}
	 * @return the Condition<br>
	 */
	public static Condition<BundleEvent> bundleEquals(Bundle bundle) {

		return MappedCondition.mappedCondition(BundleEvent::getBundle, BundleConditions.isEqualsTo(bundle),
			"BundleEvent::getBundle");
	}

	/**
	 * Creates a {@link Condition} to be met by an {@link BundleEvent}. Checking
	 * if the bundle of the {@link BundleEvent} that had a change occur in its
	 * lifecycle <b>is not null</b>.
	 *
	 * <pre>
	 * List<BundleEvent> bundleEvents = null;
	 *
	 * public static void example_bundleIsNotNull() {
	 *
	 * 	assertThat(bundleEvents)// created an {@link ListAssert}
	 * 		.have(bundleIsNotNull())
	 * 		.filteredOn(bundleIsNotNull())
	 * 		.first()// map to {@link ObjectAssert}
	 * 		.has(bundleIsNotNull());// used on {@link ObjectAssert}
	 * }
	 * </pre>
	 *
	 * @return the Condition
	 */
	public static Condition<BundleEvent> bundleIsNotNull() {
		return VerboseCondition.verboseCondition((be) -> be.getBundle() != null, "bundle is not <null>",
			(be) -> " was <" + (be.getBundle() == null ? "null"
				: be.getBundle()
					.toString())
				+ ">");
	}

	/**
	 * Creates a {@link Condition} to be met by an {@link BundleEvent}. Checking
	 * if the bundle of the {@link BundleEvent} that had a change occur in its
	 * lifecycle <b>is null</b>.
	 *
	 * <pre>
	 * List<BundleEvent> bundleEvents = null;
	 *
	 * public static void example_bundleIsNull() {
	 *
	 * 	assertThat(bundleEvents)// created an {@link ListAssert}
	 * 		.have(bundleIsNull())
	 * 		.filteredOn(bundleIsNull())
	 * 		.first()// map to {@link ObjectAssert}
	 * 		.has(bundleIsNull());// used on {@link ObjectAssert}
	 * }
	 * </pre>
	 *
	 * @return the Condition
	 */
	public static Condition<BundleEvent> bundleIsNull() {
		return VerboseCondition.verboseCondition((be) -> be.getBundle() == null, "bundle is <null>",
			(be) -> " was <" + (be.getBundle() == null ? "null"
				: be.getBundle()
					.toString())
				+ ">");
	}

	/**
	 * Creates a {@link Condition} to be met by an {@link BundleEvent}. Checking
	 * if a given type-mask <b>matches</b> the type <i>and</i> the given
	 * {@link Bundle} <b>equals</b> the Bundle and the other given
	 * {@link Bundle} <b>equals</b> the origin of the of lifecyle event.
	 *
	 * <pre>
	 * List<BundleEvent> bundleEvents = null;
	 *
	 * public static void example_matches(int typeMask, Bundle bundle, Bundle origin) {
	 *
	 * 	assertThat(bundleEvents)// created an {@link ListAssert}
	 * 		.have(matches(typeMask, bundle, origin))
	 * 		.filteredOn(matches(typeMask, bundle, origin))
	 * 		.first()// map to
	 * 				// {@link ObjectAssert}
	 * 		.has(matches(typeMask, bundle, origin));// used on
	 * 			{@link ObjectAssert}
	 * }
	 * </pre>
	 *
	 * @param typeMask - the typeMask that would be checked against the bundle
	 *            type of the {@link BundleEvent}
	 * @param bundle - the bundle that would be checked against the bundle of
	 *            the {@link BundleEvent}
	 * @param origin - the bundle that would be checked against the origin of
	 *            the {@link BundleEvent}
	 * @return the Condition
	 */
	public static Condition<BundleEvent> matches(int typeMask, Bundle bundle, Bundle origin) {
		return Assertions.allOf(type(typeMask), bundleEquals(bundle), originEquals(origin));
	}

	/**
	 * Creates a {@link Condition} to be met by an {@link BundleEvent}. Checking
	 * if a {@link Bundle} is <b>equal</b> the bundle of the {@link BundleEvent}
	 * that was the origin of the event.
	 *
	 * <pre>
	 * List<BundleEvent> bundleEvents = null;
	 *
	 * public static void example_originEquals(Bundle bundle) {
	 *
	 * 	assertThat(bundleEvents)// created an {@link ListAssert}
	 * 		.have(originEquals(bundle))
	 * 		.filteredOn(originEquals(bundle))
	 * 		.first()// map to {@link ObjectAssert}
	 * 		.has(originEquals(bundle));// used on {@link ObjectAssert}
	 * }
	 * </pre>
	 *
	 * @param bundle - the bundle that would be checked against the bundle of
	 *            the {@link BundleEvent}
	 * @return the Condition
	 */
	public static Condition<BundleEvent> originEquals(Bundle bundle) {

		return MappedCondition.mappedCondition(BundleEvent::getOrigin, BundleConditions.isEqualsTo(bundle),
			"BundleEvent::getOrigin");
	}

	/**
	 * Creates a {@link Condition} to be met by an {@link BundleEvent}. Checking
	 * if the bundle of the {@link BundleEvent} that was the origin of the
	 * event. <b>is not null</b>.
	 *
	 * <pre>
	 * List<BundleEvent> bundleEvents = null;
	 *
	 * public static void example_originIsNotNull() {
	 *
	 * 	assertThat(bundleEvents)// created an {@link ListAssert}
	 * 		.have(originIsNotNull())
	 * 		.filteredOn(originIsNotNull())
	 * 		.first()// map to {@link ObjectAssert}
	 * 		.has(originIsNotNull());// used on {@link ObjectAssert}
	 * }
	 * </pre>
	 *
	 * @return the Condition
	 */
	public static Condition<BundleEvent> originIsNotNull() {
		return VerboseCondition.verboseCondition((be) -> be.getOrigin() != null, "origin is not <null>",
			be -> " was" + be);
	}

	/**
	 * Creates a {@link Condition} to be met by an {@link BundleEvent}. Checking
	 * if the bundle of the {@link BundleEvent} that was the origin of the
	 * event. <b>is null</b>.
	 *
	 * <pre>
	 * List<BundleEvent> bundleEvents = null;
	 *
	 * public static void example_originIsNull() {
	 *
	 * 	assertThat(bundleEvents)// created an {@link ListAssert}
	 * 		.have(originIsNull())
	 * 		.filteredOn(originIsNull())
	 * 		.first()// map to {@link ObjectAssert}
	 * 		.has(originIsNull());// used on {@link ObjectAssert}
	 * }
	 * </pre>
	 *
	 * @return the Condition
	 */
	public static Condition<BundleEvent> originIsNull() {
		return VerboseCondition.verboseCondition((be) -> be.getOrigin() == null, "origin is <null>",
			be -> "was " + be);
	}

	/**
	 * Creates a {@link Condition} to be met by an {@link BundleEvent}. Checking
	 * if a type-mask <b>matches</b> the {@link BundleEvent} type of lifecyle
	 * event.
	 *
	 * <pre>
	 * List<BundleEvent> bundleEvents = null;
	 *
	 * public static void example_type(int typeMask) {
	 *
	 * 	assertThat(bundleEvents)// created an {@link ListAssert}
	 * 		.have(type(typeMask))
	 * 		.filteredOn(type(typeMask))
	 * 		.first()// map to {@link ObjectAssert}
	 * 		.has(type(typeMask));// used on {@link ObjectAssert}
	 * }
	 * </pre>
	 *
	 * @param expectedEventTypeMask - the typeMask that would be checked against
	 *            the bundle type of the {@link BundleEvent}
	 * @return the Condition
	 */
	public static Condition<BundleEvent> type(final int expectedEventTypeMask) {
		return VerboseCondition.verboseCondition(
			(BundleEvent be) -> Bitmap.typeMatchesMask(be.getType(), expectedEventTypeMask),
			"type matches mask <" + BundleEventType.BITMAP.maskToString(expectedEventTypeMask) + ">", //
			be -> " was <" + BundleEventType.BITMAP.toString(be.getType()) + ">");
	}

	/**
	 * Creates a {@link Condition} to be met by an {@link BundleEvent}. Checking
	 * if a given type-mask <b>matches</b> the type <i>and</i> the given
	 * {@link Bundle} <b>equals</b> the Bundle of the of lifecyle event.
	 *
	 * <pre>
	 * List<BundleEvent> bundleEvents = null;
	 *
	 * public static void example_typeAndBundle(int typeMask, Bundle bundle) {
	 *
	 * 	assertThat(bundleEvents)// created an {@link ListAssert}
	 * 		.have(typeAndBundle(typeMask, bundle))
	 * 		.filteredOn(typeAndBundle(typeMask, bundle))
	 * 		.first()// map to
	 * 				// {@link ObjectAssert}
	 * 		.has(typeAndBundle(typeMask, bundle));// used on
	 * 				{@link ObjectAssert}
	 * }
	 * </pre>
	 *
	 * @param typeMask - the typeMask that would be checked against the bundle
	 *            type of the {@link BundleEvent}
	 * @param bundle - the bundle that would be checked against the bundle of
	 *            the {@link BundleEvent}
	 * @return the Condition
	 */
	public static Condition<BundleEvent> typeAndBundle(int typeMask, Bundle bundle) {
		return Assertions.allOf(type(typeMask), bundleEquals(bundle));
	}
}
