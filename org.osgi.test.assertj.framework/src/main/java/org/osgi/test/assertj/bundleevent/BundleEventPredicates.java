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
 *
 * @since 1.1
 */
public final class BundleEventPredicates {

	private BundleEventPredicates() {}

	// BundleEvents

	/**
	 * Returns a predicate that tests if the object Bundle event.
	 *
	 * @return the predicate
	 */
	public static <T> Predicate<T> bundleEvent() {
		return e -> e instanceof BundleEvent;
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
}
