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

package org.osgi.test.junit5.inject;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;
import static org.junit.platform.commons.support.AnnotationSupport.findAnnotation;
import static org.junit.platform.commons.support.AnnotationSupport.isAnnotated;
import static org.osgi.test.common.inject.FieldInjector.findAnnotatedFields;
import static org.osgi.test.common.inject.FieldInjector.findAnnotatedNonStaticFields;
import static org.osgi.test.common.inject.FieldInjector.setField;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.TestInstances;
import org.osgi.test.common.inject.TargetType;

public abstract class InjectingExtension<INJECTION extends Annotation>
	implements BeforeEachCallback, BeforeAllCallback, ParameterResolver, AfterAllCallback, AfterEachCallback {

	private final Class<INJECTION>	annotation;
	private final List<Class<?>>	targetTypes;

	protected InjectingExtension(Class<INJECTION> annotation, Class<?>... targetTypes) {
		this.annotation = requireNonNull(annotation);
		this.targetTypes = Arrays.asList(targetTypes);
	}

	protected Class<INJECTION> annotation() {
		return annotation;
	}

	protected List<Class<?>> targetTypes() {
		return targetTypes;
	}

	@Override
	public void beforeAll(ExtensionContext extensionContext) throws Exception {
		List<Field> fields = findAnnotatedFields(extensionContext.getRequiredTestClass(), annotation(),
			m -> Modifier.isStatic(m.getModifiers()));

		fields.stream()
			.filter(field -> supportsField(field, extensionContext))
			.forEach(field -> setField(field, null, resolveField(field, extensionContext)));
		if (isLifecyclePerClass(extensionContext)) {
			injectNonStaticFields(extensionContext, extensionContext.getRequiredTestInstance());
		}
		injectNonStaticFields(extensionContext);
	}

	@Override
	public void afterAll(ExtensionContext extensionContext) throws Exception {}

	@Override
	public void beforeEach(ExtensionContext extensionContext) throws Exception {
		if (!isLifecyclePerClass(extensionContext)) {
			injectNonStaticFields(extensionContext, extensionContext.getRequiredTestInstance());
		}
		injectNonStaticFields(extensionContext);
	}

	@Override
	public void afterEach(ExtensionContext extensionContext) throws Exception {}

	protected int disallowedFieldModifiers() {
		return Modifier.FINAL | Modifier.PRIVATE;
	}

	/**
	 * Determine if this extender supports resolution for the specified
	 * {@link Field} for the specified {@link ExtensionContext}.
	 */
	protected boolean supportsField(Field field, ExtensionContext extensionContext) {
		if (!isAnnotated(field, annotation())) {
			return false;
		}
		TargetType targetType = TargetType.of(field);
		try {
			if (!supportsType(targetType, extensionContext)) {
				return false;
			}
		} catch (ParameterResolutionException pre) {
			// Convert to ExtensionConfigurationException for field
			ExtensionConfigurationException ece = new ExtensionConfigurationException(pre.getMessage(), pre.getCause());
			ece.setStackTrace(pre.getStackTrace());
			throw ece;
		}

		if ((field.getModifiers() & disallowedFieldModifiers()) != 0) {
			throw new ExtensionConfigurationException(
				String.format("Field %s must not be %s for annotation @%s.", field.getName(),
					Modifier.toString(field.getModifiers() & disallowedFieldModifiers()),
					annotation().getSimpleName()));
		}
		return true;
	}

	/**
	 * Determine if this resolver supports resolution of an argument for the
	 * {@link Parameter} in the specified {@link ParameterContext} for the
	 * specified {@link ExtensionContext}.
	 */
	@Override
	public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
		throws ParameterResolutionException {
		if (!parameterContext.isAnnotated(annotation())) {
			return false;
		}
		TargetType targetType = TargetType.of(parameterContext.getParameter());
		if (!supportsType(targetType, extensionContext)) {
			return false;
		}
		return true;
	}

	/**
	 * Determine if this extender supports resolution for the specified
	 * {@link TargetType} for the specified {@link ExtensionContext}.
	 */
	protected boolean supportsType(TargetType targetType, ExtensionContext extensionContext)
		throws ParameterResolutionException {
		if (!targetTypes().isEmpty()) {
			Class<?> type = targetType.getType();
			if (targetTypes().stream()
				.noneMatch(type::isAssignableFrom)) {
				throw new ParameterResolutionException(
					String.format("Element %s has an unsupported type %s for annotation @%s. Supported types are: %s.",
						targetType.getName(), type.getName(), annotation().getSimpleName(), targetTypes().stream()
							.map(Class::getName)
							.collect(joining())));
			}
		}
		return true;
	}

	/**
	 * Resolve the value for the specified {@link Field} for the specified
	 * {@link ExtensionContext}.
	 */
	protected Object resolveField(Field field, ExtensionContext extensionContext) {
		INJECTION injection = findAnnotation(field, annotation()).get();
		TargetType targetType = TargetType.of(field);
		try {
			return resolveValue(targetType, injection, extensionContext);
		} catch (ParameterResolutionException pre) {
			// Convert to ExtensionConfigurationException for field
			ExtensionConfigurationException ece = new ExtensionConfigurationException(pre.getMessage(), pre.getCause());
			ece.setStackTrace(pre.getStackTrace());
			throw ece;
		}
	}

	/**
	 * Resolve the value for the specified {@link ParameterContext} for the
	 * specified {@link ExtensionContext}.
	 */
	@Override
	public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
		INJECTION injection = parameterContext.findAnnotation(annotation())
			.get();
		TargetType targetType = TargetType.of(parameterContext.getParameter());
		return resolveValue(targetType, injection, extensionContext);
	}

	/**
	 * Resolve the value for the specified {@link TargetType} and injection
	 * annotation for the specified {@link ExtensionContext}.
	 */
	protected abstract Object resolveValue(TargetType targetType, INJECTION injection,
		ExtensionContext extensionContext) throws ParameterResolutionException;

	private void injectNonStaticFields(ExtensionContext extensionContext) {
		if (!extensionContext.getTestInstances()
			.isPresent()) {
			return;
		}
		TestInstances instances = extensionContext.getRequiredTestInstances();
		Object innerMost = instances.getInnermostInstance();
		for (Object instance : instances.getAllInstances()) {
			// Skip the innermost; it will be set by the caller if necessary
			if (innerMost == instance) {
				continue;
			}
			final Class<?> testClass = instance.getClass();
			boolean perInstance = !isLifecyclePerClass(testClass);
			if (perInstance) {
				injectNonStaticFields(extensionContext, instance);
			}
		}
	}

	private void injectNonStaticFields(ExtensionContext extensionContext, Object instance) {
		final Class<?> testClass = instance.getClass();
		List<Field> fields = findAnnotatedNonStaticFields(testClass, annotation());

		fields.stream()
			.filter(field -> supportsField(field, extensionContext))
			.forEach(field -> setField(field, instance, resolveField(field, extensionContext)));
	}

	protected boolean isLifecyclePerClass(ExtensionContext context) {
		return context.getTestInstanceLifecycle()
			.filter(Lifecycle.PER_CLASS::equals)
			.isPresent();
	}

	protected boolean isLifecyclePerClass(Class<?> testClass) {
		return findAnnotation(testClass, TestInstance.class).map(TestInstance::value)
			.filter(Lifecycle.PER_CLASS::equals)
			.isPresent();
	}
}
