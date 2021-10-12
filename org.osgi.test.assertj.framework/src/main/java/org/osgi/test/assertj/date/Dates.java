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

package org.osgi.test.assertj.date;

import java.time.Instant;
import java.util.Date;

import org.assertj.core.api.AbstractDateAssert;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.InstanceOfAssertFactory;

public class Dates {

	private Dates() {}

	public static long getTime(Object expected) {
		if (expected == null) {
			throw new IllegalArgumentException("expected cannot be null");
		} else if (expected instanceof Long) {
			return (Long) expected;
		} else if (expected instanceof Date) {
			return ((Date) expected).getTime();
		} else if (expected instanceof Instant) {
			return ((Instant) expected).toEpochMilli();
		} else {
			throw new IllegalArgumentException("Expected must be a long, Date or Instant");
		}
	}

	public static final InstanceOfAssertFactory<Long, AbstractDateAssert<?>> LONG_AS_DATE = new InstanceOfAssertFactory<>(
	Long.class, date -> Assertions.assertThat(new Date(date)));

}
