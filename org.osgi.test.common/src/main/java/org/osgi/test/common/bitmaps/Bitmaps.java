/*
 * Copyright (c) OSGi Alliance (2020). All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
import static org.osgi.framework.ServiceEvent.MODIFIED;
import static org.osgi.framework.ServiceEvent.MODIFIED_ENDMATCH;
import static org.osgi.framework.ServiceEvent.REGISTERED;
import static org.osgi.framework.ServiceEvent.UNREGISTERING;

public class Bitmaps {

	public static final int[] BUNDLEEVENT_TYPES = {
		INSTALLED, STARTED, STOPPED, UPDATED, UNINSTALLED, RESOLVED, UNRESOLVED, STARTING, STOPPING, LAZY_ACTIVATION
	};

	public static String bundleEventTypeToString(int type) {
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

	public static final Bitmap BUNDLEVENT_TYPE = new Bitmap(BUNDLEEVENT_TYPES, Bitmaps::bundleEventTypeToString);

	public static String serviceEventTypeToString(int type) {
		switch (type) {
			case REGISTERED :
				return "REGISTERED";
			case MODIFIED :
				return "MODIFIED";
			case UNREGISTERING :
				return "UNREGISTERING";
			case MODIFIED_ENDMATCH :
				return "MODIFIED_ENDMATCH";
			default :
				return null;
		}
	}

	public static final int[]	SERVICEEVENT_TYPES	= {
		REGISTERED, MODIFIED, UNREGISTERING, MODIFIED_ENDMATCH
	};

	public static final Bitmap	SERVICEEVENT_TYPE	= new Bitmap(SERVICEEVENT_TYPES, Bitmaps::serviceEventTypeToString);
}
