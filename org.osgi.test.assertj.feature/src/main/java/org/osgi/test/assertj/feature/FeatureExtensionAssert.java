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

import org.osgi.util.feature.FeatureExtension;

/**
 * {@link FeatureExtension} specific assertions - Generated by
 * CustomAssertionGenerator. Although this class is not final to allow Soft
 * assertions proxy, if you wish to extend it, extend
 * {@link AbstractFeatureExtensionAssert} instead.
 */
public class FeatureExtensionAssert extends AbstractFeatureExtensionAssert<FeatureExtensionAssert, FeatureExtension> {

	/**
	 * Creates a new <code>{@link FeatureExtensionAssert}</code> to make
	 * assertions on actual FeatureExtension.
	 *
	 * @param actual the FeatureExtension we want to make assertions on.
	 */
	public FeatureExtensionAssert(FeatureExtension actual) {
		super(actual, FeatureExtensionAssert.class);
	}

	/**
	 * An entry point for FeatureExtensionAssert to follow AssertJ standard
	 * <code>assertThat()</code> statements.<br>
	 * With a static import, one can write directly:
	 * <code>assertThat(myFeatureExtension)</code> and get specific assertion
	 * with code completion.
	 *
	 * @param actual the FeatureExtension we want to make assertions on.
	 * @return a new <code>{@link FeatureExtensionAssert}</code>
	 */
	@org.assertj.core.util.CheckReturnValue
	public static FeatureExtensionAssert assertThat(FeatureExtension actual) {
		return new FeatureExtensionAssert(actual);
	}
}
