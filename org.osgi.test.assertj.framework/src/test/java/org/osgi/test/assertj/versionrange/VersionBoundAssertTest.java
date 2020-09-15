package org.osgi.test.assertj.versionrange;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.osgi.test.assertj.version.AbstractVersionAssertTest;

abstract public class VersionBoundAssertTest extends AbstractVersionAssertTest<VersionBoundAssert> {

	boolean isOpen;

	protected VersionBoundAssertTest(boolean open) {
		super(actual -> new VersionBoundAssert(actual, open));
	}

	@DisplayName("VersionBoundAssert for a closed bound")
	public static class ClosedVersionBoundAssertTest extends VersionBoundAssertTest {
		public ClosedVersionBoundAssertTest() {
			super(false);
		}

		@Test
		public void opennessTests() {
			assertPassing("isClosed", x -> aut.isClosed(), null);
			assertFailing("isOpen", x -> aut.isOpen(), null)
				.hasMessageMatching("(?si).*to be open.*but it was closed.*");
		}
	}

	@DisplayName("VersionBoundAssert for an open bound")
	public static class OpenVersionBoundAssertTest extends VersionBoundAssertTest {
		public OpenVersionBoundAssertTest() {
			super(true);
		}

		@Test
		public void opennessTests() {
			assertPassing("isOpen", x -> aut.isOpen(), null);
			assertFailing("isClosed", x -> aut.isClosed(), null)
				.hasMessageMatching("(?si).*to be closed.*but it was open.*");
		}
	}
}
