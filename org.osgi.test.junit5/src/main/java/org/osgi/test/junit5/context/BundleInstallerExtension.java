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

package org.osgi.test.junit5.context;

import java.lang.reflect.Field;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store;
import org.junit.jupiter.api.extension.ParameterContext;
import org.osgi.framework.BundleContext;
import org.osgi.test.common.annotation.InjectBundleInstaller;
import org.osgi.test.common.install.BundleInstaller;
import org.osgi.test.junit5.inject.InjectingExtension;

/**
 * A JUnit 5 Extension to get a {@link BundleInstaller} for the test bundle.
 * <p>
 * The {@link BundleContext} implementation used by this extension will
 * automatically clean up all service registrations, bundle, service and
 * framework listeners, as well as installed bundles left behind.
 * <p>
 * Example:
 *
 * <pre>
 * &#64;ExtendWith(BundleInstallerExtension.class)
 * class MyTests {
 *
 * 	&#64;InjectBundleInstaller
 * 	BundleInstaller bundleInstaller;
 *
 * 	&#64;Test
 * 	public void aTest() {
 * 		// use bundleInstaller
 * 	}
 * }
 * </pre>
 *
 * @since 1.1
 */
public class BundleInstallerExtension extends InjectingExtension<InjectBundleInstaller> {

	public static final String	INSTALL_BUNDLE_KEY	= "bundle.installer";

	public BundleInstallerExtension() {
		super(InjectBundleInstaller.class, BundleInstaller.class);
	}

	public static BundleInstaller getBundleInstaller(ExtensionContext extensionContext) {
		return getStore(extensionContext).getOrComputeIfAbsent(INSTALL_BUNDLE_KEY,
			key -> new BundleInstaller(BundleContextExtension.getBundleContext(extensionContext)),
			BundleInstaller.class);
	}

	static Store getStore(ExtensionContext extensionContext) {
		return extensionContext
			.getStore(Namespace.create(BundleInstallerExtension.class, extensionContext.getUniqueId()));
	}

	@Override
	protected Object resolveField(Field field, ExtensionContext extensionContext) {
		return getBundleInstaller(extensionContext);
	}

	@Override
	public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
		return getBundleInstaller(extensionContext);
	}
}
