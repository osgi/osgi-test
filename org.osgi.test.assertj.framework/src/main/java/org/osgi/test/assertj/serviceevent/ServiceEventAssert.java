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

package org.osgi.test.assertj.serviceevent;

import org.assertj.core.api.InstanceOfAssertFactory;
import org.osgi.framework.ServiceEvent;

public class ServiceEventAssert extends AbstractServiceEventAssert<ServiceEventAssert, ServiceEvent> {

	public static final InstanceOfAssertFactory<ServiceEvent, ServiceEventAssert> SERVICE_EVENT = new InstanceOfAssertFactory<>(
		ServiceEvent.class, ServiceEventAssert::new);

	public ServiceEventAssert(ServiceEvent actual) {
		super(actual, ServiceEventAssert.class, ServiceEvent::getType);
	}

	public static ServiceEventAssert assertThat(ServiceEvent actual) {
		return new ServiceEventAssert(actual);
	}
}
