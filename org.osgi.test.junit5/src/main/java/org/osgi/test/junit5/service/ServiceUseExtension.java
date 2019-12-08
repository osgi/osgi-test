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

import java.lang.reflect.Field;
import java.util.function.Predicate;

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
import org.osgi.test.common.service.BaseServiceUse;
import org.osgi.test.common.tracking.TrackServices;
import org.osgi.test.junit5.context.BundleContextExtension;

public class ServiceUseExtension<T> extends BaseServiceUse<T>
	implements BeforeEachCallback, ParameterResolver {

	public static class Builder<T> {

		private final Class<T>	serviceType;
		private final BundleContextExtension	contextExtension;
		private Filter			filter;
		private int				cardinality	= 1;
		private long			timeout		= TrackServices.DEFAULT_TIMEOUT;

		/**
		 * Create with default BundleContextExtension.
		 *
		 * @param serviceType of the service
		 */
		public Builder(Class<T> serviceType) {
			this(serviceType, new BundleContextExtension());
		}

		/**
		 * Create with available BundleContextExtension.
		 *
		 * @param serviceType of the service
		 * @param contextExtension the BundleContextExtension if available
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
		 * Filter string used to target more specific services using the
		 * {@code String.format} pattern.
		 *
		 * @param format a format string
		 * @param args arguments to the format string
		 */
		public Builder<T> filter(String format, Object... args) {
			this.filter = format("(&%s%s)", this.filter.toString(), String.format(requireNonNull(format), args));
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
	private final Filter					filter;
	private final int						cardinality;
	private final long						timeout;
	private volatile TrackServices<T>		trackServices;

	@SuppressWarnings("unchecked")
	protected ServiceUseExtension(Class<T> serviceType, BundleContextExtension contextExtension, Filter filter,
		int cardinality, long timeout) {
		super(serviceType);
		this.contextExtension = contextExtension;
		this.filter = filter;
		this.cardinality = cardinality;
		this.timeout = timeout;
	}

	@Override
	public void beforeEach(ExtensionContext extensionContext) throws Exception {
		contextExtension.beforeEach(extensionContext);
		// set this in order to be able to honour the BaseServiceUse contract
		trackServices = getTrackServices(extensionContext);
		injectFields(trackServices, extensionContext, extensionContext.getRequiredTestInstance(),
			ReflectionUtils::isNotStatic);
	}

	@Override
	public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
		throws ParameterResolutionException {

		assertRequired();
		return getTrackServices(extensionContext).tracker()
			.getService();
	}

	@Override
	public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
		throws ParameterResolutionException {
		boolean annotated = parameterContext.isAnnotated(ServiceUseParameter.class);
		if (annotated && parameterContext.getParameter()
			.getType()
			.equals(getServiceType())) {

			return true;
		}
		return false;
	}

	private void assertRequired() {
		if (cardinality < 1) {
			throw new ExtensionConfigurationException(
				"Can only resolve @ServiceParameter when cardinality is greater than 0.");
		}
	}

	private void assertValidFieldCandidate(Field field, TrackServices<T> trackServices) {
		assertRequired();
		if (isPrivate(field)) {
			throw new ExtensionConfigurationException("@ServiceParameter field [" + field + "] must not be private.");
		}
	}

	private void injectFields(TrackServices<T> ts, ExtensionContext extensionContext, Object testInstance,
		Predicate<Field> predicate) {
		findAnnotatedFields(extensionContext.getRequiredTestClass(), ServiceUseParameter.class, predicate)
			.forEach(field -> {
				if (!field.getType()
					.equals(getServiceType())) {
					return;
				}
				assertValidFieldCandidate(field, ts);
				try {
					makeAccessible(field).set(testInstance, ts.tracker()
						.getService());
				} catch (Throwable t) {
					ExceptionUtils.throwAsUncheckedException(t);
				}
			});
	}

	private TrackServices<T> getTrackServices(ExtensionContext extensionContext) {
		CloseableTrackServices<T> closeableTrackServices = extensionContext.getStore(NAMESPACE)
			.getOrComputeIfAbsent(filter.toString(), k -> {
				TrackServices<T> ts = new TrackServices<>(filter, cardinality, timeout);
				ts.init(contextExtension.getBundleContext(extensionContext));
				return new CloseableTrackServices<T>(ts);
			}, CloseableTrackServices.class);
		return closeableTrackServices.get();
	}

	@Override
	protected TrackServices<T> getTrackServices() {
		return trackServices;
	}

	public static class CloseableTrackServices<T> implements CloseableResource {

		private final TrackServices<T> trackServices;

		CloseableTrackServices(TrackServices<T> trackServices) {
			this.trackServices = trackServices;
		}

		@Override
		public void close() throws Exception {
			trackServices.close();
		}

		public TrackServices<T> get() {
			return trackServices;
		}

	}

}
