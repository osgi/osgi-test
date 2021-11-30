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

package org.osgi.test.assertj.test.testutil;

import static org.mockito.Mockito.mock;

import java.util.function.Supplier;

import org.assertj.core.api.Assert;
import org.assertj.core.api.AssertFactory;
import org.assertj.core.api.SoftAssertionsProvider;
import org.junit.jupiter.api.Test;

// "SAP" = "SoftAssertionsProvider"
public abstract class AbstractAssertAndSAPTest<SELF extends Assert<SELF, ACTUAL>, ACTUAL, SAP extends SoftAssertionsProvider>
	extends AbstractAssertTest<SELF, ACTUAL> {

	protected final Class<ACTUAL>		actualClass;
	protected final Class<SAP> sap;
	protected final Supplier<ACTUAL>	actualFactory;

	protected AbstractAssertAndSAPTest(AssertFactory<ACTUAL, SELF> assertThat, Class<SAP> sap,
		Class<ACTUAL> actualClass) {
		this(assertThat, sap, actualClass, null);
	}

	protected AbstractAssertAndSAPTest(AssertFactory<ACTUAL, SELF> assertThat, Class<SAP> sap,
		Class<ACTUAL> actualClass,
		Supplier<ACTUAL> actualFactory) {
		super(assertThat);
		this.sap = sap;
		this.actualClass = actualClass;
		this.actualFactory = actualFactory == null ? () -> mock(actualClass) : actualFactory;
	}

	@Test
	void softAssertionsProvider() throws Exception {
		setActual(actualFactory.get());
		softAssertionsProvider(sap, actualClass);
	}
}
