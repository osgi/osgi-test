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

package org.osgi.test.assertj.monitoring.internal;

import org.osgi.test.assertj.monitoring.MonitoringAssertionResult;

class MonitoringResultAssertionImpl extends AbstractMonitoringAssertion<MonitoringResultAssertionImpl, EventRecording>
		implements MonitoringAssertionResult {

	public MonitoringResultAssertionImpl(EventRecording actual, Class<MonitoringResultAssertionImpl> selfType) {
		super(actual, selfType);
	}

	public static MonitoringResultAssertionImpl assertThat(EventRecording actual) {
		return new MonitoringResultAssertionImpl(actual, MonitoringResultAssertionImpl.class);
	}

}
