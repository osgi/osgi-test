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

package org.osgi.test.junit5.test;

import static org.junit.platform.commons.util.AnnotationUtils.findAnnotatedFields;
import static org.junit.platform.commons.util.ReflectionUtils.isPrivate;
import static org.junit.platform.commons.util.ReflectionUtils.makeAccessible;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.platform.commons.util.ExceptionUtils;
import org.junit.platform.commons.util.ReflectionUtils;

public class ExecutorExtension implements AfterEachCallback, BeforeEachCallback, ParameterResolver {

	final static Namespace				NAMESPACE	= Namespace.create(ExecutorExtension.class);
	final static String					KEY			= "executor";

	@Override
	public void beforeEach(ExtensionContext extensionContext) throws Exception {
		get(extensionContext);
		injectFields(extensionContext, extensionContext.getRequiredTestInstance(), ReflectionUtils::isNotStatic);
	}

	@Override
	public void afterEach(ExtensionContext extensionContext) throws Exception {
		ScheduledExecutorService executor = extensionContext.getStore(NAMESPACE)
			.remove(KEY, ScheduledExecutorService.class);

		if (executor != null) {
			executor.shutdownNow();
			executor.awaitTermination(100, TimeUnit.MILLISECONDS);
		}
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
		return get(extensionContext);
	}

	/**
	 * Determine if the {@link Parameter} in the supplied
	 * {@link ParameterContext} is annotated with
	 * {@link ExecutorParameter @ExecutorParameter}.
	 */
	@Override
	public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
		boolean annotated = parameterContext.isAnnotated(ExecutorParameter.class);
		if (annotated && parameterContext.getDeclaringExecutable() instanceof Constructor) {
			throw new ParameterResolutionException(
				"@ExecutorParameter is not supported on constructor parameters. Please use field injection instead.");
		}
		return annotated;
	}

	private void assertSupportedType(String target, Class<?> type) {
		if (type != ScheduledExecutorService.class) {
			throw new ExtensionConfigurationException("Can only resolve @ExecutorParameter " + target + " of type "
				+ ScheduledExecutorService.class.getName() + " but was: " + type.getName());
		}
	}

	private void assertValidFieldCandidate(Field field) {
		assertSupportedType("field", field.getType());
		if (isPrivate(field)) {
			throw new ExtensionConfigurationException("@ExecutorParameter field [" + field + "] must not be private.");
		}
	}

	ScheduledExecutorService get(ExtensionContext extensionContext) {
		return extensionContext.getStore(NAMESPACE)
			.getOrComputeIfAbsent(KEY, k -> Executors.newSingleThreadScheduledExecutor(),
				ScheduledExecutorService.class);
	}

	private void injectFields(ExtensionContext extensionContext, Object testInstance, Predicate<Field> predicate) {
		findAnnotatedFields(extensionContext.getRequiredTestClass(), ExecutorParameter.class, predicate)
			.forEach(field -> {
				assertValidFieldCandidate(field);
				try {
					makeAccessible(field).set(testInstance, get(extensionContext));
				} catch (Throwable t) {
					ExceptionUtils.throwAsUncheckedException(t);
				}
			});
	}

}
