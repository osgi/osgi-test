package org.osgi.test.assertj.NotPartOfPR;

import static org.assertj.core.api.Assertions.allOf;
import static org.assertj.core.api.Assertions.not;
import static org.osgi.test.assertj.NotPartOfPR.Conditions.DictionaryConditions.servicePropertiesContains;
import static org.osgi.test.assertj.NotPartOfPR.Conditions.ServiceReferenceConditions.objectClass;
import static org.osgi.test.assertj.NotPartOfPR.Conditions.ServiceReferenceConditions.servicePropertiesHas;

import java.util.Collections;
import java.util.Dictionary;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Stream;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.Condition;
import org.assertj.core.api.IterableAssert;
import org.assertj.core.api.ListAssert;
import org.assertj.core.api.ObjectAssert;
import org.assertj.core.condition.MappedCondition;
import org.assertj.core.condition.VerboseCondition;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceReference;
import org.osgi.test.common.bitmaps.BundleEventType;
import org.osgi.test.common.bitmaps.FrameworkEventType;
import org.osgi.test.common.bitmaps.ServiceEventType;
import org.osgi.test.common.dictionary.Dictionaries;

/**
 * A Utility-Class thats Provides static methods to create
 * {@link Conditions}.<br>
 * Conditions could be used on {@link ObjectAssert}<br>
 * <p>
 * Example:
 *
 * <pre>
 * Object object = null;
 *
 * assertThat(object)//
 * 		.is(condition) // Verifies that the actual value satisfies condition
 * 		.isNot(condition)// Verifies that the actual value does not satisfy
 * 		// the condition
 * 		.has(condition) // alias for is()
 * 		.doesNotHave(condition); // aliasfor isNot()
 * </pre>
 *
 * <br>
 * and {@link IterableAssert}<br>
 * <br>
 * <p>
 * Example:
 *
 * <pre>
 * List<Object> objects = null;
 *
 * assertThat(objects)//
 * 		.are(condition)// Verifies each element satisfies the condition.
 * 		.have(condition) // alias for are()
 * 		.areNot(condition)//
 * 		.doNotHave(condition)// alias for are not
 * 		.areAtLeast(1, condition).areAtLeastOne(condition).areAtMost(1, condition).areExactly(1, condition)
 * 		.filteredOn(condition) // Filters the iterable under test keeping
 * 		// only elements matching the given
 * 		// Condition.
 * 		.first().is(condition);
 *
 * </pre>
 *
 * <br>
 */
public interface Conditions {

	static boolean typeMatchesMask(int type, int mask) {
		return (type & mask) != 0;
	}
	/**
	 * A Utility-Class thats Provides static methods to create {@link Conditions}
	 * for an {@link Bundle}
	 */
	interface BundleConditions {
		/**
		 * Creates a {@link Condition} to be met by an {@link Bundle}. Checking if a
		 * {@link Bundle} is <b>equal</b> an other Bundle.
		 * <p>
		 * Example:
		 *
		 * <pre>
		 * List<Bundle> bundles = null;
		 *
		 * static void sameAs(Bundle bundle) {
		 *
		 * 	assertThat(bundles)// created an {@link ListAssert}
		 * 			.have(sameAs(bundle)).filteredOn(sameAs(bundle)).first()// map to {@link ObjectAssert}
		 * 			.is(sameAs(bundle));// used on {@link ObjectAssert}
		 * }
		 * </pre>
		 *
		 * @param serviceReferences - the expected serviceReferences that would be
		 *                          checked against other {@link ServiceReference}s
		 * @return the Condition<br>
		 */
		static Condition<Bundle> sameAs(Bundle bundle) {
			Condition<Bundle> c = VerboseCondition.verboseCondition((b) -> Objects.equals(b, bundle), "bundle equals",
					Bundle::toString);
			return c;
		}
	}

	/**
	 * A Utility-Class thats Provides static methods to create {@link Conditions}
	 * for an {@link BundleEvent}
	 */
	interface BundleEventConditions {

		/**
		 * Creates a {@link Condition} to be met by an {@link BundleEvent}. Checking if
		 * a {@link Bundle} is <b>equal</b> the bundle of the {@link BundleEvent} that
		 * had a change occur in its lifecycle.
		 * <p>
		 * Example:
		 *
		 * <pre>
		 * List<BundleEvent> bundleEvents = null;
		 *
		 * static void example_bundleEquals(Bundle bundle) {
		 *
		 * 	assertThat(bundleEvents)// created an {@link ListAssert}
		 * 			.have(bundleEquals(bundle)).filteredOn(bundleEquals(bundle)).first()// map to {@link ObjectAssert}
		 * 			.has(bundleEquals(bundle));// used on {@link ObjectAssert}
		 * }
		 * </pre>
		 *
		 * @param bundle - the bundle that would be checked against the bundle of the
		 *               {@link BundleEvent}
		 * @return the Condition<br>
		 */
		static Condition<BundleEvent> bundleEquals(Bundle bundle) {

			return MappedCondition.mappedCondition(BundleEvent::getBundle, BundleConditions.sameAs(bundle),
					"BundleEvent::getBundle");
		}

		/**
		 * Creates a {@link Condition} to be met by an {@link BundleEvent}. Checking if
		 * the bundle of the {@link BundleEvent} that had a change occur in its
		 * lifecycle <b>is not null</b>.
		 *
		 * <pre>
		 * List<BundleEvent> bundleEvents = null;
		 *
		 * static void example_bundleIsNotNull() {
		 *
		 * 	assertThat(bundleEvents)// created an {@link ListAssert}
		 * 			.have(bundleIsNotNull()).filteredOn(bundleIsNotNull()).first()// map to {@link ObjectAssert}
		 * 			.has(bundleIsNotNull());// used on {@link ObjectAssert}
		 * }
		 * </pre>
		 *
		 * @return the Condition
		 */
		static Condition<BundleEvent> bundleIsNotNull() {
			return not(bundleIsNull());
		}

		/**
		 * Creates a {@link Condition} to be met by an {@link BundleEvent}. Checking if
		 * the bundle of the {@link BundleEvent} that had a change occur in its
		 * lifecycle <b>is null</b>.
		 *
		 * <pre>
		 * List<BundleEvent> bundleEvents = null;
		 *
		 * static void example_bundleIsNull() {
		 *
		 * 	assertThat(bundleEvents)// created an {@link ListAssert}
		 * 			.have(bundleIsNull()).filteredOn(bundleIsNull()).first()// map to {@link ObjectAssert}
		 * 			.has(bundleIsNull());// used on {@link ObjectAssert}
		 * }
		 * </pre>
		 *
		 * @return the Condition
		 */
		static Condition<BundleEvent> bundleIsNull() {
			return bundleEquals(null);
		}

		/**
		 * Creates a {@link Condition} to be met by an {@link BundleEvent}. Checking if
		 * a given type-mask <b>matches</b> the type <i>and</i> the given {@link Bundle}
		 * <b>equals</b> the Bundle and the other given {@link Bundle} <b>equals</b> the
		 * origin of the of lifecyle event.
		 *
		 * <pre>
		 * List<BundleEvent> bundleEvents = null;
		 *
		 * static void example_matches(int typeMask, Bundle bundle, Bundle origin) {
		 *
		 * 	assertThat(bundleEvents)// created an {@link ListAssert}
		 * 			.have(matches(typeMask, bundle, origin)).filteredOn(matches(typeMask, bundle, origin)).first()// map to
		 * 																											// {@link
		 * 																											// ObjectAssert}
		 * 			.has(matches(typeMask, bundle, origin));// used on {@link
		 * 	// ObjectAssert}
		 * }
		 * </pre>
		 *
		 * @param typeMask - the typeMask that would be checked against the bundle type
		 *                 of the {@link BundleEvent}
		 * @param bundle   - the bundle that would be checked against the bundle of the
		 *                 {@link BundleEvent}
		 * @param origin   - the bundle that would be checked against the origin of the
		 *                 {@link BundleEvent}
		 * @return the Condition
		 */
		static Condition<BundleEvent> matches(int typeMask, Bundle bundle, Bundle origin) {
			return Assertions.allOf(type(typeMask), bundleEquals(bundle), originEquals(origin));
		}

		/**
		 * Creates a {@link Condition} to be met by an {@link BundleEvent}. Checking if
		 * a {@link Bundle} is <b>equal</b> the bundle of the {@link BundleEvent} that
		 * was the origin of the event.
		 *
		 * <pre>
		 * List<BundleEvent> bundleEvents = null;
		 *
		 * static void example_originEquals(Bundle bundle) {
		 *
		 * 	assertThat(bundleEvents)// created an {@link ListAssert}
		 * 			.have(originEquals(bundle)).filteredOn(originEquals(bundle)).first()// map to {@link ObjectAssert}
		 * 			.has(originEquals(bundle));// used on {@link ObjectAssert}
		 * }
		 * </pre>
		 *
		 * @param bundle - the bundle that would be checked against the bundle of the
		 *               {@link BundleEvent}
		 * @return the Condition
		 */
		static Condition<BundleEvent> originEquals(Bundle bundle) {

			return MappedCondition.mappedCondition(BundleEvent::getOrigin, BundleConditions.sameAs(bundle),
					"BundleEvent::getOrigin");
		}

		/**
		 * Creates a {@link Condition} to be met by an {@link BundleEvent}. Checking if
		 * the bundle of the {@link BundleEvent} that was the origin of the event. <b>is
		 * not null</b>.
		 *
		 * <pre>
		 * List<BundleEvent> bundleEvents = null;
		 *
		 * static void example_originIsNotNull() {
		 *
		 * 	assertThat(bundleEvents)// created an {@link ListAssert}
		 * 			.have(originIsNotNull()).filteredOn(originIsNotNull()).first()// map to {@link ObjectAssert}
		 * 			.has(originIsNotNull());// used on {@link ObjectAssert}
		 * }
		 * </pre>
		 *
		 * @return the Condition
		 */
		static Condition<BundleEvent> originIsNotNull() {
			return not(originIsNull());
		}

