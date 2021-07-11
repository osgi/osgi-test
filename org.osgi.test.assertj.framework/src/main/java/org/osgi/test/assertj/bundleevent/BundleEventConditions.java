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
 * A Utility-Class thats Provides static methods to create {@link Condition}s
 * for an {@link BundleEvent}
 */
public interface BundleEventConditions {

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
	 * static void example_bundleEquals(Bundle bundle) {
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
	static Condition<BundleEvent> bundleEquals(Bundle bundle) {

		return MappedCondition.mappedCondition(BundleEvent::getBundle, BundleConditions.sameAs(bundle),
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
	 * static void example_bundleIsNotNull() {
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
	static Condition<BundleEvent> bundleIsNotNull() {
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
	 * static void example_bundleIsNull() {
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
	static Condition<BundleEvent> bundleIsNull() {
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
	 * static void example_matches(int typeMask, Bundle bundle, Bundle origin) {
	 *
	 * 	assertThat(bundleEvents)// created an {@link ListAssert}
	 * 		.have(matches(typeMask, bundle, origin))
	 * 		.filteredOn(matches(typeMask, bundle, origin))
	 * 		.first()// map to
	 * 				// {@link
	 * 				// ObjectAssert}
	 * 		.has(matches(typeMask, bundle, origin));// used on {@link
	 * 	// ObjectAssert}
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
	static Condition<BundleEvent> matches(int typeMask, Bundle bundle, Bundle origin) {
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
	 * static void example_originEquals(Bundle bundle) {
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
	static Condition<BundleEvent> originEquals(Bundle bundle) {

		return MappedCondition.mappedCondition(BundleEvent::getOrigin, BundleConditions.sameAs(bundle),
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
	 * static void example_originIsNotNull() {
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
	static Condition<BundleEvent> originIsNotNull() {
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
	 * static void example_originIsNull() {
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
	static Condition<BundleEvent> originIsNull() {
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
	 * static void example_type(int typeMask) {
	 *
	 * 	assertThat(bundleEvents)// created an {@link ListAssert}
	 * 		.have(type(typeMask))
	 * 		.filteredOn(type(typeMask))
	 * 		.first()// map to {@link ObjectAssert}
	 * 		.has(type(typeMask));// used on {@link ObjectAssert}
	 * }
	 * </pre>
	 *
	 * @param typeMask - the typeMask that would be checked against the bundle
	 *            type of the {@link BundleEvent}
	 * @return the Condition
	 */
	static Condition<BundleEvent> type(final int expectedEventTypeMask) {
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
	 * static void example_typeAndBundle(int typeMask, Bundle bundle) {
	 *
	 * 	assertThat(bundleEvents)// created an {@link ListAssert}
	 * 		.have(typeAndBundle(typeMask, bundle))
	 * 		.filteredOn(typeAndBundle(typeMask, bundle))
	 * 		.first()// map to
	 * 				// {@link
	 * 				// ObjectAssert}
	 * 		.has(typeAndBundle(typeMask, bundle));// used on {@link
	 * 												// ObjectAssert}
	 * }
	 * </pre>
	 *
	 * @param typeMask - the typeMask that would be checked against the bundle
	 *            type of the {@link BundleEvent}
	 * @param bundle - the bundle that would be checked against the bundle of
	 *            the {@link BundleEvent}
	 * @return the Condition
	 */
	static Condition<BundleEvent> typeAndBundle(int mask, Bundle bundle) {
		return Assertions.allOf(type(mask), bundleEquals(bundle));
	}

	/**
	 * Creates a {@link Condition} to be met by an {@link BundleEvent}. Checking
	 * if the {@link BundleEvent} type <b>matches BundleEvent.INSTALLED</b>.
	 *
	 * <pre>
	 * List<BundleEvent> bundleEvents = null;
	 *
	 * static void example_typeInstalled()) {
	 *
	 * 	assertThat(bundleEvents)// created an {@link ListAssert}
	 * 		.have(typeInstalled()))
	 * 		.filteredOn(typeInstalled()))
	 * 		.first()// map to {@link ObjectAssert}
	 * 		.has(typeInstalled());// used on {@link ObjectAssert}
	 * }
	 * </pre>
	 *
	 * @return the Condition
	 */
	static Condition<BundleEvent> typeInstalled() {
		return type(BundleEvent.INSTALLED);
	}

	/**
	 * Creates a {@link Condition} to be met by an {@link BundleEvent}. Checking
	 * if a the type of the {@link BundleEvent} is <b>BundleEvent.INSTALLED</b>
	 * <i>and</i> the given {@link Bundle} <b>equals</b> the Bundle of the of
	 * lifecyle event.
	 *
	 * <pre>
	 * List<BundleEvent> bundleEvents = null;
	 *
	 * static void example_typeInstalledAndBundleEquals(Bundle bundle) {
	 *
	 * 	assertThat(bundleEvents)// created an {@link ListAssert}
	 * 		.have(typeInstalledAndBundleEquals(bundle))
	 * 		.filteredOn(typeInstalledAndBundleEquals(bundle))
	 * 		.first()// map
	 * 				// to
	 * 				// {@link
	 * 				// ObjectAssert}
	 * 		.has(typeInstalledAndBundleEquals(bundle));// used on {@link
	 * 													// ObjectAssert}
	 * }
	 * </pre>
	 *
	 * @return the Condition
	 */
	static Condition<BundleEvent> typeInstalledAndBundleEquals(Bundle bundle) {
		return Assertions.allOf(typeInstalled(), bundleEquals(bundle));
	}

	/**
	 * Creates a {@link Condition} to be met by an {@link BundleEvent}. Checking
	 * if the {@link BundleEvent} type <b>matches
	 * BundleEvent.LAZY_ACTIVATION</b>.
	 *
	 * <pre>
	 * List<BundleEvent> bundleEvents = null;
	 *
	 * static void example_typeLazyActivation()) {
	 *
	 * 	assertThat(bundleEvents)// created an {@link ListAssert}
	 * 		.have(typeLazyActivation()))
	 * 		.filteredOn(typeLazyActivation()))
	 * 		.first()// map to {@link ObjectAssert}
	 * 		.has(typeLazyActivation());// used on {@link ObjectAssert}
	 * }
	 * </pre>
	 *
	 * @return the Condition
	 */
	static Condition<BundleEvent> typeLazyActivation() {
		return type(BundleEvent.LAZY_ACTIVATION);
	}

	/**
	 * Creates a {@link Condition} to be met by an {@link BundleEvent}. Checking
	 * if a the type of the {@link BundleEvent} is
	 * <b>BundleEvent.LAZY_ACTIVATION</b> <i>and</i> the given {@link Bundle}
	 * <b>equals</b> the Bundle of the of lifecyle event.
	 *
	 * <pre>
	 * List<BundleEvent> bundleEvents = null;
	 *
	 * static void example_typeLazyActivationAndBundleEquals(Bundle bundle) {
	 *
	 * 	assertThat(bundleEvents)// created an {@link ListAssert}
	 * 		.have(typeLazyActivationAndBundleEquals(bundle))
	 * 		.filteredOn(typeLazyActivationAndBundleEquals(bundle))
	 * 		.first()// map to {@link ObjectAssert}
	 * 		.has(typeLazyActivationAndBundleEquals(bundle));// used on {@link
	 * 	// ObjectAssert}
	 * }
	 * </pre>
	 *
	 * @param bundle - the bundle that would be checked against the bundle of
	 *            the {@link BundleEvent}
	 * @return the Condition
	 */
	static Condition<BundleEvent> typeLazyActivationAndBundleEquals(Bundle bundle) {
		return Assertions.allOf(typeLazyActivation(), bundleEquals(bundle));
	}

	/**
	 * Creates a {@link Condition} to be met by an {@link BundleEvent}. Checking
	 * if the {@link BundleEvent} type <b>matches BundleEvent.RESOLVED</b>.
	 *
	 * <pre>
	 * List<BundleEvent> bundleEvents = null;
	 *
	 * static void example_typeResolved()) {
	 *
	 * 	assertThat(bundleEvents)// created an {@link ListAssert}
	 * 		.have(typeResolved()))
	 * 		.filteredOn(typeResolved()))
	 * 		.first()// map to {@link ObjectAssert}
	 * 		.has(typeResolved());// used on {@link ObjectAssert}
	 * }
	 * </pre>
	 *
	 * @return the Condition
	 */
	static Condition<BundleEvent> typeResolved() {
		return type(BundleEvent.RESOLVED);
	}

	/**
	 * Creates a {@link Condition} to be met by an {@link BundleEvent}. Checking
	 * if a the type of the {@link BundleEvent} is <b>BundleEvent.RESOLVED</b>
	 * <i>and</i> the given {@link Bundle} <b>equals</b> the Bundle of the of
	 * lifecyle event.
	 *
	 * <pre>
	 * List<BundleEvent> bundleEvents = null;
	 *
	 * static void example_typeResolvedAndBundleEquals(Bundle bundle) {
	 *
	 * 	assertThat(bundleEvents)// created an {@link ListAssert}
	 * 		.have(typeResolvedAndBundleEquals(bundle))
	 * 		.filteredOn(typeResolvedAndBundleEquals(bundle))
	 * 		.first()// map
	 * 				// to
	 * 				// {@link
	 * 				// ObjectAssert}
	 * 		.has(typeResolvedAndBundleEquals(bundle));// used on {@link
	 * 	// ObjectAssert}
	 * }
	 * </pre>
	 *
	 * @param bundle - the bundle that would be checked against the bundle of
	 *            the {@link BundleEvent}
	 * @return the Condition
	 */
	static Condition<BundleEvent> typeResolvedAndBundleEquals(Bundle bundle) {
		return Assertions.allOf(typeResolved(), bundleEquals(bundle));
	}

	/**
	 * Creates a {@link Condition} to be met by an {@link BundleEvent}. Checking
	 * if the {@link BundleEvent} type <b>matches BundleEvent.STARTED</b>.
	 *
	 * <pre>
	 * List<BundleEvent> bundleEvents = null;
	 *
	 * static void example_typeStarted()) {
	 *
	 * 	assertThat(bundleEvents)// created an {@link ListAssert}
	 * 		.have(typeStarted()))
	 * 		.filteredOn(typeStarted()))
	 * 		.first()// map to {@link ObjectAssert}
	 * 		.has(typeStarted());// used on {@link ObjectAssert}
	 * }
	 * </pre>
	 *
	 * @return the Condition
	 */
	static Condition<BundleEvent> typeStarted() {
		return type(BundleEvent.STARTED);
	}

	/**
	 * Creates a {@link Condition} to be met by an {@link BundleEvent}. Checking
	 * if a the type of the {@link BundleEvent} is <b>BundleEvent.STARTED</b>
	 * <i>and</i> the given {@link Bundle} <b>equals</b> the Bundle of the of
	 * lifecyle event.
	 *
	 * <pre>
	 * List<BundleEvent> bundleEvents = null;
	 *
	 * static void example_typeStartedAndBundleEquals(Bundle bundle) {
	 *
	 * 	assertThat(bundleEvents)// created an {@link ListAssert}
	 * 		.have(typeStartedAndBundleEquals(bundle))
	 * 		.filteredOn(typeStartedAndBundleEquals(bundle))
	 * 		.first()// map to
	 * 				// {@link
	 * 				// ObjectAssert}
	 * 		.has(typeStartedAndBundleEquals(bundle));// used on {@link
	 * 	// ObjectAssert}
	 * }
	 * </pre>
	 *
	 * @param bundle - the bundle that would be checked against the bundle of
	 *            the {@link BundleEvent}
	 * @return the Condition
	 */
	static Condition<BundleEvent> typeStartedAndBundleEquals(Bundle bundle) {
		return Assertions.allOf(typeStarted(), bundleEquals(bundle));
	}

	/**
	 * Creates a {@link Condition} to be met by an {@link BundleEvent}. Checking
	 * if the {@link BundleEvent} type <b>matches BundleEvent.STARTING</b>.
	 *
	 * <pre>
	 * List<BundleEvent> bundleEvents = null;
	 *
	 * static void example_typeStarting()) {
	 *
	 * 	assertThat(bundleEvents)// created an {@link ListAssert}
	 * 		.have(typeStarting()))
	 * 		.filteredOn(typeStarting()))
	 * 		.first()// map to {@link ObjectAssert}
	 * 		.has(typeStarting());// used on {@link ObjectAssert}
	 * }
	 * </pre>
	 *
	 * @return the Condition
	 */
	static Condition<BundleEvent> typeStarting() {
		return type(BundleEvent.STARTING);
	}

	/**
	 * Creates a {@link Condition} to be met by an {@link BundleEvent}. Checking
	 * if a the type of the {@link BundleEvent} is <b>BundleEvent.STARTING</b>
	 * <i>and</i> the given {@link Bundle} <b>equals</b> the Bundle of the of
	 * lifecyle event.
	 *
	 * <pre>
	 * List<BundleEvent> bundleEvents = null;
	 *
	 * static void example_typeStartingAndBundleEquals(Bundle bundle) {
	 *
	 * 	assertThat(bundleEvents)// created an {@link ListAssert}
	 * 		.have(typeStartingAndBundleEquals(bundle))
	 * 		.filteredOn(typeStartingAndBundleEquals(bundle))
	 * 		.first()// map
	 * 				// to
	 * 				// {@link
	 * 				// ObjectAssert}
	 * 		.has(typeStartingAndBundleEquals(bundle));// used on {@link
	 * 	// ObjectAssert}
	 * }
	 * </pre>
	 *
	 * @param bundle - the bundle that would be checked against the bundle of
	 *            the {@link BundleEvent}
	 * @return the Condition
	 */
	static Condition<BundleEvent> typeStartingAndBundleEquals(Bundle bundle) {
		return Assertions.allOf(typeStarting(), bundleEquals(bundle));
	}

	/**
	 * Creates a {@link Condition} to be met by an {@link BundleEvent}. Checking
	 * if the {@link BundleEvent} type <b>matches BundleEvent.STOPPED</b>.
	 *
	 * <pre>
	 * List<BundleEvent> bundleEvents = null;
	 *
	 * static void example_typeStopped()) {
	 *
	 * 	assertThat(bundleEvents)// created an {@link ListAssert}
	 * 		.have(typeStopped()))
	 * 		.filteredOn(typeStopped()))
	 * 		.first()// map to {@link ObjectAssert}
	 * 		.has(typeStopped());// used on {@link ObjectAssert}
	 * }
	 * </pre>
	 *
	 * @return the Condition
	 */
	static Condition<BundleEvent> typeStopped() {
		return type(BundleEvent.STOPPED);
	}

	/**
	 * Creates a {@link Condition} to be met by an {@link BundleEvent}. Checking
	 * if a the type of the {@link BundleEvent} is <b>BundleEvent.STOPPED</b>
	 * <i>and</i> the given {@link Bundle} <b>equals</b> the Bundle of the of
	 * lifecyle event.
	 *
	 * <pre>
	 * List<BundleEvent> bundleEvents = null;
	 *
	 * static void example_typeStoppedAndBundleEquals(Bundle bundle) {
	 *
	 * 	assertThat(bundleEvents)// created an {@link ListAssert}
	 * 		.have(typeStoppedAndBundleEquals(bundle))
	 * 		.filteredOn(typeStoppedAndBundleEquals(bundle))
	 * 		.first()// map to
	 * 				// {@link
	 * 				// ObjectAssert}
	 * 		.has(typeStoppedAndBundleEquals(bundle));// used on {@link
	 * 	// ObjectAssert}
	 * }
	 * </pre>
	 *
	 * @param bundle - the bundle that would be checked against the bundle of
	 *            the {@link BundleEvent}
	 * @return the Condition
	 */
	static Condition<BundleEvent> typeStoppedAndBundleEquals(Bundle bundle) {
		return Assertions.allOf(typeStopped(), bundleEquals(bundle));
	}

	/**
	 * Creates a {@link Condition} to be met by an {@link BundleEvent}. Checking
	 * if the {@link BundleEvent} type <b>matches BundleEvent.STOPPING</b>.
	 *
	 * <pre>
	 * List<BundleEvent> bundleEvents = null;
	 *
	 * static void example_typeStopping()) {
	 *
	 * 	assertThat(bundleEvents)// created an {@link ListAssert}
	 * 		.have(typeStopping()))
	 * 		.filteredOn(typeStopping()))
	 * 		.first()// map to {@link ObjectAssert}
	 * 		.has(typeStopping());// used on {@link ObjectAssert}
	 * }
	 * </pre>
	 *
	 * @return the Condition
	 */
	static Condition<BundleEvent> typeStopping() {
		return type(BundleEvent.STOPPING);
	}

	/**
	 * Creates a {@link Condition} to be met by an {@link BundleEvent}. Checking
	 * if a the type of the {@link BundleEvent} is <b>BundleEvent.STOPPING</b>
	 * <i>and</i> the given {@link Bundle} <b>equals</b> the Bundle of the of
	 * lifecyle event.
	 *
	 * <pre>
	 * List<BundleEvent> bundleEvents = null;
	 *
	 * static void example_typeStoppingAndBundleEquals(Bundle bundle) {
	 *
	 * 	assertThat(bundleEvents)// created an {@link ListAssert}
	 * 		.have(typeStoppingAndBundleEquals(bundle))
	 * 		.filteredOn(typeStoppingAndBundleEquals(bundle))
	 * 		.first()// map
	 * 				// to
	 * 				// {@link
	 * 				// ObjectAssert}
	 * 		.has(typeStoppingAndBundleEquals(bundle));// used on {@link
	 * 	// ObjectAssert}
	 * }
	 * </pre>
	 *
	 * @param bundle - the bundle that would be checked against the bundle of
	 *            the {@link BundleEvent}
	 * @return the Condition
	 */
	static Condition<BundleEvent> typeStoppingAndBundleEquals(Bundle bundle) {
		return Assertions.allOf(typeStopping(), bundleEquals(bundle));
	}

	/**
	 * Creates a {@link Condition} to be met by an {@link BundleEvent}. Checking
	 * if the {@link BundleEvent} type <b>matches BundleEvent.UNINSTALLED</b>.
	 *
	 * <pre>
	 * List<BundleEvent> bundleEvents = null;
	 *
	 * static void example_typeUninstalled()) {
	 *
	 * 	assertThat(bundleEvents)// created an {@link ListAssert}
	 * 		.have(typeUninstalled()))
	 * 		.filteredOn(typeUninstalled()))
	 * 		.first()// map to {@link ObjectAssert}
	 * 		.has(typeUninstalled());// used on {@link ObjectAssert}
	 * }
	 * </pre>
	 *
	 * @return the Condition
	 */
	static Condition<BundleEvent> typeUninstalled() {
		return type(BundleEvent.UNINSTALLED);
	}

	/**
	 * Creates a {@link Condition} to be met by an {@link BundleEvent}. Checking
	 * if a the type of the {@link BundleEvent} is
	 * <b>BundleEvent.UNINSTALLED</b> <i>and</i> the given {@link Bundle}
	 * <b>equals</b> the Bundle of the of lifecyle event.
	 *
	 * <pre>
	 * List<BundleEvent> bundleEvents = null;
	 *
	 * static void example_typeUninstalledAndBundleEquals(Bundle bundle) {
	 *
	 * 	assertThat(bundleEvents)// created an {@link ListAssert}
	 * 		.have(typeUninstalledAndBundleEquals(bundle))
	 * 		.filteredOn(typeUninstalledAndBundleEquals(bundle))
	 * 		.first()// map
	 * 				// to
	 * 				// {@link
	 * 				// ObjectAssert}
	 * 		.has(typeUninstalledAndBundleEquals(bundle));// used on {@link
	 * 	// ObjectAssert}
	 * }
	 * </pre>
	 *
	 * @param bundle - the bundle that would be checked against the bundle of
	 *            the {@link BundleEvent}
	 * @return the Condition
	 */
	static Condition<BundleEvent> typeUninstalledAndBundleEquals(Bundle bundle) {
		return Assertions.allOf(typeUninstalled(), bundleEquals(bundle));
	}

	/**
	 * Creates a {@link Condition} to be met by an {@link BundleEvent}. Checking
	 * if the {@link BundleEvent} type <b>matches BundleEvent.UNRESOLVED</b>.
	 *
	 * <pre>
	 * List<BundleEvent> bundleEvents = null;
	 *
	 * static void example_typeUnresolved()) {
	 *
	 * 	assertThat(bundleEvents)// created an {@link ListAssert}
	 * 		.have(typeUnresolved()))
	 * 		.filteredOn(typeUnresolved()))
	 * 		.first()// map to {@link ObjectAssert}
	 * 		.has(typeUnresolved());// used on {@link ObjectAssert}
	 * }
	 * </pre>
	 *
	 * @return the Condition
	 */
	static Condition<BundleEvent> typeUnresolved() {
		return type(BundleEvent.UNRESOLVED);
	}

	/**
	 * Creates a {@link Condition} to be met by an {@link BundleEvent}. Checking
	 * if a the type of the {@link BundleEvent} is <b>BundleEvent.UNRESOLVED</b>
	 * <i>and</i> the given {@link Bundle} <b>equals</b> the Bundle of the of
	 * lifecyle event.
	 *
	 * <pre>
	 * List<BundleEvent> bundleEvents = null;
	 *
	 * static void example_typeUnresolvedAndBundleEquals(Bundle bundle) {
	 *
	 * 	assertThat(bundleEvents)// created an {@link ListAssert}
	 * 		.have(typeUnresolvedAndBundleEquals(bundle))
	 * 		.filteredOn(typeUnresolvedAndBundleEquals(bundle))
	 * 		.first()// map
	 * 				// to
	 * 				// {@link
	 * 				// ObjectAssert}
	 * 		.has(typeUnresolvedAndBundleEquals(bundle));// used on {@link
	 * 	// ObjectAssert}
	 * }
	 * </pre>
	 *
	 * @param bundle - the bundle that would be checked against the bundle of
	 *            the {@link BundleEvent}
	 * @return the Condition
	 */
	static Condition<BundleEvent> typeUnresolvedAndBundleEquals(Bundle bundle) {
		return Assertions.allOf(typeUnresolved(), bundleEquals(bundle));
	}

	/**
	 * Creates a {@link Condition} to be met by an {@link BundleEvent}. Checking
	 * if the {@link BundleEvent} type <b>matches BundleEvent.UPDATED</b>.
	 *
	 * <pre>
	 * List<BundleEvent> bundleEvents = null;
	 *
	 * static void example_typeUpdated() {
	 *
	 * 	assertThat(bundleEvents)// created an {@link ListAssert}
	 * 		.have(typeUpdated()))
	 * 		.filteredOn(typeUpdated()))
	 * 		.first()// map to {@link ObjectAssert}
	 * 		.has(typeUpdated());// used on {@link ObjectAssert}
	 * }
	 * </pre>
	 *
	 * @return the Condition
	 */
	static Condition<BundleEvent> typeUpdated() {
		return type(BundleEvent.UPDATED);
	}

	/**
	 * Creates a {@link Condition} to be met by an {@link BundleEvent}. Checking
	 * if a the type of the {@link BundleEvent} is <b>BundleEvent.UPDATED</b>
	 * <i>and</i> the given {@link Bundle} <b>equals</b> the Bundle of the of
	 * lifecyle event.
	 *
	 * <pre>
	 * List<BundleEvent> bundleEvents = null;
	 *
	 * static void example_typeUpdatedAndBundleEquals(Bundle bundle) {
	 *
	 * 	assertThat(bundleEvents)// created an {@link ListAssert}
	 * 		.have(typeUpdatedAndBundleEquals(bundle))
	 * 		.filteredOn(typeUpdatedAndBundleEquals(bundle))
	 * 		.first()// map to
	 * 				// {@link
	 * 				// ObjectAssert}
	 * 		.has(typeUpdatedAndBundleEquals(bundle));// used on {@link
	 * 	// ObjectAssert}
	 * }
	 * </pre>
	 *
	 * @param bundle - the bundle that would be checked against the bundle of
	 *            the {@link BundleEvent}
	 * @return the Condition
	 */
	static Condition<BundleEvent> typeUpdatedAndBundleEquals(Bundle bundle) {
		return Assertions.allOf(typeUpdated(), bundleEquals(bundle));
	}
}
