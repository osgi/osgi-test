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
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;

import org.osgi.framework.connect.ConnectContent;
import org.osgi.framework.connect.ConnectModule;

public class JUnit5Module implements ConnectContent, ConnectModule {

	private Map<String, String>	headers;
	private ClassLoader			classLoader;
	private File				location;
	private JarFile				jarFile;
	private String				name;
	private boolean				useOSGiLoader;

	public JUnit5Module(String name, Map<String, String> headers, ClassLoader classLoader, File location) {
		this.name = name;
		this.headers = headers;
		this.classLoader = classLoader;
		this.location = location;
	}

	@Override
	public ConnectContent getContent() throws IOException {
		return this;
	}

	@Override
	public Optional<Map<String, String>> getHeaders() {
		return Optional.of(headers);
	}

	@Override
	public Iterable<String> getEntries() throws IOException {
		if (jarFile != null) {
			return jarFile.stream()
				.map(JarEntry::getName)
				.collect(Collectors.toList());
		}
		if (location != null && location.isDirectory()) {
			Stream<String> stream = Files.walk(location.toPath())
				.map(p -> p.toString());
			List<String> collect = stream
				.collect(Collectors.toList());
			stream.close();
			return collect;
		}
		return Collections.emptyList();
	}

	@Override
	public Optional<ConnectEntry> getEntry(String path) {
		if (jarFile != null) {
			final ZipEntry entry = jarFile.getEntry(path);
			if (entry == null) {
				return Optional.empty();
			}
			return Optional.of(new ZipConnectEntry(jarFile, entry));
		}
		if (location != null && location.isDirectory()) {
			File file = new File(location, path);
			if (file.isFile()) {
				return Optional.of(new FileConnectEntry(file));
			}
		}
		return Optional.empty();
	}

	@Override
	public Optional<ClassLoader> getClassLoader() {
		if (useOSGiLoader) {
			return Optional.empty();
		}
		return Optional.of(classLoader);
	}

	@Override
	public void open() throws IOException {
		if (location != null && jarFile == null && location.isFile()) {
			jarFile = new JarFile(location);
		}
	}

	@Override
	public void close() throws IOException {
		if (jarFile != null) {
			jarFile.close();
		}
	}

	public String getName() {
		return name;
	}

	public File getLocation() {
		return location;
	}

	public void setUseOSGiLoader(boolean useOSGiLoader) {
		this.useOSGiLoader = useOSGiLoader;
	}

}
