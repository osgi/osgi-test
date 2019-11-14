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

import static org.osgi.test.common.filter.Filters.format;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.context.BundleContextExtension.CloseableResourceBundleContext;
import org.osgi.test.junit5.service.ServiceUseExtension.CloseableTrackServices;

class WithServiceUseExtension<T> implements AutoCloseable {
	private final ExtensionContext	extensionContext;
	final BundleContextExtension	bundleContextExtension;
	final ServiceUseExtension<T>	serviceUseExtension;

	public WithServiceUseExtension(ExtensionContext extensionContext, Class<T> serviceType, String filterString,
		int cardinality, long timeout)
		throws Exception {

		this.extensionContext = extensionContext;
		this.bundleContextExtension = new BundleContextExtension();

		Filter filter = (filterString == null) ? format("(objectClass=%s)", serviceType.getName())
			: format("(&(objectClass=%s)%s)", serviceType.getName(), filterString);

		this.serviceUseExtension = new ServiceUseExtension<>(serviceType, bundleContextExtension,
			filter, cardinality, timeout);
	}

	public void bceInit() throws Exception {
		this.bundleContextExtension.beforeEach(extensionContext);
	}

	public void init() throws Exception {
		this.serviceUseExtension.beforeEach(extensionContext);
	}

	@Override
	public void close() throws Exception {
		CloseableTrackServices<T> cts = extensionContext.getStore(ServiceUseExtension.NAMESPACE)
			.remove(serviceUseExtension.getFilter()
				.toString(), ServiceUseExtension.CloseableTrackServices.class);

		if (cts != null) {
			cts.close();
		}

		CloseableResourceBundleContext crbc = extensionContext.getStore(BundleContextExtension.NAMESPACE)
			.remove(BundleContextExtension.KEY, BundleContextExtension.CloseableResourceBundleContext.class);

		if (crbc != null) {
			crbc.close();
		}
	}

	public BundleContext getBundleContext() {
		return bundleContextExtension.getBundleContext(extensionContext);
	}

	public ServiceUseExtension<T> getExtension() {
		return serviceUseExtension;
	}
}