		/**
		 * Creates a {@link Condition} to be met by an {@link BundleEvent}. Checking if
		 * the bundle of the {@link BundleEvent} that was the origin of the event. <b>is
		 * null</b>.
		 *
		 * <pre>
		 * List<BundleEvent> bundleEvents = null;
		 *
		 * static void example_originIsNull() {
		 *
		 * 	assertThat(bundleEvents)// created an {@link ListAssert}
		 * 			.have(originIsNull()).filteredOn(originIsNull()).first()// map to {@link ObjectAssert}
		 * 			.has(originIsNull());// used on {@link ObjectAssert}
		 * }
		 * </pre>
		 *
		 * @return the Condition
		 */
		static Condition<BundleEvent> originIsNull() {
			return VerboseCondition.verboseCondition((be) -> be.getOrigin() == null, "origin is null",
					BundleEvent::toString);
		}

		/**
		 * Creates a {@link Condition} to be met by an {@link BundleEvent}. Checking if
		 * a type-mask <b>matches</b> the {@link BundleEvent} type of lifecyle event.
		 *
		 * <pre>
		 * List<BundleEvent> bundleEvents = null;
		 *
		 * static void example_type(int typeMask) {
		 *
		 * 	assertThat(bundleEvents)// created an {@link ListAssert}
		 * 			.have(type(typeMask)).filteredOn(type(typeMask)).first()// map to {@link ObjectAssert}
		 * 			.has(type(typeMask));// used on {@link ObjectAssert}
		 * }
		 * </pre>
		 *
		 * @param typeMask - the typeMask that would be checked against the bundle type
		 *                 of the {@link BundleEvent}
		 * @return the Condition
		 */
		static Condition<BundleEvent> type(final int expectedEventTypeMask) {
			return VerboseCondition.verboseCondition(
					(BundleEvent be) -> typeMatchesMask(be.getType(), expectedEventTypeMask),
					"type matches mask '" + BundleEventType.BITMAP.maskToString(expectedEventTypeMask) + "'", //
					be -> BundleEventType.BITMAP.toString(be.getType()));
		}

		/**
		 * Creates a {@link Condition} to be met by an {@link BundleEvent}. Checking if
		 * a given type-mask <b>matches</b> the type <i>and</i> the given {@link Bundle}
		 * <b>equals</b> the Bundle of the of lifecyle event.
		 *
		 * <pre>
		 * List<BundleEvent> bundleEvents = null;
		 *
		 * static void example_typeAndBundle(int typeMask, Bundle bundle) {
		 *
		 * 	assertThat(bundleEvents)// created an {@link ListAssert}
		 * 			.have(typeAndBundle(typeMask, bundle)).filteredOn(typeAndBundle(typeMask, bundle)).first()// map to
		 * 																										// {@link
		 * 																										// ObjectAssert}
		 * 			.has(typeAndBundle(typeMask, bundle));// used on {@link ObjectAssert}
		 * }
		 * </pre>
		 *
		 * @param typeMask - the typeMask that would be checked against the bundle type
		 *                 of the {@link BundleEvent}
		 * @param bundle   - the bundle that would be checked against the bundle of the
		 *                 {@link BundleEvent}
		 * @return the Condition
		 */
		static Condition<BundleEvent> typeAndBundle(int mask, Bundle bundle) {
			return Assertions.allOf(type(mask), bundleEquals(bundle));
		}

		/**
		 * Creates a {@link Condition} to be met by an {@link BundleEvent}. Checking if
		 * the {@link BundleEvent} type <b>matches BundleEvent.INSTALLED</b>.
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
		 * Creates a {@link Condition} to be met by an {@link BundleEvent}. Checking if
		 * a the type of the {@link BundleEvent} is <b>BundleEvent.INSTALLED</b>
		 * <i>and</i> the given {@link Bundle} <b>equals</b> the Bundle of the of
		 * lifecyle event.
		 *
		 * <pre>
		 * List<BundleEvent> bundleEvents = null;
		 *
		 * static void example_typeInstalledAndBundleEquals(Bundle bundle) {
		 *
		 * 	assertThat(bundleEvents)// created an {@link ListAssert}
		 * 			.have(typeInstalledAndBundleEquals(bundle)).filteredOn(typeInstalledAndBundleEquals(bundle)).first()// map
		 * 																												// to
		 * 																												// {@link
		 * 																												// ObjectAssert}
		 * 			.has(typeInstalledAndBundleEquals(bundle));// used on {@link
		 * 														// ObjectAssert}
		 * }
		 * </pre>
		 *
		 * @return the Condition
		 */
		static Condition<BundleEvent> typeInstalledAndBundleEquals(Bundle bundle) {
			return Assertions.allOf(typeInstalled(), bundleEquals(bundle));
		}

		/**
		 * Creates a {@link Condition} to be met by an {@link BundleEvent}. Checking if
		 * the {@link BundleEvent} type <b>matches BundleEvent.LAZY_ACTIVATION</b>.
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
		 * Creates a {@link Condition} to be met by an {@link BundleEvent}. Checking if
		 * a the type of the {@link BundleEvent} is <b>BundleEvent.LAZY_ACTIVATION</b>
		 * <i>and</i> the given {@link Bundle} <b>equals</b> the Bundle of the of
		 * lifecyle event.
		 *
		 * <pre>
		 * List<BundleEvent> bundleEvents = null;
		 *
		 * static void example_typeLazyActivationAndBundleEquals(Bundle bundle) {
		 *
		 * 	assertThat(bundleEvents)// created an {@link ListAssert}
		 * 			.have(typeLazyActivationAndBundleEquals(bundle)).filteredOn(typeLazyActivationAndBundleEquals(bundle))
		 * 			.first()// map to {@link ObjectAssert}
		 * 			.has(typeLazyActivationAndBundleEquals(bundle));// used on {@link
		 * 	// ObjectAssert}
		 * }
		 * </pre>
		 *
		 * @param bundle - the bundle that would be checked against the bundle of the
		 *               {@link BundleEvent}
		 * @return the Condition
		 */
		static Condition<BundleEvent> typeLazyActivationAndBundleEquals(Bundle bundle) {
			return Assertions.allOf(typeLazyActivation(), bundleEquals(bundle));
		}

		/**
		 * Creates a {@link Condition} to be met by an {@link BundleEvent}. Checking if
		 * the {@link BundleEvent} type <b>matches BundleEvent.RESOLVED</b>.
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
		 * Creates a {@link Condition} to be met by an {@link BundleEvent}. Checking if
		 * a the type of the {@link BundleEvent} is <b>BundleEvent.RESOLVED</b>
		 * <i>and</i> the given {@link Bundle} <b>equals</b> the Bundle of the of
		 * lifecyle event.
		 *
		 * <pre>
		 * List<BundleEvent> bundleEvents = null;
		 *
		 * static void example_typeResolvedAndBundleEquals(Bundle bundle) {
		 *
		 * 	assertThat(bundleEvents)// created an {@link ListAssert}
		 * 			.have(typeResolvedAndBundleEquals(bundle)).filteredOn(typeResolvedAndBundleEquals(bundle)).first()// map
		 * 																												// to
		 * 																												// {@link
		 * 																												// ObjectAssert}
		 * 			.has(typeResolvedAndBundleEquals(bundle));// used on {@link
		 * 	// ObjectAssert}
		 * }
		 * </pre>
		 *
		 * @param bundle - the bundle that would be checked against the bundle of the
		 *               {@link BundleEvent}
		 * @return the Condition
		 */
		static Condition<BundleEvent> typeResolvedAndBundleEquals(Bundle bundle) {
			return Assertions.allOf(typeResolved(), bundleEquals(bundle));
		}

		/**
		 * Creates a {@link Condition} to be met by an {@link BundleEvent}. Checking if
		 * the {@link BundleEvent} type <b>matches BundleEvent.STARTED</b>.
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
		 * Creates a {@link Condition} to be met by an {@link BundleEvent}. Checking if
		 * a the type of the {@link BundleEvent} is <b>BundleEvent.STARTED</b>
		 * <i>and</i> the given {@link Bundle} <b>equals</b> the Bundle of the of
		 * lifecyle event.
		 *
		 * <pre>
		 * List<BundleEvent> bundleEvents = null;
		 *
		 * static void example_typeStartedAndBundleEquals(Bundle bundle) {
		 *
		 * 	assertThat(bundleEvents)// created an {@link ListAssert}
		 * 			.have(typeStartedAndBundleEquals(bundle)).filteredOn(typeStartedAndBundleEquals(bundle)).first()// map to
		 * 																											// {@link
		 * 																											// ObjectAssert}
		 * 			.has(typeStartedAndBundleEquals(bundle));// used on {@link
		 * 	// ObjectAssert}
		 * }
		 * </pre>
		 *
		 * @param bundle - the bundle that would be checked against the bundle of the
		 *               {@link BundleEvent}
		 * @return the Condition
		 */
		static Condition<BundleEvent> typeStartedAndBundleEquals(Bundle bundle) {
			return Assertions.allOf(typeStarted(), bundleEquals(bundle));
		}

		/**
		 * Creates a {@link Condition} to be met by an {@link BundleEvent}. Checking if
		 * the {@link BundleEvent} type <b>matches BundleEvent.STARTING</b>.
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
		 * Creates a {@link Condition} to be met by an {@link BundleEvent}. Checking if
		 * a the type of the {@link BundleEvent} is <b>BundleEvent.STARTING</b>
		 * <i>and</i> the given {@link Bundle} <b>equals</b> the Bundle of the of
		 * lifecyle event.
		 *
		 * <pre>
		 * List<BundleEvent> bundleEvents = null;
		 *
		 * static void example_typeStartingAndBundleEquals(Bundle bundle) {
		 *
		 * 	assertThat(bundleEvents)// created an {@link ListAssert}
		 * 			.have(typeStartingAndBundleEquals(bundle)).filteredOn(typeStartingAndBundleEquals(bundle)).first()// map
		 * 																												// to
		 * 																												// {@link
		 * 																												// ObjectAssert}
		 * 			.has(typeStartingAndBundleEquals(bundle));// used on {@link
		 * 	// ObjectAssert}
		 * }
		 * </pre>
		 *
		 * @param bundle - the bundle that would be checked against the bundle of the
		 *               {@link BundleEvent}
		 * @return the Condition
		 */
		static Condition<BundleEvent> typeStartingAndBundleEquals(Bundle bundle) {
			return Assertions.allOf(typeStarting(), bundleEquals(bundle));
		}

