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
