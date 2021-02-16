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

import java.util.function.Supplier;

/**
 * Supplier interface that allows exceptions.
 *
 * @param <R> the result type
 */
@FunctionalInterface
public interface SupplierWithException<R> {
	R get() throws Exception;

	default Supplier<R> orElseThrow() {
		return () -> {
			try {
				return get();
			} catch (Exception e) {
				throw Exceptions.duck(e);
			}
		};
	}

	default Supplier<R> orElse(R orElse) {
		return () -> {
			try {
				return get();
			} catch (Exception e) {
				return orElse;
			}
		};
	}

	default Supplier<R> orElseGet(Supplier<? extends R> orElseGet) {
		requireNonNull(orElseGet);
		return () -> {
			try {
				return get();
			} catch (Exception e) {
				return orElseGet.get();
			}
		};
	}

	static <R> Supplier<R> asSupplier(SupplierWithException<R> unchecked) {
		return unchecked.orElseThrow();
	}

	static <R> Supplier<R> asSupplierOrElse(SupplierWithException<R> unchecked, R orElse) {
		return unchecked.orElse(orElse);
	}

	static <R> Supplier<R> asSupplierOrElseGet(SupplierWithException<R> unchecked, Supplier<? extends R> orElseGet) {
		return unchecked.orElseGet(orElseGet);
	}
}
