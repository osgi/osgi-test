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

package org.osgi.test.junit5;

import static org.junit.platform.commons.util.AnnotationUtils.findAnnotatedFields;
import static org.junit.platform.commons.util.ReflectionUtils.isPrivate;
import static org.junit.platform.commons.util.ReflectionUtils.makeAccessible;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.util.function.Predicate;

import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store.CloseableResource;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.platform.commons.util.ExceptionUtils;
import org.junit.platform.commons.util.ReflectionUtils;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.test.common.CloseableBundleContext;

public class BundleContextExtension
	implements BeforeEachCallback, ParameterResolver {

	private static final String					KEY			= "bundle.context";
	private static final Namespace				NAMESPACE	= Namespace.create(BundleContextExtension.class);

	@Override
	public void beforeEach(ExtensionContext extensionContext) throws Exception {
		injectFields(extensionContext, extensionContext.getRequiredTestInstance(), ReflectionUtils::isNotStatic);
	}

	/**
	 * Resolve the BundleContext for the {@link Parameter} in the supplied
	 * {@link ParameterContext}.
	 */
	@Override
	public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
		Class<?> parameterType = parameterContext.getParameter()
			.getType();
		assertSupportedType("parameter", parameterType);
		return getBundleContext(parameterType, extensionContext);
	}

	/**
	 * Determine if the {@link Parameter} in the supplied
	 * {@link ParameterContext} is annotated with
	 * {@link BundleContextParameter @BundleContextParameter}.
	 */
	@Override
	public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
		boolean annotated = parameterContext.isAnnotated(BundleContextParameter.class);
		if (annotated && parameterContext.getDeclaringExecutable() instanceof Constructor) {
			throw new ParameterResolutionException(
				"@BundleContextParameter is not supported on constructor parameters. Please use field injection instead.");
		}
		return annotated;
	}

	private void assertSupportedType(String target, Class<?> type) {
		if (type != BundleContext.class) {
			throw new ExtensionConfigurationException("Can only resolve @BundleContextParameter " + target
				+ " of type " + BundleContext.class.getName() + " but was: " + type.getName());
		}
	}

	private void assertValidFieldCandidate(Field field) {
		assertSupportedType("field", field.getType());
		if (isPrivate(field)) {
			throw new ExtensionConfigurationException(
				"@BundleContextParameter field [" + field + "] must not be private.");
		}
	}

	private Object getBundleContext(Class<?> type, ExtensionContext extensionContext) {
		BundleContext bundleContext = extensionContext.getStore(NAMESPACE)
			.getOrComputeIfAbsent(KEY,
				key -> new CloseableResourceBundleContext(extensionContext.getRequiredTestClass(),
					FrameworkUtil.getBundle(extensionContext.getRequiredTestClass())
					.getBundleContext()),
				CloseableResourceBundleContext.class)
			.get();

		return bundleContext;
	}

	private void injectFields(ExtensionContext extensionContext, Object testInstance, Predicate<Field> predicate) {
		findAnnotatedFields(extensionContext.getRequiredTestClass(), BundleContextParameter.class, predicate)
			.forEach(field -> {
				assertValidFieldCandidate(field);
				try {
					makeAccessible(field).set(testInstance, getBundleContext(field.getType(), extensionContext));
				} catch (Throwable t) {
					ExceptionUtils.throwAsUncheckedException(t);
				}
			});
	}

	private static class CloseableResourceBundleContext implements CloseableResource {

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