		/**
		 * Creates a {@link Condition} to be met by an {@link BundleEvent}. Checking if
		 * the {@link BundleEvent} type <b>matches BundleEvent.STOPPED</b>.
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
		 * Creates a {@link Condition} to be met by an {@link BundleEvent}. Checking if
		 * a the type of the {@link BundleEvent} is <b>BundleEvent.STOPPED</b>
		 * <i>and</i> the given {@link Bundle} <b>equals</b> the Bundle of the of
		 * lifecyle event.
		 *
		 * <pre>
		 * List<BundleEvent> bundleEvents = null;
		 *
		 * static void example_typeStoppedAndBundleEquals(Bundle bundle) {
		 *
		 * 	assertThat(bundleEvents)// created an {@link ListAssert}
		 * 			.have(typeStoppedAndBundleEquals(bundle)).filteredOn(typeStoppedAndBundleEquals(bundle)).first()// map to
		 * 																											// {@link
		 * 																											// ObjectAssert}
		 * 			.has(typeStoppedAndBundleEquals(bundle));// used on {@link
		 * 	// ObjectAssert}
		 * }
		 * </pre>
		 *
		 * @param bundle - the bundle that would be checked against the bundle of the
		 *               {@link BundleEvent}
		 * @return the Condition
		 */
		static Condition<BundleEvent> typeStoppedAndBundleEquals(Bundle bundle) {
			return Assertions.allOf(typeStopped(), bundleEquals(bundle));
		}

		/**
		 * Creates a {@link Condition} to be met by an {@link BundleEvent}. Checking if
		 * the {@link BundleEvent} type <b>matches BundleEvent.STOPPING</b>.
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
		 * Creates a {@link Condition} to be met by an {@link BundleEvent}. Checking if
		 * a the type of the {@link BundleEvent} is <b>BundleEvent.STOPPING</b>
		 * <i>and</i> the given {@link Bundle} <b>equals</b> the Bundle of the of
		 * lifecyle event.
		 *
		 * <pre>
		 * List<BundleEvent> bundleEvents = null;
		 *
		 * static void example_typeStoppingAndBundleEquals(Bundle bundle) {
		 *
		 * 	assertThat(bundleEvents)// created an {@link ListAssert}
		 * 			.have(typeStoppingAndBundleEquals(bundle)).filteredOn(typeStoppingAndBundleEquals(bundle)).first()// map
		 * 																												// to
		 * 																												// {@link
		 * 																												// ObjectAssert}
		 * 			.has(typeStoppingAndBundleEquals(bundle));// used on {@link
		 * 	// ObjectAssert}
		 * }
		 * </pre>
		 *
		 * @param bundle - the bundle that would be checked against the bundle of the
		 *               {@link BundleEvent}
		 * @return the Condition
		 */
		static Condition<BundleEvent> typeStoppingAndBundleEquals(Bundle bundle) {
			return Assertions.allOf(typeStopping(), bundleEquals(bundle));
		}

		/**
		 * Creates a {@link Condition} to be met by an {@link BundleEvent}. Checking if
		 * the {@link BundleEvent} type <b>matches BundleEvent.UNINSTALLED</b>.
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
		 * Creates a {@link Condition} to be met by an {@link BundleEvent}. Checking if
		 * a the type of the {@link BundleEvent} is <b>BundleEvent.UNINSTALLED</b>
		 * <i>and</i> the given {@link Bundle} <b>equals</b> the Bundle of the of
		 * lifecyle event.
		 *
		 * <pre>
		 * List<BundleEvent> bundleEvents = null;
		 *
		 * static void example_typeUninstalledAndBundleEquals(Bundle bundle) {
		 *
		 * 	assertThat(bundleEvents)// created an {@link ListAssert}
		 * 			.have(typeUninstalledAndBundleEquals(bundle)).filteredOn(typeUninstalledAndBundleEquals(bundle)).first()// map
		 * 																													// to
		 * 																													// {@link
		 * 																													// ObjectAssert}
		 * 			.has(typeUninstalledAndBundleEquals(bundle));// used on {@link
		 * 	// ObjectAssert}
		 * }
		 * </pre>
		 *
		 * @param bundle - the bundle that would be checked against the bundle of the
		 *               {@link BundleEvent}
		 * @return the Condition
		 */
		static Condition<BundleEvent> typeUninstalledAndBundleEquals(Bundle bundle) {
			return Assertions.allOf(typeUninstalled(), bundleEquals(bundle));
		}

		/**
		 * Creates a {@link Condition} to be met by an {@link BundleEvent}. Checking if
		 * the {@link BundleEvent} type <b>matches BundleEvent.UNRESOLVED</b>.
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
		 * Creates a {@link Condition} to be met by an {@link BundleEvent}. Checking if
		 * a the type of the {@link BundleEvent} is <b>BundleEvent.UNRESOLVED</b>
		 * <i>and</i> the given {@link Bundle} <b>equals</b> the Bundle of the of
		 * lifecyle event.
		 *
		 * <pre>
		 * List<BundleEvent> bundleEvents = null;
		 *
		 * static void example_typeUnresolvedAndBundleEquals(Bundle bundle) {
		 *
		 * 	assertThat(bundleEvents)// created an {@link ListAssert}
		 * 			.have(typeUnresolvedAndBundleEquals(bundle)).filteredOn(typeUnresolvedAndBundleEquals(bundle)).first()// map
		 * 																													// to
		 * 																													// {@link
		 * 																													// ObjectAssert}
		 * 			.has(typeUnresolvedAndBundleEquals(bundle));// used on {@link
		 * 	// ObjectAssert}
		 * }
		 * </pre>
		 *
		 * @param bundle - the bundle that would be checked against the bundle of the
		 *               {@link BundleEvent}
		 * @return the Condition
		 */
		static Condition<BundleEvent> typeUnresolvedAndBundleEquals(Bundle bundle) {
			return Assertions.allOf(typeUnresolved(), bundleEquals(bundle));
		}

		/**
		 * Creates a {@link Condition} to be met by an {@link BundleEvent}. Checking if
		 * the {@link BundleEvent} type <b>matches BundleEvent.UPDATED</b>.
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
		 * Creates a {@link Condition} to be met by an {@link BundleEvent}. Checking if
		 * a the type of the {@link BundleEvent} is <b>BundleEvent.UPDATED</b>
		 * <i>and</i> the given {@link Bundle} <b>equals</b> the Bundle of the of
		 * lifecyle event.
		 *
		 * <pre>
		 * List<BundleEvent> bundleEvents = null;
		 *
		 * static void example_typeUpdatedAndBundleEquals(Bundle bundle) {
		 *
		 * 	assertThat(bundleEvents)// created an {@link ListAssert}
		 * 			.have(typeUpdatedAndBundleEquals(bundle)).filteredOn(typeUpdatedAndBundleEquals(bundle)).first()// map to
		 * 																											// {@link
		 * 																											// ObjectAssert}
		 * 			.has(typeUpdatedAndBundleEquals(bundle));// used on {@link
		 * 	// ObjectAssert}
		 * }
		 * </pre>
		 *
		 * @param bundle - the bundle that would be checked against the bundle of the
		 *               {@link BundleEvent}
		 * @return the Condition
		 */
		static Condition<BundleEvent> typeUpdatedAndBundleEquals(Bundle bundle) {
			return Assertions.allOf(typeUpdated(), bundleEquals(bundle));
		}
	}

	/**
	 * A Utility-Class thats Provides static methods to create
	 * {@link DictionaryConditions} for an {@link Dictionary}
	 */
	interface DictionaryConditions {
		static Condition<Dictionary<String, Object>> servicePropertiesContains(Dictionary<String, Object> dictionary) {
			return servicePropertiesContains(Dictionaries.asMap(dictionary));
		}

		static Condition<Dictionary<String, Object>> servicePropertiesMatch(String filter)
				throws InvalidSyntaxException {
			Filter f = FrameworkUtil.createFilter(filter);

			return new Condition<Dictionary<String, Object>>(d -> {
				f.match(d);
				return true;
			}, "machts filter %s", filter);

		}

		static Condition<Dictionary<String, Object>> servicePropertiesContains(Map<String, Object> map) {
			return new Condition<Dictionary<String, Object>>(d -> {
				List<String> keys = Collections.list(d.keys());
				for (Entry<String, Object> entry : map.entrySet()) {
					if (!keys.contains(entry.getKey())) {
						return false;
					}
					if (!Objects.equals(d.get(entry.getKey()), entry.getValue())) {
						return false;
					}
				}
				return true;
			}, "contains ServiceProperties %s", map);
		}

		static Condition<Dictionary<String, Object>> servicePropertyContains(final String key, Object value) {
			return servicePropertiesContains(Dictionaries.dictionaryOf(key, value));
		}
	}

	/**
	 * A Utility-Class thats Provides static methods to create
	 * {@link FrameworkEventConditions} for an {@link FrameworkEvent}
	 */
	interface FrameworkEventConditions {

