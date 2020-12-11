/*
 * Copyright (c) OSGi Alliance (2020). All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.osgi.test.common.event;

import java.util.function.Predicate;

import org.osgi.framework.BundleEvent;

public class BundleEvents extends Events {

	private BundleEvents() {}

	// BundleEvents

	public static Predicate<Object> isBundleEvent() {
		return e -> e instanceof BundleEvent;
	}

	public static Predicate<Object> isBundleEventAnd(Predicate<BundleEvent> predicate) {
		return e -> isBundleEvent().test(e) && predicate.test((BundleEvent) e);
	}

	static Predicate<BundleEvent> isType(final int eventTypeMask) {
		return e -> (e.getType() & eventTypeMask) != 0;
	}

	// BundleEvents - by type
	static Predicate<BundleEvent> isTypeInstalled() {
		return e -> isType(BundleEvent.INSTALLED).test(e);
	}

	static Predicate<BundleEvent> isTypeLazyActivation() {
		return e -> isType(BundleEvent.LAZY_ACTIVATION).test(e);
	}

	static Predicate<BundleEvent> isTypeResolved() {
		return e -> isType(BundleEvent.RESOLVED).test(e);
	}

	static Predicate<BundleEvent> isTypeStarted() {
		return e -> isType(BundleEvent.STARTED).test(e);
	}

	static Predicate<BundleEvent> isTypeStarting() {
		return e -> isType(BundleEvent.STARTING).test(e);
	}

	static Predicate<BundleEvent> isTypeStopped() {
		return e -> isType(BundleEvent.STOPPED).test(e);
	}

	static Predicate<BundleEvent> isTypeStopping() {
		return e -> isType(BundleEvent.STOPPING).test(e);
	}

	static Predicate<BundleEvent> isTypeUninstalled() {
		return e -> isType(BundleEvent.UNINSTALLED).test(e);
	}

	static Predicate<BundleEvent> isTypeUnresolved() {
		return e -> isType(BundleEvent.UNRESOLVED).test(e);
	}

	static Predicate<BundleEvent> isTypeUpdated() {
		return e -> isType(BundleEvent.UPDATED).test(e);
	}
}
