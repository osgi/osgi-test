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
package org.osgi.test.common.event;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class Events {

	public static <E> Predicate<E> any() {
		return element -> true;
	}

	public static <E> Predicate<Optional<E>> isPresentAnd(final Predicate<E> predicate) {
		return optional -> optional.isPresent() && predicate.test(optional.get());
	}

	public static <E> Predicate<List<E>> element(int index, final Predicate<E> predicate) {
		return elements -> elements.size() > 0 && elements.size() >= index && predicate.test(elements.get(index));
	}

	public static <E> Predicate<List<E>> first(final Predicate<E> predicate) {
		return elements -> element(0, predicate).test(elements);
	}

	public static <E> Predicate<List<E>> last(final Predicate<E> predicate) {
		return elements -> element(elements.size() - 1, predicate).test(elements);
	}

	public static <E> Predicate<List<E>> all(final Predicate<E> predicate) {
		return elements -> elements.stream()
			.allMatch(predicate);
	}

	public static <E> Predicate<List<E>> any(final Predicate<E> predicate) {
		return elements -> elements.stream()
			.anyMatch(predicate);
	}

	public static <E> Predicate<List<E>> none(final Predicate<E> predicate) {
		return elements -> elements.stream()
			.noneMatch(predicate);
	}

	@SafeVarargs
	public static <E> Predicate<List<E>> ordered(final Predicate<E> predicate, final Predicate<E>... predicatesNext) {
		return elements -> {

			List<Predicate<E>> predicates = new ArrayList<>();
			predicates.add(predicate);
			predicates.addAll(Arrays.asList(predicatesNext));

			long skip = 0;
			for (int i = 0; i <= predicatesNext.length - 1; i++) {
				Predicate<E> predicateBefore = predicates.get(i);
				Predicate<E> predicateAfter = predicates.get(i + 1);

				Optional<E> before = elements.stream()
					.skip(skip)
					.filter(predicateBefore)
					.findFirst();
				Optional<E> after = elements.stream()
					.skip(skip)
					.filter(predicateAfter)
					.findFirst();

				if (before.isPresent() && after.isPresent()) {

					int indexBefore = elements.indexOf(before.get());
					int indexAfter = elements.indexOf(after.get());
					if (indexBefore >= indexAfter) {
						return false;
					}
					skip = indexBefore;
				} else {
					return false;
				}
			}
			return true;
		};
	}

	public static <E> Predicate<List<E>> hasSize(long size, final Predicate<E> predicate) {
		return elements -> elements.stream()
			.filter(predicate)
			.count() == size;
	}

	public static <E> Predicate<List<E>> hasMoreThen(long size, final Predicate<E> predicate) {
		return elements -> elements.stream()
			.filter(predicate)
			.count() > size;
	}

	public static <E> Predicate<List<E>> hasLessThen(long size, final Predicate<E> predicate) {
		return elements -> elements.stream()
			.filter(predicate)
			.count() < size;
	}
}
