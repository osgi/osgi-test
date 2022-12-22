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

import static java.util.stream.Collectors.toList;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.support.AnnotationConsumer;
import org.osgi.framework.ServiceReference;
import org.osgi.test.common.inject.TargetType;
import org.osgi.test.common.service.ServiceConfiguration;

public class ServiceArgumentsProvider implements ArgumentsProvider, AnnotationConsumer<ServiceSource> {
	private ServiceSource source;

	@SuppressWarnings("resource")
	@Override
	public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
		List<TargetType> targetTypes = context.getElement()
			.filter(Method.class::isInstance)
			.map(Method.class::cast)
			.map(Method::getParameters)
			.map(params -> Arrays.stream(params)
				.map(TargetType::of)
				.collect(toList()))
			.orElse(Collections.emptyList());
		try {
			ServiceConfiguration<?> sc = ServiceExtension.getServiceConfiguration(source.serviceType(), source.filter(), source.filterArguments(), source.cardinality(), source.timeout(), context);
			Stream<Object[]> arguments = sc.getServiceReferences()
				.stream()
				.filter(Objects::nonNull)
				.map(reference -> targetTypes.stream()
					.map(targetType -> {
						if (targetType.matches(source.serviceType())) {
							return sc.getTracked()
								.get(reference);
						}

						if (targetType.matches(ServiceReference.class, source.serviceType())) {
							return reference;
						}

						if (targetType.matches(Dictionary.class, String.class, Object.class)) {
							Dictionary<String, Object> dict = new Hashtable<>();
							for (String key : reference.getPropertyKeys()) {
								dict.put(key, reference.getProperty(key));
							}
							return dict;
						}

						if (targetType.matches(Map.class, String.class, Object.class)) {
							Map<String, Object> map = new HashMap<>();
							for (String key : reference.getPropertyKeys()) {
								map.put(key, reference.getProperty(key));
							}
							return map;
						}
						// special value to indicate it should be filtered out
						return this;
					})
					.filter(argument -> argument != this)
					.toArray());
			return arguments.map(Arguments::of);
		} catch (AssertionError e) {
			throw new ParameterResolutionException("@ServiceSource: " + e.getMessage(), e);
		}
	}

	@Override
	public void accept(ServiceSource source) {
		this.source = source;
	}
}
