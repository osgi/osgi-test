package org.osgi.test.common.inject;

import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class TargetType {

	private Class<?>	type;
	private List<Type>	genericTypes;

	private TargetType(Class<?> type, List<Type> genericType) {
		super();
		this.type = type;
		this.genericTypes = genericType;
	}

	public Class<?> getType() {
		return type;
	}

	public List<Type> getGenericParameterizedTypes() {
		return genericTypes;
	}

	public boolean hasParameterizedTypes() {
		return !genericTypes.isEmpty();
	}

	public Optional<Type> getFirstGenericTypes() {

		if (genericTypes.isEmpty()) {
			return Optional.empty();
		} else {
			return Optional.of(genericTypes.get(0));
		}
	}

	public static TargetType of(Field field) {
		Class<?> type = field.getType();
		Type genericType = field.getGenericType();
		if (genericType instanceof ParameterizedType) {
			return of(type, (ParameterizedType) genericType);
		}
		return new TargetType(type, Collections.emptyList());
	}

	public static TargetType of(Type type, ParameterizedType pt) {

		List<Type> genericTypes = new ArrayList<Type>();

		if (pt != null) {
			Type[] ts = pt.getActualTypeArguments();
			for (Type t : ts) {
				genericTypes.add(t);
			}
		}

		return new TargetType((Class<?>) type, genericTypes);

	}

	public static TargetType of(Parameter parameter) {

		Class<?> memberType = parameter.getType();

		Type pt = parameter.getParameterizedType();
		if (pt instanceof ParameterizedType) {
			return of(memberType, (ParameterizedType) pt);
		}
		return new TargetType(memberType, Collections.emptyList());
	}

	public boolean matches(Class<?> compareType) {
		return Objects.equals(type, compareType);
	}

	public boolean matches(Class<?> compareType, List<Type> compareGenericTypes) {
		return matches(compareType) && Objects.equals(genericTypes, compareGenericTypes);
	}

	public boolean matches(Class<?> compareType, Type... compareGenericType) {
		return matches(compareType, Arrays.asList(compareGenericType));
	}

}
