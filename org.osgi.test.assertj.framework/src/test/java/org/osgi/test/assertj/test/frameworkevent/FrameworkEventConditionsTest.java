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

package org.osgi.test.assertj.test.frameworkevent;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkEvent;
import org.osgi.test.assertj.frameworkevent.FrameworkEventConditions;
import org.osgi.test.assertj.test.testutil.ConditionAssert;
import org.osgi.test.common.bitmaps.FrameworkEventType;

class FrameworkEventConditionsTest implements ConditionAssert {

	Bundle			bundle;
	FrameworkEvent	frameworkEvent;
	Bundle			otherbundle;
	Throwable		throwable;

	@BeforeEach
	private void beforEach() {
		frameworkEvent = mock(FrameworkEvent.class, "theFrameworkEvent");
		bundle = mock(Bundle.class, "theBundle");
		otherbundle = mock(Bundle.class, "otherbundle");
		throwable = new NullPointerException("NullPointerException");
	}

	@Test
	void bundleIsNotNull() throws Exception {

		failingHas(FrameworkEventConditions.bundleIsNotNull(), frameworkEvent, "bundle is <null>");

		when(frameworkEvent.getBundle()).thenReturn(bundle);
		passingHas(FrameworkEventConditions.bundleIsNotNull(), frameworkEvent);
	}

	@Test
	void throwableIsNotNull() throws Exception {

		failingHas(FrameworkEventConditions.throwableIsNotNull(), frameworkEvent, "throwable is <null>");

		when(frameworkEvent.getThrowable()).thenReturn(throwable);
		passingHas(FrameworkEventConditions.throwableIsNotNull(), frameworkEvent);

	}

	@Test
	void throwableIsNull() throws Exception {

		passingHas(FrameworkEventConditions.throwableIsNull(), frameworkEvent);

		when(frameworkEvent.getThrowable()).thenReturn(throwable);
		failingHas(FrameworkEventConditions.throwableIsNull(), frameworkEvent, "throwable is <null>");
	}

	@Test
	void throwableOfClass() throws Exception {

		when(frameworkEvent.getThrowable()).thenReturn(throwable);

		passingHas(FrameworkEventConditions.throwableOfClass(NullPointerException.class), frameworkEvent);

		failingHas(FrameworkEventConditions.throwableOfClass(IllegalArgumentException.class), frameworkEvent,
			"throwable of Class");
	}

	@Test
	void matches() throws Exception {

		when(frameworkEvent.getType()).thenReturn(FrameworkEvent.ERROR);
		when(frameworkEvent.getThrowable()).thenReturn(throwable);
		when(frameworkEvent.getBundle()).thenReturn(bundle);

		passingHas(FrameworkEventConditions.matches(FrameworkEvent.ERROR, NullPointerException.class), frameworkEvent);

		failingHas(FrameworkEventConditions.matches(FrameworkEvent.ERROR, IllegalArgumentException.class),
			frameworkEvent);

		failingHas(FrameworkEventConditions.matches(FrameworkEvent.INFO, NullPointerException.class), frameworkEvent);

		passingHas(FrameworkEventConditions.matches(FrameworkEvent.ERROR, bundle, NullPointerException.class),
			frameworkEvent);

		failingHas(FrameworkEventConditions.matches(FrameworkEvent.ERROR, bundle, IllegalArgumentException.class),
			frameworkEvent);

		failingHas(FrameworkEventConditions.matches(FrameworkEvent.INFO, bundle, NullPointerException.class),
			frameworkEvent);

		failingHas(FrameworkEventConditions.matches(FrameworkEvent.ERROR, otherbundle, NullPointerException.class),
			frameworkEvent);

	}

	@Test
	void typeAnd() throws Exception {
		when(frameworkEvent.getType()).thenReturn(FrameworkEvent.ERROR);
		when(frameworkEvent.getThrowable()).thenReturn(throwable);
		when(frameworkEvent.getBundle()).thenReturn(bundle);
		passingHas(FrameworkEventConditions.typeAndBundle(FrameworkEvent.ERROR, bundle), frameworkEvent);

		failingHas(FrameworkEventConditions.typeAndBundle(FrameworkEvent.INFO, bundle), frameworkEvent);
		failingHas(FrameworkEventConditions.typeAndBundle(FrameworkEvent.ERROR, otherbundle), frameworkEvent);

	}

	@Test
	void bundleEquals() throws Exception {

		when(frameworkEvent.getBundle()).thenReturn(bundle);

		passingHas(FrameworkEventConditions.bundleEquals(bundle), frameworkEvent);

		failingHas(FrameworkEventConditions.bundleEquals(otherbundle), frameworkEvent);
	}

	@Test
	void type() throws Exception {

		when(frameworkEvent.getType()).thenReturn(FrameworkEvent.ERROR);
		failingHas(FrameworkEventConditions.type(FrameworkEvent.INFO), frameworkEvent, "type matches mask <%s>",
			FrameworkEventType.BITMAP.maskToString(FrameworkEvent.INFO));
	}

}
