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

package org.osgi.test.junit5.context;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.osgi.framework.Bundle;
import org.osgi.test.common.annotation.InjectInstalledBundle;
import org.osgi.test.common.inject.TargetType;
import org.osgi.test.common.install.BundleInstaller;
import org.osgi.test.junit5.inject.InjectingExtension;

/**
 * This Extension loads a {@link Bundle} from a given location and installs the
 * Bundle using the {@link InjectInstalledBundle}.
 *
 * <pre>
 * &#64;ExtendWith(InstalledBundleExtension.class)
 * ...
 * &#64;InjectInstalledBundle
 * Bundle installedBundle;
 * </pre>
 */
public class InstalledBundleExtension extends InjectingExtension<InjectInstalledBundle> {

	public InstalledBundleExtension() {
		super(InjectInstalledBundle.class, Bundle.class);
	}

	public static Bundle installedBundleOf(InjectInstalledBundle injectBundle, ExtensionContext extensionContext) {
		try {
			BundleInstaller ib = BundleInstallerExtension.getBundleInstaller(extensionContext);

			String spec = injectBundle.value();
			if (prefixMatch(spec, "http:", "https:", "file:")) {
				URL url = new URI(spec).parseServerAuthority()
					.toURL();
				return ib.installBundle(url, injectBundle.start());
			} else {
				return ib.installBundle(BundleInstaller.EmbeddedLocation.of(ib.getBundleContext(), spec),
					injectBundle.start());
			}
		} catch (URISyntaxException | MalformedURLException e) {
			throw new ExtensionConfigurationException(
				String.format("Could not parse URL from given String %s.", injectBundle.value()), e);
		}
	}

	private static boolean prefixMatch(String target, String... prefixes) {
		for (String prefix : prefixes) {
			if (target.regionMatches(true, 0, prefix, 0, prefix.length())) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected Object resolveValue(TargetType targetType, InjectInstalledBundle injectBundle,
		ExtensionContext extensionContext) throws ParameterResolutionException {
		try {
			return installedBundleOf(injectBundle, extensionContext);
		} catch (Exception e) {
			throw new ParameterResolutionException(String.format("@%s [%s]: couldn't resolve bundle parameter [%s]: %s",
				annotation().getSimpleName(), targetType.getName(), injectBundle.value(), e));
		}
	}
}
