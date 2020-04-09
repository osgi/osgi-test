/*
 * Copyright (c) OSGi Alliance (2019). All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.osgi.test.junit5.service;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.osgi.framework.BundleContext;
import org.osgi.test.junit5.context.BundleContextExtension;

class WithServiceUseExtension<T> implements AutoCloseable {
	private final ExtensionContext	extensionContext;
	final ServiceUseConfiguration<T>	serviceUseConfiguration;

	public WithServiceUseExtension(ExtensionContext extensionContext, Class<T> serviceType, String filterString,
		int cardinality, long timeout)
		throws Exception {

		this.extensionContext = extensionContext;

		this.serviceUseConfiguration = new ServiceUseConfiguration<>(serviceType, extensionContext,
			(filterString == null) ? "" : filterString, new String[0], cardinality, timeout);
	}

	public void init() throws Exception {
		serviceUseConfiguration.init();
	}

	@Override
	public void close() throws Exception {
		serviceUseConfiguration.close();
	}

	public BundleContext getBundleContext() {
		return BundleContextExtension.getBundleContext(extensionContext);
	}

	public ServiceUseConfiguration<T> getExtension() {
		return serviceUseConfiguration;
	}
}