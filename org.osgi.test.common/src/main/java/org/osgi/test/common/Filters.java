/*
 * Copyright (c) OSGi Alliance (2019). All Rights Reserved.
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

package org.osgi.test.common;

import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;

public class Filters {

	private Filters() {}

	/**
	 * Utility method for creating a {@link Filter} using a format string.
	 * <p>
	 * See {@link String#format(String, Object...)}
	 *
	 * @param format a format string
	 * @param args format arguments
	 * @return filter
	 */
	public static Filter format(String format, Object... args) {
		try {
			return FrameworkUtil.createFilter(String.format(format, args));
		} catch (InvalidSyntaxException ise) {
			throwsUnchecked(ise);
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	private static <E extends Throwable> void throwsUnchecked(Throwable throwable) throws E {
		throw (E) throwable;
	}

}
