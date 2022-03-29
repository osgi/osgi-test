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

package org.osgi.test.common.context;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.test.common.bitmaps.BundleState;

public final class ContextHelper {
	private ContextHelper() {}

	public static BundleContext getBundleContext(Class<?> testClass) {
		Bundle bundle = FrameworkUtil.getBundle(testClass);
		if (bundle == null) {
			throw new IllegalStateException(
				String.format("No BundleContext available - The class (%s) must be loaded from a bundle", testClass));
		}
		BundleContext context = bundle.getBundleContext();
		if (context == null) {
			throw new IllegalStateException(String.format(
				"No BundleContext available - The bundle of the class (%s) must be started to have a BundleContext; bundle current state: %s",
				testClass, BundleState.toString(bundle.getState())));
		}
		return context;
	}
}
