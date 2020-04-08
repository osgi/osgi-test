/*
 * Copyright (c) OSGi Alliance (2019-2020). All Rights Reserved.
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
package org.osgi.test.common.exceptions;

import static java.util.Objects.requireNonNull;

import java.util.function.BiPredicate;
import java.util.function.BooleanSupplier;

/**
 * BiPredicate interface that allows exceptions.
 *
 * @param <T> the type of the first argument
 * @param <U> the type of the second argument
 */
@FunctionalInterface
public interface BiPredicateWithException<T, U> {
	boolean test(T t, U u) throws Exception;

	default BiPredicate<T, U> orElseThrow() {
		return (t, u) -> {
			try {
				return test(t, u);
			} catch (Exception e) {
				throw Exceptions.duck(e);
			}
		};
	}

	default BiPredicate<T, U> orElse(boolean orElse) {
		return (t, u) -> {
			try {
				return test(t, u);
			} catch (Exception e) {
				return orElse;
			}
		};
	}

	default BiPredicate<T, U> orElseGet(BooleanSupplier orElseGet) {
		requireNonNull(orElseGet);
		return (t, u) -> {
			try {
				return test(t, u);
			} catch (Exception e) {
				return orElseGet.getAsBoolean();
			}
		};
	}

	static <T, U> BiPredicate<T, U> asBiPredicate(BiPredicateWithException<T, U> unchecked) {
		return unchecked.orElseThrow();
	}

	static <T, U> BiPredicate<T, U> asBiPredicateOrElse(BiPredicateWithException<T, U> unchecked, boolean orElse) {
		return unchecked.orElse(orElse);
	}

	static <T, U> BiPredicate<T, U> asBiPredicateOrElseGet(BiPredicateWithException<T, U> unchecked,
		BooleanSupplier orElseGet) {
		return unchecked.orElseGet(orElseGet);
	}
}