		/**
		 * Creates a {@link Condition} to be met by an {@link FrameworkEvent}. Checking
		 * if a {@link Bundle} is <b>equal</b> the bundle that is associated with the
		 * event {@link BundleEvent}.
		 * <p>
		 * Example:
		 *
		 * <pre>
		 * List<FrameworkEvent> frameworkEvents = null;
		 *
		 * static void example_bundleEquals(Bundle bundle) {
		 *
		 * 	assertThat(bundleEvents)// created an {@link ListAssert}
		 * 			.have(bundleEquals(bundle)).filteredOn(bundleEquals(bundle)).first()// map to {@link ObjectAssert}
		 * 			.has(bundleEquals(bundle));// used on {@link ObjectAssert}
		 * }
		 * </pre>
		 *
		 * @param bundle - the bundle that would be checked against the bundle of the
		 *               {@link FrameworkEvent}
		 * @return the Condition<br>
		 */
		static Condition<FrameworkEvent> bundleEquals(Bundle bundle) {

			return MappedCondition.mappedCondition(FrameworkEvent::getBundle, BundleConditions.sameAs(bundle),
					"FrameworkEvent::getBundle");

		}

		/**
		 * Creates a {@link Condition} to be met by an {@link FrameworkEvent}. Checking
		 * if the bundle that is associated with the eventof the {@link FrameworkEvent}
		 * <b>is not null</b>.
		 *
		 * <pre>
		 * List<FrameworkEvent> frameworkEvents = null;
		 *
		 * static void example_bundleIsNotNull() {
		 *
		 * 	assertThat(frameworkEvents)// created an {@link ListAssert}
		 * 			.have(bundleIsNotNull()).filteredOn(bundleIsNotNull()).first()// map to {@link ObjectAssert}
		 * 			.has(bundleIsNotNull());// used on {@link ObjectAssert}
		 * }
		 * </pre>
		 *
		 * @return the Condition
		 */
		static Condition<FrameworkEvent> bundleIsNotNull() {
			return not(bundleIsNull());
		}

		/**
		 * Creates a {@link Condition} to be met by an {@link FrameworkEvent}. Checking
		 * if the bundle that is associated with the eventof the {@link FrameworkEvent}
		 * <b>is null</b>.
		 *
		 * <pre>
		 * List<FrameworkEvent> frameworkEvents = null;
		 *
		 * static void example_bundleIsNull() {
		 *
		 * 	assertThat(frameworkEvents)// created an {@link ListAssert}
		 * 			.have(bundleIsNull()).filteredOn(bundleIsNull()).first()// map to {@link ObjectAssert}
		 * 			.has(bundleIsNull());// used on {@link ObjectAssert}
		 * }
		 * </pre>
		 *
		 * @return the Condition
		 */
		static Condition<FrameworkEvent> bundleIsNull() {
			return bundleEquals(null);
		}

		/**
		 * Creates a {@link Condition} to be met by an {@link FrameworkEvent}. Checking
		 * if a given type-mask <b>matches</b> the type <i>and</i> the given
		 * {@link Bundle} <b>equals</b> the Bundle and the throwable is an instanceof
		 * the throwableClass.
		 *
		 * <pre>
		 * List<FrameworkEvent> frameworkEvents = null;
		 *
		 * static void example_matches(int typeMask, Bundle bundle, Class<Throwable> throwableClass) {
		 *
		 * 	assertThat(frameworkEvents)// created an {@link ListAssert}
		 * 			.have(matches(typeMask, bundle, throwableClass)).filteredOn(matches(typeMask, bundle, throwableClass))
		 * 			.first()// map to {@link ObjectAssert}
		 * 			.has(matches(typeMask, bundle, throwableClass));// used on {@link
		 * 	// ObjectAssert}
		 * }
		 * </pre>
		 *
		 * @param typeMask       - the typeMask that would be checked against the bundle
		 *                       type of the {@link FrameworkEvent}
		 * @param bundle         - the bundle that would be checked against the bundle
		 *                       of the {@link FrameworkEvent}
		 * @param throwableClass - the Class that would be checked against the throwable
		 *                       of the {@link FrameworkEvent}
		 * @return the Condition
		 */
		static Condition<FrameworkEvent> matches(final int eventTypeMask, Bundle bundle,
				final Class<Throwable> throwableClass) {
			return Assertions.allOf(type(eventTypeMask), bundleEquals(bundle), throwableOfClass(throwableClass));
		}

		/**
		 * Creates a {@link Condition} to be met by an {@link FrameworkEvent}. Checking
		 * if a given type-mask <b>matches</b> the type <i>and</i> the throwable is an
		 * instanceof the throwableClass.
		 *
		 * <pre>
		 * List<FrameworkEvent> frameworkEvents = null;
		 *
		 * static void example_matches(int typeMask, Class<Throwable> throwableClass) {
		 *
		 * 	assertThat(frameworkEvents)// created an {@link ListAssert}
		 * 			.have(matches(typeMask, throwableClass)).filteredOn(matches(typeMask, throwableClass)).first()// map to
		 * 																											// {@link
		 * 																											// ObjectAssert}
		 * 			.has(matches(typeMask, throwableClass));// used on {@link
		 * 	// ObjectAssert}
		 * }
		 * </pre>
		 *
		 * @param typeMask       - the typeMask that would be checked against the bundle
		 *                       type of the {@link FrameworkEvent}
		 * @param throwableClass - the Class that would be checked against the throwable
		 *                       of the {@link FrameworkEvent}
		 * @return the Condition
		 */
		static Condition<FrameworkEvent> matches(final int eventTypeMask, final Class<Throwable> throwableClass) {
			return Assertions.allOf(type(eventTypeMask), throwableOfClass(throwableClass));
		}

		/**
		 * Creates a {@link Condition} to be met by an {@link FrameworkEvent}. Checking
		 * if a throwable is not null.
		 *
		 * <pre>
		 * List<FrameworkEvent> frameworkEvents = null;
		 *
		 * static void example_throwableIsNotNull() {
		 *
		 * 	assertThat(frameworkEvents)// created an {@link ListAssert}
		 * 			.have(throwableIsNotNull()).filteredOn(throwableIsNotNull()).first()// map to {@link ObjectAssert}
		 * 			.has(throwableIsNotNull());// used on {@link
		 * 	// ObjectAssert}
		 * }
		 * </pre>
		 *
		 * @return the Condition
		 */
		static Condition<FrameworkEvent> throwableIsNotNull() {
			return not(throwableIsNull());
		}

		/**
		 * Creates a {@link Condition} to be met by an {@link FrameworkEvent}. Checking
		 * if a throwable is null.
		 *
		 * <pre>
		 * List<FrameworkEvent> frameworkEvents = null;
		 *
		 * static void example_throwableIsNull() {
		 *
		 * 	assertThat(frameworkEvents)// created an {@link ListAssert}
		 * 			.have(throwableIsNull()).filteredOn(throwableIsNull()).first()// map to {@link ObjectAssert}
		 * 			.has(throwableIsNull());// used on {@link
		 * 	// ObjectAssert}
		 * }
		 * </pre>
		 *
		 * @return the Condition
		 */
		static Condition<FrameworkEvent> throwableIsNull() {
			return VerboseCondition.verboseCondition((fe) -> fe.getThrowable() == null, "throwable is null",
					(fe) -> fe.getThrowable().toString());
		}

		/**
		 * Creates a {@link Condition} to be met by an {@link FrameworkEvent}. Checking
		 * if a throwable is an instanceof the throwableClass.
		 *
		 * <pre>
		 * List<FrameworkEvent> frameworkEvents = null;
		 *
		 * static void example_throwableOfClass(Class<Throwable> throwableClass) {
		 *
		 * 	assertThat(frameworkEvents)// created an {@link ListAssert}
		 * 			.have(throwableOfClass(throwableClass)).filteredOn(throwableOfClass(throwableClass)).first()// map to
		 * 																										// {@link
		 * 																										// ObjectAssert}
		 * 			.has(throwableOfClass(throwableClass));// used on {@link
		 * 	// ObjectAssert}
		 * }
		 * </pre>
		 *
		 * @param throwableClass - the Class that would be checked against the throwable
		 *                       of the {@link FrameworkEvent}
		 * @return the Condition
		 */
		static Condition<FrameworkEvent> throwableOfClass(final Class<? extends Throwable> throwableClass) {

			Condition<FrameworkEvent> cond = VerboseCondition.verboseCondition(
					(fe) -> fe.getThrowable().getClass().isAssignableFrom(throwableClass), "throwable of class",
					fe -> fe.getThrowable().toString());

			return allOf(throwableIsNotNull(), cond);//

		}

		/**
		 * Creates a {@link Condition} to be met by an {@link FrameworkEvent}. Checking
		 * if a given type-mask <b>matches</b> the type.
		 *
		 * <pre>
		 * List<FrameworkEvent> frameworkEvents = null;
		 *
		 * static void example_type(int typeMask) {
		 *
		 * 	assertThat(frameworkEvents)// created an {@link ListAssert}
		 * 			.have(type(type)).filteredOn(type(typeMask)).first()// map to {@link ObjectAssert}
		 * 			.has(type(typeMask));// used on {@link
		 * 	// ObjectAssert}
		 * }
		 * </pre>
		 *
		 * @param typeMask - the typeMask that would be checked against the bundle type
		 *                 of the {@link FrameworkEvent}
		 * @return the Condition
		 */
		static Condition<FrameworkEvent> type(final int eventTypeMask) {

			return VerboseCondition.verboseCondition(
					(FrameworkEvent fe) -> typeMatchesMask(fe.getType(), eventTypeMask),
					"type matches mask " + FrameworkEventType.BITMAP.maskToString(eventTypeMask), //
					fe -> FrameworkEventType.BITMAP.toString(fe.getType()));

		}

		/**
		 * Creates a {@link Condition} to be met by an {@link FrameworkEvent}. Checking
		 * if a given type-mask <b>matches</b> the type <i>and</i> the given
		 * {@link Bundle} <b>equals</b> the Bundle.
		 *
		 * <pre>
		 * List<FrameworkEvent> frameworkEvents = null;
		 *
		 * static void example_typeAndBundle(int typeMask, Bundle bundle) {
		 *
		 * 	assertThat(frameworkEvents)// created an {@link ListAssert}
		 * 			.have(typeAndBundle(typeMask, bundles)).filteredOn(typeAndBundle(typeMask, bundle)).first()// map to
		 * 																										// {@link
		 * 																										// ObjectAssert}
		 * 			.has(typeAndBundle(typeMask, bundle));// used on {@link
		 * 	// ObjectAssert}
		 * }
		 * </pre>
		 *
		 * @param typeMask - the typeMask that would be checked against the bundle type
		 *                 of the {@link FrameworkEvent}
		 * @param bundle   - the bundle that would be checked against the bundle of the
		 *                 {@link FrameworkEvent}
		 * @return the Condition
		 */
		static Condition<FrameworkEvent> typeAndBundle(final int eventTypeMask, Bundle bundle) {
			return Assertions.allOf(type(eventTypeMask), bundleEquals(bundle));
		}

