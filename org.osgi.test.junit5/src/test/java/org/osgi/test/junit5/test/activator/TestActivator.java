package org.osgi.test.junit5.test.activator;

import static org.osgi.framework.Constants.SERVICE_RANKING;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.test.common.dictionary.Dictionaries;
import org.osgi.test.junit5.test.types.Bar;

public class TestActivator implements BundleActivator {

	public static Bar			BAR	= new Bar() {};

	ServiceRegistration<Bar>	barReg;

	@Override
	public void start(BundleContext context) throws Exception {
		barReg = context.registerService(Bar.class, BAR, Dictionaries.dictionaryOf(SERVICE_RANKING, 1));
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		barReg.unregister();
	}

}
