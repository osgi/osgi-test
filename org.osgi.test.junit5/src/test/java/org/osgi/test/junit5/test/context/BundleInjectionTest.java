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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Version;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.annotation.InjectBundleInstaller;
import org.osgi.test.common.annotation.InjectInstalledBundle;
import org.osgi.test.common.install.BundleInstaller;
import org.osgi.test.common.install.BundleInstaller.EmbeddedLocation;
import org.osgi.test.junit5.test.testutils.OSGiSoftAssertions;

public class BundleInjectionTest {

	private static final String	TB1_JAR	= "tb1.jar";

	@InjectBundleInstaller
	static BundleInstaller		bI;

	@InjectInstalledBundle(TB1_JAR)
	static volatile Bundle		bundleFieldStatic;

	@InjectInstalledBundle(TB1_JAR)
	Bundle						bundleField;

	static OSGiSoftAssertions	staticSoftly;

	OSGiSoftAssertions			softly;

	@BeforeAll
	static void beforeAll(@InjectInstalledBundle(TB1_JAR) Bundle bundleParam) {
		assertThat(bI).isNotNull();
		Bundle iBBundle = bI.installBundle(TB1_JAR, false);

		assertThat(bundleFieldStatic).isNotNull();
		staticSoftly = new OSGiSoftAssertions();
		staticSoftly.assertThat(bundleFieldStatic)
			.as("bundleFieldStatic:beforeAll")
			.isNotNull()
			.isSameAs(bundleParam)
			.isSameAs(iBBundle);

		staticSoftly.assertAll();
	}

	@BeforeEach
	void beforeEach(@InjectInstalledBundle(TB1_JAR) Bundle bundleParam) {
		Bundle iBBundle = bI.installBundle(TB1_JAR, false);
		assertThat(bundleParam).isNotNull();
		softly = new OSGiSoftAssertions();
		softly.assertThat(bundleParam)
			.as("bundleParam:beforeEach")
			.isNotNull()
			.isSameAs(bundleField)
			.isSameAs(bundleFieldStatic)
			.isSameAs(iBBundle);
		softly.assertAll();
	}

	@Test
	void innerTest(@InjectInstalledBundle(TB1_JAR) Bundle bundleParam) {

		assertThat(bI).isNotNull();
		Bundle iBBundle = bI.installBundle(TB1_JAR, false);

		assertThat(bundleParam).isNotNull();
		softly = new OSGiSoftAssertions();
		softly.assertThat(bundleParam)
			.as("bundleParam:innerTest")
			.isNotNull()
			.isSameAs(bundleField)
			.isSameAs(bundleFieldStatic)
			.isSameAs(iBBundle);
		softly.assertAll();
	}

	@Test
	void embeddedLocationTest(@InjectBundleContext BundleContext bundleContext) throws IOException {

		EmbeddedLocation eLoc = BundleInstaller.EmbeddedLocation.of(bundleContext, TB1_JAR);
		assertThat(eLoc).isNotNull();

		assertThat(eLoc.openStream(bundleContext)).isNotNull()
			.isNotEmpty();
		EmbeddedLocation eLocEx = BundleInstaller.EmbeddedLocation.of("unknown", Version.valueOf("1.1.1"), "/",
			"unknown.jar");
		assertThat(eLocEx).isNotNull();
		assertThatThrownBy(() -> eLocEx.openStream(bundleContext)).isInstanceOf(IllegalArgumentException.class);

		assertThatThrownBy(() -> BundleInstaller.EmbeddedLocation.of(bundleContext, "a:b:c:d"))
			.isInstanceOf(IllegalArgumentException.class);

	}

	@AfterEach
	void afterEach(@InjectInstalledBundle(TB1_JAR) Bundle bundleParam) {
		assertThat(bI).isNotNull();
		Bundle iBBundle = bI.installBundle(TB1_JAR, false);
		softly = new OSGiSoftAssertions();
		softly.assertThat(bundleParam)
			.as("afterEach")
			.isNotNull()
			.isSameAs(bundleField)
			.isSameAs(bundleFieldStatic)
			.isSameAs(iBBundle);
		softly.assertAll();
	}

	@AfterAll
	static void afterAll(@InjectInstalledBundle(TB1_JAR) Bundle bundleParam) {

		assertThat(bI).isNotNull();
		Bundle iBBundle = bI.installBundle(TB1_JAR, false);

		staticSoftly = new OSGiSoftAssertions();
		staticSoftly.assertThat(bundleParam)
			.as("afterAll")
			.isNotNull()
			.isSameAs(iBBundle)
			.isSameAs(bundleFieldStatic);
		staticSoftly.assertAll();
	}
}
