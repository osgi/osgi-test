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

package org.osgi.test.junit5.cm;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Predicate;

import org.osgi.test.common.annotation.config.WithConfiguration;
import org.osgi.test.common.annotation.config.WithConfigurations;
import org.osgi.test.common.annotation.config.WithFactoryConfiguration;
import org.osgi.test.common.annotation.config.WithFactoryConfigurations;

class AnnotationUtil {

	private AnnotationUtil() {}

	private static Predicate<Annotation> ANY_WITH_CONF_ANNNOTATIONS = t -> t.annotationType()
		.equals(WithFactoryConfiguration.class)
		|| t.annotationType()
			.equals(WithFactoryConfigurations.class)
		|| t.annotationType()
			.equals(WithConfiguration.class)
		|| t.annotationType()
			.equals(WithConfigurations.class);

	public static List<Annotation> findAllConfigAnnotations(AnnotatedElement annotatedElement) {
		return findAllAnnotationsMatching(annotatedElement, ANY_WITH_CONF_ANNNOTATIONS);
	}

	private static List<Annotation> findAllAnnotationsMatching(AnnotatedElement annotatedElement,
		Predicate<Annotation> test) {
		final ArrayList<Annotation> found = new ArrayList<>();
		findAllAnnotationsMatching(annotatedElement, test, found, new HashSet<>());
		return found;
	}

	private static void findAllAnnotationsMatching(AnnotatedElement annotatedElement, Predicate<Annotation> test,
		ArrayList<? super Annotation> found, Set<Entry<AnnotatedElement, Annotation>> visited) {
		Annotation[] declaredAnnotations = annotatedElement.getDeclaredAnnotations();

		for (Annotation ann : declaredAnnotations) {
			if (isJavaLangAnnotation(ann) || isJunitAnnotation(ann)) {
				continue;
			}

			if (visited.add(new AbstractMap.SimpleEntry<>(annotatedElement, ann))) {
				findAllAnnotationsMatching(ann.annotationType(), test, found, visited);
			}

			if (test.test(ann)) {
				found.add(ann);
			}
		}
	}

	private static boolean isJavaLangAnnotation(Annotation annotation) {

		return annotation.annotationType()
			.getName()
			.startsWith("java.lang.annotation");
	}

	private static boolean isJunitAnnotation(Annotation annotation) {

		return annotation.annotationType()
			.getName()
			.startsWith("org.junit");
	}

}
