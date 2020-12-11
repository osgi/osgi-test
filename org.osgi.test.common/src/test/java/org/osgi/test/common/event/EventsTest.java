package org.osgi.test.common.event;

import java.util.ArrayList;
import java.util.List;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;

public class EventsTest {

	@Test
	void testListPredicate() throws Exception {

		String first = "_first";
		String second = "_second";
		String third = "_third";

		List<Object> list = new ArrayList<>();
		list.add(first);
		list.add(second);
		list.add(third);

		SoftAssertions softly = new SoftAssertions();

		softly.assertThat(Events.all(e -> e.toString()
			.startsWith("_"))
			.test(list))
			.isTrue();

		softly.assertThat(Events.any(e -> e.equals(second))
			.test(list))
			.isTrue();

		softly.assertThat(Events.element(0, e -> e.equals(first))
			.test(list))
			.isTrue();

		softly.assertThat(Events.element(1, e -> e.equals(second))
			.test(list))
			.isTrue();

		softly.assertThat(Events.element(2, e -> e.equals(third))
			.test(list))
			.isTrue();

		softly.assertThat(Events.ordered(e -> e.equals(first))
			.test(list))
			.isTrue();

		softly.assertThat(Events.ordered(e -> e.equals(first), e -> e.equals(second))
			.test(list))
			.isTrue();

		softly.assertThat(Events.ordered(e -> e.equals(first), e -> e.equals(third))
			.test(list))
			.isTrue();
		softly.assertThat(Events.ordered(e -> e.equals(second), e -> e.equals(third))
			.test(list))
			.isTrue();

		softly.assertThat(Events.ordered(e -> e.equals(third))
			.test(list))
			.isTrue();

		softly.assertThat(Events.ordered(e -> e.equals(first), e -> e.equals(second), e -> e.equals(third))
			.test(list))
			.isTrue();

		softly.assertThat(Events.ordered(e -> e.equals(first), e -> e.equals(second), e -> e.equals("?"))
			.test(list))
			.isFalse();

		softly.assertThat(Events.ordered(e -> e.equals(first), e -> e.equals(second), e -> e.equals(second))
			.test(list))
			.isFalse();

		softly.assertThat(Events.first(e -> e.equals(first))
			.test(list))
			.isTrue();

		softly.assertThat(Events.last(e -> e.equals(third))
			.test(list))
			.isTrue();

		softly.assertThat(Events.none(e -> e.equals("?"))
			.test(list))
			.isTrue();

		softly.assertThat(Events.hasSize(3, e -> e.toString()
			.startsWith("_"))
			.test(list))
			.isTrue();

		softly.assertThat(Events.hasLessThen(4, e -> e.toString()
			.startsWith("_"))
			.test(list))
			.isTrue();

		softly.assertThat(Events.hasMoreThen(2, e -> e.toString()
			.startsWith("_"))
			.test(list))
			.isTrue();

		softly.assertAll();
	}
}
