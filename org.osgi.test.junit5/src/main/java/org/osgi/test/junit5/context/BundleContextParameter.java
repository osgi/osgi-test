package org.osgi.test.junit5.context;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.osgi.framework.BundleContext;

/**
 * Inject {@link BundleContextParameter} into test classes and methods.
 * <p>
 * The {@link BundleContext} implementation provided by this rule will
 * automatically clean up all service registrations, bundle, service and
 * framework listeners, as well as installed bundles left behind.
 * <p>
 * Example:
 *
 * <pre>
 * &#64;ExtendWith(BundleContextExtension.class)
 * class MyTests {
 * 	// reused by all tests if static, otherwise injected per test
 * 	&#64;BundleContextParameter
 * 	static BundleContext bundleContext;
 *
 * 	&#64;Test
 * 	public void simpleTest() {
 * 		Bundle bundle = bundleContext.getBundle();
 * 	}
 *
 * 	// OR
 *
 * 	&#64;Test
 * 	public void otherTest(&#64;BundleContextParameter BundleContext bundleContext) {
 * 		Bundle bundle = bundleContext.getBundle();
 * 	}
 * }
 * </pre>
 */
@Target({
	FIELD, PARAMETER
})
@Retention(RUNTIME)
@Documented
public @interface BundleContextParameter {}