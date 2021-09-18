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

package org.osgi.test.assertj.test.bundleevent;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;
import org.osgi.test.assertj.bundleevent.BundleEventConditions;
import org.osgi.test.assertj.test.testutil.ConditionAssert;
import org.osgi.test.common.bitmaps.BundleEventType;

class BundleEventConditionsTests implements ConditionAssert {

	Bundle		bundle;
	BundleEvent	bundleEvent;
	Bundle		otherbundle;

	@BeforeEach
	private void beforEach() {
		bundleEvent = mock(BundleEvent.class, "theBundleEvent");
		bundle = mock(Bundle.class, "theBundle");
		otherbundle = mock(Bundle.class, "otherbundle");
	}

	@Test
	void bundleEquals() throws Exception {

		when(bundleEvent.getBundle()).thenReturn(bundle);
		passingHas(BundleEventConditions.bundleEquals(bundle), bundleEvent);

		failingHas(BundleEventConditions.bundleEquals(otherbundle), bundleEvent, "bundle equals", otherbundle);
	}

	@Test
	void originEquals() throws Exception {

		when(bundleEvent.getOrigin()).thenReturn(bundle);
		passingHas(BundleEventConditions.originEquals(bundle), bundleEvent);

		failingHas(BundleEventConditions.originEquals(otherbundle), bundleEvent, "::getOrigin.*bundle equals",
			otherbundle);
	}

	@Test
	void matches() throws Exception {

		when(bundleEvent.getOrigin()).thenReturn(bundle);
		when(bundleEvent.getBundle()).thenReturn(bundle);
		when(bundleEvent.getType()).thenReturn(BundleEvent.INSTALLED);

		passingHas(BundleEventConditions.matches(BundleEvent.INSTALLED, bundle, bundle), bundleEvent);

		failingHas(BundleEventConditions.matches(BundleEvent.STOPPED, bundle, bundle), bundleEvent);

		failingHas(BundleEventConditions.matches(BundleEvent.INSTALLED, otherbundle, bundle), bundleEvent);

		failingHas(BundleEventConditions.matches(BundleEvent.INSTALLED, bundle, otherbundle), bundleEvent);
	}

	@Test
	void bundleIsNotNull() throws Exception {

		failingHas(BundleEventConditions.bundleIsNotNull(), bundleEvent, "bundle is not <null>");

		when(bundleEvent.getBundle()).thenReturn(bundle);
		passingHas(BundleEventConditions.bundleIsNotNull(), bundleEvent);
	}

	@Test
	void originIsNull() throws Exception {

		passingHas(BundleEventConditions.originIsNull(), bundleEvent);

		when(bundleEvent.getOrigin()).thenReturn(bundle);
		failingHas(BundleEventConditions.originIsNull(), bundleEvent, "origin is <null>");
	}

	@Test
	void originIsNotNull() throws Exception {

		failingHas(BundleEventConditions.originIsNotNull(), bundleEvent, "origin is not <null>");

		when(bundleEvent.getOrigin()).thenReturn(bundle);
		passingHas(BundleEventConditions.originIsNotNull(), bundleEvent);
	}

	@Test
	void bundleIsNull() throws Exception {

		passingHas(BundleEventConditions.bundleIsNull(), bundleEvent);

		when(bundleEvent.getBundle()).thenReturn(bundle);
		failingHas(BundleEventConditions.bundleIsNull(), bundleEvent, "bundle is <null>");
	}

	@Test
	void type() throws Exception {

		when(bundleEvent.getBundle()).thenReturn(bundle);

		when(bundleEvent.getType()).thenReturn(BundleEvent.INSTALLED);
		passingHas(BundleEventConditions.type(BundleEvent.INSTALLED), bundleEvent);

		when(bundleEvent.getType()).thenReturn(BundleEvent.UPDATED);
		failingHas(BundleEventConditions.type(BundleEvent.INSTALLED), bundleEvent, "type matches mask <%s>",
			BundleEventType.BITMAP.maskToString(BundleEvent.INSTALLED));

		when(bundleEvent.getType()).thenReturn(BundleEvent.INSTALLED);
		passingHas(BundleEventConditions.typeAndBundle(BundleEvent.INSTALLED, bundle), bundleEvent);

	}
}
