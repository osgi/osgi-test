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
package org.osgi.test.junit5.framework;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.connect.ConnectModule;
import org.osgi.framework.connect.ModuleConnector;

class JUnit5ModuleConnector implements ModuleConnector {

	private Map<String, JUnit5Module> moduleMap = new HashMap<>();
	private Map<JUnit5Module, Bundle>	bundleMap	= new HashMap<>();

	@Override
	public void initialize(File storage, Map<String, String> configuration) {

	}

	@Override
	public Optional<ConnectModule> connect(String location) throws BundleException {
		return Optional.ofNullable(moduleMap.get(location));
	}

	@Override
	public Optional<BundleActivator> newBundleActivator() {
		return Optional.empty();
	}

	public void install(List<JUnit5Module> modules, BundleContext bundleContext) throws BundleException {
		for (JUnit5Module module : modules) {
			moduleMap.put(module.getName(), module);
			bundleMap.put(module, bundleContext.installBundle(module.getName()));
		}
	}

	Bundle getBundle(JUnit5Module module) {
		return bundleMap.get(module);
	}

	public Optional<Bundle> getBundle(File location) {
		return bundleMap.entrySet()
			.stream()
			.filter(entry -> location.equals(entry.getKey()
				.getLocation()))
			.findAny()
			.map(entry -> entry.getValue());
	}

}
