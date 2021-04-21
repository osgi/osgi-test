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

package org.osgi.test.junit5.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.osgi.test.junit5.testutils.TestKitUtils.assertThatTest;
import static org.osgi.test.junit5.testutils.TestKitUtils.checkClass;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import org.assertj.core.api.AbstractThrowableAssert;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;
import org.osgi.test.common.context.CloseableBundleContext;
import org.osgi.test.common.dictionary.Dictionaries;
import org.osgi.test.common.exceptions.Exceptions;
import org.osgi.test.junit5.ExecutorExtension;
import org.osgi.test.junit5.ExecutorParameter;
import org.osgi.test.junit5.types.Foo;

@ExtendWith(ExecutorExtension.class)
abstract class AbstractServiceExtensionTest {

	@ExtendWith(ServiceExtension.class)
	abstract static class TestBase {
		static AtomicReference<SoftAssertions>		lastSoftAssertions	= new AtomicReference<>();
		static AtomicReference<Foo>					lastService			= new AtomicReference<>();
		static AtomicReference<List<Foo>>			lastServices		= new AtomicReference<>();
		SoftAssertions								softly;

		Foo getService() {
			return null;
		}

		List<Foo> getServices() {
			return null;
		}

		@BeforeAll
		static void beforeAll() {
			lastSoftAssertions.set(null);
			lastService.set(null);
			lastServices.set(null);
		}

		@BeforeEach
		void beforeEach() {
			lastSoftAssertions.set(softly = new SoftAssertions());
			lastService.set(null);
			lastServices.set(null);
		}

		@Test
		abstract void test() throws Exception;

		@AfterEach
		void afterEach() {
			lastService.set(getService());
			lastServices.set(getServices());
		}
	}

	@ExecutorParameter
	protected ScheduledExecutorService	executor;
	protected BundleContext				bundleContext;
	protected String					testMethodName;

	@BeforeEach
	public void beforeEach(TestInfo testInfo) {
		testMethodName = testInfo.getTestMethod()
			.get()
			.getName();

		bundleContext = CloseableBundleContext.proxy(FrameworkUtil.getBundle(ServiceExtensionTest.class));
	}

	@AfterEach
	public void afterEach() throws Exception {
		((AutoCloseable) bundleContext).close();
		assertThat(FrameworkUtil.getBundle(getClass())
			.getRegisteredServices()).as("registered services")
				.isNull();
	}

	protected AbstractThrowableAssert<?, ? extends Throwable> futureAssertThatTest(Class<?> testClass) {
		return futureAssertThatTest(testClass, 10);
	}

	protected AbstractThrowableAssert<?, ? extends Throwable> futureAssertThatTest(Class<?> testClass, int delay) {
		checkClass(testClass);
		try {
			return executor.schedule(() -> assertThatTest(testClass), delay, TimeUnit.MILLISECONDS)
				.get(delay + 200000, TimeUnit.MILLISECONDS);
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			throw Exceptions.duck(e);
		}
	}

	protected ScheduledFuture<ServiceRegistration<Foo>> schedule(Foo afoo, String key, String value) {
		return executor.schedule(() -> bundleContext.registerService(Foo.class, afoo,
			Dictionaries.dictionaryOf(key, value, "case", testMethodName)), 10, TimeUnit.MILLISECONDS);
	}

	protected ScheduledFuture<ServiceRegistration<Foo>> schedule(Foo afoo) {
		return executor.schedule(
			() -> bundleContext.registerService(Foo.class, afoo, Dictionaries.dictionaryOf("case", testMethodName)), 10,
			TimeUnit.MILLISECONDS);
	}

	protected static final String	FILTER				= "(foo=bar)";
	protected static final String	MALFORMED_FILTER	= "(foo=baz";
}
