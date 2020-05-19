package org.osgi.test.junit5.context;

import java.util.Map;

import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.annotation.InjectInstallBundle;
import org.osgi.test.common.install.InstallBundle;
import org.osgi.test.junit5.context.BundleContextExtensionTest.BundleChecker;

@ExtendWith(PreDestroyCallback.class)
@ExtendWith(BundleContextExtension.class)
class InstallBundleMultiLevelCleanupTest<RESOURCE> extends MultiLevelCleanupTest {
	static final Bundle											bundle	= FrameworkUtil
		.getBundle(InstallBundleMultiLevelCleanupTest.class);

	@InjectBundleContext
	static BundleContext										staticBC;

	@InjectBundleContext
	BundleContext												bundleContext;

	@InjectInstallBundle
	static InstallBundle	staticIB;

	@InjectInstallBundle
	InstallBundle			installBundle;

	static class InstallBundleChecker extends BundleChecker {

		final InstallBundle ib;

		public InstallBundleChecker(BundleContext bc, Map<Scope, Bundle> scopedResourcesMap, InstallBundle ib) {
			super(bc, scopedResourcesMap);
			this.ib = ib;
		}

		@Override
		public Bundle doSetupResource(Scope inScope) {
			return ib.installBundle(inScope.toString()
				.replace(".", "/") + ".jar");
		}
	}

	@SuppressWarnings("unchecked")
	static AbstractResourceChecker<?> getGlobalResourceChecker() {
		return new InstallBundleChecker(bundle.getBundleContext(), (Map<Scope, Bundle>) scopedResourcesMap,
			new InstallBundle(bundle.getBundleContext()));
	}

	@SuppressWarnings("unchecked")
	static AbstractResourceChecker<?> getStaticResourceChecker() {
		return new InstallBundleChecker(staticBC, (Map<Scope, Bundle>) scopedResourcesMap, staticIB);
	}

	@SuppressWarnings("unchecked")
	@Override
	AbstractResourceChecker<?> getResourceChecker() {
		return new InstallBundleChecker(bundleContext, (Map<Scope, Bundle>) scopedResourcesMap, installBundle);
	}
}
