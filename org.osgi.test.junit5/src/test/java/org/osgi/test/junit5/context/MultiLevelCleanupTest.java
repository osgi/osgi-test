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

package org.osgi.test.junit5.context;

import static org.osgi.test.junit5.context.MultiLevelCleanupTest.CallbackPoint.AFTER_AFTER_EACH;
import static org.osgi.test.junit5.context.MultiLevelCleanupTest.CallbackPoint.AFTER_ALL;
import static org.osgi.test.junit5.context.MultiLevelCleanupTest.CallbackPoint.AFTER_EACH;
import static org.osgi.test.junit5.context.MultiLevelCleanupTest.CallbackPoint.BEFORE_ALL;
import static org.osgi.test.junit5.context.MultiLevelCleanupTest.CallbackPoint.BEFORE_CLASS;
import static org.osgi.test.junit5.context.MultiLevelCleanupTest.CallbackPoint.BEFORE_EACH;
import static org.osgi.test.junit5.context.MultiLevelCleanupTest.CallbackPoint.INNER_TEST;
import static org.osgi.test.junit5.context.MultiLevelCleanupTest.CallbackPoint.NESTED_AFTER_EACH;
import static org.osgi.test.junit5.context.MultiLevelCleanupTest.CallbackPoint.NESTED_BEFORE_EACH;
import static org.osgi.test.junit5.context.MultiLevelCleanupTest.CallbackPoint.NESTED_TEST;
import static org.osgi.test.junit5.context.MultiLevelCleanupTest.CallbackPoint.PARAMETERIZED_TEST;

import java.lang.reflect.Method;
import java.util.EnumSet;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.osgi.test.common.exceptions.Exceptions;
import org.osgi.test.junit5.testutils.OSGiSoftAssertions;

/*
 * The MultiLevelCleanupTest is a base class for tests that are designed to be run inside EngineTestKit.
 * Its basic purpose is to monitor the setup/tear down of "resources" during test execution as the test scope changes. In Jupiter
 * there are arbitrary levels of scope, but the ones that Extensions have transparent access to are:
 *
 * * Global (not used by our extensions)
 * * Class level - scope begins when Jupiter starts processing a particular test class (before any @BeforeAll
 *   methods are called) and ends after the last test (or @AfterAll callback if it has one) has been processed.
 * * Method level - scope begins when the test class is instantiated, just before @BeforeEach (or the test
 *   method itself, if the class doesn't define a @BeforeEach) is called. Scope ends after the last @AfterEach
 *   is called.
 * * Nested (non-static) classes - as with top-level classes, scope begins before the first @BeforeAll and
 *   ends after the last @AfterAll.
 *   * Nested (non-static) classes will likely in turn have their own test methods (with their own scope)
 *     and may also have their own nested classes (which also have their own scope).
 *
 * Within each scope there are typically multiple callback entry points which Jupiter will execute in a
 * particular defined order. For example, at class scope there is @BeforeAll/@AfterAll, at test scope there is
 * @BeforeEach/@Test/@AfterEach, and for nested test level it will call
 * @BeforeEach/Nested.@BeforeEach/Nested.@Test/Nested.@AfterEach/@AfterEach.
 *
 * The structure of MultiLevelCleanupTest provides methods and a nested test class that reflects these layers
 * of scope and their lifecycle methods. At each of these callback points in the test execution, it will:
 *
 * 1. Check that the resources that should already be set up in earlier callback points (eg,
 *    at a higher scope or by an earlier callback in the same scope) are set up.
 * 2. Check that any resources which should *not* have been set up yet are not set up (which would indicate
 *    that they were not properly cleaned up)
 * 2. Set up a new resource corresponding to the current callback point.
 * 3. Check that the set of resources that are set up now includes the freshly-setup resource
 * 4. Check that the set of resources that are not set up no longer includes the freshly-setup resource.
 *
 * In this context, a "resource" is anything that the BundleContext keeps track of (and that CloseableBundleContext
 * is supposed to automatically clean up on close()) - for example, an installed Bundle,
 * a registered BundleListener/ServiceListener/FrameworkListener, a service registration, etc. Resource checks are
 * performed by subclasses of the AbstractResourceChecker class, where each subclass
 * defines three methods for setting up the resource, checking that it is set up, and checking that it is not
 * set up.
 *
 * To use MultiLevelCleanupTest, you must subclass it and implement the factory methods to return an instance
 * of the correct AbstractResourceChecker subclass specific to the type of resource you are checking. There are
 * three factory methods that concrete subclasses need to implement:
 *
 * 1. getGlobalResourceChecker() - checking that resources are all torn down before and after the whole test run
 * 2. getStaticResourceChecker() - static method for a resource checker that checks that resources are setup/torn
 *    down during the beforeAll()/afterAll() phase. (Can't just use getResourceChecker() because it is called
 *    from a static context.)
 * 3. getResourceChecker() - return a resource checker for the current test instance at the current test scope.
 */
