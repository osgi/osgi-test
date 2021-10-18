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

import java.util.Map;
import java.util.function.BiFunction;

import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.test.common.annotation.InjectBundleContext;

@ExtendWith(PreDestroyCallback.class)
class BundleContextMultiLevelCleanupTest<RESOURCE> extends MultiLevelCleanupTest {
	static final Bundle		bundle	= FrameworkUtil.getBundle(BundleContextMultiLevelCleanupTest.class);

	@InjectBundleContext
	static BundleContext	staticBC;

	@InjectBundleContext
	BundleContext			bundleContext;

	@SuppressWarnings("unchecked")
	static <STATIC> void setFactory(
		BiFunction<BundleContext, Map<CallbackPoint, STATIC>, AbstractResourceChecker<STATIC>> factory) {
		BundleContextMultiLevelCleanupTest.factory = (bc, map) -> factory.apply(bc, (Map<CallbackPoint, STATIC>) map);
	}

	static BiFunction<BundleContext, Map<CallbackPoint, ?>, AbstractResourceChecker<?>> factory;

	static AbstractResourceChecker<?> getGlobalResourceChecker() {
		return factory.apply(bundle.getBundleContext(), resourcesMap);
	}

	static AbstractResourceChecker<?> getStaticResourceChecker() {
		return factory.apply(staticBC, resourcesMap);
	}

	@SuppressWarnings("unchecked")
	@Override
	AbstractResourceChecker<?> getResourceChecker() {
		return factory.apply(bundleContext, resourcesMap);
	}
}
