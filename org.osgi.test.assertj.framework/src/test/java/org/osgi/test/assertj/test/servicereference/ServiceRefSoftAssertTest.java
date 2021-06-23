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

import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.Version;
import org.osgi.test.assertj.servicereference.ServiceReferenceAssert;
import org.osgi.test.assertj.servicereference.ServiceReferenceSoftAssertions;

@ExtendWith(SoftAssertionsExtension.class)
public class ServiceRefSoftAssertTest {

	@Test
	public void isAssignableTo(ServiceReferenceSoftAssertions softly) throws Exception {

		ServiceReference<?> sr = mock(ServiceReference.class);
		when(sr.isAssignableTo(null, null)).thenReturn(true);

		softly.assertThat(sr).isAssignableTo(null, null);

		when(sr.isAssignableTo(null, null)).thenReturn(false);

		softly.assertThatCode(() -> ServiceReferenceAssert.assertThat(sr).isAssignableTo(null, null))
				.as("is not assignable to")
				.isInstanceOf(AssertionError.class);
	}

	@Test
	public void isRegisteredInBundle(ServiceReferenceSoftAssertions softly) throws Exception {

		Bundle bundle = mock(Bundle.class);
		Bundle otherBundle = mock(Bundle.class);
		ServiceReference<?> sr = mock(ServiceReference.class);

		when(sr.getBundle()).thenReturn(bundle);
		when(bundle.getSymbolicName()).thenReturn("foo");
		Version version = Version.parseVersion("1.0.0");
		when(bundle.getVersion()).thenReturn(version);

		softly.assertThat(sr).isRegisteredInBundle(bundle);
		softly.assertThat(sr).isRegisteredInBundle("foo");
		softly.assertThat(sr).isRegisteredInBundle("foo", "1.0.0");

		when(sr.isAssignableTo(null, null)).thenReturn(false);

		softly
				.assertThatCode(
						() -> ServiceReferenceAssert.assertThat(sr).isRegisteredInBundle(otherBundle))
				.as("is not registered in Bundle")
				.isInstanceOf(AssertionError.class);

		softly.assertThatCode(() -> ServiceReferenceAssert.assertThat(sr).isRegisteredInBundle("bar"))
				.as("is not registered in Bundle")
				.isInstanceOf(AssertionError.class);

		softly
				.assertThatCode(
						() -> ServiceReferenceAssert.assertThat(sr).isRegisteredInBundle("foo", "0.0.1"))
				.as("is not registered in Bundle")
				.isInstanceOf(AssertionError.class);

	}

}
