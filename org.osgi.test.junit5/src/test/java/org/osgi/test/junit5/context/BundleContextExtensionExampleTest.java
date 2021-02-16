/*
 * Copyright (c) OSGi Alliance (2019, 2021). All Rights Reserved.
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

package org.osgi.test.junit5.context;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.osgi.framework.BundleContext;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.annotation.InjectBundleInstaller;
import org.osgi.test.common.install.BundleInstaller;

/**
 * This is how a real test class should use {@link BundleContextExtension}.
 */
@ExtendWith(BundleContextExtension.class)
public class BundleContextExtensionExampleTest {

	// BundleContext injection

	@InjectBundleContext
	static BundleContext classLevelContext;

	@BeforeAll
	static void beforeAll() throws InterruptedException {
		assertThat(classLevelContext).isNotNull();
	}

	BundleContext currentMethodContext;

	@BeforeEach
	void beforeEach(@InjectBundleContext BundleContext bc) {
		currentMethodContext = bc;
		assertThat(bc).isNotSameAs(classLevelContext);
	}

	@Test
	public void testBundleContext1(@InjectBundleContext BundleContext bundleContext1) {
		assertThat(bundleContext1).isNotNull()
			.isSameAs(bundleContext2)
			.isSameAs(currentMethodContext);
	}

	// OR

	@InjectBundleContext
	BundleContext bundleContext2;

	@Test
	public void testBundleContext2() {
		assertThat(bundleContext2).isNotNull()
			.isSameAs(currentMethodContext)
			.isNotSameAs(classLevelContext);
	}

	@ParameterizedTest
	@ValueSource(ints = {
		1, 2, 3
	})
	// Parameter is not used; just a dummy placeholder so that
	// we can demonstrate the extension's usage with parameterized tests.
	void testBundleContextFromDynamic(int value, @InjectBundleContext BundleContext bc) {
		assertThat(bc).isSameAs(currentMethodContext)
			.isNotSameAs(classLevelContext);
	}

	@Nested
	class NestedExampleTest {

		@BeforeEach
		void beforeEach(@InjectBundleContext BundleContext bc) {
			assertThat(classLevelContext).isNotSameAs(bc)
				.isEqualTo(bc);
		}

		@InjectBundleContext
		BundleContext currentMethodBC;

		@Test
		void testNestedBundleContext1(@InjectBundleContext BundleContext bc) {
			assertThat(bc).isSameAs(currentMethodBC)
				.isNotSameAs(classLevelContext);
		}

	}

	// BundleInstaller injection

	@Test
	public void testBundleInstaller(@InjectBundleInstaller
	BundleInstaller bundleInstaller1) {
		assertThat(bundleInstaller1).isNotNull()
			.isSameAs(bundleInstaller2);
	}

	// OR

	@InjectBundleInstaller
	BundleInstaller bundleInstaller2;

	@Test
	public void testBundleInstaller() {
		assertThat(bundleInstaller2).isNotNull();
	}

}
