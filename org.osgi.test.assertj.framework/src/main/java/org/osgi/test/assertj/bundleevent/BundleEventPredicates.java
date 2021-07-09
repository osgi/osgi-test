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

import java.util.function.Predicate;

import org.osgi.framework.BundleEvent;

/**
 * The Interface BundleEventPredicates.
 */
public class BundleEventPredicates {

	private BundleEventPredicates() {}

	// BundleEvents

	/**
	 * Returns a predicate that tests if the object Bundle event.
	 *
	 * @return the predicate
	 */
	public static Predicate<Object> bundleEvent() {
		return e -> e instanceof BundleEvent;
	}

	/**
	 * Returns a predicate that tests if the object Bundle event and matches the
	 * given eventTypeMask.
	 *
	 * @param predicate the predicate
	 * @return the predicate
	 */
	public static Predicate<Object> bundleEventAnd(Predicate<BundleEvent> predicate) {
		return e -> bundleEvent().test(e) && predicate.test((BundleEvent) e);
	}

	/**
	 * Returns a predicate that tests if the event-type matches the given
	 * eventTypeMask.
	 *
	 * @param eventTypeMask the event type mask
	 * @return the predicate
	 */
	public static Predicate<BundleEvent> type(final int eventTypeMask) {
		return e -> (e.getType() & eventTypeMask) != 0;
	}

	/**
	 * Returns a predicate that tests if the event-type is installed.
	 *
	 * @return the predicate
	 */
	// BundleEvents - by type
	public static Predicate<BundleEvent> typeInstalled() {
		return e -> type(BundleEvent.INSTALLED).test(e);
	}

	/**
	 * Returns a predicate that tests if the event-type is lazy activation.
	 *
	 * @return the predicate
	 */
	public static Predicate<BundleEvent> typeLazyActivation() {
		return e -> type(BundleEvent.LAZY_ACTIVATION).test(e);
	}

	/**
	 * Returns a predicate that tests if the event-type is resolved.
	 *
	 * @return the predicate
	 */
	public static Predicate<BundleEvent> typeResolved() {
		return e -> type(BundleEvent.RESOLVED).test(e);
	}

	/**
	 * Returns a predicate that tests if the event-type is started.
	 *
	 * @return the predicate
	 */
	public static Predicate<BundleEvent> typeStarted() {
		return e -> type(BundleEvent.STARTED).test(e);
	}

	/**
	 * Returns a predicate that tests if the event-type is starting.
	 *
	 * @return the predicate
	 */
	public static Predicate<BundleEvent> typeStarting() {
		return e -> type(BundleEvent.STARTING).test(e);
	}

	/**
	 * Returns a predicate that tests if the event-type is stopped.
	 *
	 * @return the predicate
	 */
	public static Predicate<BundleEvent> typeStopped() {
		return e -> type(BundleEvent.STOPPED).test(e);
	}

	/**
	 * Returns a predicate that tests if the event-type is stopping.
	 *
	 * @return the predicate
	 */
	public static Predicate<BundleEvent> typeStopping() {
		return e -> type(BundleEvent.STOPPING).test(e);
	}

	/**
	 * Returns a predicate that tests if the event-type is uninstalled.
	 *
	 * @return the predicate
	 */
	public static Predicate<BundleEvent> typeUninstalled() {
		return e -> type(BundleEvent.UNINSTALLED).test(e);
	}

	/**
	 * Returns a predicate that tests if the event-type is unresolved.
	 *
	 * @return the predicate
	 */
	public static Predicate<BundleEvent> typeUnresolved() {
		return e -> type(BundleEvent.UNRESOLVED).test(e);
	}

	/**
	 * Returns a predicate that tests if the event-type is updated.
	 *
	 * @return the predicate
	 */
	public static Predicate<BundleEvent> typeUpdated() {
		return e -> type(BundleEvent.UPDATED).test(e);
	}
}
