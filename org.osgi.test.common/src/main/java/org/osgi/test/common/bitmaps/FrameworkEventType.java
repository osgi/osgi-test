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

import static org.osgi.framework.FrameworkEvent.ERROR;
import static org.osgi.framework.FrameworkEvent.INFO;
import static org.osgi.framework.FrameworkEvent.PACKAGES_REFRESHED;
import static org.osgi.framework.FrameworkEvent.STARTED;
import static org.osgi.framework.FrameworkEvent.STARTLEVEL_CHANGED;
import static org.osgi.framework.FrameworkEvent.STOPPED;
import static org.osgi.framework.FrameworkEvent.STOPPED_BOOTCLASSPATH_MODIFIED;
import static org.osgi.framework.FrameworkEvent.STOPPED_UPDATE;
import static org.osgi.framework.FrameworkEvent.WAIT_TIMEDOUT;
import static org.osgi.framework.FrameworkEvent.WARNING;

public class FrameworkEventType {

	public static String typeToString(int type) {
		switch (type) {
			case STARTED :
				return "STARTED";
			case ERROR :
				return "ERROR";
			case WARNING :
				return "WARNING";
			case INFO :
				return "INFO";
			case PACKAGES_REFRESHED :
				return "PACKAGES_REFRESHED";
			case STARTLEVEL_CHANGED :
				return "STARTLEVEL_CHANGED";
			case STOPPED :
				return "STOPPED";
			case STOPPED_BOOTCLASSPATH_MODIFIED :
				return "STOPPED_BOOTCLASSPATH_MODIFIED";
			case STOPPED_UPDATE :
				return "STOPPED_UPDATE";
			case WAIT_TIMEDOUT :
				return "WAIT_TIMEDOUT";
			default :
				return null;
		}
	}

	public static final int[]	TYPES	= {
		STARTED, ERROR, PACKAGES_REFRESHED, STARTLEVEL_CHANGED, WARNING, INFO, STOPPED, STOPPED_UPDATE,
		STOPPED_BOOTCLASSPATH_MODIFIED, WAIT_TIMEDOUT
	};

	public static final Bitmap	BITMAP	= new Bitmap(TYPES,
		FrameworkEventType::typeToString);
}
