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

package org.osgi.test.assertj.bundlecontext;

import org.assertj.core.api.InstanceOfAssertFactory;
import org.osgi.framework.BundleContext;

public class BundleContextAssert extends AbstractBundleContextAssert<BundleContextAssert, BundleContext> {

	public static final InstanceOfAssertFactory<BundleContext, BundleContextAssert> BUNDLE_CONTEXT = new InstanceOfAssertFactory<>(
		BundleContext.class, BundleContextAssert::assertThat);

	public BundleContextAssert(BundleContext actual) {
		super(actual, BundleContextAssert.class);
	}

	public static BundleContextAssert assertThat(BundleContext actual) {
		return new BundleContextAssert(actual);
	}
}
