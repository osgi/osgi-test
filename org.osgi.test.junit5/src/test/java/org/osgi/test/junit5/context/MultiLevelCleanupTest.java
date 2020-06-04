package org.osgi.test.junit5.context;

import static org.osgi.test.junit5.context.MultiLevelCleanupTest.Scope.AFTER_ALL;
import static org.osgi.test.junit5.context.MultiLevelCleanupTest.Scope.AFTER_EACH;
import static org.osgi.test.junit5.context.MultiLevelCleanupTest.Scope.BEFORE_ALL;
import static org.osgi.test.junit5.context.MultiLevelCleanupTest.Scope.BEFORE_CLASS;
import static org.osgi.test.junit5.context.MultiLevelCleanupTest.Scope.BEFORE_EACH;
import static org.osgi.test.junit5.context.MultiLevelCleanupTest.Scope.INNER_TEST;
import static org.osgi.test.junit5.context.MultiLevelCleanupTest.Scope.NESTED_AFTER_EACH;
import static org.osgi.test.junit5.context.MultiLevelCleanupTest.Scope.NESTED_BEFORE_EACH;
import static org.osgi.test.junit5.context.MultiLevelCleanupTest.Scope.NESTED_TEST;
import static org.osgi.test.junit5.context.MultiLevelCleanupTest.Scope.PARAMETERIZED_TEST;

