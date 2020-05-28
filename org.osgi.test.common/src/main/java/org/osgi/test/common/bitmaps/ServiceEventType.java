package org.osgi.test.common.bitmaps;

import static org.osgi.framework.ServiceEvent.MODIFIED;
import static org.osgi.framework.ServiceEvent.MODIFIED_ENDMATCH;
import static org.osgi.framework.ServiceEvent.REGISTERED;
import static org.osgi.framework.ServiceEvent.UNREGISTERING;

public class ServiceEventType {

	public static String typeToString(int type) {
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
	public static final Bitmap	BITMAP	= new Bitmap(TYPES, ServiceEventType::typeToString);
}
