/*
 * Copyright (c) OSGi Alliance (2020). All Rights Reserved.
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

package org.osgi.test.junit4.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Rule;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.service.ServiceAware;
import org.osgi.test.junit4.context.BundleContextRule;
import org.osgi.test.junit4.types.Foo;

public class OwnServiceTest {

	@Rule
	public BundleContextRule	bcr	= new BundleContextRule();
	@Rule
	public ServiceRule			sur	= new ServiceRule();

	@InjectBundleContext
	BundleContext				bundleContext;
	@InjectService(cardinality = 0)
	ServiceAware<Foo>	fooServiceAware;

	@Test
	public void testWithLogServiceUse() throws Exception {
		bundleContext.registerService(Foo.class, new Foo() {}, null);
		assertThat(fooServiceAware.isEmpty()).isFalse();
	}

}
