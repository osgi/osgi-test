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

import static org.osgi.test.common.inject.FieldInjector.assertFieldIsOfType;
import static org.osgi.test.common.inject.FieldInjector.assertParameterIsOfType;
import static org.osgi.test.common.inject.FieldInjector.setField;

import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.net.MalformedURLException;
import java.net.URL;

import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.test.common.annotation.InjectInstalledBundle;
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
		super(InjectInstalledBundle.class);
	}

	/**
	 * Resolve {@link Parameter} annotated with
	 * {@link InjectInstalledBundle @InjectInstalledBundle} in the supplied
	 * {@link ParameterContext}.
	 */
	@Override
	public Object injectParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
		Parameter parameter = parameterContext.getParameter();
		Class<?> parameterType = parameter.getType();
		assertParameterIsOfType(parameterType, Bundle.class, InjectInstalledBundle.class,
			ParameterResolutionException::new);
		InjectInstalledBundle injectBundle = parameter.getAnnotation(InjectInstalledBundle.class);
		return installedBundleOf(injectBundle, extensionContext);
	}

	public static Bundle installedBundleOf(InjectInstalledBundle injectBundle, ExtensionContext extensionContext) {

		try {
			BundleContext bc = BundleContextExtension.getBundleContext(extensionContext);
			BundleInstaller ib = BundleInstallerExtension.getBundleInstaller(extensionContext);

			String spec = injectBundle.value();
			if (spec.startsWith("http:") || spec.startsWith("https:") || spec.startsWith("file:")) {
				return ib.installBundle(new URL(injectBundle.value()), injectBundle.start());
			} else {
				return ib.installBundle(BundleInstaller.EmbeddedLocation.of(bc, spec), injectBundle.start());
			}
		} catch (MalformedURLException | IllegalArgumentException e) {
			throw new ExtensionConfigurationException(
				String.format("Could not parse URL from given String %s.", injectBundle.value()), e);
		}

	}

	static Store getStore(ExtensionContext extensionContext) {
		return extensionContext
			.getStore(Namespace.create(InstalledBundleExtension.class, extensionContext.getUniqueId()));
	}

	@Override
	protected void injectField(Field field, Object instance, ExtensionContext context) {
		assertFieldIsOfType(field, Bundle.class, InjectInstalledBundle.class, ExtensionConfigurationException::new);
		InjectInstalledBundle injectBundle = field.getAnnotation(InjectInstalledBundle.class);
		setField(field, instance, installedBundleOf(injectBundle, context));
	}

}
