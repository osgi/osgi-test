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

package org.osgi.test.junit4.context;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.test.common.context.CloseableBundleContext;
import org.osgi.test.common.install.InstallBundle;

/**
 * A JUnit 4 Rule to get the OSGi {@link BundleContext} of the test bundle.
 * <p>
 * The {@link BundleContext} implementation provided by this rule will
 * automatically clean up all service registrations, bundle, service and
 * framework listeners, as well as installed bundles left behind.
 * <p>
 * Example:
 *
 * <pre>
 * &#64;Rule
 * public BundleContextRule rule = new BundleContextRule();
 *
 * &#64;Test
 * public void aTest() {
 * 	Bundle bundle = rule.get()
 * 		.getBundle();
 * }
 * </pre>
 */
public class BundleContextRule implements AutoCloseable, InstallBundle, TestRule {

	private BundleContext bundleContext;

	@Override
	public BundleContext getBundleContext() {
		return bundleContext;
	}

	public void init(Class<?> testClass) {
		if (bundleContext == null) {
			bundleContext = CloseableBundleContext.proxy(testClass, FrameworkUtil.getBundle(testClass)
				.getBundleContext());
		}
	}

	@Override
	public void close() throws Exception {
		if (bundleContext != null) {
			((AutoCloseable) bundleContext).close();
			bundleContext = null;
		}
	}

	@Override
	public Statement apply(Statement statement, Description description) {
		return new Statement() {
			@Override
			public void evaluate() throws Throwable {
				init(description.getTestClass());
				try {
					statement.evaluate();
				} finally {
					close();
				}
			}
		};
	}

}
