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
package org.osgi.test.junit5.service;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.support.AnnotationConsumer;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.test.common.inject.TargetType;
import org.osgi.test.common.service.ServiceConfiguration;
import org.osgi.test.junit5.context.BundleContextExtension;

public class ServiceArgumentsProvider implements ArgumentsProvider, AnnotationConsumer<ServiceSource> {

	private ServiceSource serviceSource;

	@SuppressWarnings("resource")
	@Override
	public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
		BundleContext bundleContext = BundleContextExtension.getBundleContext(context);

		try {
			ServiceConfiguration<?> sc = ServiceExtension.getServiceConfiguration(serviceSource.serviceType(),
				serviceSource.filter(), serviceSource.filterArguments(), serviceSource.cardinality(), serviceSource.timeout(), context);

			Stream<Arguments> stream = sc.getServiceReferences()
				.stream()
				.filter(Objects::nonNull)
				.map((sr) -> {

					List<Object> list = new ArrayList<>();
					Optional<AnnotatedElement> oElement = context.getElement();
					if (oElement.isPresent()) {
						if (oElement.get() instanceof Method) {
							Method method = (Method) oElement.get();
							for (Parameter param : method.getParameters()) {

								TargetType targetType = TargetType.of(param);
								if (targetType.matches(serviceSource.serviceType())) {
									Object service = sc.getTracked()
										.get(sr);
									list.add(service);
									continue;
								}

								if (targetType.matches(ServiceReference.class, serviceSource.serviceType())) {
									list.add(sr);
									continue;
								}

								if (targetType.matches(Dictionary.class, String.class, Object.class)) {
									Dictionary<String, Object> dict = new Hashtable<>();
									for (String key : sr.getPropertyKeys()) {
										dict.put(key, sr.getProperty(key));
									}
									list.add(dict);
									continue;
								}

								if (targetType.matches(Map.class, String.class, Object.class)) {
									Map<String, Object> map = new HashMap<>();
									for (String key : sr.getPropertyKeys()) {
										map.put(key, sr.getProperty(key));
									}
									list.add(map);
									continue;
								}

							}
						}
					}
					return Arguments.of(list.toArray());
				});

			return stream;
		} catch (AssertionError e) {
			throw new ParameterResolutionException("@ServiceSource: " + e.getMessage(), e);
		}
	}

	@Override
	public void accept(ServiceSource annotation) {
		this.serviceSource = annotation;

	}

}
