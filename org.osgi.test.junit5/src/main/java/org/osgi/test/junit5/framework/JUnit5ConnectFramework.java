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
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

import org.junit.jupiter.api.extension.ExtensionContext.Store.CloseableResource;
import org.junit.platform.commons.PreconditionViolationException;
import org.junit.platform.commons.support.AnnotationSupport;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.connect.ConnectFrameworkFactory;
import org.osgi.framework.connect.FrameworkUtilHelper;
import org.osgi.framework.launch.Framework;
import org.osgi.test.common.annotation.WithBundle;
import org.osgi.test.common.annotation.WithExportedPackage;

class JUnit5ConnectFramework implements CloseableResource, FrameworkUtilHelper {

	private static final String			FILE_SCHEME		= "file";
	private static final String			JAR_SCHEME		= "jar";

	final Framework						framework;
	final FrameworkEvents				frameworkEvents	= new FrameworkEvents();
	private Class<?>					testClass;

	private final JUnit5ModuleConnector	connector		= new JUnit5ModuleConnector();

	public JUnit5ConnectFramework(Class<?> testClass, String uniqueId) throws IOException, BundleException {
		Map<String, List<WithBundle>> bundleMap = AnnotationSupport
			.findRepeatableAnnotations(testClass, WithBundle.class)
			.stream()
			.collect(Collectors.groupingBy(WithBundle::value));
		List<String> additionalPackages = AnnotationSupport
			.findRepeatableAnnotations(testClass, WithExportedPackage.class)
			.stream()
			.map(WithExportedPackage::value)
			.collect(Collectors.toList());
		this.testClass = testClass;
		List<JUnit5Module> modules = new ArrayList<>();
		ClassLoader classLoader = testClass.getClassLoader();
		TestProbeModule probeModule = new TestProbeModule("test-probe-" + uniqueId, classLoader, additionalPackages);
		modules.add(probeModule);
		Enumeration<URL> resources = classLoader.getResources(JarFile.MANIFEST_NAME);
		Set<String> missingBundles = new HashSet<>(bundleMap.keySet());
		while (resources.hasMoreElements()) {
			URL url = resources.nextElement();
			JUnit5Module module = getModule(url, classLoader);
			if (module != null) {
				module.setUseOSGiLoader(bundleMap.getOrDefault(module.getName(), Collections.emptyList())
					.stream()
					.anyMatch(WithBundle::isolated));
				if (bundleMap.containsKey(module.getName())) {
					missingBundles.remove(module.getName());
					modules.add(module);
				}
			}
		}
		if (!missingBundles.isEmpty()) {
			throw new PreconditionViolationException(
				"The follwoing bundles that where requested could not be found: " + missingBundles);
		}
		Map<String, String> p = new HashMap<>();
		p.put("osgi.framework.useSystemProperties", "false");
		p.put("osgi.parentClassloader", "fwk");
		p.put(Constants.FRAMEWORK_STORAGE,
			System.getProperty("java.io.tmpdir") + File.separator + "osgi-test-" + uniqueId);
		p.put(Constants.FRAMEWORK_BEGINNING_STARTLEVEL, "6");
		ServiceLoader<ConnectFrameworkFactory> sl = ServiceLoader.load(ConnectFrameworkFactory.class,
			getClass().getClassLoader());
		ConnectFrameworkFactory factory = sl.iterator()
			.next();

		framework = factory.newFramework(p, connector);
		JUnit5FrameworkUtilHelper.additionalHelpers.add(this);
		framework.init(frameworkEvents);
		framework.getBundleContext()
			.addFrameworkListener(frameworkEvents);
		connector.install(modules, framework.getBundleContext());
		JUnit5FrameworkUtilHelper.testProbeMap.put(testClass, connector.getBundle(probeModule));
		framework.start();
		for (JUnit5Module module : modules) {
			if (bundleMap.getOrDefault(module.getName(), Collections.emptyList())
				.stream()
				.anyMatch(WithBundle::start)) {
				framework.getBundleContext()
					.getBundle(module.getName())
					.start();
			}
		}
		framework.getBundleContext()
			.getBundle(probeModule.getName())
			.start();
	}

	Bundle[] getBundles() {
		return framework.getBundleContext()
			.getBundles();
	}

	@Override
	public void close() throws Throwable {
		framework.getBundleContext()
			.removeFrameworkListener(frameworkEvents);
		framework.stop();
		framework.waitForStop(TimeUnit.SECONDS.toMillis(30));
		JUnit5FrameworkUtilHelper.additionalHelpers.remove(this);
		JUnit5FrameworkUtilHelper.testProbeMap.remove(testClass);
	}

	private static final JUnit5Module getModule(URL url, ClassLoader classLoader) {
		try {
			Manifest manifest;
			try (InputStream stream = url.openStream()) {
				manifest = new Manifest(stream);
			}
			Attributes attributes = manifest.getMainAttributes();
			String value = attributes.getValue(Constants.BUNDLE_SYMBOLICNAME);
			if (value != null) {
				Map<String, String> headers = new LinkedHashMap<String, String>();
				for (Entry<Object, Object> entry : attributes.entrySet()) {
					headers.put(entry.getKey()
						.toString(),
						entry.getValue()
							.toString());
				}
				return new JUnit5Module(value.split(";")[0].trim(), Collections.unmodifiableMap(headers), classLoader,
					getFileLocation(url.toURI()));
			}
			return null;
		} catch (IOException e) {
			return null;
		} catch (URISyntaxException e) {
			return null;
		}
	}

	private static File getFileLocation(URI uri) {
		if (JAR_SCHEME.equalsIgnoreCase(uri.getScheme())) {
			String remainingPart = uri.toASCIIString()
				.substring(JAR_SCHEME.length() + 1)
				.split("!")[0];
			if (remainingPart.toLowerCase()
				.startsWith(FILE_SCHEME + ":")) {
				return getFileLocation(URI.create(remainingPart));
			}
		} else if (FILE_SCHEME.equalsIgnoreCase(uri.getScheme())) {
			File file = new File(uri);
			if (file.exists()) {
				if (file.getName()
					.equals("MANIFEST.MF")) {
					return file.getParentFile()
						.getParentFile();
				}
				return file;
			}
		}
		return null;
	}

	private static File getFileLocation(URL url) {
		if (url != null) {
			try {
				return getFileLocation(url.toURI());
			} catch (URISyntaxException e) {}
		}
		return null;
	}

	@Override
	public Optional<Bundle> getBundle(Class<?> classFromBundle) {
		ClassLoader classLoader = classFromBundle.getClassLoader();
		if (classLoader == null) {
			return Optional.empty();
		}
		ProtectionDomain protectionDomain = classFromBundle.getProtectionDomain();
		if (protectionDomain == null) {
			return Optional.empty();
		}
		CodeSource codeSource = protectionDomain.getCodeSource();
		if (codeSource == null) {
			return Optional.empty();
		}
		File location = getFileLocation(codeSource.getLocation());
		if (location == null) {
			return Optional.empty();
		}
		return connector.getBundle(location);
	}
}
