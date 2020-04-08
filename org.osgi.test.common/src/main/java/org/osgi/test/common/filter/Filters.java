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
package org.osgi.test.common.filter;

import java.util.function.Function;

import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkUtil;
import org.osgi.test.common.exceptions.FunctionWithException;

public class Filters {

	private Filters() {}

	private static final Function<String, Filter> createFilter = FunctionWithException
		.asFunction(FrameworkUtil::createFilter);

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
		String filter = String.format(format, args);
		return createFilter.apply(filter);
	}

}
