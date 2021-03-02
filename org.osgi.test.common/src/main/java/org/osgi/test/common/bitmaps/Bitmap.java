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

package org.osgi.test.common.bitmaps;

import java.util.Map;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Useful class for producing human-friendly dumps of bit fields. Has support
 * functions for single-bit values ({@link #toString(int)} and multi-bit fields
 * {@link #maskToString(int)}.
 */
public class Bitmap {

	final int[]					types;
	final int					KNOWN_MASK;
	final int					UNKNOWN_MASK;
	final IntFunction<String>	mappingFunction;

	/**
	 * Constructs a bitmap
	 *
	 * @param types the full set of known allowed field values in this bit map.
	 * @param mappingFunction function to produce a human-friendly string for
	 *            the given map.
	 */
	public Bitmap(int[] types, IntFunction<String> mappingFunction) {
		this.types = types;
		this.mappingFunction = mappingFunction;
		this.KNOWN_MASK = IntStream.of(types)
			.reduce((x, y) -> x | y)
			.getAsInt();
		this.UNKNOWN_MASK = ~KNOWN_MASK;
	}

	public Bitmap(Map<Integer, String> mapping) {
		this(mapping
			.keySet()
			.stream()
			.mapToInt(
				Integer::intValue)
			.sorted()
			.toArray(), mapping::get);
	}

	/**
	 * @return A mask of all known allowed values in this bit mask.
	 */
	public int getKnownMask() {
		return KNOWN_MASK;
	}

	/**
	 * @return A mask of all values that are not known to be allowed in this bit
	 *         mask.
	 */
	public int getUnknownMask() {
		return KNOWN_MASK;
	}

	/**
	 * Checks to see if it has exactly one bit set.
	 *
	 * @param type the bit field to check
	 * @return {@code true} if only one bit set, otherwise {@code false}.
	 */
	public static boolean hasSingleBit(int type) {
		return ((type & (type - 1)) == 0);
	}

	/**
	 * Return a string representation of the given type. Expects to be passed a
	 * field with exactly one bit set - if you want to dump a bit mask, then
	 * please use {@link #maskToString(int)}
	 *
	 * @param type the field being converted.
	 * @return Description of the given field.
	 * @throws IllegalArgumentException if the field has more than one bit set.
	 * @see #maskToString(int)
	 */
	public String toString(int type) {
		if (!hasSingleBit(type)) {
			throw new IllegalArgumentException(
				"Multiple bits set in type (" + type + ") - do you mean to use maskToString()?");
		}
		final String retval = mappingFunction.apply(type);
		return retval == null ? "UNKNOWN" : retval;
	}

	/**
	 * Return a string representation of the given type mask. If the mask
	 * contains multiple bits, their string representations are separated by
	 * pipes (" | ").
	 *
	 * @param mask the field being converted.
	 * @return Description of the given field.
	 * @see #toString(int)
	 */
	public String maskToString(int mask) {
		Stream<String> bits = IntStream.of(
			types)
			.filter(x -> (x & mask) != 0)
			.mapToObj(this::toString);

		if ((mask & UNKNOWN_MASK) != 0) {
			bits = Stream.concat(bits, Stream.of("UNKNOWN"));
		}
		return mask + ":" + bits.collect(Collectors.joining(" | "));
	}
}
