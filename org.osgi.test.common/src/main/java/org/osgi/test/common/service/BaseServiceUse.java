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

package org.osgi.test.common.service;

import static java.lang.reflect.Array.newInstance;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

import java.util.List;
import java.util.SortedMap;

import org.osgi.framework.Filter;
import org.osgi.framework.ServiceReference;
import org.osgi.test.common.tracking.TrackServices;
import org.osgi.test.common.tracking.TrackingConfig;
import org.osgi.util.tracker.ServiceTracker;

public abstract class BaseServiceUse<T> implements AutoCloseable, ServiceAware<T>, TrackingConfig {

	private final T[] empty;
	private final Class<T>	serviceType;

	@SuppressWarnings("unchecked")
	public BaseServiceUse(Class<T> serviceType) {
		this.serviceType = serviceType;
		empty = (T[]) newInstance(serviceType, 0);
	}

	protected abstract TrackServices<T> getTrackServices();

	@Override
	public void close() throws Exception {
		getTrackServices().close();
	}

	@Override
	public int getCardinality() {
		return getTrackServices().getCardinality();
	}

	@Override
	public Filter getFilter() {
		return getTrackServices().getFilter();
	}

	@Override
	public T getService() {
		return getTracker().getService();
	}

	@Override
	public T getService(ServiceReference<T> reference) {
		return getTracker().getService(reference);
	}

	@Override
	public ServiceReference<T> getServiceReference() {
		return getTracker().getServiceReference();
	}

	@Override
	public List<ServiceReference<T>> getServiceReferences() {
		ServiceReference<T>[] serviceReferences = getTracker().getServiceReferences();
		return (serviceReferences == null) ? emptyList() : asList(serviceReferences);
	}

	@Override
	public List<T> getServices() {
		return asList(getTracker().getServices(empty));
	}

	@Override
	public Class<T> getServiceType() {
		return serviceType;
	}

	@Override
	public long getTimeout() {
		return getTrackServices().getTimeout();
	}

	@Override
	public int getTrackingCount() {
		return getTracker().getTrackingCount();
	}

	@Override
	public SortedMap<ServiceReference<T>, T> getTracked() {
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
	public T waitForService(long timeout) throws InterruptedException {
		return getTracker().waitForService(timeout);
	}

	private ServiceTracker<T, T> getTracker() {
		return getTrackServices().tracker();
	}

}
