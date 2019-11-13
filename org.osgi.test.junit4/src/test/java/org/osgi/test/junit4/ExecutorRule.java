package org.osgi.test.junit4;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class ExecutorRule implements TestRule {

	private ScheduledExecutorService	executor;

	public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay) {
		return executor.schedule(callable, delay, TimeUnit.MILLISECONDS);
	}

	@Override
	public Statement apply(Statement statement, Description description) {
		return new Statement() {
			@Override
			public void evaluate() throws Throwable {
				executor = Executors.newSingleThreadScheduledExecutor();
				try {
					statement.evaluate();
				} finally {
					executor.shutdownNow();
					executor.awaitTermination(100, TimeUnit.MILLISECONDS);
				}
			}
		};
	}

}
