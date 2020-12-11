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

package org.osgi.test.common.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.FrameworkListener;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.test.common.event.EventRecording.TimedEvent;

public class EventRecordingTest {

	static List<FrameworkListener>	frameworkListeners	= new ArrayList<>();
	static List<ServiceListener>	serviceListeners	= new ArrayList<>();
	static List<BundleListener>		bundleListeners		= new ArrayList<>();
	static BundleContext			bundleContext;

	static Bundle					bundle;

	@BeforeAll
	public static void beforeAll() {
		bundle = mock(Bundle.class);

		when(bundle.getSymbolicName()).thenReturn("test");

		bundleContext = mock(BundleContext.class);

		doAnswer(invocation -> {
			Object listener = invocation.getArgument(0);
			bundleListeners.add((BundleListener) listener);
			return null;
		}).when(bundleContext)
			.addBundleListener(Mockito.any(BundleListener.class));

		doAnswer(invocation -> {
			Object listener = invocation.getArgument(0);
			bundleListeners.remove(listener);
			return null;
		}).when(bundleContext)
			.removeBundleListener(Mockito.any(BundleListener.class));

		doAnswer(invocation -> {
			Object listener = invocation.getArgument(0);
			frameworkListeners.add((FrameworkListener) listener);
			return null;
		}).when(bundleContext)
			.addFrameworkListener(Mockito.any(FrameworkListener.class));

		doAnswer(invocation -> {
			Object listener = invocation.getArgument(0);
			frameworkListeners.remove(listener);
			return null;
		}).when(bundleContext)
			.removeFrameworkListener(Mockito.any(FrameworkListener.class));

		doAnswer(invocation -> {
			Object listener = invocation.getArgument(0);
			serviceListeners.add((ServiceListener) listener);
			return null;
		}).when(bundleContext)
			.addServiceListener(Mockito.any(ServiceListener.class));
		doAnswer(invocation -> {
			Object listener = invocation.getArgument(0);
			serviceListeners.remove(listener);
			return null;
		}).when(bundleContext)
			.removeServiceListener(Mockito.any(ServiceListener.class));

	}

	@Test
	public void test_takeSnapsot() throws Exception {

		// Test any Match
		Predicate<Object> matches = (event) -> {
			if (event instanceof ServiceEvent) {
				return ((ServiceEvent) event).getType() == ServiceEvent.REGISTERED;
			}
			return false;
		};

		Runnable ecexute = () -> {

			assertThat(frameworkListeners).hasSize(1);

			assertThat(bundleListeners).hasSize(1);

			assertThat(serviceListeners).hasSize(1);

			frameworkListeners
				.forEach((l) -> l.frameworkEvent(new FrameworkEvent(FrameworkEvent.INFO, mock(Bundle.class), null)));

			bundleListeners.forEach(
				(l) -> l.bundleChanged(new BundleEvent(BundleEvent.INSTALLED, mock(Bundle.class), mock(Bundle.class))));

			serviceListeners.forEach(
				(l) -> l.serviceChanged(new ServiceEvent(ServiceEvent.REGISTERED, mock(ServiceReference.class))));

		};
		// Create an Obervator with the given count and Predicate-Matcher
		EventRecording result = EventRecording.recordEvents(bundleContext, ecexute, matches, 0);

		// Check that the event happened

		assertThat(result).isNotNull();
		assertThat(result.isTimedOut()).isFalse();
		assertThat(result.timedEvents()).isNotNull();
		assertThat(result.timedEvents()).hasSize(3);

		assertThat(result.timedEvents()).element(0)
			.extracting(TimedEvent::getEvent)
			.isInstanceOf(FrameworkEvent.class);

		assertThat(result.timedEvents()).element(1)
			.extracting(TimedEvent::getEvent)
			.isInstanceOf(BundleEvent.class);

		assertThat(result.timedEvents()).element(2)
			.extracting(TimedEvent::getEvent)
			.isInstanceOf(ServiceEvent.class);

		assertThat(bundleListeners).isEmpty();
		assertThat(frameworkListeners).isEmpty();
		assertThat(serviceListeners).isEmpty();

	}

	class A {}
}
