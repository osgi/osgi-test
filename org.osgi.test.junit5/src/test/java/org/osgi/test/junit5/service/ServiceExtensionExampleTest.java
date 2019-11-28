/*
 * Copyright (c) OSGi Alliance (2019). All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.osgi.test.junit5.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.osgi.service.log.LogService;
import org.osgi.test.junit5.types.Foo;

public class ServiceExtensionExampleTest {

	@RegisterExtension
	ServiceUseExtension<Foo>	fExt	= new ServiceUseExtension.Builder<>(	//
		Foo.class)																	//
			.cardinality(0)
			.build();
	@RegisterExtension
	ServiceUseExtension<LogService>	lsExt	= new ServiceUseExtension.Builder<>(	//
		LogService.class).build();

	@ServiceUseParameter
	LogService						logService;

	@Test
	public void testNoService() throws Exception {
		assertThat(fExt.getService()).isNull();
	}

	@Test
	public void testWithLogServiceUse() throws Exception {
		assertThat(lsExt.getService()).isNotNull();
	}

	@Test
	public void testWithLogServiceField() throws Exception {
		assertThat(logService).isNotNull();
	}

	@Test
	public void testWithLogServiceParameter(@ServiceUseParameter LogService logService) throws Exception {
		assertThat(logService).isNotNull();
	}

}
