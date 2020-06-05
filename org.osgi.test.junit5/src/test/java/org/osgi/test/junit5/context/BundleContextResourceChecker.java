package org.osgi.test.junit5.context;

import java.util.Map;

import org.osgi.framework.BundleContext;
import org.osgi.test.junit5.context.MultiLevelCleanupTest.CallbackPoint;

abstract class BundleContextResourceChecker<RESOURCE> extends AbstractResourceChecker<RESOURCE> {

	final BundleContext bc;

	BundleContextResourceChecker(BundleContext bc, Map<CallbackPoint, RESOURCE> scopedResourcesMap) {
		super(scopedResourcesMap);
		this.bc = bc;
	}

}
