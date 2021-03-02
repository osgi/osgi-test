/*******************************************************************************
 * Copyright (c) Contributors to the Eclipse Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 *******************************************************************************/

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
