/*
 * Copyright (c) OSGi Alliance (2019, 2021). All Rights Reserved.
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

import java.util.Arrays;
import java.util.Objects;

public class ServiceConfigurationKey<S> {

	final Class<S>	serviceType;
	final String	filter;
	final String[]	filterArguments;
	final int		cardinality;
	final long		timeout;

	public ServiceConfigurationKey(Class<S> serviceType, String filter, String[] filterArguments, int cardinality,
		long timeout) {
		this.serviceType = requireNonNull(serviceType);
		this.filter = filter;
		this.filterArguments = filterArguments;
		this.cardinality = cardinality;
		this.timeout = timeout;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(filterArguments);
		result = prime * result + Objects.hash(cardinality, filter, serviceType.getName(), timeout);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof ServiceConfigurationKey)) {
			return false;
		}
		ServiceConfigurationKey<?> other = (ServiceConfigurationKey<?>) obj;
		return cardinality == other.cardinality && Objects.equals(filter, other.filter)
			&& Arrays.equals(filterArguments, other.filterArguments)
			&& Objects.equals(serviceType.getName(), other.serviceType.getName())
			&& timeout == other.timeout;
	}
}
