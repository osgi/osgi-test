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
import java.util.function.Function;

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

public abstract class InjectingExtension<A extends Annotation>
	implements BeforeEachCallback, BeforeAllCallback, ParameterResolver, AfterAllCallback, AfterEachCallback {

	private final Class<A>			annotation;
	private final List<Class<?>>	targetTypes;

	protected InjectingExtension(Class<A> annotation, Class<?>... targetTypes) {
		this.annotation = requireNonNull(annotation);
		this.targetTypes = Arrays.asList(targetTypes);
	}

	protected Class<A> annotation() {
		return annotation;
	}

	protected List<Class<?>> targetTypes() {
		return targetTypes;
	}

	/**
	 * Determine if this extender supports resolution for the specified
	 * {@link TargetType} for the specified {@link ExtensionContext}.
	 */
	protected boolean supportsType(TargetType targetType, Function<String, ? extends RuntimeException> exception,
		ExtensionContext extensionContext) {
		if (!targetTypes().isEmpty()) {
			Class<?> type = targetType.getType();
			if (targetTypes().stream()
				.noneMatch(type::isAssignableFrom)) {
				throw exception.apply(
					String.format("Element %s has an unsupported type %s for annotation @%s. Supported types are: %s.",
						targetType.getName(), type.getName(), annotation().getSimpleName(), targetTypes().stream()
							.map(Class::getName)
							.collect(joining())));
			}
		}
		return true;
	}

	/**
	 * Determine if this extender supports resolution for the specified
	 * {@link Field} for the specified {@link ExtensionContext}.
	 */
	protected boolean supportsField(Field field, ExtensionContext extensionContext) {
		if (!isAnnotated(field, annotation())) {
			return false;
		}
		if (!supportsType(TargetType.of(field), ExtensionConfigurationException::new, extensionContext)) {
			return false;
		}
		if (Modifier.isFinal(field.getModifiers()) || Modifier.isPrivate(field.getModifiers())) {
			throw new ExtensionConfigurationException(
				String.format("Field %s must not be private or final for annotation @%s.", field.getName(),
					annotation().getSimpleName()));
		}
		return true;
	}

	/**
	 * Resolve the value for the specified {@link Field} for the specified
	 * {@link ExtensionContext}.
	 */
	protected abstract Object resolveField(Field field, ExtensionContext extensionContext);

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
		if (!supportsType(TargetType.of(parameterContext.getParameter()), ParameterResolutionException::new,
			extensionContext)) {
			return false;
		}
		return true;
	}

	@Override
	public void beforeAll(ExtensionContext extensionContext) throws Exception {
		List<Field> fields = findAnnotatedFields(extensionContext.getRequiredTestClass(), annotation(),
			m -> Modifier.isStatic(m.getModifiers()));

		fields.stream()
			.filter(field -> supportsField(field, extensionContext))
			.forEach(field -> setField(field, null, resolveField(field, extensionContext)));
		if (isPerClass(extensionContext)) {
			injectNonStaticFields(extensionContext, extensionContext.getRequiredTestInstance());
		}
		injectNonStaticFields(extensionContext);
	}

	@Override
	public void afterAll(ExtensionContext extensionContext) throws Exception {}


	@Override
	public void beforeEach(ExtensionContext extensionContext) throws Exception {
		if (!isPerClass(extensionContext)) {
			injectNonStaticFields(extensionContext, extensionContext.getRequiredTestInstance());
		}
		injectNonStaticFields(extensionContext);
	}

	@Override
	public void afterEach(ExtensionContext extensionContext) throws Exception {}

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
			boolean perInstance = !isAnnotatedPerClass(testClass);
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

	private static boolean isPerClass(ExtensionContext context) {
		return context.getTestInstanceLifecycle()
			.filter(Lifecycle.PER_CLASS::equals)
			.isPresent();
	}

	private static boolean isAnnotatedPerClass(Class<?> testClass) {
		return findAnnotation(testClass, TestInstance.class).map(TestInstance::value)
			.filter(Lifecycle.PER_CLASS::equals)
			.isPresent();
	}
}
