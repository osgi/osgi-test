package org.osgi.test.junit5.inject;

import static org.junit.platform.commons.support.AnnotationSupport.findAnnotation;
import static org.osgi.test.common.inject.FieldInjector.findAnnotatedFields;
import static org.osgi.test.common.inject.FieldInjector.findAnnotatedNonStaticFields;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.List;

import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.TestInstances;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.annotation.InjectBundleInstaller;

public abstract class InjectingExtension<A extends Annotation>
	implements BeforeEachCallback, BeforeAllCallback, ParameterResolver {

	protected final Class<A> supported;

	protected InjectingExtension(Class<A> annotation) {
		supported = annotation;
	}

	void injectNonStaticFields(ExtensionContext extensionContext) {
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

	static boolean isPerClass(ExtensionContext context) {
		return context.getTestInstanceLifecycle()
			.orElse(Lifecycle.PER_METHOD) == Lifecycle.PER_CLASS;
	}

	static boolean isAnnotatedPerClass(Class<?> testClass) {
		return findAnnotation(testClass, TestInstance.class).map(TestInstance::value)
			.map(x -> x == Lifecycle.PER_CLASS)
			.orElse(false);
	}

	void injectNonStaticFields(ExtensionContext extensionContext, Object instance) {
		final Class<?> testClass = instance.getClass();
		List<Field> fields = findAnnotatedNonStaticFields(testClass, supported);

		fields.forEach(field -> injectField(field, instance, extensionContext));
	}

	/**
	 * Resolve {@link Parameter} annotated with
	 * {@link InjectBundleContext @InjectBundleContext} OR
	 * {@link InjectBundleInstaller @InjectBundleInstaller} in the supplied
	 * {@link ParameterContext}.
	 */
	@Override
	public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
		if (parameterContext.isAnnotated(supported)) {
			return injectParameter(parameterContext, extensionContext);
		}

		throw new ExtensionConfigurationException(
			"No parameter types known to " + getClass().getSimpleName() + " were found");
	}

	/**
	 * Determine if the {@link Parameter} in the supplied
	 * {@link ParameterContext} is annotated with
	 * {@link InjectBundleContext @InjectBundleContext} OR
	 * {@link InjectBundleInstaller @InjectBundleInstaller}.
	 */
	@Override
	public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
		final boolean ours = parameterContext.isAnnotated(supported);

		if (ours && (parameterContext.getDeclaringExecutable() instanceof Constructor)) {
			throw new ParameterResolutionException(
				getClass().getSimpleName() + " does not support parameter injection on constructors");
		}
		return ours;
	}

	protected abstract void injectField(Field field, Object instance, ExtensionContext context);

	protected abstract Object injectParameter(ParameterContext parameterContext, ExtensionContext extensionContext);

	@Override
	public void beforeAll(ExtensionContext extensionContext) throws Exception {
		List<Field> fields = findAnnotatedFields(extensionContext.getRequiredTestClass(), supported,
			m -> Modifier.isStatic(m.getModifiers()));

		fields.forEach(field -> injectField(field, null, extensionContext));
		if (isPerClass(extensionContext)) {
			injectNonStaticFields(extensionContext, extensionContext.getRequiredTestInstance());
		}
		injectNonStaticFields(extensionContext);
	}

	@Override
	public void beforeEach(ExtensionContext extensionContext) throws Exception {
		if (!isPerClass(extensionContext)) {
			injectNonStaticFields(extensionContext, extensionContext.getRequiredTestInstance());
		}
		injectNonStaticFields(extensionContext);
	}
}
