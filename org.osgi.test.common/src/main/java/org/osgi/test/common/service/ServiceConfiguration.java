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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.SortedMap;
import java.util.function.Function;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.ServiceReference;
import org.osgi.test.common.tracking.TrackServices;
import org.osgi.util.tracker.ServiceTracker;

public class ServiceConfiguration<S> implements AutoCloseable, ServiceAware<S> {

	private final Class<S>			serviceType;
	private final TrackServices<S>	trackServices;

	public ServiceConfiguration(Class<S> serviceType, String format, String[] args, int cardinality, long timeout) {
		this.serviceType = requireNonNull(serviceType);

		Filter filter = format("(objectClass=%s)", serviceType.getName());
		format = String.format(requireNonNull(format), (Object[]) requireNonNull(args));
		if (!format.isEmpty()) {
			filter = format("(&%s%s)", filter.toString(), format);
		}
		if (cardinality < 0) {
			throw new IllegalArgumentException("cardinality must be zero or greater");
		}
		if (timeout < 0) {
			throw new IllegalArgumentException("timeout must be zero or greater");
		}

		trackServices = new TrackServices<>(filter, cardinality, timeout);
	}

	public ServiceConfiguration<S> init(BundleContext bundleContext) {
		trackServices.init(bundleContext);
		return this;
	}

	@Override
	public void close() throws Exception {
		trackServices.close();
	}

	@Override
	public String toString() {
		return String.format(
			"ServiceConfiguration [Class=\"%s\", filter=\"%s\", cardinality=%s, timeout=%s]",
			getServiceType(), getFilter(), getCardinality(), getTimeout());
	}

	@Override
	public int getCardinality() {
		return trackServices.getCardinality();
	}

	@Override
	public Filter getFilter() {
		return trackServices.getFilter();
	}

	@Override
	public S getService() {
		return getTracker().getService();
	}

	@Override
	public S getService(ServiceReference<S> reference) {
		return getTracker().getService(reference);
	}

	@Override
	public ServiceReference<S> getServiceReference() {
		return getTracker().getServiceReference();
	}

	private <R> List<R> listOf(Function<ServiceReference<S>, R> mapper) {
		ServiceReference<S>[] serviceReferences = getTracker().getServiceReferences();
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
		return trackServices.getTimeout();
	}

	@Override
	public int getTrackingCount() {
		return getTracker().getTrackingCount();
	}

	@Override
	public SortedMap<ServiceReference<S>, S> getTracked() {
		return getTracker().getTracked();
	}

	@Override
	public boolean isEmpty() {
		return getTracker().isEmpty();
	}

	@Override
	public int size() {
		return getTracker().size();
	}

	@Override
	public S waitForService(long timeout) throws InterruptedException {
		return getTracker().waitForService(timeout);
	}

	private ServiceTracker<S, S> getTracker() {
		return trackServices.tracker();
	}

}
