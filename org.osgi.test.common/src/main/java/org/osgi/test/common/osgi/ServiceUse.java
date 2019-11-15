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

package org.osgi.test.common.osgi;

import static java.util.Objects.requireNonNull;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

public class ServiceUse<T> implements AutoCloseable {

	public static final long		DEFAULT_TIMEOUT	= 200;

	private final int				cardinality;
	private final Filter			filter;
	private final long				timeout;

	private ServiceTracker<T, T>	tracker;

	public ServiceUse(Filter filter, int cardinality, long timeout) {
		this.filter = requireNonNull(filter);

		if (cardinality < 0) {
			throw new IllegalArgumentException("cardinality must be zero or greater");
		}
		this.cardinality = cardinality;

		if (timeout < 0) {
			throw new IllegalArgumentException("timeout must be zero or greater");
		}
		this.timeout = timeout;
	}

	@Override
	public void close() throws Exception {
		if (tracker != null) {
			tracker.close();
		}
	}

	public void init(BundleContext bundleContext) {
		final long endTime = System.currentTimeMillis() + timeout;
		CountDownLatch countDownLatch = new CountDownLatch(getCardinality());

		tracker = new ServiceTracker<>(bundleContext, getFilter(),
			new InnerCustomizer(bundleContext, countDownLatch, getCustomizer()));
		tracker.open();

		try {
			if (!countDownLatch.await(getTimeout(), TimeUnit.MILLISECONDS)) {
				throw new AssertionError(
					getCardinality() + " services " + getFilter() + " didn't arrive within " + getTimeout() + "ms");
			}

			// CountDownLatch is fired when the last addingService() is called,
			// but this completes before the service is actually added to the
			// tracker. Need to poll-wait for a bit while the actual addition
			// completes (shouldn't be long).
			while (tracker.size() < cardinality) {
				if (System.currentTimeMillis() > endTime) {
					throw new AssertionError(
						getCardinality() + " services " + getFilter() + " didn't arrive within " + getTimeout() + "ms");
				}
				Thread.sleep(10);
			}
		} catch (InterruptedException e) {
			throw new AssertionError(e);
		}
	}

	public ServiceTracker<T, T> tracker() {
		return tracker;
	}

	/**
	 * Override by sub types in order to return a customizer used by the
	 * tracker. The default implementation returns {@code null}.
	 *
	 * @return customizer used by the tracker
	 */
	public ServiceTrackerCustomizer<T, T> getCustomizer() {
		return null;
	}

	public int getCardinality() {
		return cardinality;
	}

	public Filter getFilter() {
		return filter;
	}

	public long getTimeout() {
		return timeout;
	}

	private class InnerCustomizer implements ServiceTrackerCustomizer<T, T> {

		private BundleContext								bundleContext;
		private CountDownLatch								countDownLatch;
		private Optional<ServiceTrackerCustomizer<T, T>>	delegate;

		public InnerCustomizer(BundleContext bundleContext, CountDownLatch countDownLatch,
			ServiceTrackerCustomizer<T, T> delegate) {
			this.bundleContext = bundleContext;
			this.countDownLatch = countDownLatch;
			this.delegate = Optional.ofNullable(delegate);
		}

		@Override
		public T addingService(ServiceReference<T> reference) {
			final T service = delegate.map(c -> c.addingService(reference))
				.orElseGet(() -> bundleContext.getService(reference));

			try {
				return service;
			} finally {
				if (service != null) {
					countDownLatch.countDown();
				}
			}
		}

		@Override
		public void modifiedService(ServiceReference<T> reference, T service) {
			delegate.ifPresent(c -> c.modifiedService(reference, service));
		}

		@Override
		public void removedService(ServiceReference<T> reference, T service) {
			delegate.map(c -> {
				c.removedService(reference, service);
				return true;
			})
				.orElseGet(() -> bundleContext.ungetService(reference));
		}

	}

}
