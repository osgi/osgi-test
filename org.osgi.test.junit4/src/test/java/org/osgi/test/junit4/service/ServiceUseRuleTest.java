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

package org.osgi.test.junit4.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.Hashtable;

import org.assertj.core.api.SoftAssertions;
import org.junit.Rule;
import org.junit.Test;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.test.common.tracking.TrackServices;
import org.osgi.test.junit4.ExecutorRule;
import org.osgi.test.junit4.context.BundleContextRule;
import org.osgi.test.junit4.types.Foo;

public class ServiceUseRuleTest {

	@Rule
	public ExecutorRule executor = new ExecutorRule();

	@Test
	public void basicAssumptions() throws Exception {
		try (BundleContextRule bcRule = new BundleContextRule();
			ServiceUseRule<Foo> foos = new ServiceUseRule.Builder<>(Foo.class, bcRule) //
				.cardinality(0)
				.build()) {

			bcRule.init(getClass());

			assertThat(bcRule.getBundleContext()
				.getBundle()
				.getRegisteredServices()).isNull();

			foos.init(getClass());

			SoftAssertions softly = new SoftAssertions();

			softly.assertThat(foos.isEmpty())
				.isTrue();
			softly.assertThat(foos.getTimeout())
				.isEqualTo(TrackServices.DEFAULT_TIMEOUT);
			softly.assertThat(foos.getCardinality())
				.isEqualTo(0);

			softly.assertAll();
		}
	}

	@Test
	public void requiredFailsWhenNoService() throws Exception {
		assertThatExceptionOfType(AssertionError.class) //
			.isThrownBy(() -> {
				try (BundleContextRule bcRule = new BundleContextRule();
					ServiceUseRule<Foo> foos = new ServiceUseRule.Builder<>(Foo.class, bcRule) //
						.build()) {

					bcRule.init(getClass());

					assertThat(bcRule.getBundleContext()
						.getBundle()
						.getRegisteredServices()).isNull();

					foos.init(getClass());
				}
			})
			.withMessageContaining(
				" services (objectClass=org.osgi.test.junit4.types.Foo) didn't arrive within 200ms");
	}

	@Test
	public void requiredFailsWhenNoServiceWithTimeout() throws Exception {
		assertThatExceptionOfType(AssertionError.class) //
			.isThrownBy(() -> {
				try (BundleContextRule bcRule = new BundleContextRule();
					ServiceUseRule<Foo> foos = new ServiceUseRule.Builder<>(Foo.class, bcRule) //
						.timeout(50)
						.build()) {

					bcRule.init(getClass());

					assertThat(bcRule.getBundleContext()
						.getBundle()
						.getRegisteredServices()).isNull();

					foos.init(getClass());
				}
			})
			.withMessageContaining(
				" services (objectClass=org.osgi.test.junit4.types.Foo) didn't arrive within 50ms");
	}

	@SuppressWarnings("serial")
	@Test
	public void successWhenService() throws Exception {
		try (BundleContextRule bcRule = new BundleContextRule();
			ServiceUseRule<Foo> foos = new ServiceUseRule.Builder<>(Foo.class, bcRule) //
				.build();) {

			bcRule.init(getClass());

			assertThat(bcRule.getBundleContext()
				.getBundle()
				.getRegisteredServices()).isNull();

			final Foo afoo = new Foo() {};

			executor.schedule(
				() -> bcRule.getBundleContext()
					.registerService(Foo.class, afoo, new Hashtable<String, Object>() {
						{
							put("case", "successWhenService");
						}
					}),
				0);

			foos.init(getClass());

			SoftAssertions softly = new SoftAssertions();

			softly.assertThat(foos.isEmpty())
				.isFalse();
			softly.assertThat(foos.size())
				.isEqualTo(1);
			softly.assertThat(foos.getTimeout())
				.isEqualTo(TrackServices.DEFAULT_TIMEOUT);
			softly.assertThat(foos.getCardinality())
				.isGreaterThan(0);

			softly.assertAll();
		}
	}

	@SuppressWarnings("serial")
	@Test
	public void successWhenServiceWithTimeout() throws Exception {
		try (BundleContextRule bcRule = new BundleContextRule();
			ServiceUseRule<Foo> foos = new ServiceUseRule.Builder<>(Foo.class, bcRule)
				.timeout(1000)
				.build()) {

			bcRule.init(getClass());

			assertThat(bcRule.getBundleContext()
				.getBundle()
				.getRegisteredServices()).isNull();

			final Foo afoo = new Foo() {};

			executor.schedule(
				() -> bcRule.getBundleContext()
					.registerService(Foo.class, afoo, new Hashtable<String, Object>() {
						{
							put("case", "successWhenServiceWithTimeout");
						}
					}),
				0);

			foos.init(getClass());

			SoftAssertions softly = new SoftAssertions();

			softly.assertThat(foos.getService())
				.isEqualTo(afoo);
			softly.assertThat(foos.getServiceReference())
				.isNotNull();
			softly.assertThat(foos.getServiceReferences())
				.hasSize(1);
			softly.assertThat(foos.getServices())
				.containsExactly(afoo);
			softly.assertThat(foos.getTimeout())
				.isEqualTo(1000);
			softly.assertThat(foos.getTracked())
				.hasSize(1);
			softly.assertThat(foos.getTrackingCount())
				.isGreaterThan(0);
			softly.assertThat(foos.getCardinality())
				.isEqualTo(1);
			softly.assertThat(foos.size())
				.isEqualTo(1);
			softly.assertThat(foos.isEmpty())
				.isFalse();
			softly.assertThat(foos.waitForService(20))
				.isEqualTo(afoo);

			softly.assertAll();
		}
	}

