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

package org.osgi.test.common.exceptions;

import static java.util.Objects.requireNonNull;

import java.util.function.BooleanSupplier;
import java.util.function.Predicate;

/**
 * Predicate interface that allows exceptions.
 *
 * @param <T> the type of the argument
 */
@FunctionalInterface
public interface PredicateWithException<T> {
	boolean test(T t) throws Exception;

	default Predicate<T> orElseThrow() {
		return t -> {
			try {
				return test(t);
			} catch (Exception e) {
				throw Exceptions.duck(e);
			}
		};
	}

	default Predicate<T> orElse(boolean orElse) {
		return t -> {
			try {
				return test(t);
			} catch (Exception e) {
				return orElse;
			}
		};
	}

	default Predicate<T> orElseGet(BooleanSupplier orElseGet) {
		requireNonNull(orElseGet);
		return t -> {
			try {
				return test(t);
			} catch (Exception e) {
				return orElseGet.getAsBoolean();
			}
		};
	}

	static <T> Predicate<T> asPredicate(PredicateWithException<T> unchecked) {
		return unchecked.orElseThrow();
	}

	static <T> Predicate<T> asPredicateOrElse(PredicateWithException<T> unchecked, boolean orElse) {
		return unchecked.orElse(orElse);
	}

	static <T> Predicate<T> asPredicateOrElseGet(PredicateWithException<T> unchecked, BooleanSupplier orElseGet) {
		return unchecked.orElseGet(orElseGet);
	}
}
