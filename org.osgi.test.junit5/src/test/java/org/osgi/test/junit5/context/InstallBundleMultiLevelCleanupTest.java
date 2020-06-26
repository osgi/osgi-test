package org.osgi.test.junit5.context;

import java.util.Map;

import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.annotation.InjectInstallBundle;
import org.osgi.test.common.install.InstallBundle;
import org.osgi.test.junit5.context.BundleContextExtension_CleanupTest.BundleChecker;

@ExtendWith(PreDestroyCallback.class)
@ExtendWith(BundleContextExtension.class)
class InstallBundleMultiLevelCleanupTest extends MultiLevelCleanupTest {
	static final Bundle		bundle	= FrameworkUtil.getBundle(InstallBundleMultiLevelCleanupTest.class);

	@InjectBundleContext
	static BundleContext	staticBC;

	@InjectBundleContext
	BundleContext			bundleContext;

	@InjectInstallBundle
	static InstallBundle	staticIB;

	@InjectInstallBundle
	InstallBundle			installBundle;

	static class InstallBundleChecker extends BundleChecker {

		final InstallBundle ib;

		public InstallBundleChecker(BundleContext bc, Map<CallbackPoint, Bundle> scopedResourcesMap, InstallBundle ib) {
			super(bc, scopedResourcesMap);
			this.ib = ib;
		}

		@Override
		public Bundle doSetupResource(CallbackPoint inScope) {
			return ib.installBundle(inScope.toString()
				.replace(".", "/") + ".jar");
		}
	}

	@SuppressWarnings("unchecked")
	static InstallBundleChecker getGlobalResourceChecker() {
		return new InstallBundleChecker(bundle.getBundleContext(), (Map<CallbackPoint, Bundle>) resourcesMap,
			new InstallBundle(bundle.getBundleContext()));
	}

	@SuppressWarnings("unchecked")
	static InstallBundleChecker getStaticResourceChecker() {
		return new InstallBundleChecker(staticBC, (Map<CallbackPoint, Bundle>) resourcesMap, staticIB);
	}

	@SuppressWarnings("unchecked")
	@Override
	InstallBundleChecker getResourceChecker() {
		return new InstallBundleChecker(bundleContext, (Map<CallbackPoint, Bundle>) resourcesMap, installBundle);
	}
}
