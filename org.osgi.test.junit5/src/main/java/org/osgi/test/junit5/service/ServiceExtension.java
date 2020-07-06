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

package org.osgi.test.junit5.service;

import static org.osgi.test.common.inject.FieldInjector.findAnnotatedFields;
import static org.osgi.test.common.inject.FieldInjector.setField;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.service.ServiceAware;
import org.osgi.test.common.service.ServiceConfiguration;
import org.osgi.test.common.service.ServiceConfigurationKey;

/**
 * A JUnit 5 Extension to depend on OSGi services.
 * <p>
 * Example: <br>
 *
 * <pre>
 * &#64;ExtendWith(ServiceExtension.class)
 * class MyTests {
 *
 * 	&#64;InjectService
 * 	Foo foo;
 *
 * 	&#64;Test
 * 	public void test() {
 * 		// use foo
 * 	}
 * }
 * </pre>
 */
public class ServiceExtension implements BeforeEachCallback, ParameterResolver {

	final static Namespace NAMESPACE = Namespace.create(ServiceExtension.class);

	@Override
	public void beforeEach(ExtensionContext extensionContext) throws Exception {
		List<Field> fields = findAnnotatedFields(extensionContext.getRequiredTestClass(), InjectService.class);

		BundleContext bundleContext = FrameworkUtil.getBundle(extensionContext.getRequiredTestClass())
			.getBundleContext();

		fields.forEach(field -> {
			assertValidFieldCandidate(field);

			InjectService serviceUseParameter = field.getAnnotation(InjectService.class);
			Class<?> memberType = field.getType();
			Type genericMemberType = field.getGenericType();

			setField(field, extensionContext.getRequiredTestInstance(), resolveReturnValue(memberType,
				genericMemberType, serviceUseParameter, bundleContext, extensionContext));
		});
	}

	@Override
	public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
		throws ParameterResolutionException {

		BundleContext bundleContext = FrameworkUtil.getBundle(extensionContext.getRequiredTestClass())
			.getBundleContext();

		Optional<InjectService> injectService = parameterContext.findAnnotation(InjectService.class);
		Parameter parameter = parameterContext.getParameter();
		Class<?> memberType = parameter.getType();
		Type genericMemberType = parameter.getParameterizedType();

		return resolveReturnValue(memberType, genericMemberType, injectService.get(), bundleContext, extensionContext);
	}

	@Override
	public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
		throws ParameterResolutionException {

		if (!parameterContext.isAnnotated(InjectService.class)) {
			return false;
		}

		Parameter parameter = parameterContext.getParameter();
		Class<?> memberType = parameter.getType();
		Type genericMemberType = parameter.getParameterizedType();
		Type serviceType = genericMemberType;

		if (List.class.equals(memberType) && (genericMemberType instanceof ParameterizedType)) {
			serviceType = ((ParameterizedType) genericMemberType).getActualTypeArguments()[0];
		} else if (ServiceAware.class.equals(memberType) && (genericMemberType instanceof ParameterizedType)) {
			serviceType = ((ParameterizedType) genericMemberType).getActualTypeArguments()[0];
		}
		// The service type must be a raw type
		if (serviceType instanceof Class) {
			return true;
		}
		throw new ParameterResolutionException("Can only resolve @" + InjectService.class.getSimpleName()
			+ " parameter for services with non-generic types, service type was: " + serviceType.getTypeName());
	}

	static void assertValidFieldCandidate(Field field) {
		if (Modifier.isFinal(field.getModifiers()) || Modifier.isPrivate(field.getModifiers())
			|| Modifier.isStatic(field.getModifiers())) {
			throw new ExtensionConfigurationException("@" + InjectService.class.getSimpleName() + " field ["
				+ field.getName() + "] must not be final, private or static.");
		}
	}

	static <X> ServiceConfiguration<X> getServiceConfiguration(InjectService injectService, Class<X> serviceType,
		BundleContext bundleContext, ExtensionContext extensionContext) {
		@SuppressWarnings("unchecked")
		ServiceConfiguration<X> serviceConfiguration = extensionContext.getStore(NAMESPACE)
			.getOrComputeIfAbsent(new ServiceConfigurationKey(serviceType, injectService),
				k -> new ServiceConfiguration<>(serviceType, injectService.filter(), injectService.filterArguments(),
					injectService.cardinality(), injectService.timeout()).init(bundleContext),
				ServiceConfiguration.class);
		return serviceConfiguration;
	}

	static Object resolveReturnValue(Class<?> memberType, Type genericMemberType, InjectService injectService,
		BundleContext bundleContext, ExtensionContext extensionContext) throws ParameterResolutionException {

		Type serviceType = genericMemberType;

		if (List.class.equals(memberType) && (genericMemberType instanceof ParameterizedType)) {
			serviceType = ((ParameterizedType) serviceType).getActualTypeArguments()[0];
		} else if (ServiceAware.class.equals(memberType) && (genericMemberType instanceof ParameterizedType)) {
			serviceType = ((ParameterizedType) serviceType).getActualTypeArguments()[0];
		}

		// supportsParameter() If Jupiter does the right thing then this method
		// should not be called without serviceType being a class
		assert serviceType instanceof Class;

		ServiceConfiguration<?> configuration = getServiceConfiguration(injectService, (Class<?>) serviceType,
			bundleContext, extensionContext);

		if (List.class.equals(memberType) && (genericMemberType instanceof ParameterizedType)) {
			return configuration.getServices();
		} else if (ServiceAware.class.equals(memberType) && (genericMemberType instanceof ParameterizedType)) {
			return configuration;
		}

		return configuration.getService();
	}

}
