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

package org.osgi.test.common.context;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.osgi.framework.Bundle.ACTIVE;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceObjects;
import org.osgi.framework.ServiceReference;

public class CloseableBundleContextTest extends SoftAssertions {
	Bundle			upstreamBundle;
	BundleContext			upstream;
	BundleContext			sut;

	@BeforeEach
	void beforeEach() {
		upstreamBundle = mock(Bundle.class);
		upstream = mock(BundleContext.class);
		when(upstreamBundle.getBundleContext()).thenReturn(upstream);
		when(upstreamBundle.getState()).thenReturn(ACTIVE);
		sut = CloseableBundleContext.proxy(upstreamBundle);
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"hi", "there", "somethingElse"
	})
	void toString_showsDelegateInfo(String value) {
		when(upstream.toString()).thenReturn(value);

		assertThat(sut.toString()).contains(value)
			.startsWith(CloseableBundleContext.class.getSimpleName());
	}

	@Test
	void hashCode_returnsUpstreamHashcode() {
		assertThat(sut.hashCode()).isEqualTo(upstream.hashCode());
	}

	@Test
	void equals_equalsUpstream() {
		assertThat(sut).isEqualTo(upstream);
	}

	@Test
	void implementsAutoCloseable() throws Exception {
		Assertions.assertThat(sut)
			.isInstanceOf(AutoCloseable.class);

		// ((AutoCloseable) sut).close();
	}

	@Nested
	class CloseableServiceObjectsTest {
		ServiceObjects<Object>	upstreamSO;
		ServiceObjects<Object>	sutSO;
		AutoCloseable			sutSOCloseable;

		final static String		s1	= "service1";
		final static String		s2	= "service2";

		@SuppressWarnings("unchecked")
		@BeforeEach
		void beforeEach() {
			upstreamSO = mock(ServiceObjects.class);
			when(upstreamSO.getService()).thenReturn(s1, s2, s1, s2);
			when(upstream.getServiceObjects(any(ServiceReference.class))).thenReturn(upstreamSO);
			sutSO = sut.getServiceObjects(mock(ServiceReference.class));
			Assertions.assertThat(sutSO)
				.isInstanceOf(AutoCloseable.class);
			sutSOCloseable = (AutoCloseable) sutSO;
		}

		@SuppressWarnings("unchecked")
		@Test
		void getService_returnsServices_andClosesThem() throws Exception {
			assertThat(sutSO.getService()).as("1")
				.isSameAs(s1);
			assertThat(sutSO.getService()).as("2")
				.isSameAs(s2);
			assertThat(sutSO.getService()).as("3")
				.isSameAs(s1);
			assertThat(sutSO.getService()).as("4")
				.isSameAs(s2);
			reset(upstreamSO);
			sutSOCloseable.close();
			check(() -> verify(upstreamSO, times(2)).ungetService(s1));
			check(() -> verify(upstreamSO, times(2)).ungetService(s2));
			check(() -> verifyNoMoreInteractions(upstreamSO));
		}

		@SuppressWarnings("unchecked")
		@Test
		void ungetService_decrementsUsage() throws Exception {
			sutSO.getService();
			sutSO.getService();
			sutSO.getService();
			sutSO.getService();
			sutSO.ungetService(s1);
			check(() -> verify(upstreamSO).ungetService(s1));
			reset(upstreamSO);
			sutSOCloseable.close();
			check(() -> verify(upstreamSO).ungetService(s1));
			check(() -> verify(upstreamSO, times(2)).ungetService(s2));
			check(() -> verifyNoMoreInteractions(upstreamSO));
		}

		@Test
		void ungetService_whenNoneGotten_asserts() {
			assertThatCode(() -> sutSO.ungetService(s1)).isInstanceOf(AssertionError.class)
				.hasMessageContaining(s1);
		}

		@Test
		void ungetService_whenOneLeft_emptiesReferences() {
			sutSO.getService();
			sutSO.getService();
			sutSO.getService();
			assertThatCode(() -> sutSO.ungetService(s1)).doesNotThrowAnyException();
			assertThatCode(() -> sutSO.ungetService(s1)).doesNotThrowAnyException();
			assertThatCode(() -> sutSO.ungetService(s1)).isInstanceOf(AssertionError.class)
				.hasMessageContaining(s1);
		}

		@ParameterizedTest
		@ValueSource(strings = {
			"hi", "there", "somethingElse"
		})
		void toString_showsDelegateInfo(String value) {
			when(upstreamSO.toString()).thenReturn(value);

			assertThat(sutSO.toString()).contains(value)
				.startsWith(CloseableServiceObjects.class.getSimpleName());
		}

		@Test
		void hashCode_returnsUpstreamHashcode() {
			assertThat(sutSO.hashCode()).isEqualTo(upstreamSO.hashCode());
		}

		@Test
		void equals_equalsUpstream() {
			assertThat(sutSO).isEqualTo(upstreamSO);
		}

		@Test
		void unmodifiedMethods_passedThrough() {
			@SuppressWarnings("unchecked")
			ServiceReference<Object> ref = mock(ServiceReference.class);
			when(upstreamSO.getServiceReference()).thenReturn(ref);
			assertThat(sutSO.getServiceReference()).isSameAs(ref);
			check(() -> verify(upstreamSO).getServiceReference());
		}
	}

	@AfterEach
	void afterEach() {
		assertAll();
	}
}
