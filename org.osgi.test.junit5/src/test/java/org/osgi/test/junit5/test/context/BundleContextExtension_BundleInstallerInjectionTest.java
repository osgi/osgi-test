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

package org.osgi.test.junit5.test.context;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.osgi.framework.BundleContext;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.annotation.InjectBundleInstaller;
import org.osgi.test.common.install.BundleInstaller;
import org.osgi.test.junit5.test.testutils.OSGiSoftAssertions;

public class BundleContextExtension_BundleInstallerInjectionTest {

	@InjectBundleContext
	static BundleContext		staticBC;

	@InjectBundleInstaller
	static BundleInstaller		staticBI;

	@InjectBundleContext
	BundleContext				bundleContext;

	@InjectBundleInstaller
	BundleInstaller				bundleInstaller;

	final BundleInstaller		constructorBI;

	static OSGiSoftAssertions	staticSoftly;

	OSGiSoftAssertions			softly;

	BundleContextExtension_BundleInstallerInjectionTest(@InjectBundleInstaller
	BundleInstaller bi) {
		constructorBI = bi;
		assertThat(constructorBI).isSameAs(staticBI);
	}

	@BeforeAll
	static void beforeAll(@InjectBundleInstaller
	BundleInstaller bi) {
		assertThat(staticBI).isNotNull();
		staticSoftly = new OSGiSoftAssertions();
		staticSoftly.assertThat(staticBI)
			.as("staticBI:beforeAll")
			.isNotNull()
			.isSameAs(bi);
		staticSoftly.assertThat(staticBI.getBundleContext())
			.isSameAs(staticBC);
		staticSoftly.assertAll();
	}

	@BeforeEach
	void beforeEach(@InjectBundleInstaller
	BundleInstaller bi) {
		assertThat(bundleInstaller).isNotNull();
		softly = new OSGiSoftAssertions();
		softly.assertThat(bundleInstaller)
			.as("bundleInstaller:beforeEach")
			.isNotNull()
			.isSameAs(bi)
			.isNotSameAs(constructorBI)
			.extracting(BundleInstaller::getBundleContext)
			.isSameAs(bundleContext);
		softly.assertAll();
	}

	@Test
	void innerTest(@InjectBundleInstaller
	BundleInstaller b) {
		assertThat(bundleInstaller).isNotNull();
		softly = new OSGiSoftAssertions();
		softly.assertThat(bundleInstaller)
			.as("bundleInstaller:innerTest")
			.isNotNull()
			.isSameAs(b)
			.extracting(BundleInstaller::getBundleContext)
			.isSameAs(bundleContext);
		softly.assertAll();
	}

	@ParameterizedTest
	@ValueSource(ints = {
		1, 2, 3
	})
	// This test is meant to check that the extension is doing the
	// right thing before and after parameterized tests, hence
	// the parameter is not actually used.
	void parameterizedTest(int unused, @InjectBundleInstaller
	BundleInstaller bi) {
		assertThat(bundleInstaller).isNotNull();
		softly = new OSGiSoftAssertions();
		softly.assertThat(bundleInstaller)
			.as("bundleInstaller:parameterizedTest")
			.isNotNull()
			.isSameAs(bi)
			.extracting(BundleInstaller::getBundleContext)
			.isSameAs(bundleContext);
		softly.assertAll();
	}

	@Nested
	class NestedTest {

		@InjectBundleContext
		BundleContext	nestedBC;

		@InjectBundleInstaller
		BundleInstaller	nestedIB;

		@BeforeEach
		void beforeEach(@InjectBundleInstaller
		BundleInstaller bi) {
			assertThat(bundleInstaller).isNotNull();
			softly = new OSGiSoftAssertions();
			softly.assertThat(nestedIB)
				.as("bundleInstaller:nested.beforeEach")
				.isNotNull()
				.isSameAs(bi)
				.extracting(BundleInstaller::getBundleContext)
				.isSameAs(nestedBC);
			softly.assertAll();
		}

		@Test
		void test(@InjectBundleInstaller
		BundleInstaller bi) {
			softly = new OSGiSoftAssertions();
			softly.assertThat(nestedIB)
				.as("bundleInstaller:nested.test")
				.isNotNull()
				.isSameAs(bi)
				.extracting(BundleInstaller::getBundleContext)
				.isSameAs(nestedBC);
			softly.assertAll();
		}

		@AfterEach
		void afterEach(@InjectBundleInstaller
		BundleInstaller bi) {
			softly = new OSGiSoftAssertions();
			softly.assertThat(nestedIB)
				.as("bundleInstaller:nested.afterEach")
				.isNotNull()
				.isSameAs(bi)
				.extracting(BundleInstaller::getBundleContext)
				.isSameAs(nestedBC);
			softly.assertAll();
		}
	}

	@AfterEach
	void afterEach(@InjectBundleInstaller
	BundleInstaller bi) {
		softly = new OSGiSoftAssertions();
		softly.assertThat(bundleInstaller)
			.as("bundleInstaller:afterEach")
			.isNotNull()
			.isSameAs(bi)
			.extracting(BundleInstaller::getBundleContext)
			.isSameAs(bundleContext);
		softly.assertAll();
	}

	@AfterAll
	static void afterAll(@InjectBundleInstaller
	BundleInstaller bi) {
		staticSoftly = new OSGiSoftAssertions();
		staticSoftly.assertThat(staticBI)
			.as("staticBI:AfterAll")
			.isNotNull()
			.isSameAs(bi)
			.extracting(BundleInstaller::getBundleContext)
			.isSameAs(staticBC);
		staticSoftly.assertAll();
	}
}
