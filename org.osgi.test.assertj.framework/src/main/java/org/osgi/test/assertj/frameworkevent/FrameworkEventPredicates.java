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

import java.util.function.Predicate;

import org.osgi.framework.FrameworkEvent;

/**
 * The Class FrameworkEventPredicates.
 */
public class FrameworkEventPredicates {

	private FrameworkEventPredicates() {}
	/**
	 * Returns a predicate that tests if the object is a framework event.
	 *
	 * @return the predicate
	 */
	public static Predicate<Object> frameworkEvent() {
		return e -> e instanceof FrameworkEvent;
	}

	/**
	 * Returns a predicate that tests if the object is a framework event and the
	 * given predicate also matches.
	 *
	 * @param predicate the predicate
	 * @return the predicate
	 */
	public static Predicate<Object> frameworkEventAnd(Predicate<FrameworkEvent> predicate) {
		return e -> frameworkEvent().test(e) && predicate.test((FrameworkEvent) e);
	}

	/**
	 * Returns a predicate that tests if the event-type matches the
	 * eventTypeMask.
	 *
	 * @param eventTypeMask the event type mask
	 * @return the predicate
	 */
	public static Predicate<FrameworkEvent> type(final int eventTypeMask) {
		return e -> (e.getType() & eventTypeMask) != 0;
	}

	/**
	 * Returns a predicate that tests if the event-type is error.
	 *
	 * @return the predicate
	 */
	public static Predicate<FrameworkEvent> typeError() {
		return e -> type(FrameworkEvent.ERROR).test(e);
	}

	/**
	 * Returns a predicate that tests if the event-type is info.
	 *
	 * @return the predicate
	 */
	public static Predicate<FrameworkEvent> typeInfo() {
		return e -> type(FrameworkEvent.INFO).test(e);
	}

	/**
	 * Returns a predicate that tests if the event-type is packages refreshed.
	 *
	 * @return the predicate
	 */
	public static Predicate<FrameworkEvent> typePackagesRefreshed() {
		return e -> type(FrameworkEvent.PACKAGES_REFRESHED).test(e);
	}

	/**
	 * Returns a predicate that tests if the event-type is started.
	 *
	 * @return the predicate
	 */
	public static Predicate<FrameworkEvent> typeStarted() {
		return e -> type(FrameworkEvent.STARTED).test(e);
	}

	/**
	 * Returns a predicate that tests if the event-type is startlevel changed.
	 *
	 * @return the predicate
	 */
	public static Predicate<FrameworkEvent> typeStartlevelChanged() {
		return e -> type(FrameworkEvent.STARTLEVEL_CHANGED).test(e);
	}

	/**
	 * Returns a predicate that tests if the event-type is stopped.
	 *
	 * @return the predicate
	 */
	public static Predicate<FrameworkEvent> typeStopped() {
		return e -> type(FrameworkEvent.STOPPED).test(e);
	}

	/**
	 * Returns a predicate that tests if the event-type is stopped update.
	 *
	 * @return the predicate
	 */
	public static Predicate<FrameworkEvent> typeStoppedUpdate() {
		return e -> type(FrameworkEvent.STOPPED_UPDATE).test(e);
	}

	/**
	 * Returns a predicate that tests if the event-type is wait-timeout.
	 *
	 * @return the predicate
	 */
	public static Predicate<FrameworkEvent> typeWaitTimeout() {
		return e -> type(FrameworkEvent.WAIT_TIMEDOUT).test(e);
	}

	/**
	 * Returns a predicate that tests if the event-type is warning.
	 *
	 * @return the predicate
	 */
	public static Predicate<FrameworkEvent> typeWarning() {
		return e -> type(FrameworkEvent.WARNING).test(e);
	}
}
