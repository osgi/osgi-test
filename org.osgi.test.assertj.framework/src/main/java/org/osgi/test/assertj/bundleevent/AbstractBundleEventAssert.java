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

import static org.osgi.test.assertj.bundle.BundleAssert.BUNDLE;

import java.util.Objects;
import java.util.function.ToIntFunction;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;
import org.osgi.test.assertj.bundle.AbstractBundleAssert;
import org.osgi.test.assertj.event.AbstractBitmappedTypeEventAssert;
import org.osgi.test.common.bitmaps.BundleEventType;

public abstract class AbstractBundleEventAssert<SELF extends AbstractBundleEventAssert<SELF, ACTUAL>, ACTUAL extends BundleEvent>
	extends AbstractBitmappedTypeEventAssert<SELF, ACTUAL> {

	protected AbstractBundleEventAssert(ACTUAL actual, Class<SELF> selfType, ToIntFunction<ACTUAL> getType) {
		super(actual, selfType, getType, BundleEventType.BITMAP);
	}

	public SELF hasBundle(Bundle expected) {
		isNotNull();
		if (!Objects.equals(actual.getBundle(), expected)) {
			throw this.failureWithActualExpected(actual.getBundle(), expected,
				"%nExpecting%n <%s>%nto have bundle source:%n <%s>%n but was:%n<%s>", actual, expected,
				actual.getBundle());
		}
		return myself;
	}

	public AbstractBundleAssert<?, ? extends Bundle> hasBundleThat() {
		return isNotNull().extracting(BundleEvent::getBundle, BUNDLE)
			.as(actual + ".bundle");
	}

	public SELF hasOrigin(Bundle expected) {
		isNotNull();
		if (!Objects.equals(actual.getOrigin(), expected)) {
			throw failureWithActualExpected(actual.getOrigin(), expected,
				"%nExpecting%n <%s>%nto have originating bundle:%n <%s>%n but was:%n<%s>", actual, expected,
				actual.getOrigin());
		}
		return myself;
	}

	public AbstractBundleAssert<?, ? extends Bundle> hasOriginThat() {
		return isNotNull().extracting(BundleEvent::getOrigin, BUNDLE)
			.as(actual + ".origin");
	}
}
