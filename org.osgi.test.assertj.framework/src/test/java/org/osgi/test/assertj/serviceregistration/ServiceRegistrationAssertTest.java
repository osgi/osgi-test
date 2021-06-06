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

package org.osgi.test.assertj.serviceregistration;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.osgi.framework.ServiceRegistration;

class ServiceRegistrationAssertTest {

	@Test
	void hasServiceReference() {
		ServiceRegistration<?> serviceRegistration = mock(ServiceRegistration.class);

		assertNotNull(ServiceRegistrationAssert.assertThat(serviceRegistration)
			.hasServiceReferenceThat());

		assertThatExceptionOfType(AssertionError.class).isThrownBy(() -> ServiceRegistrationAssert.assertThat(null)
			.hasServiceReferenceThat())
			.withFailMessage("Expecting actual not to be null");
	}

}
