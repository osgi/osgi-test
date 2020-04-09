/*
 * Copyright (c) OSGi Alliance (2019, 2020). All Rights Reserved.
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

import static org.osgi.test.common.inject.FieldInjector.findAnnotatedNonStaticFields;
import static org.osgi.test.common.inject.FieldInjector.setField;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.List;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store.CloseableResource;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.annotation.InjectInstallBundle;
import org.osgi.test.common.context.CloseableBundleContext;
import org.osgi.test.common.install.InstallBundle;

/**
 * A JUnit 5 Extension to get the OSGi {@link BundleContext} of the test bundle.
 * <p>
 * The {@link BundleContext} implementation provided by this rule will
 * automatically clean up all service registrations, bundle, service and
 * framework listeners, as well as installed bundles left behind.
 * <p>
 * Example:
 *
 * <pre>
 * &#64;ExtendWith(BundleContextExtension.class)
 * class MyTests {
 *
 * 	&#64;InjectBundleContext
 * 	BundleContext bundleContext;
 *
 * 	&#64;Test
 * 	public void aTest() {
 * 		Bundle bundle = bundleContext.getBundle();
 * 	}
 * }
 * </pre>
 */
public class BundleContextExtension implements AfterEachCallback, BeforeEachCallback, ParameterResolver {

	public static final String		BUNDLE_CONTEXT_KEY	= "bundle.context";
	public static final String		INSTALL_BUNDLE_KEY	= "install.bundle";
	public static final Namespace	NAMESPACE			= Namespace.create(BundleContextExtension.class);

	@Override
	public void beforeEach(ExtensionContext extensionContext) throws Exception {
		List<Field> fields = findAnnotatedNonStaticFields(extensionContext.getRequiredTestClass(),
			InjectBundleContext.class);

		fields.forEach(field -> {
			assertFieldIsBundleContext(field);
			setField(field, extensionContext.getRequiredTestInstance(), getBundleContext(extensionContext));
		});

		fields = findAnnotatedNonStaticFields(extensionContext.getRequiredTestClass(), InjectInstallBundle.class);

		fields.forEach(field -> {
			assertFieldIsInstallBundle(field);
			setField(field, extensionContext.getRequiredTestInstance(), getInstallbundle(extensionContext));
		});
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
	 * {@link InjectBundleContext @BundleContextParameter} OR
	 * {@link InjectInstallBundle @InstallBundleParameter} in the supplied
	 * {@link ParameterContext}.
	 */
	@Override
	public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
		Class<?> parameterType = parameterContext.getParameter()
			.getType();

		if (parameterContext.isAnnotated(InjectBundleContext.class)) {
			assertIsBundleContext("parameter", parameterType);
			return getBundleContext(extensionContext);
		} else if (parameterContext.isAnnotated(InjectInstallBundle.class)) {
			assertIsInstallBundle("parameter", parameterType);
			return getInstallbundle(extensionContext);
		}

		throw new ExtensionConfigurationException("No parameter types known to BundleContextExtension were found");
	}

	/**
	 * Determine if the {@link Parameter} in the supplied
	 * {@link ParameterContext} is annotated with
	 * {@link InjectBundleContext @BundleContextParameter} OR
	 * {@link InjectInstallBundle @InstallBundleParameter}.
	 */
	@Override
	public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
		boolean annotatedBundleContextParameter = parameterContext.isAnnotated(InjectBundleContext.class);
		boolean annotatedInstallBundleParameter = parameterContext.isAnnotated(InjectInstallBundle.class);
		if ((annotatedBundleContextParameter || annotatedInstallBundleParameter)
			&& (parameterContext.getDeclaringExecutable() instanceof Constructor)) {
			throw new ParameterResolutionException(
				"BundleContextExtension only supports field and parameter injection.");
		}
		return annotatedBundleContextParameter || annotatedInstallBundleParameter;
	}

	private void assertIsBundleContext(String target, Class<?> type) {
		if (type != BundleContext.class) {
			throw new ExtensionConfigurationException("Can only resolve @" + InjectBundleContext.class.getSimpleName()
				+ " " + target + " of type " + BundleContext.class.getName() + " but was: " + type.getName());
		}
	}

	private void assertIsInstallBundle(String target, Class<?> type) {
		if (type != InstallBundle.class) {
			throw new ExtensionConfigurationException("Can only resolve @" + InjectInstallBundle.class.getSimpleName()
				+ " " + target + " of type " + InstallBundle.class.getName() + " but was: " + type.getName());
		}
	}

	private void assertFieldIsBundleContext(Field field) {
		assertIsBundleContext("field", field.getType());
		if (Modifier.isFinal(field.getModifiers()) || Modifier.isPrivate(field.getModifiers())
			|| Modifier.isStatic(field.getModifiers())) {
			throw new ExtensionConfigurationException(
				InjectBundleContext.class.getName() + " field [" + field + "] must not be final, private or static.");
		}
	}

	private void assertFieldIsInstallBundle(Field field) {
		assertIsInstallBundle("field", field.getType());
		if (Modifier.isFinal(field.getModifiers()) || Modifier.isPrivate(field.getModifiers())
			|| Modifier.isStatic(field.getModifiers())) {
			throw new ExtensionConfigurationException(
				InjectInstallBundle.class.getName() + " field [" + field + "] must not be final, private or static.");
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
