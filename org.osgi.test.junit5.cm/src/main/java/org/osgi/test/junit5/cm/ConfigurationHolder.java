package org.osgi.test.junit5.cm;

import java.util.Optional;

import org.osgi.service.cm.Configuration;

public class ConfigurationHolder {

	public ConfigurationHolder(ConfigurationCopy configuration, Optional<ConfigurationCopy> beforeConfiguration) {
		super();
		this.configuration = configuration;
		this.beforeConfiguration = beforeConfiguration;
	}

	public ConfigurationHolder(Configuration cmConfiguration, Optional<ConfigurationCopy> beforeConfiguration) {
		super();
		this.configuration = ConfigurationCopy.of(cmConfiguration);
		this.cmConfiguration = cmConfiguration;
		this.beforeConfiguration = beforeConfiguration;
	}

	public ConfigurationCopy getConfiguration() {
		return configuration;
	}

	public Configuration getCmConfiguration() {
		return cmConfiguration;
	}

	public Optional<ConfigurationCopy> getBeforeConfiguration() {
		return beforeConfiguration;
	}

	private ConfigurationCopy			configuration;
	private Configuration				cmConfiguration;
	private Optional<ConfigurationCopy>	beforeConfiguration;

}
