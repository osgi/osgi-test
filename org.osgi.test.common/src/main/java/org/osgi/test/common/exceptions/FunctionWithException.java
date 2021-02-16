/*
 * Copyright (c) OSGi Alliance (2019, 2021). All Rights Reserved.
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

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Function interface that allows exceptions.
 *
 * @param <T> the type of the argument
 * @param <R> the result type
 */
@FunctionalInterface
public interface FunctionWithException<T, R> {
	R apply(T t) throws Exception;

	default Function<T, R> orElseThrow() {
		return t -> {
			try {
				return apply(t);
			} catch (Exception e) {
				throw Exceptions.duck(e);
			}
		};
	}

	default Function<T, R> orElse(R orElse) {
		return t -> {
			try {
				return apply(t);
			} catch (Exception e) {
				return orElse;
			}
		};
	}

	default Function<T, R> orElseGet(Supplier<? extends R> orElseGet) {
		requireNonNull(orElseGet);
		return t -> {
			try {
				return apply(t);
			} catch (Exception e) {
				return orElseGet.get();
			}
		};
	}

	static <T, R> Function<T, R> asFunction(FunctionWithException<T, R> unchecked) {
		return unchecked.orElseThrow();
	}

	static <T, R> Function<T, R> asFunctionOrElse(FunctionWithException<T, R> unchecked, R orElse) {
		return unchecked.orElse(orElse);
	}

	static <T, R> Function<T, R> asFunctionOrElseGet(FunctionWithException<T, R> unchecked,
		Supplier<? extends R> orElseGet) {
		return unchecked.orElseGet(orElseGet);
	}
}
