package org.osgi.test.example.player.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.Test;
import org.osgi.test.example.api.Ball;

public class OpenBoxTest {

	@Test
	void myTest() {
		PlayerImpl p = new PlayerImpl();
		p.ball = mock(Ball.class);
		assertThat(p.getBall()).isNotNull();
		verifyNoInteractions(p.getBall());
		p.kickBall();
		verify(p.getBall()).kick();
	}

}
