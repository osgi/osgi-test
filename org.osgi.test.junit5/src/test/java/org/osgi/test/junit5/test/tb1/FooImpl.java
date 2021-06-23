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

package org.osgi.test.junit5.test.tb1;

import org.osgi.annotation.bundle.Header;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.PrototypeServiceFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.test.junit5.test.types.Foo;

@Header(name = Constants.BUNDLE_ACTIVATOR, value = "${@class}")
public class FooImpl implements BundleActivator, PrototypeServiceFactory<Foo> {

	private ServiceRegistration<Foo> serviceRegistration;

	@Override
	public void start(BundleContext bundleContext) throws Exception {
		serviceRegistration = bundleContext.registerService(Foo.class, this, null);
	}

	@Override
	public void stop(BundleContext bundleContext) throws Exception {
		serviceRegistration.unregister();
	}

	@Override
	public Foo getService(Bundle bundle, ServiceRegistration<Foo> registration) {
		return new Foo() {};
	}

	@Override
	public void ungetService(Bundle bundle, ServiceRegistration<Foo> registration, Foo service) {}

}