abstract class MultiLevelCleanupTest {

	// Use an enum for Scope rather than
	enum CallbackPoint {
		BEFORE_CLASS("beforeClass"),
		BEFORE_ALL("beforeAll"),
		BEFORE_EACH("beforeEach"),
		INNER_TEST("innerTest"),
		PARAMETERIZED_TEST("parameterizedTest"),
		NESTED_BEFORE_EACH("nested.beforeEach"),
		NESTED_TEST("nested.test"),
		NESTED_AFTER_EACH("nested.afterEach"),
		AFTER_EACH("afterEach"),
		AFTER_AFTER_EACH("afterAfterEach"),
		AFTER_ALL("afterAll"),
		AFTER_CLASS("afterClass");

		final String desc;

		CallbackPoint(String desc) {
			this.desc = desc;
		}

		@Override
		public final String toString() {
			return desc;
		}
	}

	static Map<CallbackPoint, ?>	resourcesMap;

	static final Bundle				bundle	= FrameworkUtil.getBundle(MultiLevelCleanupTest.class);

	static OSGiSoftAssertions		staticSoftly;

	static CallbackPoint			staticCurrentPoint;

	static void staticVerifyCurrentCallbackPoint(Class<? extends MultiLevelCleanupTest> testClass,
		CallbackPoint... pointsThatShouldBeSetup) {
		staticVerifyCurrentCallbackPoint(testClass, setOf(pointsThatShouldBeSetup));
	}

	static void staticVerifyCurrentCallbackPoint(Class<? extends MultiLevelCleanupTest> testClass,
		EnumSet<CallbackPoint> setOfPointsThatShouldBeSetup) {
		// ... check that they are all set up as expected...
		staticAssertSetup(testClass, setOfPointsThatShouldBeSetup);

		// ... now set up the resource corresponding to the current callback
		// point
		getStaticResourceChecker(testClass).setupResource(staticCurrentPoint);

		// ... now the current point should belong to the set of points that
		// should be set up
		setOfPointsThatShouldBeSetup.add(staticCurrentPoint);
		staticAssertSetup(testClass, setOfPointsThatShouldBeSetup);
	}

	static void staticAssertSetup(Class<? extends MultiLevelCleanupTest> testClass,
		EnumSet<CallbackPoint> setOfPointsThatShouldBeSetup) {
		getStaticResourceChecker(testClass).assertSetup(staticSoftly, staticCurrentPoint, setOfPointsThatShouldBeSetup);
	}

	abstract AbstractResourceChecker<?> getResourceChecker();

	static AbstractResourceChecker<?> getStaticResourceChecker(Class<? extends MultiLevelCleanupTest> testClass) {
		try {
			Method m = testClass.getDeclaredMethod("getStaticResourceChecker");
			return (AbstractResourceChecker<?>) m.invoke(null);
		} catch (Exception e) {
			throw Exceptions.duck(e);
		}
	}

	static AbstractResourceChecker<?> getGlobalResourceChecker(Class<? extends MultiLevelCleanupTest> testClass) {
		try {
			Method m = testClass.getDeclaredMethod("getGlobalResourceChecker");
			return (AbstractResourceChecker<?>) m.invoke(null);
		} catch (Exception e) {
			throw Exceptions.duck(e);
		}
	}

	static EnumSet<CallbackPoint> setOf(CallbackPoint... elements) {
		return elements == null || elements.length == 0 ? EnumSet.noneOf(CallbackPoint.class)
			: EnumSet.of(elements[0], elements);
	}

	void verifyCurrentCallbackPoint(CallbackPoint... pointsThatShouldBeSetup) {
		verifyCurrentCallbackPoint(setOf(pointsThatShouldBeSetup));
	}

	// The core testing algorithm:
	// * Invoke this method with a set of callback points that you are expecting
	// should already be setup...
	void verifyCurrentCallbackPoint(EnumSet<CallbackPoint> setOfPointsThatShouldBeSetup) {
		EnumSet<CallbackPoint> setOfPointsThatShouldNotBeSetup = EnumSet.complementOf(setOfPointsThatShouldBeSetup);
		// ... check that they are all set up as expected...
		assertSetup(setOfPointsThatShouldBeSetup);

		// ... now set up the resource corresponding to the current callback
		// point
		getResourceChecker().setupResource(currentPoint);

		// ... now the current point should belong to the set of points that
		// should be set up
		setOfPointsThatShouldBeSetup.add(currentPoint);
		assertSetup(setOfPointsThatShouldBeSetup);
	}

