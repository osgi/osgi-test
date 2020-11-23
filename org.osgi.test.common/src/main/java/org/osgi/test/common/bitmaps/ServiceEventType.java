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

import static org.osgi.framework.ServiceEvent.MODIFIED;
import static org.osgi.framework.ServiceEvent.MODIFIED_ENDMATCH;
import static org.osgi.framework.ServiceEvent.REGISTERED;
import static org.osgi.framework.ServiceEvent.UNREGISTERING;

public class ServiceEventType {

	public static String toString(int type) {
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

	public static final int[]	TYPES	= {
		REGISTERED, MODIFIED, UNREGISTERING, MODIFIED_ENDMATCH
	};
	public static final Bitmap	BITMAP	= new Bitmap(TYPES, ServiceEventType::toString);
}
