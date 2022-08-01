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
package org.osgi.test.junit5.framework;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.osgi.framework.Bundle;
import org.osgi.framework.connect.FrameworkUtilHelper;

public class JUnit5FrameworkUtilHelper implements FrameworkUtilHelper {

	static Map<Class<?>, Bundle>			testProbeMap		= new ConcurrentHashMap<>();
	static Set<FrameworkUtilHelper>			additionalHelpers	= ConcurrentHashMap.newKeySet();
	static ThreadLocal<FrameworkUtilHelper>	threadHelper		= new ThreadLocal<>();

	@Override
	public Optional<Bundle> getBundle(Class<?> classFromBundle) {
		// for the test probe we always know the bundle
		Bundle probeBundle = testProbeMap.get(classFromBundle);
		if (probeBundle != null) {
			return Optional.of(probeBundle);
		}
		// the check if we have a thread attached helper
		FrameworkUtilHelper threadHelper = JUnit5FrameworkUtilHelper.threadHelper.get();
		if (threadHelper != null) {
			return threadHelper.getBundle(classFromBundle);
		}
		// last resort (might return false positives when using multiple
		// frameworks that have overlapping classpathes!)
		for (FrameworkUtilHelper helper : additionalHelpers) {
			Optional<Bundle> bundle = helper.getBundle(classFromBundle);
			if (bundle.isPresent()) {
				return bundle;
			}
		}
		return Optional.empty();
	}

}
