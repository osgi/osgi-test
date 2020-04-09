package org.osgi.test.junit5.service;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Inject OSGi services into test classes and methods.
 * <p>
 * Example:
 *
 * <pre>
 * &#64;ExtendWith(ServiceUseExtension.class)
 * class MyTests {
 * 	// reused by all tests if static, otherwise injected per test
 * 	&#64;ServiceUseParameter
 * 	static Foo foo;
 *
 * 	// OR
 *
 * 	&#64;Test
 * 	public void otherTest(&#64;ServiceUseParameter Foo foo) {
 * 		//
 * 	}
 * }
 * </pre>
 */
@Target({
	FIELD, PARAMETER
})
@Retention(RUNTIME)
@Documented
public @interface ServiceUseParameter {

	/**
	 * Filter string used to target more specific services using the
	 * {@code String.format} pattern. Must use valid OSGi filter syntax.
	 */
	String filter() default "";

	/**
	 * Optional arguments to the format string provided by {@link #filter()}.
	 */
	String[] filterArguments() default {};

	/**
	 * Indicate the number of services that are required to arrive within the
	 * specified by {@link #timeout()} before starting the test.
	 */
	int cardinality() default 1;

	/**
	 * Indicate require services must arrive within the specified timeout.
	 */
	long timeout() default 200l;

}
