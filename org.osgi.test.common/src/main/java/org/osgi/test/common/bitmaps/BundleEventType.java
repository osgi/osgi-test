package org.osgi.test.common.bitmaps;

import static org.osgi.framework.BundleEvent.INSTALLED;
import static org.osgi.framework.BundleEvent.LAZY_ACTIVATION;
import static org.osgi.framework.BundleEvent.RESOLVED;
import static org.osgi.framework.BundleEvent.STARTED;
import static org.osgi.framework.BundleEvent.STARTING;
import static org.osgi.framework.BundleEvent.STOPPED;
import static org.osgi.framework.BundleEvent.STOPPING;
import static org.osgi.framework.BundleEvent.UNINSTALLED;
import static org.osgi.framework.BundleEvent.UNRESOLVED;
import static org.osgi.framework.BundleEvent.UPDATED;

public class BundleEventType {

	public static final int[] TYPES = {
		INSTALLED, STARTED, STOPPED, UPDATED, UNINSTALLED, RESOLVED, UNRESOLVED, STARTING, STOPPING, LAZY_ACTIVATION
	};

	public static String typeToString(int type) {
		switch (type) {
			case UNINSTALLED :
				return "UNINSTALLED";
			case UNRESOLVED :
				return "UNRESOLVED";
			case UPDATED :
				return "UPDATED";
			case INSTALLED :
				return "INSTALLED";
			case RESOLVED :
				return "RESOLVED";
			case STARTING :
				return "STARTING";
			case STARTED :
				return "STARTED";
			case STOPPING :
				return "STOPPING";
			case STOPPED :
				return "STOPPED";
			case LAZY_ACTIVATION :
				return "LAZY_ACTIVATION";
			default :
				return null;
		}
	}

	public static final Bitmap BITMAP = new Bitmap(TYPES, BundleEventType::typeToString);
}
