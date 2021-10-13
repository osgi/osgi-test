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

package org.osgi.test.assertj.test.servicereference;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.Version;
import org.osgi.test.assertj.servicereference.ServiceReferenceAssert;
import org.osgi.test.assertj.test.testutil.AbstractAssertTest;

public class ServiceReferenceAssertTest
	extends AbstractAssertTest<ServiceReferenceAssert<Object>, ServiceReference<?>> {
	public ServiceReferenceAssertTest() {
		super(ServiceReferenceAssert::assertThat);
	}

	@Test
	public void isAssignableTo() throws Exception {

		ServiceReference<?> sr = mock(ServiceReference.class);
		setActual(sr);

		when(sr.isAssignableTo(null, null)).thenReturn(true);
		assertPassing("isNot", x -> aut.isAssignableTo(null, null), null);

		when(sr.isAssignableTo(null, null)).thenReturn(false);
		assertFailing("is", x -> aut.isAssignableTo(null, null), null)
			.hasMessageMatching("(?si).*to be assignable to:.*but it was not.*");

		String bundleName = "myBundleName";
		String className = "myClassName";
		Bundle bundle = mock(Bundle.class, bundleName);

		when(sr.isAssignableTo(bundle, className)).thenReturn(false);
		assertFailing("is", x -> aut.isAssignableTo(bundle, className), null).hasMessageMatching(
			"(?si).*to be assignable to:.*<" + bundleName + ">,<" + className + ">.*but it was not.*");
	}

	@Test
	public void isRegisteredInBundle() throws Exception {

		Bundle bundle = mock(Bundle.class);
		Bundle otherBundle = mock(Bundle.class);
		ServiceReference<?> sr = mock(ServiceReference.class);
		setActual(sr);

		when(sr.getBundle()).thenReturn(bundle);
		assertPassing("isNot", x -> aut.isRegisteredInBundle(bundle), null);

		assertFailing("is", x -> aut.isRegisteredInBundle(otherBundle), null)
			.hasMessageMatching("(?si).*to be registered in Bundle:.*but it was not.*");
	}

	@Test
	public void isRegisteredInBundleBSN() throws Exception {

		Bundle bundle = mock(Bundle.class);
		ServiceReference<?> sr = mock(ServiceReference.class);
		setActual(sr);

		when(sr.getBundle()).thenReturn(bundle);
		when(bundle.getSymbolicName()).thenReturn("bsn");

		assertPassing("isNot", x -> aut.isRegisteredInBundle("bsn"), null);

		assertFailing("is", x -> aut.isRegisteredInBundle("otherBundle"), null)
			.hasMessageMatching("(?si).*to be registered in Bundle with SymbolicName:.*but it was not.*");
	}

	@Test
	public void isRegisteredInBundleBSNV() throws Exception {

		Bundle bundle = mock(Bundle.class);
		ServiceReference<?> sr = mock(ServiceReference.class);
		setActual(sr);

		when(sr.getBundle()).thenReturn(bundle);
		when(bundle.getSymbolicName()).thenReturn("bsn");
		when(bundle.getVersion()).thenReturn(Version.parseVersion("1.1.1"));

		assertPassing("isNot", x -> aut.isRegisteredInBundle("bsn", "1.1.1"), null);

		assertFailing("is", x -> aut.isRegisteredInBundle("otherBundle", "1.1.1"), null)
			.hasMessageMatching("(?si).*to be registered in Bundle with SymbolicName and Version.*but it was.*");

		assertFailing("is", x -> aut.isRegisteredInBundle("bsn", "1.1.2"), null)
			.hasMessageMatching("(?si).*to be registered in Bundle with SymbolicName and Version.*but it was.*");

		assertFailing("is", x -> aut.isRegisteredInBundle("otherBundle", "1.1.2"), null)
			.hasMessageMatching("(?si).*to be registered in Bundle with SymbolicName and Version.*but it was.*");
	}

}
