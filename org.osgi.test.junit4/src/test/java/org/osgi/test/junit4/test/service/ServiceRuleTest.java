/*******************************************************************************
 * Copyright (c) Contributors to the Eclipse Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 *******************************************************************************/

package org.osgi.test.junit4.test.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.osgi.test.common.annotation.InjectService.DEFAULT_TIMEOUT;

import java.util.concurrent.ScheduledFuture;

import org.assertj.core.api.SoftAssertions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceRegistration;
import org.osgi.test.common.dictionary.Dictionaries;
import org.osgi.test.common.service.ServiceConfiguration;
import org.osgi.test.junit4.context.BundleContextRule;
import org.osgi.test.junit4.test.ExecutorRule;
import org.osgi.test.junit4.test.types.Foo;

public class ServiceRuleTest {

	@Rule
	public ExecutorRule	executor	= new ExecutorRule();
	@Rule
	public TestName		name		= new TestName();

	@Test
	public void basicAssumptions() throws Exception {
		try (BundleContextRule bcRule = new BundleContextRule();
			ServiceConfiguration<Foo> foos = new ServiceConfiguration<>(Foo.class, "", new String[0], 0,
				DEFAULT_TIMEOUT)) {

			bcRule.init(this);

			assertThat(bcRule.getBundleContext()
				.getBundle()
				.getRegisteredServices()).isNull();

			foos.init(bcRule.getBundleContext());

			SoftAssertions softly = new SoftAssertions();

			softly.assertThat(foos.isEmpty())
				.as("isEmpty %s", foos)
				.isTrue();
			softly.assertThat(foos.getTimeout())
				.as("getTimeout %s", foos)
				.isEqualTo(DEFAULT_TIMEOUT);
			softly.assertThat(foos.getCardinality())
				.as("getCardinality %s", foos)
				.isEqualTo(0);

			softly.assertAll();
		}
	}

	@Test
	public void requiredFailsWhenNoService() throws Exception {
		assertThatExceptionOfType(AssertionError.class) //
			.isThrownBy(() -> {
				try (BundleContextRule bcRule = new BundleContextRule();
						ServiceConfiguration<Foo> foos = new ServiceConfiguration<>(Foo.class, "", new String[0], 1,
							DEFAULT_TIMEOUT)) {

						bcRule.init(this);

					assertThat(bcRule.getBundleContext()
						.getBundle()
						.getRegisteredServices()).isNull();

						foos.init(bcRule.getBundleContext());
				}
			})
			.withMessageContaining(
				" services (objectClass=" + Foo.class.getName() + ") didn't arrive within 200ms");
	}

	@Test
	public void requiredFailsWhenNoServiceWithTimeout() throws Exception {
		assertThatExceptionOfType(AssertionError.class) //
			.isThrownBy(() -> {
				try (BundleContextRule bcRule = new BundleContextRule();
					ServiceConfiguration<Foo> foos = new ServiceConfiguration<>(Foo.class, "", new String[0], 1, 50L)) {

					bcRule.init(this);

					assertThat(bcRule.getBundleContext()
						.getBundle()
						.getRegisteredServices()).isNull();

						foos.init(bcRule.getBundleContext());
				}
			})
			.withMessageContaining(
				" services (objectClass=" + Foo.class.getName() + ") didn't arrive within 50ms");
	}

	@Test
	public void successWhenService() throws Exception {
		try (BundleContextRule bcRule = new BundleContextRule();
			ServiceConfiguration<Foo> foos = new ServiceConfiguration<>(Foo.class, "", new String[0], 1,
				DEFAULT_TIMEOUT)) {

			bcRule.init(this);

			assertThat(bcRule.getBundleContext()
				.getBundle()
				.getRegisteredServices()).isNull();

			final Foo afoo = new Foo() {};

			ScheduledFuture<ServiceRegistration<Foo>> scheduledFuture = executor
				.schedule(() -> bcRule.getBundleContext()
					.registerService(Foo.class, afoo, Dictionaries.dictionaryOf("case", name.getMethodName())), 0);

			foos.init(bcRule.getBundleContext());

			// Make sure the scheduled event is processed
			assertThat(scheduledFuture.get()).isNotNull();

			SoftAssertions softly = new SoftAssertions();

			softly.assertThat(foos.isEmpty())
				.as("isEmpty %s", foos)
				.isFalse();
			softly.assertThat(foos.size())
				.as("size %s", foos)
				.isEqualTo(1);
			softly.assertThat(foos.getTimeout())
				.as("getTimeout %s", foos)
				.isEqualTo(DEFAULT_TIMEOUT);
			softly.assertThat(foos.getCardinality())
				.as("getCardinality %s", foos)
				.isEqualTo(1);

			softly.assertAll();
		}
	}

