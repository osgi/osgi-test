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
package org.osgi.test.junit5.framework;

import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.FrameworkListener;

/**
 * This class can be used as a way to listen to framework events and inspect
 * them later.
 */
public class FrameworkEvents implements FrameworkListener {

	private Map<FrameworkEvent, Long>	timeStamps	= new ConcurrentHashMap<>();
	private Set<FrameworkEvent> events = ConcurrentHashMap.newKeySet();

	@Override
	public void frameworkEvent(FrameworkEvent event) {

		if (events.add(event)) {
			timeStamps.put(event, System.currentTimeMillis());
		}
	}

	/**
	 * clear all recorded events
	 */
	public void clear() {
		events.clear();
	}

	/**
	 * @return a stream of events in the order they where recorded
	 */
	public Stream<FrameworkEvent> events() {
		return events.stream()
			.sorted(Comparator.comparingLong(event -> timeStamps.getOrDefault(event, Long.MAX_VALUE / 2)));
	}

	/**
	 * @return a stream of events in the order they where recorded and of the
	 *         given type
	 */
	public Stream<FrameworkEvent> events(int type) {
		return events().filter(event -> event.getType() == type);
	}

	/**
	 * asserts that the current recorded events do not contain any error
	 */
	public void assertErrorsFree() {
		events(FrameworkEvent.ERROR).findFirst()
			.ifPresent(event -> {
				throw new AssertionError(event.getBundle()
					.getSymbolicName(), event.getThrowable());
			});
	}

}
