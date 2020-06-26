package org.osgi.test.junit5.promise;

import org.junit.jupiter.api.Test;
import org.osgi.test.assertj.promise.PromiseSoftAssertions;
import org.osgi.test.assertj.promise.PromiseSoftAssertionsProvider;
import org.osgi.util.promise.Deferred;
import org.osgi.util.promise.Promise;

public class PromiseAssertTest {

	@Test
	void testPromiseSoftAssertion() {
		Deferred<String> d = new Deferred<>();
		d.resolve("resolved");
		Promise<String> p = d.getPromise();
		PromiseSoftAssertionsProvider softly = new PromiseSoftAssertions();
		softly.assertThat(p)
			.hasValue("resolved");
		softly.assertAll();
	}

}