	@Test
	public void successWhenServiceWithTimeout() throws Exception {
		try (BundleContextRule bcRule = new BundleContextRule();
			ServiceConfiguration<Foo> foos = new ServiceConfiguration<>(Foo.class, "", new String[0], 1, 1000L)) {

			bcRule.init(this);

			assertThat(bcRule.getBundleContext()
				.getBundle()
				.getRegisteredServices()).isNull();

			final Foo afoo = new Foo() {};

			ScheduledFuture<ServiceRegistration<Foo>> scheduledFuture = executor
				.schedule(() -> bcRule.getBundleContext()
					.registerService(Foo.class, afoo, Dictionaries.dictionaryOf("case", name.getMethodName())), 0);

			foos.init(bcRule.getBundleContext());

			// Make sure the scheduled event is processed
			assertThat(scheduledFuture.get()).isNotNull();

			SoftAssertions softly = new SoftAssertions();

			softly.assertThat(foos.getService())
				.as("getService %s", foos)
				.isEqualTo(afoo);
			softly.assertThat(foos.getServiceReference())
				.as("getServiceReference %s", foos)
				.isNotNull();
			softly.assertThat(foos.getServiceReferences())
				.as("getServiceReferences %s", foos)
				.hasSize(1);
			softly.assertThat(foos.getServices())
				.as("getServices %s", foos)
				.containsExactly(afoo);
			softly.assertThat(foos.getTimeout())
				.as("getTimeout %s", foos)
				.isEqualTo(1000);
			softly.assertThat(foos.getTracked())
				.as("getTracked %s", foos)
				.hasSize(1);
			softly.assertThat(foos.getTrackingCount())
				.as("getTrackingCount %s", foos)
				.isGreaterThanOrEqualTo(1);
			softly.assertThat(foos.getCardinality())
				.as("getCardinality %s", foos)
				.isEqualTo(1);
			softly.assertThat(foos.size())
				.as("size %s", foos)
				.isEqualTo(1);
			softly.assertThat(foos.isEmpty())
				.as("isEmpty %s", foos)
				.isFalse();
			softly.assertThat(foos.waitForService(20))
				.as("waitForService %s", foos)
				.isEqualTo(afoo);

			softly.assertAll();
		}
	}

	@Test
	public void matchByFilter() throws Exception {
		try (BundleContextRule bcRule = new BundleContextRule();
			ServiceConfiguration<Foo> fooRule = new ServiceConfiguration<>(Foo.class, "(foo=bar)", new String[0], 1,
				DEFAULT_TIMEOUT)) {

			bcRule.init(this);

			assertThat(bcRule.getBundleContext()
				.getBundle()
				.getRegisteredServices()).isNull();

			final Foo afoo = new Foo() {};

			ScheduledFuture<ServiceRegistration<?>> scheduledFuture = executor.schedule(() -> bcRule.getBundleContext()
				.registerService(Foo.class, afoo,
					Dictionaries.dictionaryOf("foo", "bar", "case", name.getMethodName())),
				0);

			fooRule.init(bcRule.getBundleContext());

			// Make sure the scheduled event is processed
			assertThat(scheduledFuture.get()).isNotNull();

			SoftAssertions softly = new SoftAssertions();

			softly.assertThat(fooRule.getService())
				.as("getService %s", fooRule)
				.isNotNull();
			softly.assertThat(fooRule.getServiceReference())
				.as("getServiceReference %s", fooRule)
				.isNotNull();
			softly.assertThat(fooRule.getServiceReferences())
				.as("getServiceReferences %s", fooRule)
				.isNotNull();
			softly.assertThat(fooRule.getServices())
				.as("getServices %s", fooRule)
				.contains(afoo);
			softly.assertThat(fooRule.getTimeout())
				.as("getTimeout %s", fooRule)
				.isEqualTo(DEFAULT_TIMEOUT);
			softly.assertThat(fooRule.getTracked())
				.as("getTracked %s", fooRule)
				.isNotEmpty();
			softly.assertThat(fooRule.getTrackingCount())
				.as("getTrackingCount %s", fooRule)
				.isGreaterThanOrEqualTo(1);
			softly.assertThat(fooRule.getCardinality())
				.as("getCardinality %s", fooRule)
				.isEqualTo(1);
			softly.assertThat(fooRule.size())
				.as("size %s", fooRule)
				.isEqualTo(1);
			softly.assertThat(fooRule.isEmpty())
				.as("isEmpty %s", fooRule)
				.isFalse();
			softly.assertThat(fooRule.waitForService(20))
				.as("waitForService %s", fooRule)
				.isNotNull();

			softly.assertAll();
		}
	}

