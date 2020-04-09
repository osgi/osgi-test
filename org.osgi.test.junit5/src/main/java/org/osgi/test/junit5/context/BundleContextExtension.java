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

package org.osgi.test.junit5.context;

import static org.junit.platform.commons.util.AnnotationUtils.findAnnotatedFields;
import static org.junit.platform.commons.util.ReflectionUtils.isPrivate;
import static org.junit.platform.commons.util.ReflectionUtils.makeAccessible;
import static org.osgi.test.common.exceptions.Exceptions.unchecked;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.util.function.Predicate;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store.CloseableResource;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.platform.commons.util.ReflectionUtils;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.test.common.context.CloseableBundleContext;
import org.osgi.test.common.install.InstallBundle;

public class BundleContextExtension implements AfterEachCallback, BeforeEachCallback, ParameterResolver {

	public static final String		BUNDLE_CONTEXT_KEY	= "bundle.context";
	public static final String		INSTALL_BUNDLE_KEY	= "install.bundle";
	public static final Namespace	NAMESPACE			= Namespace.create(BundleContextExtension.class);

	@Override
	public void beforeEach(ExtensionContext extensionContext) throws Exception {
		injectFields(extensionContext, extensionContext.getRequiredTestInstance(), ReflectionUtils::isNotStatic);
	}

	@Override
	public void afterEach(ExtensionContext extensionContext) throws Exception {
		cleanup(extensionContext);
	}

	public static void cleanup(ExtensionContext extensionContext) throws Exception {
		extensionContext.getStore(NAMESPACE)
			.remove(INSTALL_BUNDLE_KEY, ExtensionInstallBundle.class);
		CloseableResourceBundleContext closeableResourceBundleContext = extensionContext.getStore(NAMESPACE)
			.remove(BUNDLE_CONTEXT_KEY, CloseableResourceBundleContext.class);
		if (closeableResourceBundleContext != null) {
			closeableResourceBundleContext.close();
		}
	}

	/**
	 * Resolve {@link Parameter} annotated with
	 * {@link BundleContextParameter @BundleContextParameter} OR
	 * {@link InstallBundleParameter @InstallBundleParameter} in the supplied
	 * {@link ParameterContext}.
	 */
	@Override
	public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
		Class<?> parameterType = parameterContext.getParameter()
			.getType();

		if (parameterContext.isAnnotated(BundleContextParameter.class)) {
			assertSupportedType("parameter", parameterType, BundleContext.class);
			return getBundleContext(extensionContext);
		} else if (parameterContext.isAnnotated(InstallBundleParameter.class)) {
			assertSupportedType("parameter", parameterType, InstallBundle.class);
			return getInstallbundle(extensionContext);
		}

		throw new ExtensionConfigurationException("No parameter types known to BundleContextExtension were found");
	}

	/**
	 * Determine if the {@link Parameter} in the supplied
	 * {@link ParameterContext} is annotated with
	 * {@link BundleContextParameter @BundleContextParameter} OR
	 * {@link InstallBundleParameter @InstallBundleParameter}.
	 */
	@Override
	public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
		boolean annotatedBundleContextParameter = parameterContext.isAnnotated(BundleContextParameter.class);
		boolean annotatedInstallBundleParameter = parameterContext.isAnnotated(InstallBundleParameter.class);
		if ((annotatedBundleContextParameter || annotatedInstallBundleParameter)
			&& (parameterContext.getDeclaringExecutable() instanceof Constructor)) {
			throw new ParameterResolutionException(
				"BundleContextExtension only supports field and parameter injection.");
		}
		return annotatedBundleContextParameter || annotatedInstallBundleParameter;
	}

	private void assertSupportedType(String target, Class<?> type, Class<?> typeToCheckFor) {
		if (type != typeToCheckFor) {
			throw new ExtensionConfigurationException("Can only resolve @" + typeToCheckFor.getSimpleName() + " "
				+ target + " of type " + typeToCheckFor.getName() + " but was: " + type.getName());
		}
	}

	private void assertValidFieldCandidate(Field field, Class<?> typeToCheckFor) {
		assertSupportedType("field", field.getType(), typeToCheckFor);
		if (isPrivate(field)) {
			throw new ExtensionConfigurationException(
				"@BundleContextParameter field [" + field + "] must not be private.");
		}
	}

	public static BundleContext getBundleContext(ExtensionContext extensionContext) {
		BundleContext bundleContext = extensionContext.getStore(NAMESPACE)
			.getOrComputeIfAbsent(BUNDLE_CONTEXT_KEY,
				key -> new CloseableResourceBundleContext(extensionContext.getRequiredTestClass(),
					FrameworkUtil.getBundle(extensionContext.getRequiredTestClass())
						.getBundleContext()),
				CloseableResourceBundleContext.class)
			.get();

		return bundleContext;
	}

	public static InstallBundle getInstallbundle(ExtensionContext extensionContext) {
		return extensionContext.getStore(NAMESPACE)
			.getOrComputeIfAbsent(INSTALL_BUNDLE_KEY,
				key -> new ExtensionInstallBundle(getBundleContext(extensionContext)), ExtensionInstallBundle.class);
	}

	private void injectFields(ExtensionContext extensionContext, Object testInstance, Predicate<Field> predicate) {
		findAnnotatedFields(extensionContext.getRequiredTestClass(), BundleContextParameter.class, predicate)
			.forEach(field -> {
				assertValidFieldCandidate(field, BundleContext.class);
				unchecked(() -> makeAccessible(field).set(testInstance, getBundleContext(extensionContext)));
			});
		findAnnotatedFields(extensionContext.getRequiredTestClass(), InstallBundleParameter.class, predicate)
			.forEach(field -> {
				assertValidFieldCandidate(field, InstallBundle.class);
				unchecked(() -> makeAccessible(field).set(testInstance, getInstallbundle(extensionContext)));
			});
	}

	public static class ExtensionInstallBundle implements InstallBundle {

		private final BundleContext bundleContext;

		ExtensionInstallBundle(BundleContext proxiedBundleContext) {
			this.bundleContext = proxiedBundleContext;
		}

		@Override
		public BundleContext getBundleContext() {
			return bundleContext;
		}

	}

	public static class CloseableResourceBundleContext implements CloseableResource {

		private final BundleContext bundleContext;

		CloseableResourceBundleContext(Class<?> testClass, BundleContext bundleContext) {
			this.bundleContext = CloseableBundleContext.proxy(testClass, bundleContext);
		}

		@Override
		public void close() throws Exception {
			((AutoCloseable) bundleContext).close();
		}

		public BundleContext get() {
			return bundleContext;
		}

	}

}
