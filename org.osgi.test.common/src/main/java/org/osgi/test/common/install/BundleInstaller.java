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

package org.osgi.test.common.install;

import static org.osgi.test.common.exceptions.Exceptions.duck;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Version;

public class BundleInstaller {

	/**
	 * EmbeddedLocation describes a Location of a File inside a Bundle.
	 */
	public static class EmbeddedLocation {

		private static Optional<Bundle> findBundle(BundleContext bc, String bundleSymbolicName, Version bundleVersion) {

			return Stream.of(bc.getBundles())
				.filter(b -> b.getSymbolicName()
					.equals(bundleSymbolicName))
				.filter(b -> b.getVersion()
					.equals(bundleVersion))
				.findAny();
		}

		/**
		 * Creates an EmbeddedLocation by the given parameters. Existence of
		 * File is tested at creation time.
		 *
		 * @param bundleContext - if null it searches the file in the root of
		 *            the BundelContexts bundle.
		 * @param oBundleSymbolicName s- ymbolicName of the bundle that embedded
		 *            the file must not be null
		 * @param oBundleVersion - version of the bundle that embedded the file
		 *            -
		 * @param oPath - the optional path in the jar
		 * @param file - the filename must not be null
		 * @return EmbeddedLocation
		 */
		public static EmbeddedLocation of(BundleContext bundleContext, Optional<String> oBundleSymbolicName,
			Optional<String> oBundleVersion, Optional<String> oPath, String file) {

			String bundleSymbolicName;
			Version bundleVersion;

			if (oBundleSymbolicName.isPresent()) {
				bundleSymbolicName = oBundleSymbolicName.get();
				bundleVersion = oBundleVersion.map(v -> Version.parseVersion(v))
					.orElseGet(() -> {
						return Stream.of(bundleContext.getBundles())
							.filter(b -> b.getSymbolicName()
								.equals(bundleSymbolicName))
							.findFirst()
							.orElseThrow(() -> {
								return new IllegalArgumentException(
									String.format("Unknown Bundle with SymbolicName: %s", bundleSymbolicName));
							})
							.getVersion();
					});

			} else {
				Bundle bundle = bundleContext.getBundle();
				bundleSymbolicName = bundle.getSymbolicName();
				bundleVersion = oBundleVersion.map(v -> Version.parseVersion(v))
					.orElse(bundle.getVersion());
			}

			findBundle(bundleContext, bundleSymbolicName, bundleVersion).orElseThrow(() -> {
				return new IllegalArgumentException(String.format(
					"Unknown Bundle with SymbolicName '%s' and Version: '%s'", bundleSymbolicName, bundleVersion));
			});
			return new EmbeddedLocation(bundleSymbolicName, bundleVersion, oPath.orElse("/"), file);
		}

		/**
		 * Creates an EmbeddedLocation by the spec. Existence of File is tested
		 * at creation time.
		 *
		 * @param bundleContext
		 * @param spec 'bundle.symbolic.name:1.2.3:/path/file.jar'
		 *            <p>
		 *            In minimum 'file.jar' must be used. Then it searches the
		 *            file in the root of the BundelContexts bundle.
		 * @return EmbeddedLocation
		 */
		public static EmbeddedLocation of(BundleContext bundleContext, String spec) throws IllegalArgumentException {

			Optional<String> oBundleSymbolicName;
			Optional<String> oBundleVersion;
			String[] parts = spec.split(":");
			String fullPath;
			if (parts.length == 1) {

				oBundleSymbolicName = Optional.empty();
				oBundleVersion = Optional.empty();
				fullPath = parts[0];
			} else if (parts.length == 2) {
				oBundleSymbolicName = Optional.of(parts[0]);
				oBundleVersion = Optional.empty();
				fullPath = parts[1];
			} else if (parts.length == 3) {
				oBundleSymbolicName = Optional.of(parts[0]);
				oBundleVersion = Optional.of(parts[1]);
				fullPath = parts[2];
			} else {
				throw new IllegalArgumentException("Wrong pattern :" + spec);
			}

			int indexOfFileBegin = fullPath.lastIndexOf('/') + 1;

			String file = fullPath.substring(indexOfFileBegin);

			if (file.isEmpty()) {
				throw new IllegalArgumentException(String.format("could not find a filename : ", spec));

			}
			Optional<String> oPath = Optional.of(fullPath.substring(0, indexOfFileBegin));

			return EmbeddedLocation.of(bundleContext, oBundleSymbolicName, oBundleVersion, oPath, file);

		}

		/**
		 * Creates an EmbeddedLocation by the pure parameters. existence of File
		 * not tested at creation time.
		 *
		 * @param bundleSymbolicName - symbolicName of the bundle that embedded
		 *            the file must not be null
		 * @param bundleVersion - version of the bundle that embedded the file -
		 *            must not be null
		 * @param path - the path in the jar - must not be null
		 * @param file - the filename must not be null
		 * @return EmbeddedLocation
		 */
		public static EmbeddedLocation of(String bundleSymbolicName, Version bundleVersion, String path, String file) {
			return new EmbeddedLocation(bundleSymbolicName, bundleVersion, path, file);
		}

