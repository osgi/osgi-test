package org.osgi.test.junit5.cm;

import java.util.Dictionary;

import org.osgi.service.cm.Configuration;

public class ConfigurationCopy {

	private String						pid;
	private Dictionary<String, Object>	properties;
	private String						factoryPid;
	private String						bundleLocation;
	private long						changeCount;

	private ConfigurationCopy() {

	}

	static ConfigurationCopy of(Configuration configuration) {

		ConfigurationCopy copy = new ConfigurationCopy();
		copy.pid = configuration.getPid();
		copy.properties = ConfigUtil.copy(configuration.getProperties());
		copy.factoryPid = configuration.getFactoryPid();
		copy.bundleLocation = configuration.getBundleLocation();
		copy.changeCount = configuration.getChangeCount();
		return copy;
	}

	public String getPid() {

		return pid;
	}
	public Dictionary<String, Object> getProperties() {
		return properties;
	}

	public String getFactoryPid() {
		return factoryPid;
	}

	public String getBundleLocation() {
		return bundleLocation;
	}

	public long getChangeCount() {
		return changeCount;
	}

}
