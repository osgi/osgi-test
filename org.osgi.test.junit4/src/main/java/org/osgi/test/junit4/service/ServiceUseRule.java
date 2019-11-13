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

package org.osgi.test.junit4.service;

import static java.util.Objects.requireNonNull;
import static org.osgi.test.common.filter.Filters.format;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.osgi.framework.Filter;
import org.osgi.framework.ServiceReference;
import org.osgi.test.common.service.ServiceAware;
import org.osgi.test.common.tracking.TrackServices;
import org.osgi.test.common.tracking.TrackingConfig;
import org.osgi.test.junit4.context.BundleContextRule;

/**
 * A JUnit 4 Rule to depend on OSGi services.
 * <p>
 * Example: <br>
 *
 * <pre>
 * {@literal @}Rule
 * public ServiceUseRule&lt;Foo> foos = new ServiceUseRule.Builder&lt;>(Foo.class)
 *     .filter("(service.description=Acme Inc.)")
 *     .requiredWithin(300)
 *     .build();
 * </pre>
 *
 * @param <T> the service type
 */
public class ServiceUseRule<T> implements AutoCloseable, ServiceAware<T>, TestRule, TrackingConfig {

	public static class Builder<T> {

		private final Class<T>			serviceType;
		private final BundleContextRule	bundleContextRule;
		private Filter					filter;
		private int						cardinality	= 1;
		private long					timeout		= TrackServices.DEFAULT_TIMEOUT;

		/**
		 * @param serviceType of the service
		 */
		public Builder(Class<T> serviceType, BundleContextRule bundleContextRule) {
			this.serviceType = requireNonNull(serviceType);
			this.bundleContextRule = requireNonNull(bundleContextRule);
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

		public ServiceUseRule<T> build() {
			return new ServiceUseRule<>(serviceType, bundleContextRule, filter, cardinality, timeout);
		}

	}

	private final BundleContextRule	bundleContextRule;
	private final T[]				empty;
	private final TrackServices<T>	use;

	@SuppressWarnings("unchecked")
	protected ServiceUseRule(Class<T> serviceType, BundleContextRule bundleContextRule, Filter filter, int cardinality,
		long timeout) {
		this.bundleContextRule = bundleContextRule;
		this.empty = (T[]) Array.newInstance(serviceType, 0);
		this.use = new TrackServices<>(filter, cardinality, timeout);
	}

	void init(Class<?> testClass) {
		bundleContextRule.init(testClass);
		use.init(bundleContextRule.getBundleContext());
	}

	@Override
	public void close() throws Exception {
		use.close();
		bundleContextRule.close();
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
	public Statement apply(Statement statement, Description description) {
		return new Statement() {
			@Override
			public void evaluate() throws Throwable {
				init(description.getTestClass());
				try {
					statement.evaluate();
				} finally {
					close();
				}
			}
		};
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

}
