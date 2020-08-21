package org.osgi.test.common.annotation.config;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Inherited
@Target({
	TYPE, METHOD
})
@Retention(RUNTIME)
@Documented
public @interface WithConfigurations {

	WithConfiguration[] value();
}