	@Test
	public void matchMultiple() throws Exception {
		try (BundleContextRule bcRule = new BundleContextRule();
			ServiceConfiguration<Foo> fooRule = new ServiceConfiguration<>(Foo.class, "", new String[0], 2,
				DEFAULT_TIMEOUT)) {

			bcRule.init(this);

			assertThat(bcRule.getBundleContext()
				.getBundle()
				.getRegisteredServices()).isNull();

			Foo s1 = new Foo() {}, s2 = new Foo() {};
			ScheduledFuture<ServiceRegistration<Foo>> scheduledFuture1 = executor
				.schedule(() -> bcRule.getBundleContext()
					.registerService(Foo.class, s1, Dictionaries.dictionaryOf("case", name.getMethodName()
						.concat("_1"))),
					0);
			ScheduledFuture<ServiceRegistration<Foo>> scheduledFuture2 = executor
				.schedule(() -> bcRule.getBundleContext()
					.registerService(Foo.class, s2, Dictionaries.dictionaryOf("case", name.getMethodName()
						.concat("_2"))),
					0);

			fooRule.init(bcRule.getBundleContext());

			// Make sure the scheduled event is processed
			assertThat(scheduledFuture1.get()).isNotNull();
			assertThat(scheduledFuture2.get()).isNotNull();

			SoftAssertions softly = new SoftAssertions();

			softly.assertThat(fooRule.getService())
				.as("getService %s", fooRule)
				.isIn(s1, s2);
			softly.assertThat(fooRule.getServiceReference())
				.as("getServiceReference %s", fooRule)
				.isNotNull();
			softly.assertThat(fooRule.getServiceReferences())
				.as("getServiceReferences %s", fooRule)
				.isNotNull();
			softly.assertThat(fooRule.getServices())
				.as("getServices %s", fooRule)
				.containsExactlyInAnyOrder(s1, s2);
			softly.assertThat(fooRule.getTimeout())
				.as("getTimeout %s", fooRule)
				.isEqualTo(DEFAULT_TIMEOUT);
			softly.assertThat(fooRule.getTracked())
				.as("getTracked %s", fooRule)
				.isNotEmpty();
			softly.assertThat(fooRule.getTrackingCount())
				.as("getTrackingCount %s", fooRule)
				.isGreaterThanOrEqualTo(2);
			softly.assertThat(fooRule.getCardinality())
				.as("getCardinality %s", fooRule)
				.isEqualTo(2);
			softly.assertThat(fooRule.size())
				.as("size %s", fooRule)
				.isEqualTo(2);
			softly.assertThat(fooRule.isEmpty())
				.as("isEmpty %s", fooRule)
				.isFalse();
			softly.assertThat(fooRule.waitForService(20))
				.as("waitForService %s", fooRule)
				.isNotNull();

			softly.assertAll();
		}
	}

	@Test
	public void nomatchByFilter() throws Exception {
		assertThatExceptionOfType(AssertionError.class) //
			.isThrownBy(() -> {
				try (BundleContextRule bcRule = new BundleContextRule();
						ServiceConfiguration<Foo> fooRule = new ServiceConfiguration<>(Foo.class, "(foo=baz)",
							new String[0], 1, DEFAULT_TIMEOUT)) {

						bcRule.init(this);

					assertThat(bcRule.getBundleContext()
						.getBundle()
						.getRegisteredServices()).isNull();

					final Foo afoo = new Foo() {};

					ScheduledFuture<ServiceRegistration<?>> scheduledFuture = executor
						.schedule(() -> bcRule.getBundleContext()
							.registerService(Foo.class, afoo,
								Dictionaries.dictionaryOf("foo", "bar", "case", name.getMethodName())),
							0);

						fooRule.init(bcRule.getBundleContext());

					// Make sure the scheduled event is processed
					assertThat(scheduledFuture.get()).isNotNull();
				}
			});
	}

	@Test
	public void malformedFilter() throws Exception {
		assertThatExceptionOfType(InvalidSyntaxException.class) //
			.isThrownBy(() -> {
				try (BundleContextRule bcRule = new BundleContextRule();
						ServiceConfiguration<Foo> fooRule = new ServiceConfiguration<>(Foo.class, "(foo=baz",
							new String[0], 1, DEFAULT_TIMEOUT)) {
				}
			});
	}

	@Test
	public void negativeCardinality() throws Exception {
		assertThatExceptionOfType(IllegalArgumentException.class) //
			.isThrownBy(() -> {
				try (BundleContextRule bcRule = new BundleContextRule();
						ServiceConfiguration<Foo> fooRule = new ServiceConfiguration<>(Foo.class, "", new String[0], -1,
							DEFAULT_TIMEOUT)) {}
			});
	}

	@Test
	public void negativeTimeout() throws Exception {
		assertThatExceptionOfType(IllegalArgumentException.class) //
			.isThrownBy(() -> {
				try (BundleContextRule bcRule = new BundleContextRule();
						ServiceConfiguration<Foo> fooRule = new ServiceConfiguration<>(Foo.class, "", new String[0], 1,
						-1L)) {
				}
			});
	}

}
