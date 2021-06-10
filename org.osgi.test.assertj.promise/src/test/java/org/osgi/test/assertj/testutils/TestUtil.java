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

package org.osgi.test.assertj.testutils;

import java.util.concurrent.TimeUnit;

import org.assertj.core.api.StandardSoftAssertionsProvider;
import org.osgi.test.common.exceptions.Exceptions;

public class TestUtil {
	private TestUtil() {}

	static final long TIMEOUT_NS = TimeUnit.SECONDS.toNanos(10L);

	public static void waitForThreadToWait(Thread thread, StandardSoftAssertionsProvider softly) {
		final long waitTime = TIMEOUT_NS;
		final long startTime = System.nanoTime();
		int waitCount = 0;
		try {
			OUTER: while (true) {
				Thread.sleep(100);
				final Thread.State state = thread.getState();
				// System.err.println("Thread: " + thread + ", state: " +
				// state);
				switch (state) {
					case TERMINATED :
					case TIMED_WAITING :
					case WAITING :
					case BLOCKED :
						if (waitCount++ > 5) {
							break OUTER;
						}
						break;
					default :
						waitCount = 0;
						break;
				}
				final long elapsed = System.nanoTime() - startTime;
				if (elapsed > waitTime) {
					throw new InterruptedException("Thread still hasn't entered wait state after "
						+ TimeUnit.NANOSECONDS.toMillis(waitTime) + "ms");
				}
			}
		} catch (InterruptedException e) {
			throw Exceptions.duck(e);
		}
		// Check that it hasn't terminated.
		softly.assertThat(thread.getState())
			.as("thread:" + thread.getName())
			.isIn(Thread.State.WAITING, Thread.State.TIMED_WAITING, Thread.State.BLOCKED);
	}

}
