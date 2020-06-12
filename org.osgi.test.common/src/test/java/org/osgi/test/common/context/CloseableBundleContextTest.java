package org.osgi.test.common.context;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceObjects;
import org.osgi.framework.ServiceReference;

public class CloseableBundleContextTest extends SoftAssertions {
	BundleContext			upstream;
	BundleContext			sut;

	@BeforeEach
	void beforeEach() {
		upstream = mock(BundleContext.class);
		sut = CloseableBundleContext.proxy(CloseableBundleContextTest.class, upstream);
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"hi", "there", "somethingElse"
	})
	void toString_showsDelegateInfo(String value) {
		when(upstream.toString()).thenReturn(value);

		assertThat(sut.toString()).contains(String.valueOf(System.identityHashCode(sut)))
			.contains(value)
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

			assertThat(sutSO.toString()).contains(String.valueOf(System.identityHashCode(sutSO)))
				.contains(value)
				.startsWith("CloseableServiceObjects");
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
