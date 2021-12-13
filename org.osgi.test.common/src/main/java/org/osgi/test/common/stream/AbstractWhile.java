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

package org.osgi.test.common.stream;

import static java.util.Objects.requireNonNull;

import java.util.Comparator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;

abstract class AbstractWhile<T> implements Spliterator<T>, Consumer<T> {
	final Spliterator<T>		spliterator;
	final Predicate<? super T>	predicate;
	T							item;

	AbstractWhile(Spliterator<T> spliterator, Predicate<? super T> predicate) {
		this.spliterator = requireNonNull(spliterator);
		this.predicate = requireNonNull(predicate);
	}

	@Override
	public Spliterator<T> trySplit() {
		return null;
	}

	@Override
	public long estimateSize() {
		return spliterator.estimateSize();
	}

	@Override
	public long getExactSizeIfKnown() {
		return -1L;
	}

	@Override
	public int characteristics() {
		return spliterator.characteristics() & ~(Spliterator.SIZED | Spliterator.SUBSIZED);
	}

	@Override
	public Comparator<? super T> getComparator() {
		return spliterator.getComparator();
	}

	@Override
	public void accept(T item) {
		this.item = item;
	}
}
