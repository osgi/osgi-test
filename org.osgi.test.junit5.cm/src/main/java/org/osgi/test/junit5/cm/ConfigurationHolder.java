package org.osgi.test.junit5.cm;

import java.util.Optional;

public class ConfigurationHolder {

	public ConfigurationHolder(ConfigurationCopy configuration, Optional<ConfigurationCopy> beforeConfiguration) {
		super();
		this.configuration = configuration;
		this.beforeConfiguration = beforeConfiguration;
	}

	public ConfigurationCopy getConfiguration() {
		return configuration;
	}

	public Optional<ConfigurationCopy> getBeforeConfiguration() {
		return beforeConfiguration;
	}

	private ConfigurationCopy			configuration;
	private Optional<ConfigurationCopy>	beforeConfiguration;

}
