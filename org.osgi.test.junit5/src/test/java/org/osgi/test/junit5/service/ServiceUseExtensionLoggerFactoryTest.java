/*
 * Copyright (c) OSGi Alliance (2019-2020). All Rights Reserved.
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
import org.osgi.service.log.LoggerFactory;
import org.osgi.test.junit5.context.BundleContextExtension;

public class ServiceUseExtensionLoggerFactoryTest {

	@RegisterExtension
	public BundleContextExtension		bundleContextExtension	= new BundleContextExtension();
	@RegisterExtension
	public ServiceUseExtension<LoggerFactory>	loggerFactoryExtension			= new ServiceUseExtension.Builder<>(	//
		LoggerFactory.class, bundleContextExtension)
			.build();

	@Test
	public void test(@ServiceUseParameter LoggerFactory loggerFactory) throws Exception {
		assertThat(loggerFactory).isInstanceOf(LoggerFactory.class);
	}

}
