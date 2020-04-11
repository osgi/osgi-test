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

import static org.osgi.test.common.inject.FieldInjector.findAnnotatedNonStaticFields;
import static org.osgi.test.common.inject.FieldInjector.setField;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;

import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.annotation.InjectInstallBundle;
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
 * &#64;InjectBundleContext
 * BundleContext bundleContext;
 *
 * &#64;Test
 * public void aTest() {
 * 	Bundle bundle = bundleContext.getBundle();
 * }
 * </pre>
 */
public class BundleContextRule implements AutoCloseable, MethodRule {

	private volatile BundleContext	bundleContext;
	private volatile InstallBundle	installBundle;

	public BundleContext getBundleContext() {
		return bundleContext;
	}

	public BundleContextRule init(Object testInstance) {
		if (this.bundleContext != null) {
			return this;
		}

		BundleContext bundleContext = CloseableBundleContext.proxy(testInstance
			.getClass(),
			FrameworkUtil.getBundle(testInstance.getClass())
				.getBundleContext());

		installBundle = new InstallBundle(bundleContext);

		List<Field> fields = findAnnotatedNonStaticFields(testInstance.getClass(), InjectBundleContext.class);

		fields.forEach(field -> {
			assertFieldIsBundleContext(field);
			setField(field, testInstance, bundleContext);
		});

		fields = findAnnotatedNonStaticFields(testInstance.getClass(), InjectInstallBundle.class);

		fields.forEach(field -> {
			assertFieldIsInstallBundle(field);
			setField(field, testInstance, installBundle);
		});

		this.bundleContext = bundleContext;
		return this;
	}

	@Override
	public void close() throws Exception {
		if (bundleContext != null) {
			((AutoCloseable) bundleContext).close();
			bundleContext = null;
			installBundle = null;
		}
	}

	@Override
	public Statement apply(Statement statement, FrameworkMethod method, Object testInstance) {
		init(testInstance);
		return new Statement() {
			@Override
			public void evaluate() throws Throwable {
				try {
					statement.evaluate();
				} finally {
					close();
				}
			}
		};
	}

	private void assertIsBundleContext(Class<?> type) {
		if (type != BundleContext.class) {
			throw new RuntimeException("Can only resolve @" + InjectBundleContext.class.getSimpleName()
				+ " field of type " + BundleContext.class.getName() + " but was: " + type.getName());
		}
	}

	private void assertIsInstallBundle(Class<?> type) {
		if (type != InstallBundle.class) {
			throw new RuntimeException("Can only resolve @" + InjectInstallBundle.class.getSimpleName()
				+ " field of type " + InstallBundle.class.getName() + " but was: " + type.getName());
		}
	}

	private void assertFieldIsBundleContext(Field field) {
		assertIsBundleContext(field.getType());
		if (Modifier.isFinal(field.getModifiers()) || Modifier.isPrivate(field.getModifiers())
			|| Modifier.isStatic(field.getModifiers())) {
			throw new RuntimeException(
				InjectBundleContext.class.getName() + " field [" + field + "] must not be final, private or static.");
		}
	}

	private void assertFieldIsInstallBundle(Field field) {
		assertIsInstallBundle(field.getType());
		if (Modifier.isFinal(field.getModifiers()) || Modifier.isPrivate(field.getModifiers())
			|| Modifier.isStatic(field.getModifiers())) {
			throw new RuntimeException(
				InjectInstallBundle.class.getName() + " field [" + field + "] must not be final, private or static.");
		}
	}

}
