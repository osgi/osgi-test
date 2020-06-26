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

package org.osgi.test.assertj.serviceevent;

import java.util.Objects;
import java.util.function.ToIntFunction;

import org.assertj.core.error.ErrorMessageFactory;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceReference;
import org.osgi.test.assertj.event.AbstractBitmappedTypeEventAssert;
import org.osgi.test.common.bitmaps.ServiceEventType;

public abstract class AbstractServiceEventAssert<SELF extends AbstractServiceEventAssert<SELF, ACTUAL>, ACTUAL extends ServiceEvent>
	extends AbstractBitmappedTypeEventAssert<SELF, ACTUAL> {
	static {
		// SoftAssertions will require this at runtime; this
		// forces bnd to generate an import for it.
		Class<?> importError = ErrorMessageFactory.class;
	}

	protected AbstractServiceEventAssert(ACTUAL actual, Class<SELF> selfType, ToIntFunction<ACTUAL> getType) {
		super(actual, selfType, getType, ServiceEventType.BITMAP);
	}

	public SELF hasServiceReference(ServiceReference<?> expected) {
		isNotNull();
		if (!Objects.equals(actual.getServiceReference(), expected)) {
			failWithActualExpectedAndMessage(actual.getServiceReference(),
				expected,
				"%nExpecting%n <%s>%nto have service reference:%n <%s>%n but was:%n<%s>", actual, expected,
				actual.getServiceReference());
		}
		return myself;
	}
}
