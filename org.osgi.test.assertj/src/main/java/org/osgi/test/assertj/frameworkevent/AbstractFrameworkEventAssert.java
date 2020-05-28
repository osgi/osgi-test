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

package org.osgi.test.assertj.frameworkevent;

import java.util.Objects;
import java.util.function.ToIntFunction;

import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkEvent;
import org.osgi.test.assertj.event.AbstractBitmappedTypeEventAssert;
import org.osgi.test.common.bitmaps.Bitmaps;

public abstract class AbstractFrameworkEventAssert<SELF extends AbstractFrameworkEventAssert<SELF, ACTUAL>, ACTUAL extends FrameworkEvent>
	extends AbstractBitmappedTypeEventAssert<SELF, ACTUAL> {

	protected AbstractFrameworkEventAssert(ACTUAL actual, Class<SELF> selfType, ToIntFunction<ACTUAL> getType) {
		super(actual, selfType, getType, Bitmaps.FRAMEWORKEVENT_TYPE);
	}

	public SELF hasBundle(Bundle expected) {
		isNotNull();
		if (!Objects.equals(actual.getBundle(), expected)) {
			failWithActualExpectedAndMessage(actual
				.getBundle(),
				expected,
				"%nExpecting%n <%s>%nto have bundle source:%n <%s>%n but was:%n<%s>", actual, expected,
				actual.getBundle());
		}
		return myself;
	}

	public SELF hasThrowable(Throwable expected) {
		isNotNull();
		if (!Objects.equals(actual.getThrowable(), expected)) {
			failWithActualExpectedAndMessage(actual.getThrowable(), expected,
				"%nExpecting%n <%s>%nto have throwable:%n <%s>%n but was:%n<%s>", actual, expected,
				actual.getThrowable());
		}
		return myself;
	}
}
