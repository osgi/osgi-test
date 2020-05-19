package org.osgi.test.junit5.context;

import java.util.Map;
import java.util.stream.Stream;

import org.assertj.core.api.SoftAssertions;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.osgi.test.common.exceptions.Exceptions;
import org.osgi.test.junit5.context.MultiLevelCleanupTest.Scope;

abstract class AbstractResourceChecker<RESOURCE> {
	final Bundle			bundle				= FrameworkUtil.getBundle(MultiLevelCleanupTest.class);
	final Map<Scope, RESOURCE>	scopedResourcesMap;

	AbstractResourceChecker(Map<Scope, RESOURCE> scopedResourcesMap) {
		this.scopedResourcesMap = scopedResourcesMap;
	}

	void setupResource(Scope scope) {
		try {
			scopedResourcesMap.put(scope, doSetupResource(scope));
		} catch (Exception e) {
			throw Exceptions.duck(e);
		}
	}

	void assertSetup(SoftAssertions softly, Scope inScope, Scope... testScopes) {
		Stream.of(testScopes)
			.filter(
				scopedResourcesMap::containsKey)
			.forEach(fromScope -> softly
				.check(() -> doAssertSetup(inScope, fromScope, scopedResourcesMap.get(fromScope))));
	}

	void assertNotSetup(SoftAssertions softly, Scope inScope, Scope... fromScopes) {
		Stream.of(
			fromScopes)
			.filter(scopedResourcesMap::containsKey)
			.forEach(fromScope -> {
				softly.check(() -> doAssertNotSetup(inScope, fromScope, scopedResourcesMap.get(fromScope)));
			});
	}

	abstract RESOURCE doSetupResource(Scope inScope) throws Exception;

	abstract void doAssertSetup(Scope inScope, Scope fromScope, RESOURCE r) throws Exception;

	abstract void doAssertNotSetup(Scope inScope, Scope fromScope, RESOURCE r) throws Exception;
}