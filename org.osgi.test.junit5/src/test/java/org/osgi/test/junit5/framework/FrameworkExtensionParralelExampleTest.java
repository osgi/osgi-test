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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.osgi.framework.BundleException;
import org.osgi.framework.launch.Framework;

/**
 * This is how a real test class should use parralel {@link FrameworkExtension}.
 */
@Execution(ExecutionMode.CONCURRENT)
@ExtendWith(FrameworkExtension.class)
public class FrameworkExtensionParralelExampleTest {

	// Framework injection

	@Test
	public void testFrameworkParam1(@FrameworkParameter Framework framework) throws BundleException {
		assertThat(framework).isNotNull();
		System.out.println(framework.getLocation());
	}

	@Test
	public void testFrameworkParam2(@FrameworkParameter Framework framework) {
		assertThat(framework).isNotNull();
		System.out.println(framework.getLocation());
	}



}
