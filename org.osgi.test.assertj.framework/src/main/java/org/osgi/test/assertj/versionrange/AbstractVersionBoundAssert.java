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

package org.osgi.test.assertj.versionrange;

import org.osgi.framework.Version;
import org.osgi.test.assertj.version.AbstractVersionAssert;

public class AbstractVersionBoundAssert<SELF extends AbstractVersionBoundAssert<SELF, ACTUAL>, ACTUAL extends Version>
	extends AbstractVersionAssert<SELF, ACTUAL> {

	final boolean isOpen;

	protected AbstractVersionBoundAssert(ACTUAL actual, boolean open, Class<?> selfType) {
		super(actual, selfType);
		isOpen = open;
	}

	public SELF isClosed() {
		isNotNull();
		if (isOpen) {
			failWithMessage("%nExpecting version bound%n <%s>%nto be closed, but it was open", actual);
		}
		return myself;
	}

	public SELF isOpen() {
		isNotNull();
		if (!isOpen) {
			failWithMessage("%nExpecting version bound%n <%s>%nto be open, but it was closed", actual);
		}
		return myself;
	}
}
