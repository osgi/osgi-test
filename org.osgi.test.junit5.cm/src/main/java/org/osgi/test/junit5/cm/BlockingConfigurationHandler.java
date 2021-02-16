package org.osgi.test.junit5.cm;

import java.io.IOException;
import java.util.Dictionary;

import org.osgi.service.cm.Configuration;

public interface BlockingConfigurationHandler {

	boolean update(Configuration configuration, Dictionary<String, Object> dictionary, long timeout, boolean ignoreDiff)
		throws InterruptedException, IOException;

	boolean delete(Configuration configuration, long timeout) throws InterruptedException, IOException;


}
