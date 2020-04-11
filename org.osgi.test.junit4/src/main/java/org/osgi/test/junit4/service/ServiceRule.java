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

package org.osgi.test.junit4.service;

import static org.osgi.test.common.inject.FieldInjector.findAnnotatedNonStaticFields;
import static org.osgi.test.common.inject.FieldInjector.setField;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.service.ServiceAware;
import org.osgi.test.common.service.ServiceConfiguration;
import org.osgi.test.common.service.ServiceConfigurationKey;

/**
 * A JUnit 4 Rule to depend on OSGi services.
 * <p>
 * Example: <br>
 *
 * <pre>
 * &#64;Rule
 * public ServiceRule sr = new ServiceRule();
 *
 * &#64;InjectService
 * Foo foo;
 *
 * &#64;Test
 * public void aTest() {
 * 	// use foo
 * }
 * </pre>
 */
public class ServiceRule implements AutoCloseable, MethodRule {

	private final Map<ServiceConfigurationKey, ServiceConfiguration<?>> configurations = new ConcurrentHashMap<>();

	public ServiceRule init(Object testInstance) {
		BundleContext bundleContext = FrameworkUtil.getBundle(testInstance
			.getClass())
			.getBundleContext();
		List<Field> fields = findAnnotatedNonStaticFields(testInstance.getClass(), InjectService.class);

		fields.forEach(field -> {
			assertValidFieldCandidate(field);

			InjectService injectService = field.getAnnotation(InjectService.class);
			Class<?> memberType = field.getType();
			Type genericMemberType = field.getGenericType();

			setField(field, testInstance, resolveReturnValue(memberType, genericMemberType, injectService,
				bundleContext, configurations));
		});

		return this;
	}

	@Override
	public void close() throws Exception {
		for (Iterator<Entry<ServiceConfigurationKey, ServiceConfiguration<?>>> itr = configurations
			.entrySet()
			.iterator(); itr.hasNext();) {
			Entry<ServiceConfigurationKey, ServiceConfiguration<?>> entry = itr.next();
			entry.getValue()
				.close();
			itr.remove();
		}
	}

	@Override
	public Statement apply(Statement statement, FrameworkMethod method, Object testInstance) {
		init(testInstance);
		return new Statement() {
			@Override
			public void evaluate() throws Throwable {
				try {
					statement.evaluate();
				} finally {
					close();
				}
			}
		};
	}

	static void assertValidFieldCandidate(Field field) {
		if (Modifier.isFinal(field.getModifiers()) || Modifier.isPrivate(field.getModifiers())
			|| Modifier.isStatic(field.getModifiers())) {
			throw new RuntimeException(
				InjectService.class.getName() + " field [" + field + "] must not be final, private or static.");
		}
	}

	static <X> ServiceConfiguration<X> getServiceUseConfiguration(
		InjectService injectService,
		Class<X> serviceType,
		BundleContext bundleContext,
		Map<ServiceConfigurationKey, ServiceConfiguration<?>> configurations) {
		@SuppressWarnings("unchecked")
		ServiceConfiguration<X> closeableTrackServices = (ServiceConfiguration<X>) configurations.computeIfAbsent(
			new ServiceConfigurationKey(serviceType, injectService),
			k -> new ServiceConfiguration<>(serviceType, injectService.filter(), injectService.filterArguments(),
				injectService.cardinality(), injectService.timeout()).init(bundleContext));
		return closeableTrackServices;
	}

	static Object resolveReturnValue(Class<?> memberType, Type genericMemberType, InjectService serviceUseParameter,
		BundleContext bundleContext, Map<ServiceConfigurationKey, ServiceConfiguration<?>> configurations) {

		Type serviceType = genericMemberType;

		if (List.class.equals(memberType) && (genericMemberType instanceof ParameterizedType)) {
			serviceType = ((ParameterizedType) serviceType).getActualTypeArguments()[0];
		} else if (ServiceAware.class.equals(memberType)
			&& (genericMemberType instanceof ParameterizedType)) {
			serviceType = ((ParameterizedType) serviceType).getActualTypeArguments()[0];
		}

		if (!(serviceType instanceof Class)) {
			throw new IllegalStateException(
				"The only generic types allowed are List<S> and ServiceAware<S>: " + serviceType);
		}

		ServiceConfiguration<?> configuration = getServiceUseConfiguration(serviceUseParameter,
			(Class<?>) serviceType, bundleContext, configurations);

		if (List.class.equals(memberType) && (genericMemberType instanceof ParameterizedType)) {
			return configuration.getServices();
		} else if (ServiceAware.class.equals(memberType)
			&& (genericMemberType instanceof ParameterizedType)) {
			return configuration;
		}

		return configuration.getService();
	}

}
