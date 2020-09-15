package org.osgi.test.assertj.version;

public class VersionAssertTest extends AbstractVersionAssertTest<VersionAssert> {
	public VersionAssertTest() {
		super(VersionAssert::assertThat);
	}
}
