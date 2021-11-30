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

package org.osgi.test.assertj.test.version;

import org.junit.jupiter.api.Test;
import org.osgi.framework.Version;
import org.osgi.test.assertj.version.VersionAssert;
import org.osgi.test.assertj.version.VersionSoftAssertionsProvider;

public class VersionAssertTest extends AbstractVersionAssertTest<VersionAssert> {
	public VersionAssertTest() {
		super(VersionAssert::assertThat);
	}

	@Test
	void softAssertionsProvider() throws Exception {
		setActual(new Version("1.0.0"));
		softAssertionsProvider(VersionSoftAssertionsProvider.class);
	}
}