		private final String	bundleSymbolicName;
		private final Version	bundleVersion;
		private final String	file;
		private final String	path;

		private EmbeddedLocation(String bundleSymbolicName, Version bundleVersion, String path, String file) {

			Objects.requireNonNull(bundleSymbolicName, "bundleSymbolicName must not be null");
			Objects.requireNonNull(bundleVersion, "bundleVersion must not be null");
			Objects.requireNonNull(path, "path must not be null");
			Objects.requireNonNull(file, "file must not be null");

			this.bundleSymbolicName = bundleSymbolicName;
			this.bundleVersion = bundleVersion;

			path = path.startsWith("/") ? path.substring(1) : path;
			path = path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
			this.path = path;

			if (file.startsWith("/")) {
				file = file.substring(1);
			}
			this.file = file;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof EmbeddedLocation) {
				EmbeddedLocation other = (EmbeddedLocation) obj;
				return Objects.equals(bundleSymbolicName, other.bundleSymbolicName)
					&& Objects.equals(bundleVersion, other.bundleVersion) && Objects.equals(file, other.file)
					&& Objects.equals(path, other.path);
			}
			return false;
		}

		@Override
		public int hashCode() {
			return Objects.hash(bundleSymbolicName, bundleVersion, file, path);
		}

		public InputStream openStream(BundleContext bc) throws IOException, IllegalArgumentException {

			Bundle bundle = Stream.of(bc.getBundles())
				.filter(b -> b.getSymbolicName()
					.equals(bundleSymbolicName))
				.filter(b -> b.getVersion()
					.equals(bundleVersion))
				.findFirst()
				.orElseThrow(() -> new IllegalArgumentException(
					String.format("Bundle %s:%s does not exist in framework", bundleSymbolicName, bundleVersion)));

			Enumeration<URL> entries = bundle.findEntries(path, file, false);
			URL jarEmbeddedFileUrl = Collections.list(entries)
				.stream()
				.findFirst()
				.orElseThrow(() -> new IllegalArgumentException(String.format("File %s in Path %s not in Bundle %s:%s ",
					file, path, bundle.getSymbolicName(), bundle.getVersion())));

			return jarEmbeddedFileUrl.openStream();
		}

		@Override
		public String toString() {

			return String.format("bundle:" + bundleSymbolicName + ":" + bundleVersion + ":" + path + "/" + file);
		}
	}

	private final BundleContext bundleContext;

	public BundleInstaller(BundleContext bundleContext) {
		this.bundleContext = bundleContext;
	}

	/**
	 * @return The bundle context that this instance is attached to.
	 */
	public BundleContext getBundleContext() {
		return bundleContext;
	}

	/**
	 * Install a bundle from a given EmbeddedLocation.
	 * <p>
	 * When implemented against {@code CloseableBundleContext} bundles installed
	 * in this fashion are uninstalled automatically at the end of the test
	 * method.
	 *
	 * @param location of the jar resource.
	 * @param startBundle if true, start the bundle
	 * @return installed bundle
	 * @throws AssertionError if no bundle is found
	 */
	public Bundle installBundle(EmbeddedLocation location, boolean startBundle) {

		try (InputStream is = location.openStream(bundleContext)) {
			Bundle bundle = bundleContext.installBundle(location.toString(), is);
			if (startBundle) {
				bundle.start();
			}
			return bundle;
		} catch (Exception e) {
			throw duck(e);
		}
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
	 * @param pathToEmbeddedJar The entry path to the jar resource.
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
	 * @param pathToEmbeddedJar The entry path to the jar resource.
	 * @param startBundle if true, start the bundle
	 * @return installed bundle
	 * @throws AssertionError if no bundle is found
	 */
	public Bundle installBundle(String pathToEmbeddedJar, boolean startBundle) {
		int lastIndexOf = pathToEmbeddedJar.lastIndexOf('/');
		String[] parts = (lastIndexOf == -1) ? new String[] {
			"/", pathToEmbeddedJar
		} : new String[] {
			pathToEmbeddedJar.substring(0, lastIndexOf), pathToEmbeddedJar.substring(lastIndexOf + 1)
		};

		return installBundle(EmbeddedLocation.of(bundleContext, pathToEmbeddedJar), startBundle);
	}

	/**
	 * Install a bundle from a given URL.
	 * <p>
	 * When implemented against {@code CloseableBundleContext} bundles installed
	 * in this fashion are uninstalled automatically at the end of the test
	 * method.
	 *
	 * @param url to the jar resource.
	 * @param startBundle if true, start the bundle
	 * @return installed bundle
	 * @throws AssertionError if no bundle is found
	 */
	public Bundle installBundle(URL url, boolean startBundle) {

		try (InputStream is = url.openStream()) {
			Bundle bundle = bundleContext.installBundle(url.toString(), is);
			if (startBundle) {
				bundle.start();
			}
			return bundle;
		} catch (Exception e) {
			throw duck(e);
		}
	}
}
