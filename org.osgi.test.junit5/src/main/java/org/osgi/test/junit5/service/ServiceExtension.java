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

package org.osgi.test.junit5.service;

import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.List;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store;
import org.junit.jupiter.api.extension.ExtensionContext.Store.CloseableResource;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.inject.TargetType;
import org.osgi.test.common.list.ListSupplierDelegate;
import org.osgi.test.common.service.ServiceAware;
import org.osgi.test.common.service.ServiceConfiguration;
import org.osgi.test.common.service.ServiceConfigurationKey;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.inject.InjectingExtension;

/**
 * A JUnit 5 Extension to depend on OSGi services.
 * <p>
 * Example: <br>
 *
 * <pre>
 * &#64;ExtendWith(ServiceExtension.class)
 * class MyTests {
 *
 * 	&#64;InjectService
 * 	Foo foo;
 *
 * 	&#64;Test
 * 	public void test() {
 * 		// use foo
 * 	}
 * }
 * </pre>
 */
public class ServiceExtension extends InjectingExtension<InjectService> {

	public ServiceExtension() {
		super(InjectService.class);
	}

	@Override
	protected boolean supportsType(TargetType targetType, ExtensionContext extensionContext)
		throws ParameterResolutionException {
		Type serviceType;
		if (targetType.matches(List.class) || targetType.matches(ServiceAware.class)) {
			if (targetType.hasParameterizedTypes()) {
				serviceType = targetType.getFirstGenericTypes()
					.get();
				if (serviceType instanceof WildcardType) {
					serviceType = Object.class;
				}
			} else {
				serviceType = Object.class;
			}
		} else {
			serviceType = targetType.getGenericType();
		}

		if (!(serviceType instanceof Class)) {
			throw new ParameterResolutionException(String.format(
				"Element %s has an unsupported type %s for annotation @%s. Service must have non-generic type.",
				targetType.getName(), serviceType.getTypeName(), annotation().getSimpleName()));
		}

		return true;
	}

	@Override
	protected Object resolveValue(TargetType targetType, InjectService injectService, ExtensionContext extensionContext)
		throws ParameterResolutionException {
		Type serviceType = injectService.service();
		if (serviceType.equals(annotation())) {
			if (targetType.matches(List.class) || targetType.matches(ServiceAware.class)) {
				if (targetType.hasParameterizedTypes()) {
					serviceType = targetType.getFirstGenericTypes()
						.get();
					if (serviceType instanceof WildcardType) {
						serviceType = Object.class;
					}
				} else {
					serviceType = Object.class;
				}
			} else {
				serviceType = targetType.getType();
			}
		}

		if (!(serviceType instanceof Class)) {
			throw new ParameterResolutionException(String.format(
				"Element %s has an unsupported type %s for annotation @%s. Service must have non-generic type.",
				targetType.getName(), serviceType.getTypeName(), annotation().getSimpleName()));
		}

		ServiceConfiguration<?> configuration = getServiceConfiguration((Class<?>) serviceType, injectService.filter(),
			injectService.filterArguments(), injectService.cardinality(), injectService.timeout(), extensionContext);

		if (targetType.matches(ServiceAware.class)) {
			return configuration;
		}
		if (targetType.matches(List.class)) {
			return new ListSupplierDelegate<>(configuration::getServices);
		}
		return configuration.getService();
	}

	public static <S> ServiceConfiguration<S> getServiceConfiguration(Class<S> serviceType, String format,
		String[] args, int cardinality, long timeout, ExtensionContext extensionContext) {
		@SuppressWarnings("unchecked")
		ServiceConfiguration<S> serviceConfiguration = getStore(extensionContext)
			.getOrComputeIfAbsent(new ServiceConfigurationKey<>(serviceType, format, args, cardinality, timeout),
				key -> new CloseableServiceConfiguration<>(
					new ServiceConfiguration<>(key).init(BundleContextExtension.getBundleContext(extensionContext))),
				CloseableServiceConfiguration.class)
			.get();
		return serviceConfiguration;
	}

	static Store getStore(ExtensionContext extensionContext) {
		return extensionContext.getStore(Namespace.create(ServiceExtension.class, extensionContext.getUniqueId()));
	}

	public static class CloseableServiceConfiguration<S> implements CloseableResource {

		private final ServiceConfiguration<S> serviceConfiguration;

		CloseableServiceConfiguration(ServiceConfiguration<S> serviceConfiguration) {
			this.serviceConfiguration = serviceConfiguration;
		}

		@Override
		public void close() throws Exception {
			get().close();
		}

		public ServiceConfiguration<S> get() {
			return serviceConfiguration;
		}

		@Override
		public String toString() {
			return get().toString();
		}
	}
}
