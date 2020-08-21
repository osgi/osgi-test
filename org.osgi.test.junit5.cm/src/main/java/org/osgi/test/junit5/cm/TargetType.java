package org.osgi.test.junit5.cm;

import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class TargetType {

	private Class<?>	type;
	private List<Type>	genericTypes;

	public TargetType(Class<?> type, List<Type> genericType) {
		super();
		this.type = type;
		this.genericTypes = genericType;
	}

	public static TargetType of(Field field) {
		Class<?> type = field.getType();
		Type genericType = field.getGenericType();
		return new TargetType(type, Collections.singletonList(genericType));
	}

	public static TargetType of(Parameter parameter) {

		Class<?> memberType = parameter.getType();

		List<Type> genericTypes = new ArrayList<Type>();

		Type t = parameter.getParameterizedType();
		if (t instanceof ParameterizedType) {
			ParameterizedType pt = (ParameterizedType) t;
			if (pt != null) {
				Type[] ts = pt.getActualTypeArguments();
				for (Type type : ts) {
					genericTypes.add(type);
				}
			}
		}
		return new TargetType(memberType, Collections.unmodifiableList(genericTypes));
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
