package org.osgi.test.assertj.testutil;

import org.assertj.core.api.Assert;
import org.assertj.core.api.AssertFactory;
import org.assertj.core.api.SoftAssertions;

public abstract class AbstractAssertTest<SELF extends Assert<SELF, ACTUAL>, ACTUAL>
	implements AssertTest<SELF, ACTUAL> {

	protected final AssertFactory<ACTUAL, SELF>	assertThat;
	protected ACTUAL							actual;
	protected SELF								aut;
	protected SoftAssertions					softly;

	protected AbstractAssertTest(AssertFactory<ACTUAL, SELF> assertThat) {
		this.assertThat = assertThat;
	}

	@Override
	public ACTUAL actual() {
		return actual;
	}

	@Override
	public SELF aut() {
		return aut;
	}

	@Override
	public SoftAssertions softly() {
		return softly;
	}

	protected void setActual(ACTUAL actual) {
		this.actual = actual;
		aut = assertThat.createAssert(actual);
	}
}
