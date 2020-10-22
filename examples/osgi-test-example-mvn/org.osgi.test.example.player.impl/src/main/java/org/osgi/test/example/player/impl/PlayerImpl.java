package org.osgi.test.example.player.impl;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.test.example.api.Ball;
import org.osgi.test.example.api.Player;

@Component
public class PlayerImpl implements Player {

	@Reference
	Ball ball;

	@Override
	public void kickBall() {
		ball.kick();
	}

	@Override
	public Ball getBall() {
		return ball;
	}
}
