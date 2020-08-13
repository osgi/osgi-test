/*
 * Copyright (c) OSGi Alliance (2020). All Rights Reserved.
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

package org.osgi.test.common.service;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.osgi.test.common.filter.Filters.format;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.SortedMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

public class ServiceConfiguration<S> implements AutoCloseable, ServiceAware<S> {

	private final int						cardinality;
	private final Filter					filter;
	private final Class<S>					serviceType;
	private final long						timeout;
	private volatile ServiceTracker<S, S>	tracker;

	public ServiceConfiguration(ServiceConfigurationKey<S> key) {
		this(key.serviceType, key.filter, key.filterArguments, key.cardinality, key.timeout);
	}

	public ServiceConfiguration(Class<S> serviceType, String format, String[] args, int cardinality, long timeout) {
		this.serviceType = requireNonNull(serviceType);

		Filter filter = format("(objectClass=%s)", serviceType.getName());
		format = String.format(requireNonNull(format), (Object[]) requireNonNull(args));
		if (!format.isEmpty()) {
			filter = format("(&%s%s)", filter.toString(), format);
		}
		this.filter = filter;

		if (cardinality < 0) {
			throw new IllegalArgumentException("cardinality must be zero or greater");
		}
		this.cardinality = cardinality;

		if (timeout < 0) {
			throw new IllegalArgumentException("timeout must be zero or greater");
		}
		this.timeout = timeout;
	}

	public ServiceConfiguration<S> init(BundleContext bundleContext) {
		CountDownLatch countDownLatch = new CountDownLatch(getCardinality());

		ServiceTracker<S, S> tracker = new ServiceTracker<>(bundleContext, getFilter(),
			new InnerCustomizer<>(bundleContext, countDownLatch, getCustomizer()));
		tracker.open();

		try {
			final Instant endTime = Instant.now()
				.plusMillis(getTimeout());
			if (!countDownLatch.await(getTimeout(), TimeUnit.MILLISECONDS)) {
				throw new AssertionError(
					getCardinality() - tracker.size() + "/" + getCardinality() + " services " + getFilter()
						+ " didn't arrive within "
						+ getTimeout() + "ms");
			}

			// CountDownLatch is fired when the last addingService() is called,
			// but this completes before the service is actually added to the
			// tracker. Need to poll-wait for a bit while the actual addition
			// completes (shouldn't be long).
			while (tracker.size() < cardinality) {
				if (Instant.now()
					.isAfter(endTime)) {

					throw new AssertionError(
						getCardinality() - tracker.size() + "/" + getCardinality() + " services " + getFilter()
							+ " didn't arrive within "
							+ getTimeout() + "ms");
				}
				Thread.sleep(10);
			}
		} catch (InterruptedException e) {
			throw new AssertionError(e);
		}
		this.tracker = tracker;
		return this;
	}

	@Override
	public void close() throws Exception {
		final ServiceTracker<S, S> tracker = this.tracker;
		if (tracker != null) {
			tracker.close();
		}
	}

	@Override
	public String toString() {
		return String.format(
			"ServiceConfiguration [Class=\"%s\", filter=\"%s\", cardinality=%s, timeout=%s]",
			getServiceType(), getFilter(), getCardinality(), getTimeout());
	}

	@Override
	public int getCardinality() {
		return cardinality;
	}

	/**
	 * Override by sub types in order to return a customizer used by the
	 * tracker. The default implementation returns {@code null}.
	 *
	 * @return customizer used by the tracker
	 */
	public ServiceTrackerCustomizer<S, S> getCustomizer() {
		return null;
	}

	@Override
	public Filter getFilter() {
		return filter;
	}

	@Override
	public S getService() {
		return tracker.getService();
	}

	@Override
	public S getService(ServiceReference<S> reference) {
		return tracker.getService(reference);
	}

	@Override
	public ServiceReference<S> getServiceReference() {
		return tracker.getServiceReference();
	}

	private <R> List<R> listOf(Function<ServiceReference<S>, R> mapper) {
		ServiceReference<S>[] serviceReferences = tracker.getServiceReferences();
		if (serviceReferences == null) {
			return new ArrayList<>();
		}
		return Arrays.stream(serviceReferences)
			.sorted()
			.map(mapper)
			.collect(toList());
	}

	@Override
	public List<ServiceReference<S>> getServiceReferences() {
		return listOf(Function.identity());
	}

	@Override
	public List<S> getServices() {
		return listOf(this::getService);
	}

	@Override
	public Class<S> getServiceType() {
		return serviceType;
	}

	@Override
	public long getTimeout() {
		return timeout;
	}

	@Override
	public int getTrackingCount() {
		return tracker.getTrackingCount();
	}

	@Override
	public SortedMap<ServiceReference<S>, S> getTracked() {
		return tracker.getTracked();
	}

	@Override
	public boolean isEmpty() {
		return tracker.isEmpty();
	}

	@Override
	public int size() {
		return tracker.size();
	}

	@Override
	public S waitForService(long timeout) throws InterruptedException {
		return tracker.waitForService(timeout);
	}

	private static class InnerCustomizer<S> implements ServiceTrackerCustomizer<S, S> {

		private final BundleContext								bundleContext;
		private final CountDownLatch							countDownLatch;
		private final Optional<ServiceTrackerCustomizer<S, S>>	delegate;

		InnerCustomizer(BundleContext bundleContext, CountDownLatch countDownLatch,
			ServiceTrackerCustomizer<S, S> delegate) {
			this.bundleContext = bundleContext;
			this.countDownLatch = countDownLatch;
			this.delegate = Optional.ofNullable(delegate);
		}

		@Override
		public S addingService(ServiceReference<S> reference) {
			final S service = delegate.map(c -> c.addingService(reference))
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
		public void modifiedService(ServiceReference<S> reference, S service) {
			delegate.ifPresent(c -> c.modifiedService(reference, service));
		}

		@Override
		public void removedService(ServiceReference<S> reference, S service) {
			delegate.map(c -> {
				c.removedService(reference, service);
				return true;
			})
				.orElseGet(() -> bundleContext.ungetService(reference));
		}

	}

}
