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

package org.osgi.test.assertj.feature;

/**
 * Entry point for soft assertions of different data types.
 */
public class SoftAssertions extends org.assertj.core.api.SoftAssertions {

	/**
	 * Creates a new "soft" instance of
	 * <code>{@link org.osgi.test.assertj.feature.FeatureAssert}</code>.
	 *
	 * @param actual the actual value.
	 * @return the created "soft" assertion object.
	 */
	@org.assertj.core.util.CheckReturnValue
	public org.osgi.test.assertj.feature.FeatureAssert assertThat(org.osgi.util.feature.Feature actual) {
		return proxy(org.osgi.test.assertj.feature.FeatureAssert.class, org.osgi.util.feature.Feature.class, actual);
	}

	/**
	 * Creates a new "soft" instance of
	 * <code>{@link org.osgi.test.assertj.feature.FeatureArtifactAssert}</code>.
	 *
	 * @param actual the actual value.
	 * @return the created "soft" assertion object.
	 */
	@org.assertj.core.util.CheckReturnValue
	public org.osgi.test.assertj.feature.FeatureArtifactAssert assertThat(
		org.osgi.util.feature.FeatureArtifact actual) {
		return proxy(org.osgi.test.assertj.feature.FeatureArtifactAssert.class,
			org.osgi.util.feature.FeatureArtifact.class, actual);
	}

	/**
	 * Creates a new "soft" instance of
	 * <code>{@link org.osgi.test.assertj.feature.FeatureBundleAssert}</code>.
	 *
	 * @param actual the actual value.
	 * @return the created "soft" assertion object.
	 */
	@org.assertj.core.util.CheckReturnValue
	public org.osgi.test.assertj.feature.FeatureBundleAssert assertThat(org.osgi.util.feature.FeatureBundle actual) {
		return proxy(org.osgi.test.assertj.feature.FeatureBundleAssert.class, org.osgi.util.feature.FeatureBundle.class,
			actual);
	}

	/**
	 * Creates a new "soft" instance of
	 * <code>{@link org.osgi.test.assertj.feature.FeatureConfigurationAssert}</code>.
	 *
	 * @param actual the actual value.
	 * @return the created "soft" assertion object.
	 */
	@org.assertj.core.util.CheckReturnValue
	public org.osgi.test.assertj.feature.FeatureConfigurationAssert assertThat(
		org.osgi.util.feature.FeatureConfiguration actual) {
		return proxy(org.osgi.test.assertj.feature.FeatureConfigurationAssert.class,
			org.osgi.util.feature.FeatureConfiguration.class, actual);
	}

	/**
	 * Creates a new "soft" instance of
	 * <code>{@link org.osgi.test.assertj.feature.FeatureExtensionAssert}</code>.
	 *
	 * @param actual the actual value.
	 * @return the created "soft" assertion object.
	 */
	@org.assertj.core.util.CheckReturnValue
	public org.osgi.test.assertj.feature.FeatureExtensionAssert assertThat(
		org.osgi.util.feature.FeatureExtension actual) {
		return proxy(org.osgi.test.assertj.feature.FeatureExtensionAssert.class,
			org.osgi.util.feature.FeatureExtension.class, actual);
	}

	/**
	 * Creates a new "soft" instance of
	 * <code>{@link org.osgi.test.assertj.feature.IDAssert}</code>.
	 *
	 * @param actual the actual value.
	 * @return the created "soft" assertion object.
	 */
	@org.assertj.core.util.CheckReturnValue
	public org.osgi.test.assertj.feature.IDAssert assertThat(org.osgi.util.feature.ID actual) {
		return proxy(org.osgi.test.assertj.feature.IDAssert.class, org.osgi.util.feature.ID.class, actual);
	}

}
