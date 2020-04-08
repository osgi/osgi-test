/*
 * Copyright (c) OSGi Alliance (2019-2020). All Rights Reserved.
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
package org.osgi.test.junit5.context;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.framework.BundleContext;
import org.osgi.test.common.install.InstallBundle;

/**
 * This is how a real test class should use {@link BundleContextExtension}.
 */
@ExtendWith(BundleContextExtension.class)
public class BundleContextExtensionExampleTest {

	// BundleContext injection

	@Test
	public void testBundleContext1(@BundleContextParameter BundleContext bundleContext1) {
		assertThat(bundleContext1).isNotNull();
	}

	// OR

	@BundleContextParameter
	BundleContext bundleContext2;

	@Test
	public void testBundleContext2() {
		assertThat(bundleContext2).isNotNull();
	}

	// InstallBundle injection

	@Test
	public void testInstallBundle(@InstallBundleParameter InstallBundle installBundle1) {
		assertThat(installBundle1).isNotNull();
	}

	// OR

	@InstallBundleParameter
	InstallBundle installBundle2;

	@Test
	public void testInstallBundle() {
		assertThat(installBundle2).isNotNull();
	}

}
