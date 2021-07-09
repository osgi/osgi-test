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

package org.osgi.test.assertj.test.bundleevent;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;
import org.osgi.test.assertj.bundleevent.BundleEventPredicates;

public class BundleEventPredicatesTest {

	Bundle bundle = Mockito.mock(Bundle.class);

	@Test
	void test_type() throws Exception {

		SoftAssertions softly = new SoftAssertions();

		//

		//
		softly.assertThat(BundleEventPredicates.type(BundleEvent.INSTALLED)
			.test(new BundleEvent(BundleEvent.INSTALLED, bundle)))
			.isTrue();

		softly.assertThat(BundleEventPredicates.type(BundleEvent.INSTALLED)
			.test(new BundleEvent(BundleEvent.UNINSTALLED, bundle)))
			.isFalse();

		softly.assertThat(BundleEventPredicates.typeInstalled()
			.test(new BundleEvent(BundleEvent.INSTALLED, bundle)))
			.isTrue();

		softly.assertThat(BundleEventPredicates.typeInstalled()
			.test(new BundleEvent(BundleEvent.UNINSTALLED, bundle)))
			.isFalse();

		softly.assertThat(BundleEventPredicates.typeLazyActivation()
			.test(new BundleEvent(BundleEvent.LAZY_ACTIVATION, bundle)))
			.isTrue();

		softly.assertThat(BundleEventPredicates.typeLazyActivation()
			.test(new BundleEvent(BundleEvent.INSTALLED, bundle)))
			.isFalse();

		softly.assertThat(BundleEventPredicates.typeResolved()
			.test(new BundleEvent(BundleEvent.RESOLVED, bundle)))
			.isTrue();

		softly.assertThat(BundleEventPredicates.typeResolved()
			.test(new BundleEvent(BundleEvent.INSTALLED, bundle)))
			.isFalse();

		softly.assertThat(BundleEventPredicates.typeStarted()
			.test(new BundleEvent(BundleEvent.STARTED, bundle)))
			.isTrue();

		softly.assertThat(BundleEventPredicates.typeStarted()
			.test(new BundleEvent(BundleEvent.INSTALLED, bundle)))
			.isFalse();

		softly.assertThat(BundleEventPredicates.typeStarting()
			.test(new BundleEvent(BundleEvent.STARTING, bundle)))
			.isTrue();

		softly.assertThat(BundleEventPredicates.typeStarting()
			.test(new BundleEvent(BundleEvent.INSTALLED, bundle)))
			.isFalse();

		softly.assertThat(BundleEventPredicates.typeStopped()
			.test(new BundleEvent(BundleEvent.STOPPED, bundle)))
			.isTrue();

		softly.assertThat(BundleEventPredicates.typeStopped()
			.test(new BundleEvent(BundleEvent.INSTALLED, bundle)))
			.isFalse();

		softly.assertThat(BundleEventPredicates.typeStopping()
			.test(new BundleEvent(BundleEvent.STOPPING, bundle)))
			.isTrue();

		softly.assertThat(BundleEventPredicates.typeStopping()
			.test(new BundleEvent(BundleEvent.INSTALLED, bundle)))
			.isFalse();

		softly.assertThat(BundleEventPredicates.typeUnresolved()
			.test(new BundleEvent(BundleEvent.UNRESOLVED, bundle)))
			.isTrue();

		softly.assertThat(BundleEventPredicates.typeUnresolved()
			.test(new BundleEvent(BundleEvent.INSTALLED, bundle)))
			.isFalse();

		softly.assertThat(BundleEventPredicates.typeUninstalled()
			.test(new BundleEvent(BundleEvent.UNINSTALLED, bundle)))
			.isTrue();

		softly.assertThat(BundleEventPredicates.typeUninstalled()
			.test(new BundleEvent(BundleEvent.INSTALLED, bundle)))
			.isFalse();

		softly.assertThat(BundleEventPredicates.typeUpdated()
			.test(new BundleEvent(BundleEvent.UPDATED, bundle)))
			.isTrue();

		softly.assertThat(BundleEventPredicates.typeUpdated()
			.test(new BundleEvent(BundleEvent.INSTALLED, bundle)))
			.isFalse();

		softly.assertThat(BundleEventPredicates.bundleEvent()
			.test(new BundleEvent(BundleEvent.UPDATED, bundle)))
			.isTrue();

		softly.assertThat(BundleEventPredicates.bundleEvent()
			.test(""))
			.isFalse();

		softly.assertAll();
	}

	@Test
	void test_bundleEvent() throws Exception {
		assertThat(BundleEventPredicates.bundleEvent()
			.test(new BundleEvent(BundleEvent.UPDATED, bundle))).isTrue();

		assertThat(BundleEventPredicates.bundleEvent()
			.test("")).isFalse();

	}

	@Test
	void test_bundleEventAnd() throws Exception {

		AtomicBoolean flag = new AtomicBoolean(false);

		Predicate<BundleEvent> p = new Predicate<BundleEvent>() {

			@Override
			public boolean test(BundleEvent t) {
				flag.set(true);
				return true;
			}
		};

		assertThat(BundleEventPredicates.bundleEventAnd(p)
			.test(new BundleEvent(BundleEvent.UPDATED, bundle))).isTrue();

		assertThat(flag.get()).isTrue();
	}

}