		/**
		 * Creates a {@link Condition} to be met by an {@link FrameworkEvent}. Checking
		 * if type of {@link FrameworkEvent} <b>matches</b> <b>FrameworkEvent.ERROR</b>.
		 *
		 * <pre>
		 * List<FrameworkEvent> frameworkEvents = null;
		 *
		 * static void example_typeError() {
		 *
		 * 	assertThat(frameworkEvents)// created an {@link ListAssert}
		 * 			.have(typeError()).filteredOn(typeError()).first()// map to {@link ObjectAssert}
		 * 			.has(typeError());// used on {@link
		 * 	// ObjectAssert}
		 * }
		 * </pre>
		 *
		 * @return the Condition
		 */
		static Condition<FrameworkEvent> typeError() {
			return type(FrameworkEvent.ERROR);
		}

		/**
		 * Creates a {@link Condition} to be met by an {@link FrameworkEvent}. Checking
		 * if type of {@link FrameworkEvent} <b>matches</b> <b>FrameworkEvent.ERROR</b>
		 * and the throwable of the {@link FrameworkEvent} is an instance of the given
		 * Class.
		 *
		 * <pre>
		 * List<FrameworkEvent> frameworkEvents = null;
		 *
		 * static void example_typeErrorAndThrowableOfClass(Class<Throwable> throwableClass) {
		 *
		 * 	assertThat(frameworkEvents)// created an {@link ListAssert}
		 * 			.have(typeErrorAndThrowableOfClass(throwableClass))
		 * 			.filteredOn(typeErrorAndThrowableOfClass(throwableClass)).first()// map to {@link ObjectAssert}
		 * 			.has(typeErrorAndThrowableOfClass(throwableClass));// used on {@link
		 * 	// ObjectAssert}
		 * }
		 * </pre>
		 *
		 * @param throwableClass - the Class that would be checked against the throwable
		 *                       of the {@link FrameworkEvent}
		 * @return the Condition
		 */
		static Condition<FrameworkEvent> typeErrorAndThrowableOfClass(final Class<Throwable> throwableClass) {
			return Assertions.allOf(typeError(), throwableOfClass(throwableClass));
		}

		/**
		 * Creates a {@link Condition} to be met by an {@link FrameworkEvent}. Checking
		 * if type of {@link FrameworkEvent} <b>matches</b> <b>FrameworkEvent.INFO</b>.
		 *
		 * <pre>
		 * List<FrameworkEvent> frameworkEvents = null;
		 *
		 * static void example_typeInfo() {
		 *
		 * 	assertThat(frameworkEvents)// created an {@link ListAssert}
		 * 			.have(typeInfo()).filteredOn(typeInfo()).first()// map to {@link ObjectAssert}
		 * 			.has(typeInfo());// used on {@link
		 * 	// ObjectAssert}
		 * }
		 * </pre>
		 *
		 * @return the Condition
		 */

		static Condition<FrameworkEvent> typeInfo() {
			return type(FrameworkEvent.INFO);
		}

		/**
		 * Creates a {@link Condition} to be met by an {@link FrameworkEvent}. Checking
		 * if type of {@link FrameworkEvent} <b>matches</b> <b>FrameworkEvent.INFO</b>
		 * and the throwable of the {@link FrameworkEvent} is an instance of the given
		 * Class.
		 *
		 * <pre>
		 * List<FrameworkEvent> frameworkEvents = null;
		 *
		 * static void example_typeInfoAndThrowableOfClass(Class<Throwable> throwableClass) {
		 *
		 * 	assertThat(frameworkEvents)// created an {@link ListAssert}
		 * 			.have(typeInfoAndThrowableOfClass(throwableClass))
		 * 			.filteredOn(typeInfoAndThrowableOfClass(throwableClass)).first()// map to {@link ObjectAssert}
		 * 			.has(typeInfoAndThrowableOfClass(throwableClass));// used on {@link
		 * 	// ObjectAssert}
		 * }
		 * </pre>
		 *
		 * @param throwableClass - the Class that would be checked against the throwable
		 *                       of the {@link FrameworkEvent}
		 * @return the Condition
		 */
		static Condition<FrameworkEvent> typeInfoAndThrowableOfClass(final Class<Throwable> throwableClass) {
			return Assertions.allOf(typeInfo(), throwableOfClass(throwableClass));
		}

		/**
		 * Creates a {@link Condition} to be met by an {@link FrameworkEvent}. Checking
		 * if type of {@link FrameworkEvent} <b>matches</b>
		 * <b>FrameworkEvent.PACKAGES_REFRESHED</b>.
		 *
		 * <pre>
		 * List<FrameworkEvent> frameworkEvents = null;
		 *
		 * static void example_typePackagesRefreshed() {
		 *
		 * 	assertThat(frameworkEvents)// created an {@link ListAssert}
		 * 			.have(typePackagesRefreshed()).filteredOn(typePackagesRefreshed()).first()// map to {@link ObjectAssert}
		 * 			.has(typePackagesRefreshed());// used on {@link
		 * 	// ObjectAssert}
		 * }
		 * </pre>
		 *
		 * @return the Condition
		 */

		static Condition<FrameworkEvent> typePackagesRefreshed() {
			return type(FrameworkEvent.PACKAGES_REFRESHED);
		}

		/**
		 * Creates a {@link Condition} to be met by an {@link FrameworkEvent}. Checking
		 * if type of {@link FrameworkEvent} <b>matches</b>
		 * <b>FrameworkEvent.STARTED</b>.
		 *
		 * <pre>
		 * List<FrameworkEvent> frameworkEvents = null;
		 *
		 * static void example_typeStarted() {
		 *
		 * 	assertThat(frameworkEvents)// created an {@link ListAssert}
		 * 			.have(typeStarted()).filteredOn(typeStarted()).first()// map to {@link ObjectAssert}
		 * 			.has(typeStarted());// used on {@link
		 * 	// ObjectAssert}
		 * }
		 * </pre>
		 *
		 * @return the Condition
		 */

		static Condition<FrameworkEvent> typeStarted() {
			return type(FrameworkEvent.STARTED);
		}

		/**
		 * Creates a {@link Condition} to be met by an {@link FrameworkEvent}. Checking
		 * if type of {@link FrameworkEvent} <b>matches</b>
		 * <b>FrameworkEvent.STARTLEVEL_CHANGED</b>.
		 *
		 * <pre>
		 * List<FrameworkEvent> frameworkEvents = null;
		 *
		 * static void example_typeStartLevelChanged() {
		 *
		 * 	assertThat(typeStartLevelChanged)// created an {@link ListAssert}
		 * 			.have(typeError()).filteredOn(typeStartLevelChanged()).first()// map to {@link ObjectAssert}
		 * 			.has(typeStartLevelChanged());// used on {@link
		 * 	// ObjectAssert}
		 * }
		 * </pre>
		 *
		 * @return the Condition
		 */

		static Condition<FrameworkEvent> typeStartLevelChanged() {
			return type(FrameworkEvent.STARTLEVEL_CHANGED);
		}

		/**
		 * Creates a {@link Condition} to be met by an {@link FrameworkEvent}. Checking
		 * if type of {@link FrameworkEvent} <b>matches</b>
		 * <b>FrameworkEvent.STOPPED</b>.
		 *
		 * <pre>
		 * List<FrameworkEvent> frameworkEvents = null;
		 *
		 * static void example_typeStopped() {
		 *
		 * 	assertThat(frameworkEvents)// created an {@link ListAssert}
		 * 			.have(typeStopped()).filteredOn(typeStopped()).first()// map to {@link ObjectAssert}
		 * 			.has(typeStopped());// used on {@link
		 * 	// ObjectAssert}
		 * }
		 * </pre>
		 *
		 * @return the Condition
		 */

		static Condition<FrameworkEvent> typeStopped() {
			return type(FrameworkEvent.STOPPED);
		}

		/**
		 * Creates a {@link Condition} to be met by an {@link FrameworkEvent}. Checking
		 * if type of {@link FrameworkEvent} <b>matches</b>
		 * <b>FrameworkEvent.STOPPED_BOOTCLASSPATH_MODIFIED</b>.
		 *
		 * <pre>
		 * List<FrameworkEvent> frameworkEvents = null;
		 *
		 * static void example_typeStopped_BootClasspathModified() {
		 *
		 * 	assertThat(frameworkEvents)// created an {@link ListAssert}
		 * 			.have(typeStopped_BootClasspathModified()).filteredOn(typeStopped_BootClasspathModified()).first()// map
		 * 																												// to
		 * 																												// {@link
		 * 																												// ObjectAssert}
		 * 			.has(typeStopped_BootClasspathModified());// used on {@link
		 * 	// ObjectAssert}
		 * }
		 * </pre>
		 *
		 * @return the Condition
		 */

		static Condition<FrameworkEvent> typeStopped_BootClasspathModified() {
			return type(FrameworkEvent.STOPPED_BOOTCLASSPATH_MODIFIED);
		}

