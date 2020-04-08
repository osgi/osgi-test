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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.StringJoiner;
import java.util.concurrent.Callable;

public class Exceptions {
	private Exceptions() {}

	public static <V> V unchecked(Callable<? extends V> callable) {
		try {
			return callable.call();
		} catch (Exception t) {
			throw duck(t);
		}
	}

	public static void unchecked(RunnableWithException runnable) {
		try {
			runnable.run();
		} catch (Exception t) {
			throw duck(t);
		}
	}

	public static RuntimeException duck(Throwable t) {
		Exceptions.throwsUnchecked(t);
		throw new AssertionError("unreachable");
	}

	@SuppressWarnings("unchecked")
	private static <E extends Throwable> void throwsUnchecked(Throwable throwable) throws E {
		throw (E) throwable;
	}

	public static String toString(Throwable t) {
		StringWriter sw = new StringWriter();
		t.printStackTrace(new PrintWriter(sw));
		return sw.toString();
	}

	public static Throwable unrollCause(Throwable t, Class<? extends Throwable> unrollType) {
		requireNonNull(t);
		for (Throwable cause; unrollType.isInstance(t) && ((cause = t.getCause()) != null);) {
			t = cause;
		}
		return t;
	}

	public static Throwable unrollCause(Throwable t) {
		return unrollCause(t, Throwable.class);
	}

	public static String causes(Throwable t) {
		StringJoiner sj = new StringJoiner(" -> ");
		while (t != null) {
			sj.add(t.getMessage());
			t = t.getCause();
		}
		return sj.toString();
	}

}
