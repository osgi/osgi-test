package org.osgi.test.junit5.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.testkit.engine.EventType.FINISHED;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.assertj.core.api.AbstractThrowableAssert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.engine.JupiterTestEngine;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.testkit.engine.EngineTestKit;
import org.junit.platform.testkit.engine.Event;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;
import org.osgi.test.common.context.CloseableBundleContext;
import org.osgi.test.common.dictionary.Dictionaries;
import org.osgi.test.common.exceptions.Exceptions;
import org.osgi.test.common.service.ServiceAware;
import org.osgi.test.junit5.ExecutorExtension;
import org.osgi.test.junit5.ExecutorParameter;
import org.osgi.test.junit5.types.Foo;

@ExtendWith(ExecutorExtension.class)
abstract class AbstractServiceExtensionTest {

	@ExtendWith(ServiceExtension.class)
	static class TestBase {
		static AtomicReference<Foo>					lastService			= new AtomicReference<>();
		static AtomicReference<ServiceAware<Foo>>	lastServiceAware	= new AtomicReference<>();
		static AtomicReference<List<Foo>>			lastServices		= new AtomicReference<>();

		ServiceAware<Foo> getServiceAware() {
			return null;
		}

		Foo getService() {
			return null;
		}

		List<Foo> getServices() {
			return null;
		}

		@BeforeAll
		static void beforeAll() {
			lastService.set(null);
			lastServiceAware.set(null);
			lastServices.set(null);
		}

		@Test
		final void test() {
			lastService.set(getService());
			lastServiceAware.set(getServiceAware());
			lastServices.set(getServices());
		}
	}

	@ExecutorParameter
	protected ScheduledExecutorService	executor;
	protected BundleContext				bundleContext;
	protected String					testMethodName;

	protected static void checkClass(Class<?> testClass) {
		// This is to protect against developer slip-ups that can be costly...
		try {
			testClass.getDeclaredConstructor();
		} catch (NoSuchMethodException e) {
			throw new IllegalStateException(
				"Test class does not have a default constructor (did you accidentally use a non-static inner class?): "
					+ testClass);
		}
	}

	@BeforeEach
	public void beforeEach(TestInfo testInfo) {
		testMethodName = testInfo.getTestMethod()
			.get()
			.getName();

		bundleContext = CloseableBundleContext.proxy(ServiceExtensionTest.class);
	}

	@AfterEach
	public void afterEach() {
		CloseableBundleContext.close(bundleContext);
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

	protected static AbstractThrowableAssert<?, ? extends Throwable> assertThatTest(Class<?> testClass) {
		checkClass(testClass);

		Logger logger = Logger.getLogger("org.junit.jupiter");
		Level oldLevel = logger.getLevel();
		try {
			// Suppress log output while the testkit is running (see issue
			// #133).
			logger.setLevel(Level.OFF);
			Event testEvent = EngineTestKit.engine(new JupiterTestEngine())
				.selectors(selectClass(testClass))
				.execute()
				.testEvents()
				// .debug(
				// System.err)
				.filter(event -> event.getType()
					.equals(FINISHED))
				.findAny()
				.orElseThrow(() -> new IllegalStateException("Test failed to run at all"));

			TestExecutionResult result = testEvent.getPayload(TestExecutionResult.class)
				.orElseThrow(() -> new IllegalStateException("Test result payload missing"));

			return assertThat(result.getThrowable()
				.orElse(null));
		} finally {
			// Restore the filter to what it was so that we do not interfere
			// with the parent test
			logger.setLevel(oldLevel);
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