		// /**
		// * Creates a {@link Condition} to be met by an {@link FrameworkEvent}.
		// * Checking if type of {@link FrameworkEvent} <b>matches</b>
		// * <b>FrameworkEvent.STOPPED_SYSTEM_REFRESHED</b>.
		// *
		// * <pre>
		// * List<FrameworkEvent> frameworkEvents = null;
		// *
		// * static void example_typeStoppedSystemRefreshes() {
		// *
		// * assertThat(frameworkEvents)// created an {@link ListAssert}
		// * .have(typeStoppedSystemRefreshes())
		// * .filteredOn(typeStoppedSystemRefreshes())
		// * .first()// map to {@link ObjectAssert}
		// * .has(typeStoppedSystemRefreshes());// used on {@link
		// * // ObjectAssert}
		// * }
		// * </pre>
		// *
		// * @return the Condition
		// */
		//
		// static Condition<FrameworkEvent> typeStoppedSystemRefreshes() {
		// return type(FrameworkEvent.STOPPED_SYSTEM_REFRESHED);
		// }

		/**
		 * Creates a {@link Condition} to be met by an {@link FrameworkEvent}. Checking
		 * if type of {@link FrameworkEvent} <b>matches</b>
		 * <b>FrameworkEvent.STOPPED_UPDATE</b>.
		 *
		 * <pre>
		 * List<FrameworkEvent> frameworkEvents = null;
		 *
		 * static void example_typeUpdate() {
		 *
		 * 	assertThat(frameworkEvents)// created an {@link ListAssert}
		 * 			.have(typeUpdate()).filteredOn(typeUpdate()).first()// map to {@link ObjectAssert}
		 * 			.has(typeUpdate());// used on {@link
		 * 	// ObjectAssert}
		 * }
		 * </pre>
		 *
		 * @return the Condition
		 */
		static Condition<FrameworkEvent> typeStoppedUpdate() {
			return type(FrameworkEvent.STOPPED_UPDATE);
		}

		/**
		 * Creates a {@link Condition} to be met by an {@link FrameworkEvent}. Checking
		 * if type of {@link FrameworkEvent} <b>matches</b>
		 * <b>FrameworkEvent.WAIT_TIMEDOUT</b>.
		 *
		 * <pre>
		 * List<FrameworkEvent> frameworkEvents = null;
		 *
		 * static void example_typeWaitTimeout() {
		 *
		 * 	assertThat(frameworkEvents)// created an {@link ListAssert}
		 * 			.have(typeWaitTimeout()).filteredOn(typeWaitTimeout()).first()// map to {@link ObjectAssert}
		 * 			.has(typeWaitTimeout());// used on {@link
		 * 	// ObjectAssert}
		 * }
		 * </pre>
		 *
		 * @return the Condition
		 */

		static Condition<FrameworkEvent> typeWaitTimeout() {
			return type(FrameworkEvent.WAIT_TIMEDOUT);
		}

		/**
		 * Creates a {@link Condition} to be met by an {@link FrameworkEvent}. Checking
		 * if type of {@link FrameworkEvent} <b>matches</b>
		 * <b>FrameworkEvent.WARNING</b>.
		 *
		 * <pre>
		 * List<FrameworkEvent> frameworkEvents = null;
		 *
		 * static void example_typeWarning() {
		 *
		 * 	assertThat(frameworkEvents)// created an {@link ListAssert}
		 * 			.have(typeWarning()).filteredOn(typeWarning()).first()// map to {@link ObjectAssert}
		 * 			.has(typeWarning());// used on {@link
		 * 	// ObjectAssert}
		 * }
		 * </pre>
		 *
		 * @return the Condition
		 */
		static Condition<FrameworkEvent> typeWarning() {
			return type(FrameworkEvent.WARNING);
		}

		/**
		 * Creates a {@link Condition} to be met by an {@link FrameworkEvent}. Checking
		 * if type of {@link FrameworkEvent} <b>matches</b>
		 * <b>FrameworkEvent.WARNING</b> and the throwable of the {@link FrameworkEvent}
		 * is an instance of the given Class.
		 *
		 * <pre>
		 * List<FrameworkEvent> frameworkEvents = null;
		 *
		 * static void example_typeInfoAndThrowableOfClass(Class<Throwable> throwableClass) {
		 *
		 * 	assertThat(frameworkEvents)// created an {@link ListAssert}
		 * 			.have(typeInfoAndThrowableOfClass(throwableClass))
		 * 			.filteredOn(typeInfoAndThrowableOfClass(throwableClass)).first()// map to {@link ObjectAssert}
		 * 			.has(typeInfoAndThrowableOfClass(throwableClass));// used on {@link
		 * 	// ObjectAssert}
		 * }
		 * </pre>
		 *
		 * @param throwableClass - the Class that would be checked against the throwable
		 *                       of the {@link FrameworkEvent}
		 * @return the Condition
		 */
		static Condition<FrameworkEvent> typeWarningAndThrowableOfClass(final Class<Throwable> throwableClass) {
			return Assertions.allOf(typeWarning(), throwableOfClass(throwableClass));
		}

	}

	/**
	 * A Utility-Class thats Provides static methods to create
	 * {@link ServiceEventConditions} for an {@link ServiceEvent}
	 */
	interface ServiceEventConditions {

		/**
		 * Creates a {@link Condition} to be met by an {@link ServiceEvent}. Checking if
		 * a given type-mask <b>matches</b> the type and the given Class<?> objectClass
		 * matches a objectClass of the ServiceReference.
		 *
		 * <pre>
		 * List<ServiceEvent> serviceEvents = null;
		 *
		 * static void example_matches(int eventTypeMask, Class<?> objectClass) {
		 *
		 * 	assertThat(serviceEvents)// created an {@link ListAssert}
		 * 			.have(matches(eventTypeMask, objectClass)).filteredOn(matches(eventTypeMask, objectClass)).first()// map
		 * 																												// to
		 * 																												// {@link
		 * 																												// ObjectAssert}
		 * 			.has(matches(eventTypeMask, objectClass));// used on {@link
		 * 	// ObjectAssert}
		 * }
		 * </pre>
		 *
		 * @param typeMask    - the typeMask that would be checked against the bundle
		 *                    type of the {@link ServiceEvent}
		 * @param objectClass - the objectClass that would be tested against the
		 *                    ServiceReference
		 * @return the Condition
		 */
		static Condition<ServiceEvent> matches(int eventTypeMask, final Class<?> objectClass) {
			Condition<ServiceEvent> cType = type(eventTypeMask);
			Condition<ServiceEvent> cObjectClass = serviceReferenceHas(objectClass(objectClass));
			return Assertions.allOf(cType, cObjectClass);
		}

		/**
		 * Creates a {@link Condition} to be met by an {@link ServiceEvent}. Checking if
		 * a given type-mask <b>matches</b> the type and the given String filter matches
		 * the ServiceReference.
		 *
		 * <pre>
		 * List<ServiceEvent> serviceEvents = null;
		 *
		 * static void example_matches(int eventTypeMask, String filter) {
		 *
		 * 	assertThat(serviceEvents)// created an {@link ListAssert}
		 * 			.have(matches(eventTypeMask, filter)).filteredOn(matches(eventTypeMask, filter)).first()// map
		 * 																									// to
		 * 																									// {@link
		 * 																									// ObjectAssert}
		 * 			.has(matches(eventTypeMask, filter));// used on {@link
		 * 	// ObjectAssert}
		 * }
		 * </pre>
		 *
		 * @param typeMask - the typeMask that would be checked against the bundle type
		 *                 of the {@link ServiceEvent}
		 * @param filter   - the filter String would be tested against the
		 *                 ServiceReference
		 * @return the Condition
		 */
		static Condition<ServiceEvent> matches(int eventTypeMask, final String filter) throws InvalidSyntaxException {
			Condition<ServiceEvent> cType = type(eventTypeMask);
			Condition<ServiceEvent> cObjectClass = serviceReferenceHas(
					ServiceReferenceConditions.serviceReferenceMatch(filter));
			return Assertions.allOf(cType, cObjectClass);
		}

		/**
		 * Creates a {@link Condition} to be met by an {@link ServiceEvent}. Checking if
		 * a given type-mask <b>matches</b> the type and the given Class<?> objectClass
		 * matches a objectClass of the serviceReference and given dictionary's entries
		 * are contained in the serviceProperties of the ServiceReverence of the
		 * {@link ServiceEvent}
		 *
		 * <pre>
		 * List<ServiceEvent> serviceEvents = null;
		 *
		 * static void example_matches(int eventTypeMask, Class<?> objectClass, Dictionary<String, Object> dictionary) {
		 *
		 * 	assertThat(serviceEvents)// created an {@link ListAssert}
		 * 			.have(matches(eventTypeMask, objectClass, dictionary))
		 * 			.filteredOn(matches(eventTypeMask, objectClass, dictionary)).first()// map to {@link ObjectAssert}
		 * 			.has(matches(eventTypeMask, objectClass, dictionary));// used on
		 * 																	// {@link
		 * 	// ObjectAssert}
		 * }
		 * </pre>
		 *
		 * @param typeMask    - the typeMask that would be checked against the bundle
		 *                    type of the {@link ServiceEvent}
		 * @param objectClass - the objectClass that would be tested against the
		 *                    ServiceReference
		 * @param dictionary  - the dictionary's entries that would be tested against
		 *                    the ServiceReference serviceProperties
		 * @return the Condition
		 */
		static Condition<ServiceEvent> matches(int eventTypeMask, final Class<?> objectClass,
				Dictionary<String, Object> dictionary) {
			return matches(eventTypeMask, objectClass, Dictionaries.asMap(dictionary));
		}

