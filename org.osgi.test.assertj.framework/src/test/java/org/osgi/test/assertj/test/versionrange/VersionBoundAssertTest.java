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

package org.osgi.test.assertj.test.versionrange;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.osgi.test.assertj.test.version.AbstractVersionAssertTest;
import org.osgi.test.assertj.versionrange.VersionBoundAssert;

abstract public class VersionBoundAssertTest extends AbstractVersionAssertTest<VersionBoundAssert> {

	boolean isOpen;

	protected VersionBoundAssertTest(boolean open) {
		super(actual -> new VersionBoundAssert(actual, open));
	}

	@DisplayName("VersionBoundAssert for a closed bound")
	public static class ClosedVersionBoundAssertTest extends VersionBoundAssertTest {
		public ClosedVersionBoundAssertTest() {
			super(false);
		}

		@Test
		public void opennessTests() {
			assertPassing("isClosed", x -> aut.isClosed(), null);
			assertFailing("isOpen", x -> aut.isOpen(), null)
				.hasMessageMatching("(?si).*to be open.*but it was closed.*");
		}
	}

	@DisplayName("VersionBoundAssert for an open bound")
	public static class OpenVersionBoundAssertTest extends VersionBoundAssertTest {
		public OpenVersionBoundAssertTest() {
			super(true);
		}

		@Test
		public void opennessTests() {
			assertPassing("isOpen", x -> aut.isOpen(), null);
			assertFailing("isClosed", x -> aut.isClosed(), null)
				.hasMessageMatching("(?si).*to be closed.*but it was open.*");
		}
	}
}
