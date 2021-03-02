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

import static org.osgi.test.assertj.bundle.BundleAssert.BUNDLE;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.assertj.core.api.AbstractComparableAssert;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.assertj.core.api.InstanceOfAssertFactory;
import org.assertj.core.api.ListAssert;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.Version;
import org.osgi.test.assertj.bundle.BundleAssert;
import org.osgi.test.assertj.dictionary.DictionaryAssert;
import org.osgi.test.common.dictionary.Dictionaries;

public abstract class AbstractServiceReferenceAssert<SELF extends AbstractServiceReferenceAssert<SELF, ACTUAL>, ACTUAL extends ServiceReference<?>>
	extends AbstractComparableAssert<SELF, ACTUAL> {

	protected AbstractServiceReferenceAssert(ACTUAL actual, Class<?> selfType) {
		super(actual, selfType);
	}

	public DictionaryAssert<String, Object> hasServicePropertiesThat() {
		return new DictionaryAssert<String, Object>(Dictionaries.asDictionary(actual));
	}

	public BundleAssert isRegisteredInBundleThat() {
		return isNotNull().extracting(actual -> actual.getBundle(), BUNDLE)
			.as(actual + ".bundle");
	}

	@SuppressWarnings("rawtypes")
	static InstanceOfAssertFactory<List, ListAssert<Bundle>> BUNDLE_LIST = InstanceOfAssertFactories.list(Bundle.class);

	public ListAssert<Bundle> hasUsingBundlesThat() {
		return isNotNull().extracting(bundleContext -> Arrays.asList(bundleContext.getUsingBundles()), BUNDLE_LIST)
			.as(actual + ".usingBundles");
	}

	public SELF isRegisteredInBundle(Bundle bundle) {

		isNotNull();
		if (!Objects.equals(actual.getBundle(), bundle)) {
			throw failure("%nExpecting%n <%s>%nto be registered in Bundle:%n <%s>%n but it was not", actual, bundle);
		}

		return myself;
	}

	public SELF isRegisteredInBundle(String bundleSymbolicName) {

		isNotNull();
		if (actual.getBundle() == null || !actual.getBundle()
			.getSymbolicName()
			.equals(bundleSymbolicName)) {
			throw failure("%nExpecting%n <%s>%nto be registered in Bundle with SymbolicName:%n <%s>%n but it was not",
				actual, bundleSymbolicName);
		}

		return myself;
	}

	public SELF isRegisteredInBundle(String bundleSymbolicName, String version) {

		isNotNull();
		Bundle acBundle = actual.getBundle();
		String bsnva = acBundle == null ? "no bundle registered"
			: acBundle.getSymbolicName() + ":" + acBundle.getVersion();
		String bsnvExp = bundleSymbolicName + ":" + version;
		if (acBundle == null || !(acBundle.getSymbolicName()
			.equals(bundleSymbolicName)
			&& acBundle.getVersion()
				.equals(Version.parseVersion(version)))) {
			throw failureWithActualExpected(bsnva, bsnvExp,
				"%nExpecting%n <%s>%nto be registered in Bundle with SymbolicName and Version: <%s>%n but it was %s",
				actual, bsnvExp, bsnva);
		}
		return myself;
	}

	public SELF isAssignableTo(Class<?> clazz) {

		return isAssignableTo(FrameworkUtil.getBundle(clazz), clazz.getName());
	}

	public SELF isAssignableTo(Bundle bundle, String className) {

		isNotNull();
		if (!actual.isAssignableTo(bundle, className)) {
			throw failure("%nExpecting%n <%s>%nto be assignable to:%n <%s>,<%s>%n but it was not", actual, bundle,
				className);
		}
		return myself;
	}

	public SELF isBeingUsedBy(Bundle... bundles) {

		as("isBeingUsedBy").hasUsingBundlesThat()
			.contains(bundles);
		return myself;
	}

	public SELF isBeingUsedBy(String... bundleSymbolicNames) {

		for (String bsn : bundleSymbolicNames) {
			as("isBeingUsedBy").hasUsingBundlesThat()
				.anyMatch((b) -> bsn.equals(b.getSymbolicName()));
		}
		return myself;
	}

}
