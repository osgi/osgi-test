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

package org.osgi.test.junit5.bundle;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.osgi.framework.Bundle;
import org.osgi.test.assertj.bundle.BundleAssert;
import org.osgi.test.assertj.dictionary.DictionaryAssert;
import org.osgi.test.common.annotation.InjectInstallBundle;
import org.osgi.test.common.install.InstallBundle;
import org.osgi.test.junit5.context.BundleContextExtension;

@ExtendWith(BundleContextExtension.class)
public class BundleArgumentsProviderTest {

	static Bundle bundleInstalled;

	@BeforeAll
	static void beforeAll(@InjectInstallBundle InstallBundle installBundle) {
		bundleInstalled = installBundle.installBundle("tb1.jar", false);
	}

	@ParameterizedTest
	@BundleSource(stateMask = Bundle.INSTALLED)
	public void testParamInstalled(Bundle bundle) throws Exception {

		BundleAssert.assertThat(bundle)
			.isInState(Bundle.INSTALLED)
			.isSameAs(bundleInstalled);

	}

	@ParameterizedTest
	@BundleSource(stateMask = Bundle.ACTIVE)
	public void testParamActive(Bundle bundle) throws Exception {
		BundleAssert.assertThat(bundle)
			.isInState(Bundle.ACTIVE);
	}

	@ParameterizedTest
	@BundleSource(stateMask = Bundle.RESOLVED)
	public void testParamResolved(Bundle bundle) throws Exception {
		BundleAssert.assertThat(bundle)
			.isInState(Bundle.RESOLVED);
	}

	@ParameterizedTest
	@BundleSource(headerFilter = "(Bundle-Vendor=OSGi Alliance)")
	public void testHeader(Bundle bundle) throws Exception {
		DictionaryAssert.assertThat(bundle.getHeaders())
			.containsEntry("Bundle-Vendor", "OSGi Alliance");
	}

	static final String pattern = ".*\\.junit5";

	@ParameterizedTest
	@BundleSource(symbolicNamePattern = pattern)
	public void testSymbolicNamePattern(Bundle bundle) throws Exception {
		assertThat(bundle.getSymbolicName()).matches(pattern);
	}

}
