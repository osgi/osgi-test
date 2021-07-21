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

import java.time.Instant;

import org.osgi.test.assertj.monitoring.TimedEvent;


/**
 * The Class TimedEvent.
 *
 * @param <T> the generic type of the event
 */
 class TimedEventImpl<T> implements TimedEvent<T> {
	
	/** The event. */
	private T event;

	/** The instant. */
	private Instant instant = Instant.now();

	/**
	 * Instantiates a new timed event.
	 *
	 * @param event the event
	 */
	public TimedEventImpl(T event) {
		this.event = event;
	}

	/**
	 * Gets the event.
	 *
	 * @return the event
	 */
	@Override
	public T getEvent() {
		return event;
	}

	/**
	 * Gets the instant. Time where the event is fired.
	 *
	 * @return the instant
	 */
	@Override
	public Instant getInstant() {
		return instant;
	}

	/**
	 * To string.
	 *
	 * @return the string
	 */
	@Override
	public String toString() {
		return "TimedEvent [event=" + event + ", instant=" + instant + "]";
	}

}