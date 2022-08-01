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

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.osgi.framework.Constants;

public class TestProbeModule extends JUnit5Module {

	public TestProbeModule(String name, ClassLoader classLoader, Collection<String> additionalPackages) {
		super(name, generateManifest(name, additionalPackages), classLoader, null);
	}

	private static Map<String, String> generateManifest(String name, Collection<String> additionalPackages) {
		LinkedHashMap<String, String> headers = new LinkedHashMap<>();
		headers.put("Manifest-Version", "1.0");
		headers.put(Constants.BUNDLE_MANIFESTVERSION, "2");
		headers.put(Constants.BUNDLE_SYMBOLICNAME, name);
		headers.put(Constants.BUNDLE_VERSION, "1.0.0");
		if (!additionalPackages.isEmpty()) {
			headers.put(Constants.EXPORT_PACKAGE, additionalPackages.stream()
				.collect(Collectors.joining(",")));
		}
		return headers;
	}

}
