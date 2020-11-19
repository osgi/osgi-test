/*
 * Copyright (c) OSGi Alliance (2020). All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.osgi.test.junit5.bundle;

import static org.osgi.framework.Bundle.ACTIVE;
import static org.osgi.framework.Bundle.INSTALLED;
import static org.osgi.framework.Bundle.RESOLVED;
import static org.osgi.framework.Bundle.STARTING;
import static org.osgi.framework.Bundle.STOPPING;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.params.provider.ArgumentsSource;

@Target({
	ElementType.ANNOTATION_TYPE, ElementType.METHOD
})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ArgumentsSource(BundleArgumentsProvider.class)
public @interface BundleSource {

	/**
	 * Optional SymbolicNameFilter used to filter Bundles by SymbolicName using
	 * regular expression pattern.
	 *
	 * @return The symbolicNamePattern Strings.
	 */
	String[] symbolicNamePattern() default {};

	/**
	 * Optional bit mask of the Bundle states used to filter Bundles.
	 *
	 * @return The bit mask of the bundle state.
	 */
	int stateMask() default INSTALLED | RESOLVED | STARTING | STOPPING | ACTIVE;

	/**
	 * Filter string used to target a bundle by filtering the Bundle-Headers
	 * Must use valid OSGi filter syntax.
	 *
	 * @return The filter string.
	 */
	String headerFilter() default "";

}
