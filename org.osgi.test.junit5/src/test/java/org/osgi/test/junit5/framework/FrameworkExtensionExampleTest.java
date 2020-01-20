/*
 * Copyright (c) OSGi Alliance (2019). All Rights Reserved.
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

package org.osgi.test.junit5.framework;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.framework.BundleException;
import org.osgi.framework.launch.Framework;

/**
 * This is how a real test class should use {@link FrameworkExtension}.
 */
@ExtendWith(FrameworkExtension.class)
public class FrameworkExtensionExampleTest {

	// Framework injection

	@Test

	public void testFrameworkParam1(@FrameworkParameter Framework framework) throws BundleException {
		frameworkParam1 = framework.hashCode();
		assertThat(framework).isNotNull();

	}

	@Test

	public void testFrameworkParam2(@FrameworkParameter Framework framework) {
		frameworkParam2 = framework.hashCode();
		assertThat(framework).isNotNull();

	}

	// OR

	@FrameworkParameter
	Framework frameworkNonStatic;

	@Test

	public void testFrameworkMemmber1() {
		frameworkNonStatic1 = frameworkNonStatic.hashCode();

		assertThat(frameworkNonStatic).isNotNull();

	}

	@Test

	public void testFrameworkMemmber2() {

		frameworkNonStatic2 = frameworkNonStatic.hashCode();
		assertThat(frameworkNonStatic).isNotNull();

	}

	// OR

	@FrameworkParameter
	static Framework frameworkStatic;

	@Test

	public void testFrameworkStaticMemmber() {

		frameworkStatic1 = frameworkStatic.hashCode();
		assertThat(frameworkStatic).isNotNull();
	}

	@Test

	public void testFrameworkStaticMemmber2() {
		frameworkStatic2 = frameworkStatic.hashCode();
		assertThat(frameworkStatic).isNotNull();
	}

	private int	frameworkParam1;
	private int	frameworkParam2;

	private int	frameworkNonStatic1;
	private int	frameworkNonStatic2;

	private int	frameworkStatic1;
	private int	frameworkStatic2;

	@Test
	@Order(Integer.MAX_VALUE)
	public void testFrameworks() {
		// assertNotEquals(frameworkParam1, frameworkParam2);

		// assertNotEquals(frameworkNonStatic1, frameworkNonStatic2);
		assertEquals(frameworkStatic1, frameworkStatic2);
	}

}
