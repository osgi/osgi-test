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

import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.junit5.test.testutils.OSGiSoftAssertions;

@TestInstance(PER_CLASS)
public class BundleContextExtension_BundleContextInjection_PerClassTest {

	static final Bundle	bundle	= FrameworkUtil
		.getBundle(BundleContextExtension_BundleContextInjection_PerClassTest.class);

	@InjectBundleContext
	BundleContext		bundleContext;

	BundleContext		beforeAllContext;

	BundleContext		constructorContext;

	OSGiSoftAssertions	staticSoftly;

	OSGiSoftAssertions	softly;

	BundleContextExtension_BundleContextInjection_PerClassTest(@InjectBundleContext
	BundleContext bc) {
		constructorContext = bc;
	}

	@BeforeAll
	void beforeAll(@InjectBundleContext
	BundleContext bc) {
		beforeAllContext = bc;
		staticSoftly = new OSGiSoftAssertions();
		staticSoftly.assertThat(bundleContext)
			.as("staticBC:beforeAll")
			.isNotNull()
			.isSameAs(bc)
			.isSameAs(constructorContext)
			.refersToBundle(bundle);
		staticSoftly.assertAll();
	}

	@BeforeEach
	void beforeEach(@InjectBundleContext
	BundleContext bc) {
		softly = new OSGiSoftAssertions();
		softly.assertThat(bundleContext)
			.as("bundleContext:beforeEach")
			.isNotNull()
			.isNotSameAs(bc)
			.isSameAs(beforeAllContext)
			.refersToBundle(bundle);
		softly.assertAll();
	}

	@Test
	void innerTest(@InjectBundleContext
	BundleContext bc) {
		softly = new OSGiSoftAssertions();
		softly.assertThat(bundleContext)
			.as("bundleContext:innerTest")
			.isNotNull()
			.isNotSameAs(bc)
			.isSameAs(beforeAllContext)
			.refersToBundle(bundle);
		softly.assertAll();
	}

	@ParameterizedTest
	@ValueSource(ints = {
		1, 2, 3
	})
	// This test is meant to check that the extension is doing the
	// right thing before and after parameterized tests, hence
	// the parameter is not actually used.
	void parameterizedTest(int unused, @InjectBundleContext
	BundleContext bc) {
		softly = new OSGiSoftAssertions();
		softly.assertThat(bundleContext)
			.as("bundleContext:parameterizedTest")
			.isNotNull()
			.isNotSameAs(bc)
			.isSameAs(beforeAllContext)
			.refersToBundle(bundle);
		softly.assertAll();
	}

	@Nested
	@TestInstance(PER_CLASS)
	class NestedTest {

		@InjectBundleContext
		BundleContext nestedBC;

		BundleContext	beforeAllNestedBC;

		@BeforeAll
		void beforeAll(@InjectBundleContext
		BundleContext bc) {
			beforeAllNestedBC = bc;
			softly = new OSGiSoftAssertions();
			softly.assertThat(bundleContext)
				.as("bundleContext:nested.beforeAll")
				.isNotNull()
				.isNotSameAs(bc)
				.isNotSameAs(nestedBC)
				.isSameAs(beforeAllContext)
				.refersToBundle(bundle);
			softly.assertThat(nestedBC)
				.as("nestedBC:nested.beforeAll")
				.isNotNull()
				.isSameAs(bc)
				.refersToBundle(bundle);
			softly.assertAll();
		}

		@BeforeEach
		void beforeEach(@InjectBundleContext
		BundleContext bc) {
			softly = new OSGiSoftAssertions();
			softly.assertThat(bundleContext)
				.as("bundleContext:nested.beforeEach")
				.isNotNull()
				.isNotSameAs(bc)
				.isNotSameAs(nestedBC)
				.isSameAs(beforeAllContext)
				.refersToBundle(bundle);
			softly.assertThat(nestedBC)
				.as("nestedBC:nested.beforeEach")
				.isNotNull()
				.isNotSameAs(bc)
				.refersToBundle(bundle);
			softly.assertAll();
		}

		@Test
		void test(@InjectBundleContext
		BundleContext bc) {
			softly = new OSGiSoftAssertions();
			softly.assertThat(bundleContext)
				.as("bundleContext:nested.test")
				.isNotNull()
				.isNotSameAs(bc)
				.isSameAs(beforeAllContext)
				.refersToBundle(bundle);
			softly.assertThat(nestedBC)
				.as("nestedBC:nested.test")
				.isNotNull()
				.isNotSameAs(bc)
				.isSameAs(beforeAllNestedBC)
				.refersToBundle(bundle);
			softly.assertAll();
		}

		@AfterEach
		void afterEach(@InjectBundleContext
		BundleContext bc) {
			softly = new OSGiSoftAssertions();
			softly.assertThat(bundleContext)
				.as("bundleContext:nested.afterEach")
				.isNotNull()
				.isNotSameAs(bc)
				.isSameAs(beforeAllContext)
				.isNotSameAs(nestedBC)
				.refersToBundle(bundle);
			softly.assertThat(nestedBC)
				.as("nestedBC:nested.afterEach")
				.isNotNull()
				.isNotSameAs(bc)
				.isSameAs(beforeAllNestedBC)
				.refersToBundle(bundle);
			softly.assertAll();
		}

		@AfterAll
		void afterAll(@InjectBundleContext
		BundleContext bc) {
			softly = new OSGiSoftAssertions();
			softly.assertThat(bundleContext)
				.as("bundleContext:nested.afterAll")
				.isNotNull()
				.isNotSameAs(bc)
				.isNotSameAs(nestedBC)
				.isSameAs(beforeAllContext)
				.refersToBundle(bundle);
			softly.assertThat(nestedBC)
				.as("nestedBC:nested.afterAll")
				.isNotNull()
				.isSameAs(bc)
				.isSameAs(beforeAllNestedBC)
				.refersToBundle(bundle);
			softly.assertAll();
		}

	}

	@AfterEach
	void afterEach(@InjectBundleContext
	BundleContext bc) {
		softly = new OSGiSoftAssertions();
		softly.assertThat(bundleContext)
			.as("bundleContext:afterEach")
			.isNotNull()
			.isNotSameAs(bc)
			.isSameAs(beforeAllContext)
			.refersToBundle(bundle);
		softly.assertAll();
	}

	@AfterAll
	void afterAll(@InjectBundleContext
	BundleContext bc) {
		staticSoftly = new OSGiSoftAssertions();
		staticSoftly.assertThat(bundleContext)
			.as("staticBC:afterAll")
			.isNotNull()
			.isSameAs(bc)
			.isSameAs(beforeAllContext)
			.refersToBundle(bundle);
		staticSoftly.assertAll();
	}
}
