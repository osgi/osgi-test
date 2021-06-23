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

package org.osgi.test.common.test.install;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.assertj.core.api.SoftAssertions;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.Version;
import org.osgi.test.common.install.BundleInstaller;
import org.osgi.test.common.install.BundleInstaller.EmbeddedLocation;

public class BundleInstaller_EmbeddedLocationTest {

	private Bundle			bundle				= FrameworkUtil.getBundle(BundleInstaller_EmbeddedLocationTest.class);
	private BundleContext	bc					= bundle.getBundleContext();

	String					bundleSymbolicName	= bundle.getSymbolicName();
	String					bundleVersion		= bundle.getVersion()
		.toString();

	@Test
	void testSpecWithOutPath() throws Exception {

		EmbeddedLocation compareLocation = EmbeddedLocation.of(bundleSymbolicName, bundle.getVersion(), "/",
			"file.jar");
		// Without path
		List<String> specs = Lists.list(//
			bundleSymbolicName + ":" + bundleVersion + ":/file.jar", //
			bundleSymbolicName + ":" + bundleVersion + ":file.jar", //
			bundleSymbolicName + ":/file.jar", //
			bundleSymbolicName + ":file.jar", //
			"/file.jar", //
			"file.jar");//

		SoftAssertions softly = new SoftAssertions();
		for (String spec : specs) {
			EmbeddedLocation location = BundleInstaller.EmbeddedLocation.of(bc, spec);
			softly.assertThat(location)
				.isNotNull()
				.as("With Spec %s", spec)
				.isEqualTo(compareLocation);
		}
		softly.assertAll();
	}

	@Test
	void testSpecWithPath() throws Exception {
		SoftAssertions softly = new SoftAssertions();
		EmbeddedLocation compareLocation = EmbeddedLocation.of(bundleSymbolicName, bundle.getVersion(), "/path/to",
			"file.jar");

		List<String> specs = Lists.list(//
			bundleSymbolicName + ":" + bundleVersion + ":/path/to/file.jar", //
			bundleSymbolicName + ":" + bundleVersion + ":path/to/file.jar", //
			bundleSymbolicName + ":/path/to/file.jar", //
			bundleSymbolicName + ":path/to/file.jar", //
			"/path/to/file.jar", //
			"path/to/file.jar");//

		softly = new SoftAssertions();
		for (String spec : specs) {
			EmbeddedLocation location = BundleInstaller.EmbeddedLocation.of(bc, spec);

			assertThat(location).isNotNull()
				.as("With Spec %s", spec)
				.isEqualTo(compareLocation);
		}
		softly.assertAll();
	}

	@Test
	void testConstructorNull() throws Exception {
		SoftAssertions softly = new SoftAssertions();

		softly
			.assertThatThrownBy(
				() -> BundleInstaller.EmbeddedLocation.of(null, Version.parseVersion("1.1.1"), "/", "file.jar"))
			.isInstanceOf(NullPointerException.class);

		softly.assertThatThrownBy(() -> BundleInstaller.EmbeddedLocation.of("bsn", null, "/", "file.jar"))
			.isInstanceOf(NullPointerException.class);

		softly
			.assertThatThrownBy(
				() -> BundleInstaller.EmbeddedLocation.of("bsn", Version.parseVersion("1.1.1"), null, "file.jar"))
			.isInstanceOf(NullPointerException.class);

		softly
			.assertThatThrownBy(
				() -> BundleInstaller.EmbeddedLocation.of("bsn", Version.parseVersion("1.1.1"), "/", null))
			.isInstanceOf(NullPointerException.class);

		softly.assertAll();
	}

}
