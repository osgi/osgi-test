/*******************************************************************************
 * Copyright (c) Contributors to the Eclipse Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 *******************************************************************************/

package org.osgi.test.junit5.cm;

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.IOException;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.extension.ExtensionContext.Store.CloseableResource;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationEvent;
import org.osgi.service.cm.ConfigurationListener;
import org.osgi.test.common.context.ContextHelper;

public class BlockingConfigurationHandlerImpl
	implements ConfigurationListener, BlockingConfigurationHandler, CloseableResource {

	private final Map<String, CountDownLatch>	updateMap	= new HashMap<String, CountDownLatch>();
	private final Map<String, CountDownLatch>	deleteMap	= new HashMap<String, CountDownLatch>();

	private final ServiceRegistration<?>		reg;
	private final AtomicBoolean					closed		= new AtomicBoolean(false);

	public BlockingConfigurationHandlerImpl() {
		this(Optional.empty());
	}

	public BlockingConfigurationHandlerImpl(Optional<Class<?>> testClass) {
		BundleContext context = ContextHelper
			.getBundleContext(testClass.orElse(BlockingConfigurationHandlerImpl.class));
		reg = context.registerService(ConfigurationListener.class, this, null);
	}

	@Override
	public void close() throws Throwable {
		reg.unregister();
		closed.set(true);
	}

	@Override
	public boolean update(Configuration configuration, Dictionary<String, Object> dictionary, long timeout)
		throws InterruptedException, IOException {

		assertFalse(closed.get(), "BlockingConfigurationHandler was closed");

		CountDownLatch latch = createCountdownLatchUpdate(configuration.getPid());
		boolean updatedBecauseDifferent = configuration.updateIfDifferent(dictionary);
		if (!updatedBecauseDifferent) {
			updateMap.remove(configuration.getPid());
			return true;
		}
		boolean isOk = latch.await(timeout, TimeUnit.MILLISECONDS);
		return isOk;
	}

	@Override
	public boolean delete(Configuration configuration, long timeout) throws InterruptedException, IOException {

		assertFalse(closed.get(), "BlockingConfigurationHandler was closed");

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

			CountDownLatch countDownLatch = updateMap.remove(pid);
			if (countDownLatch != null) {
				countDownLatch.countDown();
			}
		} else if (event.getType() == ConfigurationEvent.CM_DELETED) {

			CountDownLatch countDownLatch = deleteMap.remove(pid);
			if (countDownLatch != null) {
				countDownLatch.countDown();
			}
		}
	}

}
