/*
 * Copyright (c) OSGi Alliance (2019, 2020). All Rights Reserved.
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

package org.osgi.test.assertj.bundlereference;

import static org.osgi.test.assertj.bundle.BundleAssert.BUNDLE;

import java.util.Objects;

import org.assertj.core.api.AbstractAssert;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleReference;
import org.osgi.test.assertj.bundle.AbstractBundleAssert;

public abstract class AbstractBundleReferenceAssert<SELF extends AbstractBundleReferenceAssert<SELF, ACTUAL>, ACTUAL extends BundleReference>
	extends AbstractAssert<SELF, ACTUAL> {

	protected AbstractBundleReferenceAssert(ACTUAL actual, Class<?> selfType) {
		super(actual, selfType);
	}

	public SELF refersToBundle(Bundle expected) {
		isNotNull();
		if (!Objects.equals(actual.getBundle(), expected)) {
			failWithActualExpectedAndMessage(actual.getBundle(), expected,
				"%nExpecting%n <%s>%nto have bundle source:%n <%s>%n but was:%n<%s>", actual, expected,
				actual.getBundle());
		}
		return myself;
	}

	public AbstractBundleAssert<?, ? extends Bundle> refersToBundleThat() {
		return isNotNull().extracting(BundleReference::getBundle,
			BUNDLE)
			.as(actual + ".bundle");
	}
}
