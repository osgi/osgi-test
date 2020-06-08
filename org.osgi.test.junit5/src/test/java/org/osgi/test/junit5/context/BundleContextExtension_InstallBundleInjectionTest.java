package org.osgi.test.junit5.context;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.osgi.framework.BundleContext;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.annotation.InjectInstallBundle;
import org.osgi.test.common.install.InstallBundle;
import org.osgi.test.junit5.testutils.OSGiSoftAssertions;

@ExtendWith(BundleContextExtension.class)
public class BundleContextExtension_InstallBundleInjectionTest {

	@InjectBundleContext
	static BundleContext		staticBC;

	@InjectInstallBundle
	static InstallBundle		staticIB;

	@InjectBundleContext
	BundleContext				bundleContext;

	@InjectInstallBundle
	InstallBundle				installBundle;

	static OSGiSoftAssertions	staticSoftly;

	OSGiSoftAssertions			softly;

	@BeforeAll
	static void beforeAll(@InjectInstallBundle InstallBundle ib) {
		assertThat(staticIB).isNotNull();
		staticSoftly = new OSGiSoftAssertions();
		staticSoftly.assertThat(staticIB)
			.as("staticIB:beforeAll")
			.isNotNull()
			.isSameAs(ib);
		staticSoftly.assertThat(staticIB.getBundleContext())
			.isSameAs(staticBC);
		staticSoftly.assertAll();
	}

	@BeforeEach
	void beforeEach(@InjectInstallBundle InstallBundle ib) {
		assertThat(installBundle).isNotNull();
		softly = new OSGiSoftAssertions();
		softly.assertThat(installBundle)
			.as("installBundle:beforeEach")
			.isNotNull()
			.isSameAs(ib)
			.extracting(InstallBundle::getBundleContext)
			.isSameAs(bundleContext);
		softly.assertAll();
	}

	@Test
	void innerTest(@InjectInstallBundle InstallBundle ib) {
		assertThat(installBundle).isNotNull();
		softly = new OSGiSoftAssertions();
		softly.assertThat(installBundle)
			.as("installBundle:innerTest")
			.isNotNull()
			.isSameAs(ib)
			.extracting(InstallBundle::getBundleContext)
			.isSameAs(bundleContext);
		softly.assertAll();
	}

	@ParameterizedTest
	@ValueSource(ints = {
		1, 2, 3
	})
	// This test is meant to check that the extension is doing the
	// right thing before and after parameterized tests, hence
	// the parameter is not actually used.
	void parameterizedTest(int unused, @InjectInstallBundle InstallBundle ib) {
		assertThat(installBundle).isNotNull();
		softly = new OSGiSoftAssertions();
		softly.assertThat(installBundle)
			.as("installBundle:parameterizedTest")
			.isNotNull()
			.isSameAs(ib)
			.extracting(InstallBundle::getBundleContext)
			.isSameAs(bundleContext);
		softly.assertAll();
	}

	@Nested
	class NestedTest {

		@InjectBundleContext
		BundleContext	nestedBC;

		@InjectInstallBundle
		InstallBundle	nestedIB;

		@BeforeEach
		void beforeEach(@InjectInstallBundle InstallBundle ib) {
			assertThat(installBundle).isNotNull();
			softly = new OSGiSoftAssertions();
			softly.assertThat(nestedIB)
				.as("installBundle:nested.beforeEach")
				.isNotNull()
				.isSameAs(ib)
				.extracting(InstallBundle::getBundleContext)
				.isSameAs(nestedBC);
			softly.assertAll();
		}

		@Test
		void test(@InjectInstallBundle InstallBundle ib) {
			softly = new OSGiSoftAssertions();
			softly.assertThat(nestedIB)
				.as("installBundle:nested.test")
				.isNotNull()
				.isSameAs(ib)
				.extracting(InstallBundle::getBundleContext)
				.isSameAs(nestedBC);
			softly.assertAll();
		}

		@AfterEach
		void afterEach(@InjectInstallBundle InstallBundle ib) {
			softly = new OSGiSoftAssertions();
			softly.assertThat(nestedIB)
				.as("installBundle:nested.afterEach")
				.isNotNull()
				.isSameAs(ib)
				.extracting(InstallBundle::getBundleContext)
				.isSameAs(nestedBC);
			softly.assertAll();
		}
	}

	@AfterEach
	void afterEach(@InjectInstallBundle InstallBundle ib) {
		softly = new OSGiSoftAssertions();
		softly.assertThat(installBundle)
			.as("installBundle:afterEach")
			.isNotNull()
			.isSameAs(ib)
			.extracting(InstallBundle::getBundleContext)
			.isSameAs(bundleContext);
		softly.assertAll();
	}

	@AfterAll
	static void afterAll(@InjectInstallBundle InstallBundle ib) {
		staticSoftly = new OSGiSoftAssertions();
		staticSoftly.assertThat(staticIB)
			.as("staticIB:beforeAll")
			.isNotNull()
			.isSameAs(ib)
			.extracting(InstallBundle::getBundleContext)
			.isSameAs(staticBC);
		staticSoftly.assertAll();
	}
}
