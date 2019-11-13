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
}