		/**
		 * Creates a {@link Condition} to be met by an {@link ServiceEvent}. Checking if
		 * a given type-mask <b>matches</b> the type and the given Class<?> objectClass
		 * matches a objectClass of the serviceReference and given Map's entries are
		 * contained in the serviceProperties of the ServiceReverence of the
		 * {@link ServiceEvent}
		 *
		 * <pre>
		 * List<ServiceEvent> serviceEvents = null;
		 *
		 * static void example_matches(int eventTypeMask, Class<?> objectClass, Map<String, Object> map) {
		 *
		 * 	assertThat(serviceEvents)// created an {@link ListAssert}
		 * 			.have(matches(eventTypeMask, objectClass, map)).filteredOn(matches(eventTypeMask, objectClass, map))
		 * 			.first()// map to {@link ObjectAssert}
		 * 			.has(matches(eventTypeMask, objectClass, map));
		 * 	// used on {@link ObjectAssert}
		 * }
		 * </pre>
		 *
		 * @param typeMask    - the typeMask that would be checked against the bundle
		 *                    type of the {@link ServiceEvent}
		 * @param objectClass - the objectClass that would be tested against the
		 *                    ServiceReference
		 * @param map         - the maps entries that would be tested against the
		 *                    ServiceReference serviceProperties
		 * @return the Condition
		 */
		static Condition<ServiceEvent> matches(int eventTypeMask, final Class<?> objectClass, Map<String, Object> map) {
			Condition<ServiceEvent> cType = type(eventTypeMask);
			Condition<ServiceEvent> cObjectClass = serviceReferenceHas(objectClass(objectClass));
			Condition<ServiceEvent> cProperties = serviceReferenceHas(
					servicePropertiesHas(servicePropertiesContains(map)));
			return Assertions.allOf(cType, cObjectClass, cProperties);
		}

		/**
		 * Creates a {@link Condition} to be met by an {@link ServiceEvent}. Checking if
		 * the serviceReference of the {@link ServiceEvent} <b>equals</b> the given
		 * serviceReference.
		 *
		 * <pre>
		 * List<ServiceEvent> serviceEvent = null;
		 *
		 * static void example_serviceReferenceEquals(ServiceReference<?> serviceReference) {
		 *
		 * 	assertThat(serviceEvent)// created an {@link ListAssert}
		 * 			.have(serviceReferenceEquals(serviceReference)).filteredOn(serviceReferenceEquals(serviceReference))
		 * 			.first()// map to {@link ObjectAssert}
		 * 			.has(serviceReferenceEquals(serviceReference));// used on {@link
		 * 															// ObjectAssert}
		 * }
		 * </pre>
		 *
		 * @return the Condition
		 */
		static Condition<ServiceEvent> serviceReferenceEquals(ServiceReference<?> serviceReference) {

			return MappedCondition.mappedCondition(ServiceEvent::getServiceReference,
					ServiceReferenceConditions.sameAs(serviceReference), "ServiceEvent::getServiceReference");
		}

		/**
		 * Creates a {@link Condition} to be met by an {@link ServiceEvent}. Checking if
		 * the serviceReference of the {@link ServiceEvent} <b>matches</b> the given
		 * serviceReferenceCondition {@link Condition}.
		 *
		 * <pre>
		 * List<ServiceEvent> serviceEvent = null;
		 *
		 * static void example_serviceReferenceHas(Condition<ServiceReference<?>> serviceReferenceCondition) {
		 *
		 * 	assertThat(serviceEvent)// created an {@link ListAssert}
		 * 			.have(serviceReferenceHas(serviceReferenceCondition))
		 * 			.filteredOn(serviceReferenceHas(serviceReferenceCondition)).first()// map to {@link ObjectAssert}
		 * 			.has(serviceReferenceHas(serviceReferenceCondition));// used on
		 * 																	// {@link
		 * 	// ObjectAssert}
		 * }
		 * </pre>
		 *
		 * @return the Condition
		 */
		static Condition<ServiceEvent> serviceReferenceHas(Condition<ServiceReference<?>> serviceReferenceCondition) {
			Condition<ServiceEvent> mc = MappedCondition.mappedCondition(ServiceEvent::getServiceReference,
					serviceReferenceCondition,
					"ServiceEvent to ServiceReference using ServiceEvent::getServiceReference");
			return Assertions.allOf(serviceReferenceIsNotNull(), mc);
		}

		/**
		 * Creates a {@link Condition} to be met by an {@link ServiceEvent}. Checking if
		 * the serviceReference of the {@link ServiceEvent} <b>is not null</b>.
		 *
		 * <pre>
		 * List<ServiceEvent> serviceEvent = null;
		 *
		 * static void example_serviceReferenceIsNull() {
		 *
		 * 	assertThat(serviceEvent)// created an {@link ListAssert}
		 * 			.have(serviceReferenceIsNotNull()).filteredOn(serviceReferenceIsNotNull()).first()// map to {@link
		 * 																								// ObjectAssert}
		 * 			.has(serviceReferenceIsNotNull());// used on {@link ObjectAssert}
		 * }
		 * </pre>
		 *
		 * @return the Condition
		 */
		static Condition<ServiceEvent> serviceReferenceIsNotNull() {
			return not(serviceReferenceIsNull());
		}

		/**
		 * Creates a {@link Condition} to be met by an {@link ServiceEvent}. Checking if
		 * the serviceReference of the {@link ServiceEvent} <b>is null</b>.
		 *
		 * <pre>
		 * List<ServiceEvent> serviceEvent = null;
		 *
		 * static void example_serviceReferenceIsNull() {
		 *
		 * 	assertThat(serviceEvent)// created an {@link ListAssert}
		 * 			.have(serviceReferenceIsNull()).filteredOn(serviceReferenceIsNull()).first()// map to {@link
		 * 																						// ObjectAssert}
		 * 			.has(serviceReferenceIsNull());// used on {@link ObjectAssert}
		 * }
		 * </pre>
		 *
		 * @return the Condition
		 */
		static Condition<ServiceEvent> serviceReferenceIsNull() {

			return VerboseCondition.verboseCondition((sericeEvent) -> sericeEvent.getServiceReference() == null,
					"serviceReference is null", (se) -> se.getServiceReference().toString());

		}

		/**
		 * Creates a {@link Condition} to be met by an {@link ServiceEvent}. Checking if
		 * a given type-mask <b>matches</b> the type.
		 *
		 * <pre>
		 * List<ServiceEvent> serviceEvents = null;
		 *
		 * static void example_type(int typeMask) {
		 *
		 * 	assertThat(serviceEvents)// created an {@link ListAssert}
		 * 			.have(type(type)).filteredOn(type(typeMask)).first()// map to {@link ObjectAssert}
		 * 			.has(type(typeMask));// used on {@link
		 * 	// ObjectAssert}
		 * }
		 * </pre>
		 *
		 * @param typeMask - the typeMask that would be checked against the bundle type
		 *                 of the {@link ServiceEvent}
		 * @return the Condition
		 */
		static Condition<ServiceEvent> type(final int eventTypeMask) {

			return VerboseCondition.verboseCondition(
					(ServiceEvent se) -> typeMatchesMask(se.getType(), eventTypeMask),
					"type matches mask " + ServiceEventType.BITMAP.maskToString(eventTypeMask), //
					se -> " but was " + ServiceEventType.BITMAP.toString(se.getType()));
		}

		/**
		 * Creates a {@link Condition} to be met by an {@link ServiceEvent}. Checking if
		 * type of {@link ServiceEvent} <b>matches</b> <b>ServiceEvent.MODIFIED</b>.
		 *
		 * <pre>
		 * List<ServiceEvent> frameworkEvents = null;
		 *
		 * static void example_typeModified() {
		 *
		 * 	assertThat(serviceEvents)// created an {@link ListAssert}
		 * 			.have(typeModified()).filteredOn(typeModified()).first()// map to {@link ObjectAssert}
		 * 			.has(typeModified());// used on {@link
		 * 	// ObjectAssert}
		 * }
		 * </pre>
		 *
		 * @return the Condition
		 */
		static Condition<ServiceEvent> typeModified() {
			return type(ServiceEvent.MODIFIED);
		}

		/**
		 * Creates a {@link Condition} to be met by an {@link ServiceEvent}. Checking if
		 * type of {@link ServiceEvent} <b>matches</b> <b>ServiceEvent.MODIFIED</b> and
		 * the given Class<?> objectClass matches a objectClass of the ServiceReference.
		 *
		 * <pre>
		 * List<ServiceEvent> serviceEvents = null;
		 *
		 * static void example_typeModifiedAndObjectClassClass<?> objectClass) {
		 *
		 * 	assertThat(serviceEvents)// created an {@link ListAssert}
		 * 		.have(typeModifiedAndObjectClass(objectClass))
		 * 		.filteredOn(typeModifiedAndObjectClass(objectClass))
		 * 		.first()// map to {@link ObjectAssert}
		 * 		.has(typeModifiedAndObjectClass(objectClass));// used on {@link
		 * 	// ObjectAssert}
		 * }
		 * </pre>
		 *
		 * @param objectClass - the objectClass that would be tested against the
		 *                    ServiceReference
		 * @return the Condition
		 */
		static Condition<ServiceEvent> typeModifiedAndObjectClass(final Class<?> objectClass) {
			return matches(ServiceEvent.MODIFIED, objectClass);
		}

		/**
		 * Creates a {@link Condition} to be met by an {@link ServiceEvent}. Checking if
		 * type of {@link ServiceEvent} <b>matches</b>
		 * <b>ServiceEvent.MODIFIED_ENDMATCH</b>.
		 *
		 * <pre>
		 * List<ServiceEvent> frameworkEvents = null;
		 *
		 * static void example_typeModifiedEndmatch() {
		 *
		 * 	assertThat(serviceEvents)// created an {@link ListAssert}
		 * 			.have(typeModifiedEndmatch()).filteredOn(typeModifiedEndmatch()).first()// map to {@link ObjectAssert}
		 * 			.has(typeModifiedEndmatch());// used on {@link
		 * 	// ObjectAssert}
		 * }
		 * </pre>
		 *
		 * @return the Condition
		 */
		static Condition<ServiceEvent> typeModifiedEndmatch() {
			return type(ServiceEvent.MODIFIED_ENDMATCH);
		}

