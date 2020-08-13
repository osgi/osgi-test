package org.osgi.test.junit5.cm;

import java.io.IOException;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationEvent;
import org.osgi.service.cm.ConfigurationListener;

public class BlockingConfigurationHandlerImpl implements ConfigurationListener, BlockingConfigurationHandler {


	private Map<String, CountDownLatch>	updateMap	= new HashMap<String, CountDownLatch>();
	private Map<String, CountDownLatch>	deleteMap	= new HashMap<String, CountDownLatch>();

	@Override
	public boolean update(Configuration configuration, Dictionary<String, Object> dictionary, long timeout)
		throws InterruptedException, IOException {
		CountDownLatch latch = createCountdownLatchUpdate(configuration.getPid());
		configuration.update(dictionary);
		boolean isOk = latch.await(timeout, TimeUnit.MILLISECONDS);
		return isOk;
	}

	@Override
	public boolean delete(Configuration configuration, long timeout) throws InterruptedException, IOException {
		CountDownLatch latch = createCountdownLatchDelete(configuration.getPid());
		configuration.delete();
		boolean isOk = latch.await(timeout, TimeUnit.MILLISECONDS);
		return isOk;
	}

	private CountDownLatch createCountdownLatchUpdate(String pid) {
		CountDownLatch countDownLatch = new CountDownLatch(1);
		updateMap.put(pid, countDownLatch);
		return countDownLatch;
	}

	private CountDownLatch createCountdownLatchDelete(String pid) {
		CountDownLatch countDownLatch = new CountDownLatch(1);
		deleteMap.put(pid, countDownLatch);
		return countDownLatch;
	}

	@Override
	public void configurationEvent(ConfigurationEvent event) {
		String pid = event.getPid();
		if (event.getType() == ConfigurationEvent.CM_UPDATED) {

			CountDownLatch countDownLatch = updateMap.get(pid);
			if (countDownLatch != null) {
				updateMap.remove(pid);
				countDownLatch.countDown();
			}
		} else if (event.getType() == ConfigurationEvent.CM_DELETED) {

			CountDownLatch countDownLatch = deleteMap.get(pid);
			if (countDownLatch != null) {
				deleteMap.remove(pid);
				countDownLatch.countDown();
			}
		}
	}

}
