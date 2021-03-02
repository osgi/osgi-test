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

package org.osgi.test.junit5.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.service.log.LogService;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.service.ServiceAware;

@ExtendWith(ServiceExtension.class)
public class FilterTest {

	@InjectService(filter = "(objectClass=org.osgi.service.log.LoggerFactory)")
	LogService					logService;
	@InjectService(filter = "(objectClass=org.osgi.service.log.LoggerFactory)")
	ServiceAware<LogService>	lsServiceAware;

	@Test
	public void testWithLogServiceUse() throws Exception {
		assertThat(lsServiceAware.getService()).isNotNull();
		assertThat(lsServiceAware.getFilter()
			.toString()).isEqualTo(
				"(&(objectClass=org.osgi.service.log.LogService)(objectClass=org.osgi.service.log.LoggerFactory))");
	}

	@Test
	public void testWithLogServiceField() throws Exception {
		assertThat(logService).isNotNull();
	}

	@Test
	public void testWithLogServiceParameter(
		@InjectService(filter = "(objectClass=org.osgi.service.log.LoggerFactory)") LogService logService)
		throws Exception {
		assertThat(logService).isNotNull();
	}

}
