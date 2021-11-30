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

package org.osgi.test.assertj.test.bundlereference;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.assertj.core.api.AssertFactory;
import org.assertj.core.api.SoftAssertionsProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleReference;
import org.osgi.test.assertj.bundlereference.AbstractBundleReferenceAssert;
import org.osgi.test.assertj.test.testutil.AbstractAssertAndSAPTest;

public abstract class AbstractBundleReferenceAssertTest<ASSERT extends AbstractBundleReferenceAssert<ASSERT, ACTUAL>, ACTUAL extends BundleReference, SAP extends SoftAssertionsProvider>
	extends AbstractAssertAndSAPTest<ASSERT, ACTUAL, SAP> {

	protected AbstractBundleReferenceAssertTest(AssertFactory<ACTUAL, ASSERT> factory, Class<SAP> sap,
		Class<ACTUAL> actualClass) {
		super(factory, sap, actualClass);
	}

	protected Bundle				bundle;
	protected Bundle				otherBundle;

	@BeforeEach
	void setUp() {
		bundle = mock(Bundle.class);
		otherBundle = mock(Bundle.class);
		ACTUAL sut = mock(actualClass);
		when(sut.getBundle()).thenReturn(bundle);
		setActual(sut);
	}

	@Test
	void refersToBundle() {
		assertEqualityAssertion("bundle", aut::refersToBundle, bundle, otherBundle);
	}

	@Test
	void refersToBundleThat() {
		assertChildAssertion("bundle", aut::refersToBundleThat, actual::getBundle);
	}
}