	void assertSetup(EnumSet<CallbackPoint> setOfPointsThatShouldBeSetup) {
		getStaticResourceChecker(getClass()).assertSetup(softly, currentPoint, setOfPointsThatShouldBeSetup);
		getResourceChecker().assertSetup(softly, currentPoint, setOfPointsThatShouldBeSetup);
	}

	OSGiSoftAssertions softly;

	@BeforeAll
	static void beforeAll(TestInfo info) {
		staticCurrentPoint = BEFORE_ALL;
		@SuppressWarnings("unchecked")
		Class<? extends MultiLevelCleanupTest> testClass = (Class<? extends MultiLevelCleanupTest>) info.getTestClass()
			.get();

		staticSoftly = new OSGiSoftAssertions();

		staticVerifyCurrentCallbackPoint(testClass, BEFORE_CLASS);
	}

	CallbackPoint currentPoint;

	@BeforeEach
	void beforeEach() {
		softly = new OSGiSoftAssertions();
		currentPoint = BEFORE_EACH;
		verifyCurrentCallbackPoint(BEFORE_CLASS, BEFORE_ALL);
	}

	@Test
	void innerTest() {
		currentPoint = INNER_TEST;
		verifyCurrentCallbackPoint(BEFORE_CLASS, BEFORE_ALL, BEFORE_EACH);
	}

	@ParameterizedTest
	@ValueSource(ints = {
		1, 2, 3
	})
	// Parameter is not used; just a dummy placeholder so that
	// we can verify the extension's behaviour with parameterized tests.
	void parameterizedTest(int unused) {
		currentPoint = PARAMETERIZED_TEST;
		verifyCurrentCallbackPoint(BEFORE_CLASS, BEFORE_ALL, BEFORE_EACH);
	}

	@Nested
	class NestedTest {

		@BeforeEach
		void beforeEach() {
			currentPoint = NESTED_BEFORE_EACH;
			verifyCurrentCallbackPoint(BEFORE_CLASS, BEFORE_ALL, BEFORE_EACH);
		}

		@Test
		void test() {
			currentPoint = NESTED_TEST;
			verifyCurrentCallbackPoint(BEFORE_CLASS, BEFORE_ALL, BEFORE_EACH, NESTED_BEFORE_EACH);
		}

		@AfterEach
		void afterEach() {
			currentPoint = NESTED_AFTER_EACH;
			verifyCurrentCallbackPoint(BEFORE_CLASS, BEFORE_ALL, BEFORE_EACH, NESTED_BEFORE_EACH, NESTED_TEST);
		}
	}

	@AfterEach
	void afterEach() {
		EnumSet<CallbackPoint> shouldBeSetup = EnumSet.of(BEFORE_CLASS, BEFORE_ALL, BEFORE_EACH);

		if (currentPoint == INNER_TEST) {
			shouldBeSetup.add(INNER_TEST);
		} else if (currentPoint == PARAMETERIZED_TEST) {
			shouldBeSetup.add(PARAMETERIZED_TEST);
		} else if (currentPoint == NESTED_AFTER_EACH) {
			Stream.of(NESTED_BEFORE_EACH, NESTED_TEST, NESTED_AFTER_EACH)
				.forEach(shouldBeSetup::add);
		} else {
			throw new IllegalStateException(
				"Something went wrong with the test - afterEach() called after " + currentPoint);
		}
		currentPoint = AFTER_EACH;
		verifyCurrentCallbackPoint(shouldBeSetup);
		softly.assertAll();
	}

	@AfterAll
	static void afterAll(TestInfo info) {
		staticCurrentPoint = AFTER_ALL;
		@SuppressWarnings("unchecked")
		Class<? extends MultiLevelCleanupTest> testClass = (Class<? extends MultiLevelCleanupTest>) info.getTestClass()
			.get();
		staticVerifyCurrentCallbackPoint(testClass, BEFORE_CLASS, BEFORE_ALL);
	}

	void afterAfterEach() {
		currentPoint = AFTER_AFTER_EACH;
		// Don't simply call verifyCurrentCallbackPoint() as we don't want to
		// attempt to set up a new resource here - this callback is only invoked
		// within an extension and extensions shouldn't be trying to set up
		// resources at this point.
		EnumSet<CallbackPoint> shouldBeSetup = EnumSet.of(BEFORE_CLASS, BEFORE_ALL);
		assertSetup(shouldBeSetup);
	}
}
