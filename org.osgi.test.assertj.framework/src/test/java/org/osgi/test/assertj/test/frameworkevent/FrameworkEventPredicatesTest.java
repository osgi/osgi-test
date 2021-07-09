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

package org.osgi.test.assertj.test.frameworkevent;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkEvent;
import org.osgi.test.assertj.frameworkevent.FrameworkEventPredicates;

public class FrameworkEventPredicatesTest {

	Bundle bundle = Mockito.mock(Bundle.class);

	@Test
	void testPredicateFramework() throws Exception {

		SoftAssertions softly = new SoftAssertions();

		//
		FrameworkEvent event = new FrameworkEvent(FrameworkEvent.STARTLEVEL_CHANGED, bundle, null);

		//
		softly.assertThat(FrameworkEventPredicates.type(FrameworkEvent.STARTLEVEL_CHANGED)
			.test(event))
			.isTrue();

		softly.assertThat(FrameworkEventPredicates.type(FrameworkEvent.STARTED)
			.test(event))
			.isFalse();

		softly.assertThat(FrameworkEventPredicates.typeError()
			.test(new FrameworkEvent(FrameworkEvent.ERROR, bundle, null)))
			.isTrue();

		softly.assertThat(FrameworkEventPredicates.typeError()
			.test(event))
			.isFalse();

		//
		softly.assertThat(FrameworkEventPredicates.typeInfo()
			.test(new FrameworkEvent(FrameworkEvent.INFO, bundle, null)))
			.isTrue();

		softly.assertThat(FrameworkEventPredicates.typeInfo()
			.test(event))
			.isFalse();

		//
		softly.assertThat(FrameworkEventPredicates.typePackagesRefreshed()
			.test(new FrameworkEvent(FrameworkEvent.PACKAGES_REFRESHED, bundle, null)))
			.isTrue();

		softly.assertThat(FrameworkEventPredicates.typePackagesRefreshed()
			.test(event))
			.isFalse();

		//
		softly.assertThat(FrameworkEventPredicates.typeStarted()
			.test(new FrameworkEvent(FrameworkEvent.STARTED, bundle, null)))
			.isTrue();

		softly.assertThat(FrameworkEventPredicates.typeStarted()
			.test(event))
			.isFalse();

		//
		softly.assertThat(FrameworkEventPredicates.typeStartlevelChanged()
			.test(new FrameworkEvent(FrameworkEvent.STARTLEVEL_CHANGED, bundle, null)))
			.isTrue();

		softly.assertThat(FrameworkEventPredicates.typeStartlevelChanged()
			.test(new FrameworkEvent(FrameworkEvent.STOPPED, bundle, null)))
			.isFalse();

		//
		softly.assertThat(FrameworkEventPredicates.typeStopped()
			.test(new FrameworkEvent(FrameworkEvent.STOPPED, bundle, null)))
			.isTrue();

		softly.assertThat(FrameworkEventPredicates.typeStopped()
			.test(event))
			.isFalse();

		//
		softly.assertThat(FrameworkEventPredicates.typeStoppedUpdate()
			.test(new FrameworkEvent(FrameworkEvent.STOPPED_UPDATE, bundle, null)))
			.isTrue();

		softly.assertThat(FrameworkEventPredicates.typeStoppedUpdate()
			.test(event))
			.isFalse();

		//
		softly.assertThat(FrameworkEventPredicates.typeWaitTimeout()
			.test(new FrameworkEvent(FrameworkEvent.WAIT_TIMEDOUT, bundle, null)))
			.isTrue();

		softly.assertThat(FrameworkEventPredicates.typeWaitTimeout()
			.test(event))
			.isFalse();

		//
		softly.assertThat(FrameworkEventPredicates.typeWarning()
			.test(new FrameworkEvent(FrameworkEvent.WARNING, bundle, null)))
			.isTrue();

		softly.assertThat(FrameworkEventPredicates.typeWarning()
			.test(event))
			.isFalse();

		softly.assertAll();
	}

	@Test
	void test_frameworkEvent() throws Exception {
		assertThat(FrameworkEventPredicates.frameworkEvent()
			.test(new FrameworkEvent(FrameworkEvent.WAIT_TIMEDOUT, bundle, null))).isTrue();

		assertThat(FrameworkEventPredicates.frameworkEvent()
			.test("")).isFalse();

	}

	@Test
	void test_frameworkEventAnd() throws Exception {

		AtomicBoolean flag = new AtomicBoolean(false);

		Predicate<FrameworkEvent> p = new Predicate<FrameworkEvent>() {

			@Override
			public boolean test(FrameworkEvent t) {
				flag.set(true);
				return true;
			}
		};

		assertThat(FrameworkEventPredicates.frameworkEventAnd(p)
			.test(new FrameworkEvent(FrameworkEvent.WAIT_TIMEDOUT, bundle, null))).isTrue();

		assertThat(flag.get()).isTrue();
	}
}
