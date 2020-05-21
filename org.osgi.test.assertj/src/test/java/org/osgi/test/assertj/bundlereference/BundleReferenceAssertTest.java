package org.osgi.test.assertj.bundlereference;

import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.framework.BundleReference;

@ExtendWith(SoftAssertionsExtension.class)
class BundleReferenceAssertTest extends AbstractBundleReferenceAssertTest<BundleReferenceAssert, BundleReference> {

	BundleReferenceAssertTest() {
		super(BundleReferenceAssert::assertThat, BundleReference.class);
	}
}
