package org.osgi.test.junit5.context;

import static org.osgi.test.junit5.context.MultiLevelCleanupTest.Scope.AFTER_AFTER_EACH;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstancePreDestroyCallback;

public class PreDestroyCallback implements TestInstancePreDestroyCallback {
	@Override
	public void preDestroyTestInstance(ExtensionContext context) throws Exception {
		MultiLevelCleanupTest instance = (MultiLevelCleanupTest) context.getRequiredTestInstances()
			.getAllInstances()
			.get(0);
		MultiLevelCleanupTest.staticCurrentScope = AFTER_AFTER_EACH;
		MultiLevelCleanupTest.afterAfterEach(instance.getClass());
	}
}