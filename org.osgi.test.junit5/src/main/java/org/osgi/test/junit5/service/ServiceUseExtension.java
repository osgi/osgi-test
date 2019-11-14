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

import static java.util.Objects.requireNonNull;
import static org.junit.platform.commons.util.AnnotationUtils.findAnnotatedFields;
import static org.junit.platform.commons.util.ReflectionUtils.isPrivate;
import static org.junit.platform.commons.util.ReflectionUtils.makeAccessible;
import static org.osgi.test.common.filter.Filters.format;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.function.Predicate;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store.CloseableResource;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.platform.commons.util.ExceptionUtils;
import org.junit.platform.commons.util.ReflectionUtils;
import org.osgi.framework.Filter;
import org.osgi.framework.ServiceReference;
import org.osgi.test.common.service.ServiceAware;
import org.osgi.test.common.tracking.TrackServices;
import org.osgi.test.common.tracking.TrackingConfig;
import org.osgi.test.junit5.context.BundleContextExtension;

public class ServiceUseExtension<T>
	implements AfterEachCallback, BeforeEachCallback, ParameterResolver, ServiceAware<T>, TrackingConfig {

	public static class Builder<T> {

		private final Class<T>	serviceType;
		private final BundleContextExtension	contextExtension;
		private Filter			filter;
		private int				cardinality	= 1;
		private long			timeout		= TrackServices.DEFAULT_TIMEOUT;

		/**
		 * @param serviceType of the service
		 */
		public Builder(Class<T> serviceType, BundleContextExtension contextExtension) {
			this.serviceType = serviceType;
			this.contextExtension = contextExtension;
			this.filter = format("(objectClass=%s)", serviceType.getName());
		}

		/**
		 * Filter string used to target more specific services.
		 *
		 * @param filter string used to target more specific services
		 */
		public Builder<T> filter(String filter) {
			this.filter = format("(&%s%s)", this.filter.toString(), requireNonNull(filter));
			return this;
		}

		/**
		 * Indicate the number of services that are required to arrive within
		 * the specified timeout before starting the test.
		 *
		 * @param cardinality the number of services required before starting
		 *            the test
		 */
		public Builder<T> cardinality(int cardinality) {
			if (cardinality < 0) {
				throw new IllegalArgumentException("cardinality must be zero or greater");
			}
			this.cardinality = cardinality;
			return this;
		}

		/**
		 * Indicate require services must arrive within the specified timeout.
		 *
		 * @param timeout the timeout after which {@link AssertionError} is
		 *            thrown
		 */
		public Builder<T> timeout(long timeout) {
			if (timeout < 0) {
				throw new IllegalArgumentException("timeout must be zero or greater");
			}
			this.timeout = timeout;
			return this;
		}

		public ServiceUseExtension<T> build() {
			return new ServiceUseExtension<>(serviceType, contextExtension, filter, cardinality, timeout);
		}

	}

	final static Namespace					NAMESPACE	= Namespace.create(ServiceUseExtension.class);

	private final BundleContextExtension	contextExtension;
	private final T[]						empty;
	private final Class<T>					serviceType;
	private final TrackServices<T>			use;

	@SuppressWarnings("unchecked")
	protected ServiceUseExtension(Class<T> serviceType, BundleContextExtension contextExtension, Filter filter,
		int cardinality, long timeout) {
		this.serviceType = serviceType;
		this.contextExtension = contextExtension;
		this.empty = (T[]) Array.newInstance(serviceType, 0);
		this.use = new TrackServices<>(filter, cardinality, timeout);
	}

	@Override
	public void beforeEach(ExtensionContext extensionContext) throws Exception {
		contextExtension.beforeEach(extensionContext);
		use.init(contextExtension.getBundleContext(extensionContext));
		getService(serviceType, extensionContext);
		injectFields(extensionContext, extensionContext.getRequiredTestInstance(), ReflectionUtils::isNotStatic);
	}

	@Override
	public void afterEach(ExtensionContext context) throws Exception {
		use.close();
	}

	@Override
	public int getCardinality() {
		return use.getCardinality();
	}

	@Override
	public Filter getFilter() {
		return use.getFilter();
	}

	@Override
	public long getTimeout() {
		return use.getTimeout();
	}

	@Override
	public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
		throws ParameterResolutionException {
		assertRequired();
		Class<?> parameterType = parameterContext.getParameter()
			.getType();
		return getService(parameterType, extensionContext);
	}

	@Override
	public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
		throws ParameterResolutionException {
		boolean annotated = parameterContext.isAnnotated(ServiceUseParameter.class);
		if (annotated && parameterContext.getParameter()
			.getType()
			.equals(serviceType)) {

			return true;
		}
		return false;
	}

	@Override
	public T waitForService(long timeout) throws InterruptedException {
		return use.tracker()
			.waitForService(timeout);
	}

	@Override
	public List<ServiceReference<T>> getServiceReferences() {
		ServiceReference<T>[] serviceReferences = use.tracker()
			.getServiceReferences();
		return (serviceReferences == null) ? Collections.emptyList() : Arrays.asList(serviceReferences);
	}

	@Override
	public ServiceReference<T> getServiceReference() {
		return use.tracker()
			.getServiceReference();
	}

	@Override
	public T getService(ServiceReference<T> reference) {
		return use.tracker()
			.getService(reference);
	}

	@Override
	public List<T> getServices() {
		@SuppressWarnings("unchecked")
		T[] services = use.tracker()
			.getServices(empty);
		return (services == null) ? Collections.emptyList() : Arrays.asList(services);
	}

	@Override
	public T getService() {
		return use.tracker()
			.getService();
	}

	@Override
	public int size() {
		return use.tracker()
			.size();
	}

	@Override
	public int getTrackingCount() {
		return use.tracker()
			.getTrackingCount();
	}

	@Override
	public SortedMap<ServiceReference<T>, T> getTracked() {
		return use.tracker()
			.getTracked();
	}

	@Override
	public boolean isEmpty() {
		return use.tracker()
			.isEmpty();
	}

	private void assertRequired() {
		if (use.getCardinality() > 0) {
			throw new ExtensionConfigurationException("Can only resolve @ServiceParameter when service is required.");
		}
	}

	private void assertValidFieldCandidate(Field field) {
		assertRequired();
		if (isPrivate(field)) {
			throw new ExtensionConfigurationException("@ServiceParameter field [" + field + "] must not be private.");
		}
	}

	private void injectFields(ExtensionContext extensionContext, Object testInstance, Predicate<Field> predicate) {
		findAnnotatedFields(extensionContext.getRequiredTestClass(), ServiceUseParameter.class, predicate)
			.forEach(field -> {
				assertValidFieldCandidate(field);
				if (!field.getType()
					.equals(serviceType)) {
					return;
				}
				try {
					makeAccessible(field).set(testInstance, getService(field.getType(), extensionContext));
				} catch (Throwable t) {
					ExceptionUtils.throwAsUncheckedException(t);
				}
			});
	}

	private Object getService(Class<?> type, ExtensionContext extensionContext) {
		TrackServices<?> serviceHolder = extensionContext.getStore(NAMESPACE)
			.getOrComputeIfAbsent(use.getFilter()
				.toString(),
				key -> new CloseableTrackServices<>(use), CloseableTrackServices.class)
			.getHolder();

		return serviceHolder.tracker()
			.getService();
	}

	static class CloseableTrackServices<T> implements CloseableResource {

		private final TrackServices<T> use;

		public CloseableTrackServices(TrackServices<T> use) {
			this.use = use;
		}

		public TrackServices<T> getHolder() {
			return use;
		}

		@Override
		public void close() throws Exception {
			use.close();
		}

	}
}
