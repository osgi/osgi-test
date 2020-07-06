/*
 * Copyright (c) OSGi Alliance (2020). All Rights Reserved.
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

package org.osgi.test.common.inject;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;
import static org.osgi.test.common.exceptions.Exceptions.unchecked;

import java.lang.annotation.Annotation;
import java.lang.annotation.Inherited;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

public class FieldInjector {

	private static final String									annotationTypeMustNotBeNull	= "annotationType must not be null";
	private static final String									classMustNotBeNull			= "class must not be null";
	private static final Comparator<Field>						fieldComparator				= (a, b) -> Integer
		.compare(
		a.getName()
		.hashCode(),
		b.getName()
			.hashCode());
	private static final Predicate<Class<? extends Annotation>>	isJavaLangAnnotation		= a -> (a != null
		&& a
		.getName()
		.startsWith("java.lang.annotation"));
	private static final Predicate<Class<?>>					isSearchable				= c -> (c != null
		&& c != Object.class);
	private static final String									predicateMustNotBeNull		= "predicate must not be null";

	public static List<Field> findAnnotatedNonStaticFields(Class<?> clazz, Class<? extends Annotation> annotationType) {
		return findAnnotatedFields(clazz, annotationType, m -> !Modifier.isStatic(m.getModifiers()));
	}

	public static List<Field> findAnnotatedFields(Class<?> clazz, Class<? extends Annotation> annotationType) {
		return findAnnotatedFields(clazz, annotationType, x -> true);
	}

	public static List<Field> findAnnotatedFields(Class<?> clazz, Class<? extends Annotation> annotationType,
		Predicate<Field> predicate) {

		requireNonNull(annotationType, annotationTypeMustNotBeNull);
		requireNonNull(predicate, predicateMustNotBeNull);

		Predicate<Field> annotated = field -> findAnnotation(field, annotationType).isPresent();

		return findAllFieldsInHierarchy(requireNonNull(clazz, classMustNotBeNull)).stream()
			.filter(annotated.and(predicate))
			.collect(collectingAndThen(toList(), Collections::unmodifiableList));
	}

	public static void setField(Field field, Object instance, Object value) {
		unchecked(() -> {
			if (!field.isAccessible()) {
				field.setAccessible(true);
			}
			field.set(instance, value);
		});
	}

	static List<Field> findAllFieldsInHierarchy(Class<?> clazz) {
		requireNonNull(clazz, classMustNotBeNull);

		List<Field> localFields = toSortedMutableList(clazz.getDeclaredFields())
			.stream()
			.filter(field -> !field.isSynthetic())
			.collect(toList());
		List<Field> superclassFields = getSuperclassFields(clazz)
			.stream()
			.filter(field -> !isFieldShadowedByLocalFields(field, localFields))
			.collect(toList());
		List<Field> interfaceFields = getInterfaceFields(clazz)
			.stream()
			.filter(field -> !isFieldShadowedByLocalFields(field, localFields))
			.collect(toList());

		List<Field> fields = new ArrayList<>();
		fields.addAll(superclassFields);
		fields.addAll(interfaceFields);
		fields.addAll(localFields);
		return fields;
	}

	static <A extends Annotation> Optional<A> findAnnotation(AnnotatedElement element, Class<A> annotationType) {
		requireNonNull(annotationType, annotationTypeMustNotBeNull);
		boolean inherited = annotationType.isAnnotationPresent(Inherited.class);
		return findAnnotation(element, annotationType, inherited, new HashSet<>());
	}

	static <A extends Annotation> Optional<A> findAnnotation(AnnotatedElement element, Class<A> annotationType,
		boolean inherited, Set<Annotation> visited) {

		if (element == null) {
			return Optional.empty();
		}

		A annotation = element.getDeclaredAnnotation(requireNonNull(annotationType, annotationTypeMustNotBeNull));
		if (annotation != null) {
			return Optional.of(annotation);
		}

		Optional<A> directMetaAnnotation = findMetaAnnotation(annotationType, element.getDeclaredAnnotations(),
			inherited, visited);
		if (directMetaAnnotation.isPresent()) {
			return directMetaAnnotation;
		}

		if (element instanceof Class) {
			Class<?> clazz = (Class<?>) element;

			for (Class<?> ifc : clazz.getInterfaces()) {
				if (ifc != Annotation.class) {
					Optional<A> annotationOnInterface = findAnnotation(ifc, annotationType, inherited, visited);
					if (annotationOnInterface.isPresent()) {
						return annotationOnInterface;
					}
				}
			}

			if (inherited) {
				Class<?> superclass = clazz.getSuperclass();
				if (superclass != null && superclass != Object.class) {
					Optional<A> annotationOnSuperclass = findAnnotation(superclass, annotationType, inherited, visited);
					if (annotationOnSuperclass.isPresent()) {
						return annotationOnSuperclass;
					}
				}
			}
		}

		return findMetaAnnotation(annotationType, element.getAnnotations(), inherited, visited);
	}

	static <A extends Annotation> Optional<A> findMetaAnnotation(
		Class<A> annotationType,
		Annotation[] candidates, boolean inherited, Set<Annotation> visited) {

		for (Annotation candidateAnnotation : candidates) {
			Class<? extends Annotation> candidateAnnotationType = candidateAnnotation.annotationType();
			if (!isJavaLangAnnotation.test(candidateAnnotationType) && visited.add(candidateAnnotation)) {
				Optional<A> metaAnnotation = findAnnotation(candidateAnnotationType, annotationType, inherited,
					visited);
				if (metaAnnotation.isPresent()) {
					return metaAnnotation;
				}
			}
		}
		return Optional.empty();
	}

	static List<Field> getInterfaceFields(Class<?> clazz) {
		List<Field> allInterfaceFields = new ArrayList<>();
		for (Class<?> ifc : clazz.getInterfaces()) {
			List<Field> localInterfaceFields = toSortedMutableList(ifc.getFields());

			List<Field> superinterfaceFields = getInterfaceFields(ifc)
				.stream()
					.filter(field -> !isFieldShadowedByLocalFields(field, localInterfaceFields))
					.collect(toList());

			allInterfaceFields.addAll(superinterfaceFields);
			allInterfaceFields.addAll(localInterfaceFields);
		}
		return allInterfaceFields;
	}

	static List<Field> getSuperclassFields(Class<?> clazz) {
		Class<?> superclass = clazz.getSuperclass();
		if (!isSearchable.test(superclass)) {
			return Collections.emptyList();
		}
		return findAllFieldsInHierarchy(superclass);
	}

	static boolean isFieldShadowedByLocalFields(Field field, List<Field> localFields) {
		return localFields.stream()
			.anyMatch(local -> local.getName()
				.equals(field.getName()));
	}

	static List<Field> toSortedMutableList(Field[] fields) {
		return Arrays.stream(fields)
			.sorted(
				fieldComparator)
			.collect(toCollection(ArrayList::new));
	}

	public static void assertParameterIsOfType(Class<?> actual, Class<?> expected,
		Class<? extends Annotation> annotationType, Function<String, ? extends RuntimeException> exception) {
		if (actual != expected) {
			throw exception.apply("Can only resolve @" + annotationType.getSimpleName()
				+ " parameter of type " + expected.getName() + " but was: " + actual.getName());
		}
	}

	public static void assertFieldIsOfType(Field field, Class<?> expected, Class<? extends Annotation> annotationType,
		Function<String, ? extends RuntimeException> exception) {
		if (field.getType() != expected) {
			throw exception.apply(
				"[" + field.getName() + "] Can only resolve @" + annotationType.getSimpleName() + " field of type "
					+ expected.getName() + " but was: " + field.getType()
						.getName());
		}
		if (Modifier.isFinal(field.getModifiers()) || Modifier.isPrivate(field.getModifiers())) {
			throw exception.apply(
				"@" + annotationType.getSimpleName() + " field [" + field.getName() + "] must not be private or final");
		}
	}

}
