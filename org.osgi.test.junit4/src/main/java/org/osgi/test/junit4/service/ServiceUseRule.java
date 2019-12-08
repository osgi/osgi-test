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

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.osgi.framework.Filter;
import org.osgi.test.common.service.BaseServiceUse;
import org.osgi.test.common.tracking.TrackServices;
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
public class ServiceUseRule<T> extends BaseServiceUse<T>
	implements TestRule {

	public static class Builder<T> {

		private final Class<T>			serviceType;
		private final BundleContextRule	bundleContextRule;
		private Filter					filter;
		private int						cardinality	= 1;
		private long					timeout		= TrackServices.DEFAULT_TIMEOUT;

		/**
		 * @param serviceType of the service
		 */
		public Builder(Class<T> serviceType) {
			this(serviceType, new BundleContextRule());
		}

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

		public ServiceUseRule<T> build() {
			return new ServiceUseRule<>(serviceType, bundleContextRule, filter, cardinality, timeout);
		}

	}

	private final BundleContextRule	bundleContextRule;
	private final TrackServices<T>	trackServices;

	@SuppressWarnings("unchecked")
	protected ServiceUseRule(Class<T> serviceType, BundleContextRule bundleContextRule, Filter filter, int cardinality,
		long timeout) {
		super(serviceType);
		this.bundleContextRule = bundleContextRule;
		this.trackServices = new TrackServices<>(filter, cardinality, timeout);
	}

	void init(Class<?> testClass) {
		bundleContextRule.init(testClass);
		trackServices.init(bundleContextRule.getBundleContext());
	}

	@Override
	public void close() throws Exception {
		trackServices.close();
		bundleContextRule.close();
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
	protected TrackServices<T> getTrackServices() {
		return trackServices;
	}

}
