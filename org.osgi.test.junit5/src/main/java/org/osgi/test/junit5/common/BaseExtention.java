package org.osgi.test.junit5.common;

import static org.junit.platform.commons.util.AnnotationUtils.findAnnotatedFields;
import static org.junit.platform.commons.util.ReflectionUtils.isPrivate;
import static org.junit.platform.commons.util.ReflectionUtils.makeAccessible;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
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


public interface BaseExtention
	extends BeforeAllCallback, BeforeEachCallback, AfterEachCallback, AfterAllCallback, ParameterResolver {

	Namespace namespace();

	String storeKey();

	Class<? extends Annotation> injectionAnnotation();

	default String injectionAnnotationName(){
		return injectionAnnotation() == null ? "<<not set>>" : injectionAnnotation().getName();
	}

	List<Class<?>> injectionClasses();

	/**
	 * Perform field injection for non-private, {@code static} fields (i.e.,
	 * class fields) of types {@code injectionClasses()} that are annotated with
	 * annotation {@code injectionAnnotation()}.
	 */
	@Override
	default void beforeAll(ExtensionContext context) {
		injectStaticFields(context, context.getRequiredTestClass());
	}

	/**
	 * Perform field injection for non-private, non-static fields (i.e.,
	 * instance fields) of types {@code injectionClasses()} that are annotated
	 * with annotation {@code injectionAnnotation()}.
	 */
	@Override
	default void beforeEach(ExtensionContext context) {
		Optional.ofNullable(context.getRequiredTestInstances())
			.ifPresent(ri -> ri.getAllInstances() //
				.forEach(instance -> injectInstanceFields(context, instance)));
	}

	default void injectStaticFields(ExtensionContext context, Class<?> testClass) {
		injectFields(context, null, testClass, ReflectionUtils::isStatic);
	}

	default void injectInstanceFields(ExtensionContext context, Object instance) {
		injectFields(context, instance, instance.getClass(), ReflectionUtils::isNotStatic);
	}

	default void injectFields(ExtensionContext context, Object testInstance, Class<?> testClass,
			Predicate<Field> predicate) {

		findAnnotatedFields(testClass, injectionAnnotation(), predicate).forEach(field -> {
			assertValidFieldCandidate(field);
			try {
				makeAccessible(field).set(testInstance, getObject(field.getType(), context));
			}
			catch (Throwable t) {
				ExceptionUtils.throwAsUncheckedException(t);
			}
		});
	}

	default void assertValidFieldCandidate(Field field) {
		assertSupportedType("field", field.getType());
		if (isPrivate(field)) {
			throw new ExtensionConfigurationException(
				"@" + injectionAnnotationName() + " field [" + field
					+ "] must not be private.");
		}
	}

	/**
	 * Determine if the {@link Parameter} in the supplied {@link ParameterContext}
	 * is annotated with Annotation given by {@code injectionAnnotation()}.
	 */
	@Override
	default boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
		boolean annotated = parameterContext.isAnnotated(injectionAnnotation());
		if (annotated && parameterContext.getDeclaringExecutable() instanceof Constructor) {
			throw new ParameterResolutionException(
				"@" + injectionAnnotationName()
					+ " is not supported on constructor parameters. Please use field injection instead.");
		}
		return annotated;
	}

	/**
	 * Resolve the {@link Parameter} in the supplied {@link ParameterContext}.
	 */
	@Override
	default Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
		Class<?> parameterType = parameterContext.getParameter().getType();
		assertSupportedType("parameter", parameterType);
		return getObject(parameterType, extensionContext);
	}

	default void assertSupportedType(String target, Class<?> type) {
		if (!injectionClasses().stream()
			.anyMatch(clazz -> clazz == type)) {
			throw new ExtensionConfigurationException("Can only resolve @" + injectionAnnotationName() + " " + target
				+ " of type " + injectionClasses().stream()
					.map(Class::getName)
					.collect(Collectors.joining(",", "{", "}"))
				+ " but was: " + type.getName());
		}
	}

	default Object getObject(Class<?> type, ExtensionContext extensionContext) {
		ExtendedCloseableResource closeableResource = extensionContext.getStore(namespace()) //
			.getOrComputeIfAbsent(storeKey(), key -> create(type, extensionContext),
				ExtendedCloseableResource.class);

		return closeableResource.get(type);
	}

	ExtendedCloseableResource create(Class<?> type, ExtensionContext extensionContext);


	public interface ExtendedCloseableResource extends CloseableResource {

		<V> V get(Class<V> requiredType);

	}


	@Override
	default void afterEach(ExtensionContext context) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	default void afterAll(ExtensionContext context) throws Exception {
		// TODO Auto-generated method stub

	}


}
