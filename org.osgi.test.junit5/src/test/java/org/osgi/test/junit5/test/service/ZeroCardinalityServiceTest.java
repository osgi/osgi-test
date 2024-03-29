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

import org.junit.jupiter.api.Test;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.service.ServiceAware;
import org.osgi.test.junit5.test.types.Foo;

public class ZeroCardinalityServiceTest {

	@InjectService(cardinality = 0)
	Foo					foo;
	@InjectService(cardinality = 0)
	ServiceAware<Foo>	fServiceAware;

	@Test
	public void testNoService() throws Exception {
		assertThat(fServiceAware.getService()).isNull();
	}

	@Test
	public void testNullField() throws Exception {
		assertThat(foo).isNull();
	}

	@Test
	public void testWithLogServiceParameter(@InjectService(cardinality = 0) Foo foo) throws Exception {
		assertThat(foo).isNull();
	}

}
