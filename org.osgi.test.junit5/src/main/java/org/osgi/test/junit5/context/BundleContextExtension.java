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

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store;
import org.junit.jupiter.api.extension.ExtensionContext.Store.CloseableResource;
import org.junit.jupiter.api.extension.ParameterContext;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.context.CloseableBundleContext;
import org.osgi.test.common.install.BundleInstaller;
import org.osgi.test.junit5.inject.InjectingExtension;

/**
 * A JUnit 5 Extension to get the OSGi {@link BundleContext} of the test bundle.
 * <p>
 * The {@link BundleContext} implementation provided by this extension will
 * automatically clean up all service registrations, bundle, service and
 * framework listeners, as well as installed bundles left behind.
 * <p>
 * Example:
 *
 * <pre>
 * &#64;ExtendWith(BundleContextExtension.class)
 * class MyTests {
 *
 * 	&#64;InjectBundleContext
 * 	BundleContext bundleContext;
 *
 * 	&#64;Test
 * 	public void aTest() {
 * 		Bundle bundle = bundleContext.getBundle();
 * 	}
 * }
 * </pre>
 */
// Include BundleInstallerExtension for backward compatibility
@ExtendWith(BundleInstallerExtension.class)
public class BundleContextExtension extends InjectingExtension<InjectBundleContext> {

	public static final String BUNDLE_CONTEXT_KEY = "bundle.context";
	/**
	 * @deprecated Replaced by
	 *             {@link BundleInstallerExtension#INSTALL_BUNDLE_KEY}.
	 */
	@Deprecated
	public static final String	INSTALL_BUNDLE_KEY	= BundleInstallerExtension.INSTALL_BUNDLE_KEY;

	public BundleContextExtension() {
		super(InjectBundleContext.class, BundleContext.class);
	}

	public static BundleContext getBundleContext(ExtensionContext extensionContext) {
		BundleContext bundleContext = getStore(extensionContext)
			.getOrComputeIfAbsent(BUNDLE_CONTEXT_KEY,
				key -> new CloseableResourceBundleContext(getParentBundleContext(extensionContext)),
				CloseableResourceBundleContext.class)
			.get();
		return bundleContext;
	}

	/**
	 * @deprecated Replaced by
	 *             {@link BundleInstallerExtension#getBundleInstaller(ExtensionContext)}.
	 */
	@Deprecated
	public static BundleInstaller getBundleInstaller(ExtensionContext extensionContext) {
		return BundleInstallerExtension.getBundleInstaller(extensionContext);
	}

	private static BundleContext getParentBundleContext(ExtensionContext extensionContext) {
		BundleContext parentContext = extensionContext.getParent()
			.filter(context -> context.getTestClass()
				.isPresent())
			.map(BundleContextExtension::getBundleContext)
			.orElseGet(() -> FrameworkUtil.getBundle(extensionContext.getRequiredTestClass())
				.getBundleContext());
		return parentContext;
	}

	public static class CloseableResourceBundleContext implements CloseableResource {

		private final BundleContext bundleContext;

		CloseableResourceBundleContext(BundleContext bundleContext) {
			this.bundleContext = CloseableBundleContext.proxy(bundleContext);
		}

		@Override
		public void close() throws Exception {
			((AutoCloseable) get()).close();
		}

		public BundleContext get() {
			return bundleContext;
		}

		@Override
		public String toString() {
			return get().toString();
		}
	}

	static Store getStore(ExtensionContext extensionContext) {
		return extensionContext
			.getStore(Namespace.create(BundleContextExtension.class, extensionContext.getUniqueId()));
	}

	@Override
	protected Object resolveField(Field field, ExtensionContext extensionContext) {
		return getBundleContext(extensionContext);
	}

	@Override
	public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
		return getBundleContext(extensionContext);
	}
}
