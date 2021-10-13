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

package org.osgi.test.assertj.servicereference;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.error.ShouldNotBeNull.shouldNotBeNull;

import org.assertj.core.api.InstanceOfAssertFactory;
import org.osgi.framework.ServiceReference;

public class ServiceReferenceAssert<SERVICE> extends
	AbstractServiceReferenceAssert<ServiceReferenceAssert<SERVICE>, ServiceReference<? extends SERVICE>, SERVICE> {

	/**
	 * Create assertion for {@link org.osgi.framework.ServiceReference}.
	 *
	 * @param actual the actual value.
	 */
	public ServiceReferenceAssert(ServiceReference<? extends SERVICE> actual) {
		super(actual, ServiceReferenceAssert.class);
	}

	/**
	 * Create assertion for {@link org.osgi.framework.ServiceReference}.
	 *
	 * @param actual the actual value.
	 * @return the created assertion object.
	 */
	public static <SERVICE> ServiceReferenceAssert<SERVICE> assertThat(ServiceReference<? extends SERVICE> actual) {
		return new ServiceReferenceAssert<SERVICE>(actual);
	}

	/**
	 * {@link InstanceOfAssertFactory} for a {@link ServiceReferenceAssert}.
	 *
	 * @param <ACTUAL> The actual type of the {@code ServiceReference}.
	 * @param <SERVICE> The type of the service that the
	 *            {@code ServiceReference} refers to.
	 * @param serviceType The service type class.
	 * @return The factory instance.
	 * @see #SERVICE_REFERENCE
	 * @since 1.1
	 */
	public static <ACTUAL extends ServiceReference<? extends SERVICE>, SERVICE> InstanceOfAssertFactory<ACTUAL, ServiceReferenceAssert<SERVICE>> serviceReference(
		Class<SERVICE> serviceType) {
		requireNonNull(serviceType, shouldNotBeNull("serviceType").create());
		@SuppressWarnings({
			"rawtypes", "unchecked"
		})
		Class<ACTUAL> type = (Class) ServiceReference.class;
		return new InstanceOfAssertFactory<>(type, ServiceReferenceAssert::<SERVICE> assertThat);
	}

	/**
	 * {@link InstanceOfAssertFactory} for a {@link ServiceReferenceAssert}
	 * using {@code Object} as the result type.
	 *
	 * @see #serviceReference(Class)
	 * @since 1.1
	 */
	public static final InstanceOfAssertFactory<ServiceReference<?>, ServiceReferenceAssert<Object>> SERVICE_REFERENCE = serviceReference(
		Object.class);

}
