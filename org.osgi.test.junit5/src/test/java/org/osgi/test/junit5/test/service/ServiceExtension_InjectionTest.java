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

package org.osgi.test.junit5.test.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.osgi.framework.Constants.SERVICE_RANKING;
import static org.osgi.test.common.dictionary.Dictionaries.dictionaryOf;

import org.assertj.core.api.junit.jupiter.InjectSoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.framework.BundleContext;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.service.ServiceAware;
import org.osgi.test.junit5.service.ServiceExtension;
import org.osgi.test.junit5.test.activator.TestActivator;
import org.osgi.test.junit5.test.testutils.OSGiSoftAssertions;
import org.osgi.test.junit5.test.types.Bar;

@ExtendWith(SoftAssertionsExtension.class)
@ExtendWith(ServiceExtension.class)
public class ServiceExtension_InjectionTest {
	@InjectSoftAssertions
	OSGiSoftAssertions		softly;

	@InjectBundleContext
	static BundleContext	staticBC;

	@InjectService
	static Bar				staticBar;

	static Bar				beforeAllBarNew;
	static Bar				beforeAllBar;

	final Bar				constructorBar;

	@InjectBundleContext
	BundleContext			bundleContext;

	@InjectService
	Bar						bar;
	@InjectService(cardinality = 0)
	ServiceAware<Bar>		barServiceAware;

	ServiceExtension_InjectionTest(@InjectService
	Bar bar) {
		constructorBar = bar;
		assertThat(beforeAllBarNew).isSameAs(bar);
	}

	@BeforeAll
	static void beforeAll(@InjectService
	Bar barParam) {
		assertThat(staticBar).isSameAs(TestActivator.BAR)
			.isSameAs(barParam);
		beforeAllBar = barParam;
		beforeAllBarNew = new Bar() {};
		staticBC.registerService(Bar.class, beforeAllBarNew, dictionaryOf(SERVICE_RANKING, 10));
	}

	@Test
	public void testWithBarServiceAware(@InjectService
	Bar barParam) throws Exception {
		Bar ourBar = new Bar() {};
		bundleContext.registerService(Bar.class, ourBar, dictionaryOf(SERVICE_RANKING, 20));
		softly.assertThat(barServiceAware.isEmpty())
			.isFalse();
		Bar service = barServiceAware.getService();
		softly.assertThat(service)
			.isSameAs(ourBar)
			.isNotSameAs(staticBar)
			.isNotSameAs(bar)
			.isNotSameAs(barParam);
		softly.assertThat(bar)
			.isSameAs(beforeAllBarNew)
			.isSameAs(constructorBar)
			.isSameAs(barParam)
			.isNotSameAs(staticBar);
		softly.assertThat(staticBar)
			.isSameAs(beforeAllBar)
			.isSameAs(TestActivator.BAR);
	}

	@AfterAll
	static void afterAll() {
		assertThat(staticBar).isSameAs(TestActivator.BAR);
	}
}