	@SuppressWarnings({
		"rawtypes", "serial", "unchecked"
	})
	@Test
	public void matchByFilter() throws Exception {
		try (BundleContextRule bcRule = new BundleContextRule();
			ServiceUseRule<Foo> fooRule = new ServiceUseRule.Builder<>(Foo.class, bcRule)
				.filter("(foo=matchByFilter)")
				.build()) {

			bcRule.init(getClass());

			assertThat(bcRule.getBundleContext()
				.getBundle()
				.getRegisteredServices()).isNull();

			final Foo afoo = new Foo() {};

			executor.schedule(() -> bcRule.getBundleContext()
				.registerService(Foo.class, afoo, new Hashtable() {
					{
						put("foo", "matchByFilter");
					}
				}), 0);

			fooRule.init(getClass());

			SoftAssertions softly = new SoftAssertions();

			softly.assertThat(fooRule.getService())
				.isNotNull();
			softly.assertThat(fooRule.getServiceReference())
				.isNotNull();
			softly.assertThat(fooRule.getServiceReferences())
				.isNotNull();
			softly.assertThat(fooRule.getServices())
				.isNotEmpty()
				.contains(afoo);
			softly.assertThat(fooRule.getTimeout())
				.isEqualTo(TrackServices.DEFAULT_TIMEOUT);
			softly.assertThat(fooRule.getTracked())
				.isNotEmpty();
			softly.assertThat(fooRule.getTrackingCount())
				.isGreaterThan(0);
			softly.assertThat(fooRule.getCardinality())
				.isEqualTo(1);
			softly.assertThat(fooRule.size())
				.isEqualTo(1);
			softly.assertThat(fooRule.isEmpty())
				.isFalse();
			softly.assertThat(fooRule.waitForService(20))
				.isNotNull();

			softly.assertAll();
		}
	}

	@SuppressWarnings("serial")
	@Test
	public void matchMultiple() throws Exception {
		try (BundleContextRule bcRule = new BundleContextRule();
			ServiceUseRule<Foo> fooRule = new ServiceUseRule.Builder<>(Foo.class, bcRule) //
				.cardinality(2)
				.build()) {

			bcRule.init(getClass());

			assertThat(bcRule.getBundleContext()
				.getBundle()
				.getRegisteredServices()).isNull();

			Foo s1 = new Foo() {}, s2 = new Foo() {};
			executor.schedule(() -> bcRule.getBundleContext()
				.registerService(Foo.class, s1, new Hashtable<String, Object>() {
					{
						put("case", "matchMultiple_1");
					}
				}), 0);
			executor.schedule(() -> bcRule.getBundleContext()
				.registerService(Foo.class, s2, new Hashtable<String, Object>() {
					{
						put("case", "matchMultiple_2");
					}
				}), 0);

			fooRule.init(getClass());

			SoftAssertions softly = new SoftAssertions();

			softly.assertThat(fooRule.getService())
				.isIn(s1, s2);
			softly.assertThat(fooRule.getServiceReference())
				.isNotNull();
			softly.assertThat(fooRule.getServiceReferences())
				.isNotNull();
			softly.assertThat(fooRule.getServices())
				.containsExactlyInAnyOrder(s1, s2);
			softly.assertThat(fooRule.getTimeout())
				.isEqualTo(TrackServices.DEFAULT_TIMEOUT);
			softly.assertThat(fooRule.getTracked())
				.isNotEmpty();
			softly.assertThat(fooRule.getTrackingCount())
				.isGreaterThan(0);
			softly.assertThat(fooRule.getCardinality())
				.isEqualTo(2);
			softly.assertThat(fooRule.size())
				.isEqualTo(2);
			softly.assertThat(fooRule.isEmpty())
				.isFalse();
			softly.assertThat(fooRule.waitForService(20))
				.isNotNull();

			softly.assertAll();
		}
	}

	@SuppressWarnings({
		"rawtypes", "serial", "unchecked"
	})
	@Test
	public void nomatchByFilter() throws Exception {
		assertThatExceptionOfType(AssertionError.class) //
			.isThrownBy(() -> {
				try (BundleContextRule bcRule = new BundleContextRule();
					ServiceUseRule<Foo> fooRule = new ServiceUseRule.Builder<>(Foo.class, bcRule) //
						.filter("(foo=baz)")
						.build()) {

					bcRule.init(getClass());

					assertThat(bcRule.getBundleContext()
						.getBundle()
						.getRegisteredServices()).isNull();

					final Foo afoo = new Foo() {};

					executor.schedule(() -> bcRule.getBundleContext()
						.registerService(Foo.class, afoo, new Hashtable() {
							{
								put("foo", "nomatchByFilter");
							}
						}), 0);

					fooRule.init(getClass());
				}
			});
	}

	@Test
	public void malformedFilter() throws Exception {
		assertThatExceptionOfType(InvalidSyntaxException.class) //
			.isThrownBy(() -> {
				try (BundleContextRule bcRule = new BundleContextRule();
					ServiceUseRule<Foo> fooRule = new ServiceUseRule.Builder<>(Foo.class, bcRule) //
						.filter("(foo=baz")
						.build()) {

					bcRule.init(getClass());

					assertThat(bcRule.getBundleContext()
						.getBundle()
						.getRegisteredServices()).isNull();

					fooRule.init(getClass());
				}
			});
	}

}