		/**
		 * Creates a {@link Condition} to be met by an {@link ServiceEvent}. Checking if
		 * type of {@link ServiceEvent} <b>matches</b>
		 * <b>ServiceEvent.MODIFIED_ENDMATCH</b> and the given Class<?> objectClass
		 * matches a objectClass of the ServiceReference.
		 *
		 * <pre>
		 * List<ServiceEvent> serviceEvents = null;
		 *
		 * static void example_typeUnregisteringAndObjectClass(Class<?> objectClass) {
		 *
		 * 	assertThat(serviceEvents)// created an {@link ListAssert}
		 * 			.have(typeModifiedEndmatchAndObjectClass(objectClass))
		 * 			.filteredOn(typeModifiedEndmatchAndObjectClass(objectClass)).first()// map to {@link ObjectAssert}
		 * 			.has(typeModifiedEndmatchAndObjectClass(objectClass));// used on
		 * 																	// {@link
		 * 	// ObjectAssert}
		 * }
		 * </pre>
		 *
		 * @param objectClass - the objectClass that would be tested against the
		 *                    ServiceReference
		 * @return the Condition
		 */
		static Condition<ServiceEvent> typeModifiedEndmatchAndObjectClass(final Class<?> objectClass) {
			return matches(ServiceEvent.MODIFIED_ENDMATCH, objectClass);
		}

		/**
		 * Creates a {@link Condition} to be met by an {@link ServiceEvent}. Checking if
		 * type of {@link ServiceEvent} <b>matches</b> <b>ServiceEvent.REGISTERED</b>.
		 *
		 * <pre>
		 * List<ServiceEvent> serviceEvents = null;
		 *
		 * static void example_typeRegistered() {
		 *
		 * 	assertThat(serviceEvents)// created an {@link ListAssert}
		 * 			.have(typeRegistered()).filteredOn(typeRegistered()).first()// map to {@link ObjectAssert}
		 * 			.has(typeRegistered());// used on {@link
		 * 	// ObjectAssert}
		 * }
		 * </pre>
		 *
		 * @return the Condition
		 */
		static Condition<ServiceEvent> typeRegistered() {
			return type(ServiceEvent.REGISTERED);
		}

		/**
		 * Creates a {@link Condition} to be met by an {@link ServiceEvent}. Checking if
		 * type of {@link ServiceEvent} <b>matches</b> <b>ServiceEvent.REGISTERED</b>
		 * and the given Class<?> objectClass matches a objectClass of the
		 * ServiceReference.
		 *
		 * <pre>
		 * List<ServiceEvent> serviceEvents = null;
		 *
		 * static void example_typeRegisteredAndObjectClass(Class<?> objectClass) {
		 *
		 * 	assertThat(serviceEvents)// created an {@link ListAssert}
		 * 			.have(typeRegisteredAndObjectClass(objectClass)).filteredOn(typeRegisteredAndObjectClass(objectClass))
		 * 			.first()// map to {@link ObjectAssert}
		 * 			.has(typeRegisteredAndObjectClass(objectClass));// used on {@link
		 * 	// ObjectAssert}
		 * }
		 * </pre>
		 *
		 * @param objectClass - the objectClass that would be tested against the
		 *                    ServiceReference
		 * @return the Condition
		 */
		static Condition<ServiceEvent> typeRegisteredAndObjectClass(final Class<?> objectClass) {
			return matches(ServiceEvent.REGISTERED, objectClass);
		}

		/**
		 * Creates a {@link Condition} to be met by an {@link ServiceEvent}. Checking if
		 * type of {@link ServiceEvent} <b>matches</b> <b>ServiceEvent.REGISTERED</b>
		 * and the given Class<?> objectClass matches a objectClass of the
		 * ServiceReference and the given dictionary contained in the serviceProperties.
		 *
		 * <pre>
		 * List<ServiceEvent> serviceEvents = null;
		 *
		 * static void example_typeRegisteredWith(Class<?> objectClass, Dictionary<String, Object> dictionary) {
		 *
		 * 	assertThat(serviceEvents)// created an {@link ListAssert}
		 * 			.have(typeRegisteredWith(objectClass, dictionary))
		 * 			.filteredOn(typeRegisteredWith(objectClass, dictionary)).first()// map to {@link ObjectAssert}
		 * 			.has(typeRegisteredWith(objectClass, dictionary));// used on {@link
		 * 	// ObjectAssert}
		 * }
		 * </pre>
		 *
		 * @param dictionary  - dictionary must contain in the serviceProperties
		 * @param objectClass - the objectClass that would be tested against the
		 *                    ServiceReference
		 * @return the Condition
		 */
		static Condition<ServiceEvent> typeRegisteredWith(final Class<?> objectClass,
				Dictionary<String, Object> dictionary) {
			return matches(ServiceEvent.REGISTERED, objectClass, dictionary);
		}

		/**
		 * Creates a {@link Condition} to be met by an {@link ServiceEvent}. Checking if
		 * type of {@link ServiceEvent} <b>matches</b> <b>ServiceEvent.REGISTERED</b>
		 * and the given Class<?> objectClass matches a objectClass of the
		 * ServiceReference and the given dictionary contained in the serviceProperties.
		 *
		 * <pre>
		 * List<ServiceEvent> serviceEvents = null;
		 *
		 * static void example_typeRegisteredWith(Class<?> objectClass, Map<String, Object> map) {
		 *
		 * 	assertThat(serviceEvents)// created an {@link ListAssert}
		 * 			.have(typeRegisteredWith(objectClass, map)).filteredOn(typeRegisteredWith(objectClass, map)).first()// map
		 * 																												// to
		 * 																												// {@link
		 * 																												// ObjectAssert}
		 * 			.has(typeRegisteredWith(objectClass, map));// used on {@link
		 * 	// ObjectAssert}
		 * }
		 * </pre>
		 *
		 * @param map         - dictionary must contain in the serviceProperties
		 * @param objectClass - the objectClass that would be tested against the
		 *                    ServiceReference
		 * @return the Condition
		 */
		static Condition<ServiceEvent> typeRegisteredWith(final Class<?> objectClass, Map<String, Object> map) {
			return matches(ServiceEvent.REGISTERED, objectClass, map);
		}

		/**
		 * Creates a {@link Condition} to be met by an {@link ServiceEvent}. Checking if
		 * type of {@link ServiceEvent} <b>matches</b>
		 * <b>ServiceEvent.UNREGISTERING</b>.
		 *
		 * <pre>
		 * List<ServiceEvent> serviceEvents = null;
		 *
		 * static void example_typeUnregistering() {
		 *
		 * 	assertThat(serviceEvents)// created an {@link ListAssert}
		 * 			.have(typeUnregistering()).filteredOn(typeUnregistering()).first()// map to {@link ObjectAssert}
		 * 			.has(typeUnregistering());// used on {@link
		 * 	// ObjectAssert}
		 * }
		 * </pre>
		 *
		 * @return the Condition
		 */
		static Condition<ServiceEvent> typeUnregistering() {
			return type(ServiceEvent.UNREGISTERING);
		}

		/**
		 * Creates a {@link Condition} to be met by an {@link ServiceEvent}. Checking if
		 * type of {@link ServiceEvent} <b>matches</b> <b>ServiceEvent.UNREGISTERING</b>
		 * and the given Class<?> objectClass matches a objectClass of the
		 * ServiceReference.
		 *
		 * <pre>
		 * List<ServiceEvent> serviceEvents = null;
		 *
		 * static void example_typeUnregisteringAndObjectClass(Class<?> objectClass) {
		 *
		 * 	assertThat(serviceEvents)// created an {@link ListAssert}
		 * 			.have(typeUnregisteringAndObjectClass(objectClass))
		 * 			.filteredOn(typeUnregisteringAndObjectClass(objectClass)).first()// map to {@link ObjectAssert}
		 * 			.has(typeUnregisteringAndObjectClass(objectClass));// used on {@link
		 * 	// ObjectAssert}
		 * }
		 * </pre>
		 *
		 * @param objectClass - the objectClass that would be tested against the
		 *                    ServiceReference
		 * @return the Condition
		 */
		static Condition<ServiceEvent> typeUnregisteringAndObjectClass(final Class<?> objectClass) {
			return matches(ServiceEvent.UNREGISTERING, objectClass);
		}
	}

	/**
	 * A Utility-Class thats Provides static methods to create
	 * {@link ServiceReferenceConditions} for an {@link ServiceReference}
	 */
	interface ServiceReferenceConditions {

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
		 * 			.have(sameAs(serviceReference)).filteredOn(sameAs(serviceReference)).first()// map to {@link
		 * 																						// ObjectAssert}
		 * 			.is(sameAs(serviceReference));// used on {@link ObjectAssert}
		 * }
		 * </pre>
		 *
		 * @param serviceReferences - the expected serviceReferences that would be
		 *                          checked against other {@link ServiceReference}s
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
		 * 			.have(objectClass(objectClass)).filteredOn(objectClass(objectClass)).first()// map to {@link
		 * 																						// ObjectAssert}
		 * 			.has(objectClass(objectClass));// used on {@link
		 * 	// ObjectAssert}
		 * }
		 * </pre>
		 *
		 * @param objectClass - the objectClass that would be tested against the
		 *                    ServiceReference
		 * @return the Condition
		 */

		static Condition<ServiceReference<?>> objectClass(final Class<?> objectClass) {
			return new Condition<ServiceReference<?>>(sr -> {
				Object classes = sr.getProperty(Constants.OBJECTCLASS);
				if (classes != null && classes instanceof String[]) {
					return Stream.of((String[]) classes).filter(Objects::nonNull)
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
		 * Checking if the serviceProperties of the {@link ServiceReference} <b>is not
		 * null</b>.
		 *
		 * <pre>
		 * List<ServiceReference> serviceReference = null;
		 *
		 * static void example_servicePropertiesIsNotNull() {
		 *
		 * 	assertThat(serviceReference)// created an {@link ListAssert}
		 * 			.have(servicePropertiesIsNotNull()).filteredOn(servicePropertiesIsNotNull()).first()// map to {@link
		 * 																								// ObjectAssert}
		 * 			.has(servicePropertiesIsNotNull());// used on {@link ObjectAssert}
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
		 * 			.have(servicePropertiesIsNull()).filteredOn(servicePropertiesIsNull()).first()// map to {@link
		 * 																							// ObjectAssert}
		 * 			.has(servicePropertiesIsNull());// used on {@link ObjectAssert}
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
}
