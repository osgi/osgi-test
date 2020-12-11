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

import org.osgi.framework.FrameworkEvent;

public class FrameworkEvents extends Events {

	private FrameworkEvents() {
		// TODO Auto-generated constructor stub
	}

	// FramworkEvents

	public static Predicate<Object> isFrameworkEvent() {
		return e -> e instanceof FrameworkEvent;
	}

	public static Predicate<Object> isFrameworkEventAnd(Predicate<FrameworkEvent> predicate) {
		return e -> isFrameworkEvent().test(e) && predicate.test((FrameworkEvent) e);
	}

	static Predicate<FrameworkEvent> isType(final int eventTypeMask) {
		return e -> (e.getType() & eventTypeMask) != 0;
	}

	static Predicate<FrameworkEvent> isTypeError() {
		return e -> isType(FrameworkEvent.ERROR).test(e);
	}

	static Predicate<FrameworkEvent> isTypeInfo() {
		return e -> isType(FrameworkEvent.INFO).test(e);
	}

	static Predicate<FrameworkEvent> isTypePackagesRefreshed() {
		return e -> isType(FrameworkEvent.PACKAGES_REFRESHED).test(e);
	}

	static Predicate<FrameworkEvent> isTypeStarted() {
		return e -> isType(FrameworkEvent.STARTED).test(e);
	}

	static Predicate<FrameworkEvent> isTypeStartlevelChanged() {
		return e -> isType(FrameworkEvent.STARTLEVEL_CHANGED).test(e);
	}

	static Predicate<FrameworkEvent> isTypeStopped() {
		return e -> isType(FrameworkEvent.STOPPED).test(e);
	}

	static Predicate<FrameworkEvent> isTypeStoppedBootclasspathModified() {
		return e -> isType(FrameworkEvent.STOPPED_BOOTCLASSPATH_MODIFIED).test(e);
	}

	static Predicate<FrameworkEvent> isTypeStoppedUpdate() {
		return e -> isType(FrameworkEvent.STOPPED_UPDATE).test(e);
	}

	static Predicate<FrameworkEvent> isTypeWaitTimeout() {
		return e -> isType(FrameworkEvent.WAIT_TIMEDOUT).test(e);
	}

	static Predicate<FrameworkEvent> isTypeWarning() {
		return e -> isType(FrameworkEvent.WARNING).test(e);
	}
}
