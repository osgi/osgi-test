package org.osgi.test.junit5.testutils;

import org.assertj.core.api.SoftAssertions;
import org.osgi.test.assertj.bundle.BundleSoftAssertionsProvider;
import org.osgi.test.assertj.bundlecontext.BundleContextSoftAssertionsProvider;
import org.osgi.test.assertj.bundleevent.BundleEventSoftAssertionsProvider;
import org.osgi.test.assertj.frameworkevent.FrameworkEventSoftAssertionsProvider;
import org.osgi.test.assertj.serviceevent.ServiceEventSoftAssertionsProvider;

public class OSGiSoftAssertions extends SoftAssertions
	implements BundleSoftAssertionsProvider,
	BundleEventSoftAssertionsProvider, BundleContextSoftAssertionsProvider, ServiceEventSoftAssertionsProvider,
	FrameworkEventSoftAssertionsProvider {}
