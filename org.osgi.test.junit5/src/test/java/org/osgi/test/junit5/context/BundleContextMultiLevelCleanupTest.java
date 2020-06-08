package org.osgi.test.junit5.context;

import java.util.Map;
import java.util.function.BiFunction;

import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.test.common.annotation.InjectBundleContext;

@ExtendWith(PreDestroyCallback.class)
@ExtendWith(BundleContextExtension.class)
class BundleContextMultiLevelCleanupTest<RESOURCE> extends MultiLevelCleanupTest {
	static final Bundle		bundle	= FrameworkUtil.getBundle(BundleContextMultiLevelCleanupTest.class);

	@InjectBundleContext
	static BundleContext	staticBC;

	@InjectBundleContext
	BundleContext			bundleContext;

	@SuppressWarnings("unchecked")
	static <STATIC> void setFactory(
		BiFunction<BundleContext, Map<CallbackPoint, STATIC>, AbstractResourceChecker<STATIC>> factory) {
		BundleContextMultiLevelCleanupTest.factory = (bc, map) -> factory.apply(bc, (Map<CallbackPoint, STATIC>) map);
	}

	static BiFunction<BundleContext, Map<CallbackPoint, ?>, AbstractResourceChecker<?>> factory;

	static AbstractResourceChecker<?> getGlobalResourceChecker() {
		return factory.apply(bundle.getBundleContext(), resourcesMap);
	}

	static AbstractResourceChecker<?> getStaticResourceChecker() {
		return factory.apply(staticBC, resourcesMap);
	}

	@SuppressWarnings("unchecked")
	@Override
	AbstractResourceChecker<?> getResourceChecker() {
		return factory.apply(bundleContext, resourcesMap);
	}
}
