/*
 * Copyright (c) OSGi Alliance (2019). All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.osgi.test.junit5.framework;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.osgi.framework.BundleException;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;
import org.osgi.test.junit5.common.BaseExtention;

public class FrameworkExtension implements BaseExtention {

	public static final String		OSGi_FRAMEWORK_KEY	= "osgi.framework";
	public static final Namespace	NAMESPACE			= Namespace.create(FrameworkExtension.class);

	@Override
	public Namespace namespace() {
		// TODO Auto-generated method stub
		return NAMESPACE;
	}

	@Override
	public String storeKey() {
		// TODO Auto-generated method stub
		return OSGi_FRAMEWORK_KEY;
	}

	@Override
	public Class<? extends Annotation> injectionAnnotation() {
		return FrameworkParameter.class;
	}

	@Override
	public List<Class<?>> injectionClasses() {

		return List.of(Framework.class);

	}

	//
	// @Override
	// public void afterEach(ExtensionContext extensionContext) throws Exception
	// {
	//
	// Framework framework = extensionContext.getStore(NAMESPACE)
	// .remove(OSGi_FRAMEWORK_KEY, Framework.class);
	// if (framework != null) {
	// framework.stop();
	// }
	// }

	@Override
	public ExtendedCloseableResource create(Class<?> type, ExtensionContext extensionContext) {

		ExtendedCloseableResource extendedCloseableResource = null;
		if (FrameworkUtil.getBundle(this.getClass()) != null) {
			if (FrameworkUtil.getBundle(this.getClass()) instanceof Framework) {
				FrameworkUtil.getBundle(this.getClass())
					.adapt(Framework.class);
			} else {
				extendedCloseableResource = getRunningFW();
			}
		} else {

			extendedCloseableResource = create(extensionContext);

		}

		return extendedCloseableResource;

	}

	/**
	 * @param extensionContext
	 * @param extendedCloseableResource
	 * @return
	 */
	private ExtendedCloseableResource create(ExtensionContext extensionContext) {
		FrameworkFactory frameworkFactory = ServiceLoader.load(FrameworkFactory.class)
			.iterator()
			.next();

		String path = null;
		try {
			String name = null;
			if (extensionContext.getTestClass()
				.isPresent()) {
				name = extensionContext.getTestClass()
					.get()
					.getName();
			} else {
				name = "unknownTestCase";
			}

			if (extensionContext.getTestMethod()
				.isPresent()) {
				name += "_" + extensionContext.getTestMethod()
					.get()
					.getName();
			} else {
				name += "_nomethod";
			}

			path = Files.createTempDirectory(name)
				.toAbsolutePath()
				.toString();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		Map<String, String> config = new HashMap<String, String>();
		config.put("osgi.console", "");
		config.put("osgi.clean", "true");
		// config.put("osgi.noShutdown", "true");
		// config.put("eclipse.ignoreApp", "true");
		config.put("osgi.bundles.defaultStartLevel", "0");
		config.put("osgi.configuration.area", path);

		final Framework framework = frameworkFactory.newFramework(config);
		try {
			framework.start();
		} catch (BundleException e) {

			e.printStackTrace();
		}

		return new ExtendedCloseableResource() {

			@Override
			public void close() throws Throwable {
				framework.stop();
			}

			@Override
			public <V> V get(Class<V> requiredType) {

				if (requiredType == Framework.class) {
					return (V) framework;
				}

				return null;
			}
		};

	}

	/**
	 * @return
	 */
	private ExtendedCloseableResource getRunningFW() {
		ExtendedCloseableResource extendedCloseableResource;
		final Framework runningFramework = FrameworkUtil.getBundle(this.getClass())
			.getBundleContext()
			.getBundle(0)
			.adapt(Framework.class);
		extendedCloseableResource = new ExtendedCloseableResource() {

			@Override
			public void close() throws Throwable {
				runningFramework.stop();
			}

			@Override
			public <V> V get(Class<V> requiredType) {

				if (requiredType == Framework.class) {
					return (V) runningFramework;
				}

				return null;
			}
		};
		return extendedCloseableResource;
	}

}
