package org.osgi.test.junit5.context;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstancePreDestroyCallback;

public class PreDestroyCallback implements TestInstancePreDestroyCallback {
	@Override
	public void preDestroyTestInstance(ExtensionContext context) throws Exception {
		MultiLevelCleanupTest instance = (MultiLevelCleanupTest) context.getRequiredTestInstances()
			.getAllInstances()
			.get(0);
		instance.afterAfterEach();
	}
}