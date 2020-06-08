package org.osgi.test.junit5.context;

import static org.assertj.core.api.Assertions.fail;

import java.util.EnumSet;
import java.util.Map;

import org.assertj.core.api.SoftAssertions;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.osgi.test.common.exceptions.Exceptions;
import org.osgi.test.junit5.context.MultiLevelCleanupTest.CallbackPoint;

abstract class AbstractResourceChecker<RESOURCE> {
	final Bundle						bundle	= FrameworkUtil.getBundle(MultiLevelCleanupTest.class);
	final Map<CallbackPoint, RESOURCE>	resourcesMap;

	AbstractResourceChecker(Map<CallbackPoint, RESOURCE> resourcesMap) {
		this.resourcesMap = resourcesMap;
	}

	void setupResource(CallbackPoint point) {
		try {
			resourcesMap.put(point, doSetupResource(point));
		} catch (Exception e) {
			throw Exceptions.duck(e);
		}
	}

	void assertSetup(SoftAssertions softly, CallbackPoint currentPoint, EnumSet<CallbackPoint> pointsThatAreSetup) {
		pointsThatAreSetup.stream()
			.forEach(pointThatIsSetup -> {
				RESOURCE r = resourcesMap.get(pointThatIsSetup);
				if (r == null) {
					fail("No entry in resource map for %s at callback point %s", pointThatIsSetup, currentPoint);
					// softly.fail("No entry in resource map for %s at
					// callback point %s", pointThatIsSetup, currentPoint);
				} else {
					softly.check(() -> doAssertSetup(currentPoint, pointThatIsSetup, r));
				}
			});
		EnumSet.complementOf(pointsThatAreSetup)
			.stream()
			.filter(resourcesMap::containsKey)
			.forEach(pointThatIsNotSetup -> {
				softly.check(
					() -> doAssertNotSetup(currentPoint, pointThatIsNotSetup, resourcesMap.get(pointThatIsNotSetup)));
			});
	}

	void tearDownResource(CallbackPoint callbackPoint) {
		RESOURCE r = resourcesMap.remove(callbackPoint);
		try {
			doTearDownResource(callbackPoint, r);
		} catch (Exception e) {
			throw Exceptions.duck(e);
		}
	}

	abstract RESOURCE doSetupResource(CallbackPoint currentPoint) throws Exception;

	abstract void doAssertSetup(CallbackPoint currentPoint, CallbackPoint pointThatIsSetup, RESOURCE r)
		throws Exception;

	abstract void doAssertNotSetup(CallbackPoint currentPoint, CallbackPoint pointThatIsNotSetup, RESOURCE r)
		throws Exception;

	abstract void doTearDownResource(CallbackPoint currentPoint, RESOURCE r) throws Exception;
}
