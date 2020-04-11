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

package org.osgi.test.common.install;

import static org.osgi.test.common.exceptions.Exceptions.duck;

import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

public class InstallBundle {

	private final BundleContext bundleContext;

	public InstallBundle(BundleContext bundleContext) {
		this.bundleContext = bundleContext;
	}

	/**
	 * Install and start a bundle embedded within the current bundle.
	 * <p>
	 * Uses {@link Bundle#findEntries(String, String, boolean)} by splitting the
	 * {@code pathToEmbeddedJar} argument on the last backslash ({@code /}). The
	 * {@code recurse} argument is set to false.
	 * <p>
	 * When implemented against {@code CloseableBundleContext} bundles installed
	 * in this fashion are uninstalled automatically at the end of the test
	 * method.
	 *
	 * @param pathToEmbeddedJar
	 * @return installed and started bundle
	 * @throws AssertionError if no bundle is found
	 */
	public Bundle installBundle(String pathToEmbeddedJar) {
		return installBundle(pathToEmbeddedJar, true);
	}

	/**
	 * Install a bundle embedded within the current bundle.
	 * <p>
	 * Uses {@link Bundle#findEntries(String, String, boolean)} by splitting the
	 * {@code pathToEmbeddedJar} argument on the last backslash ({@code /}). The
	 * {@code recurse} argument is set to false.
	 * <p>
	 * When implemented against {@code CloseableBundleContext} bundles installed
	 * in this fashion are uninstalled automatically at the end of the test
	 * method.
	 *
	 * @param pathToEmbeddedJar
	 * @param startBundle if true, start the bundle
	 * @return installed bundle
	 * @throws AssertionError if no bundle is found
	 */
	public Bundle installBundle(String pathToEmbeddedJar, boolean startBundle) {
		int lastIndexOf = pathToEmbeddedJar.lastIndexOf('/');
		String[] parts = new String[] {
			"/", pathToEmbeddedJar
		};
		if (lastIndexOf != -1) {
			parts = new String[] {
				pathToEmbeddedJar.substring(0, lastIndexOf), pathToEmbeddedJar.substring(lastIndexOf + 1)
			};
		}
		Enumeration<URL> entries = bundleContext.getBundle()
			.findEntries(parts[0], parts[1], false);
		if (!entries.hasMoreElements())
			throw new AssertionError("No bundle entry " + pathToEmbeddedJar + " found in " + bundleContext.getBundle());
		try (InputStream is = entries.nextElement()
			.openStream()) {
			Bundle bundle = bundleContext.installBundle(pathToEmbeddedJar, is);
			if (startBundle) {
				bundle.start();
			}
			return bundle;
		} catch (Exception e) {
			throw duck(e);
		}
	}

}
