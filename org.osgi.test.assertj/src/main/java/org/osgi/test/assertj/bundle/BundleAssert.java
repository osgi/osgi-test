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
package org.osgi.test.assertj.bundle;

import org.assertj.core.api.InstanceOfAssertFactory;
import org.osgi.framework.Bundle;

public class BundleAssert extends AbstractBundleAssert<BundleAssert, Bundle> {

	public static final InstanceOfAssertFactory<Bundle, BundleAssert> BUNDLE = new InstanceOfAssertFactory<>(
		Bundle.class, BundleAssert::new);

	public BundleAssert(Bundle actual) {
		super(actual, BundleAssert.class);
	}

	public static BundleAssert assertThat(Bundle actual) {
		return new BundleAssert(actual);
	}
}
