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

import static org.junit.platform.commons.util.AnnotationUtils.findAnnotatedFields;
import static org.junit.platform.commons.util.ReflectionUtils.makeAccessible;
import static org.osgi.test.common.exceptions.Exceptions.unchecked;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.platform.commons.util.ReflectionUtils;
import org.osgi.test.common.service.ServiceAware;
import org.osgi.test.junit5.context.BundleContextExtension;

public class ServiceUseExtension<T>
	implements AfterEachCallback, BeforeEachCallback, ParameterResolver {

	final static Namespace					NAMESPACE	= Namespace.create(ServiceUseExtension.class);

	@Override
	public void beforeEach(ExtensionContext extensionContext) throws Exception {
		injectFields(extensionContext, extensionContext.getRequiredTestInstance(),
			ReflectionUtils::isNotStatic);
	}

	@Override
	public void afterEach(ExtensionContext extensionContext) throws Exception {
		BundleContextExtension.cleanup(extensionContext);
	}

	@Override
	public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
		throws ParameterResolutionException {

		Optional<ServiceUseParameter> supo = parameterContext.findAnnotation(ServiceUseParameter.class);
		Parameter parameter = parameterContext.getParameter();
		Class<?> memberType = parameter.getType();
		Type genericMemberType = parameter.getParameterizedType();

		return resolveReturnValue(memberType, genericMemberType, supo.get(), extensionContext);
	}

	@Override
	public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
		throws ParameterResolutionException {

		if (!parameterContext.isAnnotated(ServiceUseParameter.class)) {
			return false;
		}

		Parameter parameter = parameterContext.getParameter();
		Class<?> memberType = parameter.getType();
		Type genericMemberType = parameter.getParameterizedType();
		Type serviceType = genericMemberType;

		if (List.class.isAssignableFrom(memberType) && (genericMemberType instanceof ParameterizedType)) {
			serviceType = ((ParameterizedType) genericMemberType).getActualTypeArguments()[0];
		}
		else if (ServiceAware.class.isAssignableFrom(memberType) && (genericMemberType instanceof ParameterizedType)) {
			serviceType = ((ParameterizedType) genericMemberType).getActualTypeArguments()[0];
		}
		// The service type must be a raw type
		if (serviceType instanceof Class) {
			return true;
		}

		return false;
	}

	static void assertValidFieldCandidate(Field field) {
		if (Modifier.isFinal(field.getModifiers()) || Modifier.isPrivate(field.getModifiers())
			|| Modifier.isStatic(field.getModifiers())) {
			throw new ExtensionConfigurationException(
				"@ServiceParameter field [" + field + "] must not be final, private or static.");
		}
	}

	static void injectFields(
		ExtensionContext extensionContext,
		Object testInstance,
		Predicate<Field> predicate) {

		List<Field> fields = findAnnotatedFields(extensionContext.getRequiredTestClass(), ServiceUseParameter.class,
			predicate);

		fields.forEach(field -> {
				assertValidFieldCandidate(field);

				ServiceUseParameter serviceUseParameter = field.getAnnotation(ServiceUseParameter.class);
				Class<?> memberType = field.getType();
				Type genericMemberType = field.getGenericType();

				unchecked(() -> makeAccessible(field).set(testInstance,
					resolveReturnValue(memberType, genericMemberType, serviceUseParameter, extensionContext)));
			});
	}

	static <X> ServiceUseConfiguration<X> getServiceUseConfiguration(
		ServiceUseParameter serviceUseParameter,
		Class<X> serviceType,
		ExtensionContext extensionContext) {
		@SuppressWarnings("unchecked")
		ServiceUseConfiguration<X> closeableTrackServices = extensionContext.getStore(
			NAMESPACE)
			.getOrComputeIfAbsent(serviceUseParameter,
				k -> new ServiceUseConfiguration<>(serviceType, extensionContext, serviceUseParameter.filter(),
					serviceUseParameter.filterArguments(), serviceUseParameter.cardinality(),
					serviceUseParameter.timeout()).init(),
				ServiceUseConfiguration.class);
		return closeableTrackServices;
	}

	static Object resolveReturnValue(Class<?> memberType, Type genericMemberType,
		ServiceUseParameter serviceUseParameter,
		ExtensionContext extensionContext) throws ParameterResolutionException {

		Type serviceType = genericMemberType;

		if (List.class.isAssignableFrom(memberType) && (genericMemberType instanceof ParameterizedType)) {
			serviceType = ((ParameterizedType) serviceType).getActualTypeArguments()[0];
		} else if (ServiceAware.class.isAssignableFrom(memberType)
			&& (genericMemberType instanceof ParameterizedType)) {
			serviceType = ((ParameterizedType) serviceType).getActualTypeArguments()[0];
		}

		ServiceUseConfiguration<?> configuration = getServiceUseConfiguration(serviceUseParameter,
			(Class<?>) serviceType, extensionContext);

		if (List.class.isAssignableFrom(memberType) && (genericMemberType instanceof ParameterizedType)) {
			return configuration.getServices();
		} else if (ServiceAware.class.isAssignableFrom(memberType)
			&& (genericMemberType instanceof ParameterizedType)) {
			return configuration;
		}

		return configuration.getService();
	}

}