import java.lang.reflect.Method;
import java.util.Map;

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
 * The MultiLevelCleanupTest is a test that is designed to be run inside EngineTestKit. It's basic purpose is
 * to monitor the setup/tear down of "resources" during test execution as the test scope changes. In Jupiter
 * there are arbitrary levels of scope, but the ones that Extensions have access to are:
 *
 * * Global (not used by us here)
 * * Class level - scope begins when Jupiter starts processing a particular test class (before any @BeforeAlland ends after it has
 */
abstract class MultiLevelCleanupTest {

	enum Scope {
		BEFORE_CLASS(
			"beforeClass"),
		BEFORE_ALL("beforeAll"),
		BEFORE_EACH("beforeEach"),
		INNER_TEST("innerTest"),
		PARAMETERIZED_TEST("parameterizedTest"),
		NESTED_BEFORE_EACH("nested.beforeEach"),
		NESTED_TEST("nested.test"),
		NESTED_AFTER_EACH("nested.afterEach"),
		AFTER_EACH("afterEach"),
		AFTER_AFTER_EACH(
			"afterAfterEach"),
		AFTER_ALL("afterAll"),
		AFTER_CLASS("afterClass");

		final String desc;

		Scope(String desc) {
			this.desc = desc;
		}

		@Override
		public final String toString() {
			return desc;
		}
	}

	static Map<Scope, ?>		scopedResourcesMap;

	static final Bundle			bundle	= FrameworkUtil.getBundle(MultiLevelCleanupTest.class);

	static OSGiSoftAssertions	staticSoftly;

	static Scope				staticCurrentScope;

	static void staticSetupResource(Class<? extends MultiLevelCleanupTest> testClass) {
		getStaticResourceChecker(testClass).setupResource(staticCurrentScope);
	}

	static void staticAssertSetup(Class<? extends MultiLevelCleanupTest> testClass, Scope... fromScopes) {
		getStaticResourceChecker(testClass).assertSetup(staticSoftly, staticCurrentScope, fromScopes);
	}

	static void staticAssertNotSetup(Class<? extends MultiLevelCleanupTest> testClass, Scope... fromScopes) {
		getStaticResourceChecker(testClass).assertNotSetup(staticSoftly, staticCurrentScope, fromScopes);
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

	void setupResource() {
		getResourceChecker().setupResource(currentScope);
	}

	@SuppressWarnings("unchecked")
	void assertSetup(Scope... fromScopes) {
		getStaticResourceChecker(getClass()).assertSetup(softly, currentScope, fromScopes);
		getResourceChecker().assertSetup(softly, currentScope, fromScopes);
	}

	@SuppressWarnings("unchecked")
	void assertNotSetup(Scope... fromScopes) {
		getStaticResourceChecker(getClass()).assertNotSetup(softly, currentScope, fromScopes);
		getResourceChecker().assertNotSetup(softly, currentScope, fromScopes);
	}

	OSGiSoftAssertions softly;

	@BeforeAll
	static void beforeAll(TestInfo info) {
		staticCurrentScope = BEFORE_ALL;
		@SuppressWarnings("unchecked")
		Class<? extends MultiLevelCleanupTest> testClass = (Class<? extends MultiLevelCleanupTest>) info
			.getTestClass()
			.get();

		staticSoftly = new OSGiSoftAssertions();

		staticAssertSetup(testClass, BEFORE_CLASS);
		staticAssertNotSetup(testClass, BEFORE_ALL, BEFORE_EACH, INNER_TEST, PARAMETERIZED_TEST, NESTED_BEFORE_EACH,
			NESTED_TEST, NESTED_AFTER_EACH, AFTER_EACH, AFTER_ALL);
		staticSetupResource(testClass);
		staticAssertSetup(testClass, BEFORE_CLASS, BEFORE_ALL);
		staticAssertNotSetup(testClass, BEFORE_EACH, INNER_TEST, PARAMETERIZED_TEST, NESTED_BEFORE_EACH, NESTED_TEST,
			NESTED_AFTER_EACH, AFTER_EACH, AFTER_ALL);
	}

	Scope currentScope;

	@BeforeEach
	void beforeEach() {
		currentScope = BEFORE_EACH;
		softly = new OSGiSoftAssertions();
		assertSetup(BEFORE_CLASS, BEFORE_ALL);
		assertNotSetup(BEFORE_EACH, INNER_TEST, PARAMETERIZED_TEST, NESTED_BEFORE_EACH, NESTED_TEST, NESTED_AFTER_EACH,
			AFTER_EACH, AFTER_ALL);
		setupResource();
		assertSetup(BEFORE_CLASS, BEFORE_ALL, BEFORE_EACH);
		assertNotSetup(INNER_TEST, PARAMETERIZED_TEST, NESTED_BEFORE_EACH, NESTED_TEST, NESTED_AFTER_EACH, AFTER_EACH,
			AFTER_ALL);
	}

	@Test
	void innerTest() {
		currentScope = INNER_TEST;
		assertSetup(BEFORE_CLASS, BEFORE_ALL, BEFORE_EACH);
		assertNotSetup(INNER_TEST, PARAMETERIZED_TEST, NESTED_BEFORE_EACH, NESTED_TEST, NESTED_AFTER_EACH, AFTER_EACH,
			AFTER_ALL);
		setupResource();
		assertSetup(BEFORE_CLASS, BEFORE_ALL, BEFORE_EACH, INNER_TEST);
		assertNotSetup(PARAMETERIZED_TEST, NESTED_BEFORE_EACH, NESTED_TEST, NESTED_AFTER_EACH, AFTER_EACH, AFTER_ALL);
	}

	@ParameterizedTest
	@ValueSource(ints = {
		1, 2, 3
	})
	void parameterizedTest(int parameter) {
		currentScope = PARAMETERIZED_TEST;
		assertSetup(BEFORE_CLASS, BEFORE_ALL, BEFORE_EACH);
		assertNotSetup(INNER_TEST, PARAMETERIZED_TEST, NESTED_BEFORE_EACH, NESTED_TEST, NESTED_AFTER_EACH, AFTER_EACH,
			AFTER_ALL);
		setupResource();
		assertSetup(BEFORE_CLASS, BEFORE_ALL, BEFORE_EACH, PARAMETERIZED_TEST);
		assertNotSetup(INNER_TEST, NESTED_BEFORE_EACH, NESTED_TEST, NESTED_AFTER_EACH, AFTER_EACH, AFTER_ALL);
	}

	@Nested
	class NestedTest {

		@BeforeEach
		void beforeEach() {
			currentScope = NESTED_BEFORE_EACH;
			assertSetup(BEFORE_CLASS, BEFORE_ALL, BEFORE_EACH);
			assertNotSetup(INNER_TEST, PARAMETERIZED_TEST, NESTED_BEFORE_EACH, NESTED_TEST, NESTED_AFTER_EACH,
				AFTER_EACH, AFTER_ALL);
			setupResource();
			assertSetup(BEFORE_CLASS, BEFORE_ALL, BEFORE_EACH, NESTED_BEFORE_EACH);
			assertNotSetup(INNER_TEST, PARAMETERIZED_TEST, NESTED_TEST, NESTED_AFTER_EACH, AFTER_EACH, AFTER_ALL);
		}

		@Test
		void test() {
			currentScope = NESTED_TEST;
			assertSetup(BEFORE_CLASS, BEFORE_ALL, BEFORE_EACH, NESTED_BEFORE_EACH);
			assertNotSetup(INNER_TEST, PARAMETERIZED_TEST, NESTED_TEST, NESTED_AFTER_EACH, AFTER_EACH, AFTER_ALL);
			setupResource();
			assertSetup(BEFORE_CLASS, BEFORE_ALL, BEFORE_EACH, NESTED_BEFORE_EACH, NESTED_TEST);
			assertNotSetup(INNER_TEST, PARAMETERIZED_TEST, NESTED_AFTER_EACH, AFTER_EACH, AFTER_ALL);
		}

		@AfterEach
		void afterEach() {
			currentScope = NESTED_AFTER_EACH;
			assertSetup(BEFORE_CLASS, BEFORE_ALL, BEFORE_EACH, NESTED_BEFORE_EACH, NESTED_TEST);
			assertNotSetup(INNER_TEST, PARAMETERIZED_TEST, NESTED_AFTER_EACH, AFTER_EACH, AFTER_ALL);
			setupResource();
			assertSetup(BEFORE_CLASS, BEFORE_ALL, BEFORE_EACH, NESTED_BEFORE_EACH, NESTED_TEST, NESTED_AFTER_EACH);
			assertNotSetup(INNER_TEST, PARAMETERIZED_TEST, AFTER_EACH, AFTER_ALL);
		}
	}

	@AfterEach
	void afterEach() {
		Scope[] alsoSetup = null;
		Scope[] alsoNotSetup = null;
		if (currentScope == INNER_TEST) {
			alsoSetup = new Scope[] {
				INNER_TEST
			};
			alsoNotSetup = new Scope[] {
				PARAMETERIZED_TEST, NESTED_BEFORE_EACH, NESTED_TEST, NESTED_AFTER_EACH
			};
		} else if (currentScope == PARAMETERIZED_TEST) {
			alsoSetup = new Scope[] {
				PARAMETERIZED_TEST
			};
			alsoNotSetup = new Scope[] {
				INNER_TEST, NESTED_BEFORE_EACH, NESTED_TEST, NESTED_AFTER_EACH
			};
		} else if (currentScope.equals("nested.afterEach")) {
			alsoSetup = new Scope[] {
				NESTED_BEFORE_EACH, NESTED_TEST, NESTED_AFTER_EACH
			};
			alsoNotSetup = new Scope[] {
				INNER_TEST, PARAMETERIZED_TEST
			};
		}
		currentScope = AFTER_EACH;
		assertSetup(BEFORE_CLASS, BEFORE_ALL, BEFORE_EACH);
		assertNotSetup(AFTER_EACH, AFTER_ALL);
		if (alsoSetup != null) {
			assertSetup(alsoSetup);
		}
		assertNotSetup(AFTER_EACH, AFTER_ALL);
		if (alsoNotSetup != null) {
			assertNotSetup(alsoNotSetup);
		}
		setupResource();
		assertSetup(BEFORE_CLASS, BEFORE_ALL, BEFORE_EACH, AFTER_EACH);
		assertNotSetup(AFTER_ALL);
		if (alsoSetup != null) {
			assertSetup(alsoSetup);
		}
		assertNotSetup(AFTER_ALL);
		if (alsoNotSetup != null) {
			assertNotSetup(alsoNotSetup);
		}
		softly.assertAll();
	}

	@AfterAll
	static void afterAll(TestInfo info) {
		staticCurrentScope = AFTER_ALL;
		@SuppressWarnings("unchecked")
		Class<? extends MultiLevelCleanupTest> testClass = (Class<? extends MultiLevelCleanupTest>) info
			.getTestClass()
			.get();
		staticAssertSetup(testClass, BEFORE_CLASS, BEFORE_ALL);
		staticAssertNotSetup(testClass, BEFORE_EACH, INNER_TEST, PARAMETERIZED_TEST, NESTED_BEFORE_EACH, NESTED_TEST,
			NESTED_AFTER_EACH, AFTER_EACH, AFTER_ALL);
		staticSetupResource(testClass);
		staticAssertSetup(testClass, BEFORE_CLASS, BEFORE_ALL, AFTER_ALL);
		staticAssertNotSetup(testClass, BEFORE_EACH, INNER_TEST, PARAMETERIZED_TEST, NESTED_BEFORE_EACH, NESTED_TEST,
			NESTED_AFTER_EACH, AFTER_EACH);
		staticSoftly.assertAll();
	}

	static void afterAfterEach(Class<? extends MultiLevelCleanupTest> testClass) {
		staticAssertSetup(testClass, BEFORE_CLASS, BEFORE_ALL);
		staticAssertNotSetup(testClass, BEFORE_EACH, INNER_TEST, PARAMETERIZED_TEST, NESTED_BEFORE_EACH, NESTED_TEST,
			NESTED_AFTER_EACH, AFTER_EACH, AFTER_ALL);
	}
}
