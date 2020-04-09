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
import static org.osgi.test.common.filter.Filters.format;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.test.common.tracking.TrackServices;

public class ServiceConfiguration<S> extends BaseServiceUse<S> {

	private final TrackServices<S>	trackServices;

	public ServiceConfiguration(Class<S> serviceType, String format, String[] args, int cardinality, long timeout) {
		super(serviceType);

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
		super.close();
	}

	@Override
	protected TrackServices<S> getTrackServices() {
		return trackServices;
	}

	@Override
	public String toString() {
		return String.format(
			"ServiceConfiguration [Class=\"%s\", filter=\"%s\", cardinality=%s, timeout=%s]",
			getServiceType(), getFilter(), getCardinality(), getTimeout());
	}

}
