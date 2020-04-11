package org.osgi.test.common.service;

import java.util.Arrays;
import java.util.Objects;

import org.osgi.test.common.annotation.InjectService;

public class ServiceConfigurationKey {

	private final int		cardinality;
	private final String	filter;
	private final String[]	filterArguments;
	private final String	serviceName;
	private final long		timeout;

	public ServiceConfigurationKey(Class<?> serviceType, InjectService injectService) {
		this.serviceName = serviceType.getName();
		this.cardinality = injectService.cardinality();
		this.filter = injectService.filter();
		this.filterArguments = injectService.filterArguments();
		this.timeout = injectService.timeout();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(filterArguments);
		result = prime * result + Objects.hash(cardinality, filter, serviceName, timeout);
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
		ServiceConfigurationKey other = (ServiceConfigurationKey) obj;
		return cardinality == other.cardinality && Objects.equals(filter, other.filter)
			&& Arrays.equals(filterArguments, other.filterArguments) && Objects.equals(serviceName, other.serviceName)
			&& timeout == other.timeout;
	}

}
