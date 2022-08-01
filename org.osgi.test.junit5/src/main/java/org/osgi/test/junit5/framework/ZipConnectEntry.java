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

import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.osgi.framework.connect.ConnectContent.ConnectEntry;

final class ZipConnectEntry implements ConnectEntry {

	private ZipEntry	entry;
	private JarFile		jarFile;

	public ZipConnectEntry(JarFile jarFile, ZipEntry entry) {
		this.jarFile = jarFile;
		this.entry = entry;
	}

	@Override
	public String getName() {
		return entry.getName();
	}

	@Override
	public long getContentLength() {
		return entry.getSize();
	}

	@Override
	public long getLastModified() {
		return entry.getTime();
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return jarFile.getInputStream(entry);
	}

}
