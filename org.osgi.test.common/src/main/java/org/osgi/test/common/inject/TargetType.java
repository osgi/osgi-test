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

package org.osgi.test.common.inject;

import static java.util.Objects.requireNonNull;

import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class TargetType {
	private final Type type;
	private final String	name;

	private TargetType(Type type, String name) {
		this.type = requireNonNull(type);
		this.name = requireNonNull(name);
	}

	public Class<?> getType() {
		if (type instanceof ParameterizedType) {
			return (Class<?>) ((ParameterizedType) type).getRawType();
		}
		return (Class<?>) type;
	}

	/**
	 * @since 1.1
	 */
	public Type getGenericType() {
		return type;
	}

	/**
	 * @since 1.1
	 */
	public String getName() {
		return name;
	}

	public List<Type> getGenericParameterizedTypes() {
		if (type instanceof ParameterizedType) {
			return Arrays.asList(((ParameterizedType) type).getActualTypeArguments());
		}
		return Collections.emptyList();
	}

	public boolean hasParameterizedTypes() {
		if (type instanceof ParameterizedType) {
			return true;
		}
		return false;
	}

	public Optional<Type> getFirstGenericTypes() {
		if (type instanceof ParameterizedType) {
			return Optional.of(((ParameterizedType) type).getActualTypeArguments()[0]);
		}
		return Optional.empty();
	}

	public static TargetType of(Field field) {
		return new TargetType(field.getGenericType(), field.getName());
	}

	public static TargetType of(Parameter parameter) {
		return new TargetType(parameter.getParameterizedType(), parameter.getName());
	}

	@Deprecated
	public static TargetType of(Type type, ParameterizedType parameterizedType) {
		assert type == parameterizedType.getRawType();
		return new TargetType(parameterizedType, "<unknown>");
	}

	public boolean matches(Class<?> compareType) {
		return Objects.equals(getType(), compareType);
	}

	public boolean matches(Class<?> compareType, List<Type> compareGenericTypes) {
		return matches(compareType) && Objects.equals(getGenericParameterizedTypes(), compareGenericTypes);
	}

	public boolean matches(Class<?> compareType, Type... compareGenericType) {
		return matches(compareType, Arrays.asList(compareGenericType));
	}

	@Override
	public String toString() {
		return name + " " + type;
	}
}
