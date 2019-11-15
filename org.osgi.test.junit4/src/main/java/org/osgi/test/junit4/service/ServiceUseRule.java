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
import static java.util.stream.Collectors.toList;
import static org.osgi.test.common.filter.Filters.format;

import java.util.Arrays;
import java.util.List;
import java.util.SortedMap;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.osgi.framework.Filter;
import org.osgi.framework.ServiceReference;
import org.osgi.test.common.service.ServiceUse;
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
public class ServiceUseRule<T> implements AutoCloseable, TestRule {

	public static class Builder<T> {

		private final Class<T>			serviceType;
		private final BundleContextRule	bundleContextRule;
		private Filter					filter;
		private int						cardinality	= 1;
		private long					timeout		= ServiceUse.DEFAULT_TIMEOUT;

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
		 * the specified timeout.
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

	private final ServiceUse<T> use;
	private final Class<T>			serviceType;
	private final BundleContextRule	bundleContextRule;

	protected ServiceUseRule(Class<T> serviceType, BundleContextRule bundleContextRule, Filter filter, int cardinality,
		long timeout) {
		this.serviceType = serviceType;
		this.bundleContextRule = bundleContextRule;
		this.use = new ServiceUse<>(filter, cardinality, timeout);
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

	public int getCardinality() {
		return use.getCardinality();
	}

	public Filter getFilter() {
		return use.getFilter();
	}

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

	public T waitForService(long timeout) throws InterruptedException {
		return use.tracker()
			.waitForService(timeout);
	}

	public ServiceReference<T>[] getServiceReferences() {
		return use.tracker()
			.getServiceReferences();
	}

	public ServiceReference<T> getServiceReference() {
		return use.tracker()
			.getServiceReference();
	}

	public T getService(ServiceReference<T> reference) {
		return use.tracker()
			.getService(reference);
	}

	public List<T> getServices() {
		Object[] services = use.tracker()
			.getServices();
		return Arrays.stream(services != null ? services : new Object[0])
			.map(serviceType::cast)
			.collect(toList());
	}

	public T getService() {
		return use.tracker()
			.getService();
	}

	public int size() {
		return use.tracker()
			.size();
	}

	public int getTrackingCount() {
		return use.tracker()
			.getTrackingCount();
	}

	public SortedMap<ServiceReference<T>, T> getTracked() {
		return use.tracker()
			.getTracked();
	}

	public boolean isEmpty() {
		return use.tracker()
			.isEmpty();
	}

}
