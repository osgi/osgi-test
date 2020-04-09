package org.osgi.test.junit5.service;

import static java.util.Objects.requireNonNull;
import static org.osgi.test.common.filter.Filters.format;
import static org.osgi.test.junit5.context.BundleContextExtension.getBundleContext;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Store.CloseableResource;
import org.osgi.framework.Filter;
import org.osgi.test.common.service.BaseServiceUse;
import org.osgi.test.common.tracking.TrackServices;
import org.osgi.test.junit5.context.BundleContextExtension;

class ServiceUseConfiguration<S> extends BaseServiceUse<S> implements CloseableResource {

	private final ExtensionContext	extensionContext;
	private final TrackServices<S>	trackServices;

	protected ServiceUseConfiguration(Class<S> serviceType, ExtensionContext extensionContext, String format,
		String[] args, int cardinality, long timeout) {
		super(serviceType);
		this.extensionContext = extensionContext;

		Filter filter = format("(objectClass=%s)", serviceType.getName());
		format = String.format(requireNonNull(format), requireNonNull(args));
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

	protected ServiceUseConfiguration<S> init() {
		trackServices.init(getBundleContext(extensionContext));
		return this;
	}

	@Override
	public void close() throws Exception {
		super.close();
		BundleContextExtension.cleanup(extensionContext);
	}

	@Override
	protected TrackServices<S> getTrackServices() {
		return trackServices;
	}

	@Override
	public String toString() {
		return String.format("ServiceUseExtension [Class=\"%s\", filter=\"%s\", cardinality=%s, timeout=%s]",
			getServiceType(), getFilter(), getCardinality(), getTimeout());
	}

}
