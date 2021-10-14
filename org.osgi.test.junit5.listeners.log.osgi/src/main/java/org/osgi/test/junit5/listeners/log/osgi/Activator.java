package org.osgi.test.junit5.listeners.log.osgi;

import static org.osgi.namespace.service.ServiceNamespace.SERVICE_NAMESPACE;

import org.junit.platform.launcher.TestExecutionListener;
import org.osgi.annotation.bundle.Header;
import org.osgi.annotation.bundle.Requirement;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.log.LoggerFactory;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

@Requirement(namespace = SERVICE_NAMESPACE, filter = "(objectClass=org.osgi.service.log.LoggerFactory)", effective = "active")
@Header(name = Constants.BUNDLE_ACTIVATOR, value = "${@class}")
public class Activator
	implements BundleActivator, ServiceTrackerCustomizer<LoggerFactory, ServiceRegistration<TestExecutionListener>> {

	BundleContext																context;

	ServiceTracker<LoggerFactory, ServiceRegistration<TestExecutionListener>>	loggerTracker;

	@Override
	public void start(BundleContext context) throws Exception {
		this.context = context;
		loggerTracker = new ServiceTracker<>(context, LoggerFactory.class, this);
		loggerTracker.open();
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		loggerTracker.close();
		loggerTracker = null;
	}

	@Override
	public ServiceRegistration<TestExecutionListener> addingService(ServiceReference<LoggerFactory> reference) {
		LoggerFactory loggerFactory = context.getService(reference);
		OSGiLogListener listener = new OSGiLogListener(loggerFactory);
		return context.registerService(TestExecutionListener.class, listener, null);
	}

	@Override
	public void modifiedService(ServiceReference<LoggerFactory> reference,
		ServiceRegistration<TestExecutionListener> listener) {}

	@Override
	public void removedService(ServiceReference<LoggerFactory> reference,
		ServiceRegistration<TestExecutionListener> listener) {
		listener.unregister();
	}
}
