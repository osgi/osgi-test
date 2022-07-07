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

import static java.util.Objects.requireNonNull;
import static org.assertj.core.error.ShouldNotBeNull.shouldNotBeNull;

import org.assertj.core.api.InstanceOfAssertFactory;
import org.osgi.framework.ServiceRegistration;

public class ServiceRegistrationAssert<SERVICE> extends
	AbstractServiceRegistrationAssert<ServiceRegistrationAssert<SERVICE>, ServiceRegistration<? extends SERVICE>, SERVICE> {
	/**
	 * Create assertion for {@link org.osgi.framework.ServiceRegistration}.
	 *
	 * @param actual the actual value.
	 */
	public ServiceRegistrationAssert(ServiceRegistration<? extends SERVICE> actual) {
		super(actual, ServiceRegistrationAssert.class);
	}

	/**
	 * Create assertion for {@link org.osgi.framework.ServiceRegistration}.
	 *
	 * @param actual the actual value.
	 * @return the created assertion object.
	 */
	public static <SERVICE> ServiceRegistrationAssert<SERVICE> assertThat(
		ServiceRegistration<? extends SERVICE> actual) {
		return new ServiceRegistrationAssert<SERVICE>(actual);
	}

	/**
	 * {@link InstanceOfAssertFactory} for a {@link ServiceRegistrationAssert}.
	 *
	 * @param <ACTUAL> The actual type of the {@code ServiceRegistration}.
	 * @param <SERVICE> The type of the service that the
	 *            {@code ServiceRegistration} refers to.
	 * @param serviceType The service type class.
	 * @return The factory instance.
	 * @see #SERVICE_REGISTRATION
	 */
	public static <ACTUAL extends ServiceRegistration<? extends SERVICE>, SERVICE> InstanceOfAssertFactory<ACTUAL, ServiceRegistrationAssert<SERVICE>> serviceRegistration(
		Class<SERVICE> serviceType) {
		requireNonNull(serviceType, shouldNotBeNull("serviceType")::create);
		@SuppressWarnings({
			"rawtypes", "unchecked"
		})
		Class<ACTUAL> type = (Class) ServiceRegistration.class;
		return new InstanceOfAssertFactory<>(type, ServiceRegistrationAssert::<SERVICE> assertThat);
	}

	/**
	 * {@link InstanceOfAssertFactory} for a {@link ServiceRegistrationAssert}
	 * using {@code Object} as the result type.
	 *
	 * @see #serviceRegistration(Class)
	 */
	public static final InstanceOfAssertFactory<ServiceRegistration<?>, ServiceRegistrationAssert<Object>> SERVICE_REGISTRATION = serviceRegistration(
		Object.class);
}
