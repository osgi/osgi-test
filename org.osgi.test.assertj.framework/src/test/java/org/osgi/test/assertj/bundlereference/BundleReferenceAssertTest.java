package org.osgi.test.assertj.bundlereference;

import org.osgi.framework.BundleReference;

class BundleReferenceAssertTest extends AbstractBundleReferenceAssertTest<BundleReferenceAssert, BundleReference> {

	BundleReferenceAssertTest() {
		super(BundleReferenceAssert::assertThat, BundleReference.class);
	}
}
