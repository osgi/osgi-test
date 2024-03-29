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

package org.osgi.test.junit5.test.bundle;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.osgi.framework.Bundle;
import org.osgi.test.assertj.bundle.BundleAssert;
import org.osgi.test.assertj.dictionary.DictionaryAssert;
import org.osgi.test.common.annotation.InjectBundleInstaller;
import org.osgi.test.common.install.BundleInstaller;
import org.osgi.test.junit5.bundle.BundleSource;

public class BundleArgumentsProviderTest {

	static Bundle bundleInstalled;

	@BeforeAll
	static void beforeAll(@InjectBundleInstaller BundleInstaller bundleInstaller) {
		bundleInstalled = bundleInstaller.installBundle("tb1.jar", false);
	}

	@ParameterizedTest
	@BundleSource(stateMask = Bundle.INSTALLED)
	public void testParamInstalled(Bundle bundle) throws Exception {
		BundleAssert.assertThat(bundle)
			.isInState(Bundle.INSTALLED)
			.hasBundleId(bundleInstalled.getBundleId());
	}

	@ParameterizedTest
	@BundleSource(stateMask = Bundle.ACTIVE)
	public void testParamActive(Bundle bundle) throws Exception {
		BundleAssert.assertThat(bundle)
			.isInState(Bundle.ACTIVE);
	}

	@ParameterizedTest
	@BundleSource(headerFilter = "(Test-Header=tb1)")
	public void testHeader(Bundle bundle) throws Exception {
		DictionaryAssert.assertThat(bundle.getHeaders())
			.containsEntry("Test-Header", "tb1");
		BundleAssert.assertThat(bundle)
			.hasBundleId(bundleInstalled.getBundleId());
	}

	static final String pattern = ".*\\.junit5(\\..*)?";

	@ParameterizedTest
	@BundleSource(symbolicNamePattern = pattern)
	public void testSymbolicNamePattern(Bundle bundle) throws Exception {
		BundleAssert.assertThat(bundle)
			.hasSymbolicNameThat()
			.matches(pattern);
	}

}
