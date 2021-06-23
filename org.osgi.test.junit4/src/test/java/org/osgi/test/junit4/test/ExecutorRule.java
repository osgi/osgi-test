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

package org.osgi.test.junit4.test;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class ExecutorRule implements TestRule {

	private ScheduledExecutorService	executor;

	public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay) {
		return executor.schedule(callable, delay, TimeUnit.MILLISECONDS);
	}

	@Override
	public Statement apply(Statement statement, Description description) {
		return new Statement() {
			@Override
			public void evaluate() throws Throwable {
				executor = Executors.newSingleThreadScheduledExecutor();
				try {
					statement.evaluate();
				} finally {
					executor.shutdownNow();
					executor.awaitTermination(100, TimeUnit.MILLISECONDS);
				}
			}
		};
	}

}
