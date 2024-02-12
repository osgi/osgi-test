package org.osgi.test.junit5.cm;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Store.CloseableResource;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

public class ConfigCloseableResource implements CloseableResource {
	private ExtensionContext				extensionContext;
	private BlockingConfigurationHandler	timeoutListener;
	private List<ConfigurationHolder>		holders	= new ArrayList<ConfigurationHolder>();

	public ConfigCloseableResource(ExtensionContext extensionContext, BlockingConfigurationHandler timeoutListener) {
		this.extensionContext = extensionContext;
		this.timeoutListener = timeoutListener;
	}

	@Override
	public void close() throws Throwable {
		ConfigurationAdmin ca = ConfigurationExtension.configurationAdmin(extensionContext);
		holders.stream()
			.forEach(holder -> {

				if (holder == null) {
					return;
				}

				if (holder.getConfiguration() == null) {
					return;
				}

				ConfigurationCopy configuration = holder.getConfiguration();
				Optional<ConfigurationCopy> configurationCopyBefore = holder.getBeforeConfiguration();

				try {
					if (configurationCopyBefore.isPresent()) {

						ConfigurationCopy copy = configurationCopyBefore.get();
						Configuration conf = null;
						if (copy.getFactoryPid() != null) {
							String name = copy.getPid()
								.substring(copy.getFactoryPid()
									.length() + 1);
							conf = ca.getFactoryConfiguration(copy.getFactoryPid(), name, copy.getBundleLocation());
						} else {
							conf = ca.getConfiguration(copy.getPid(), copy.getBundleLocation());
						}
						timeoutListener.update(conf, copy.getProperties(), 3000);
					} else {

						String pid = configuration.getPid();

						Configuration configurationToDelete = ConfigUtil.getConfigsByServicePid(ca, pid, 1000);
						if (configurationToDelete != null) {
							timeoutListener.delete(configurationToDelete, 3000);
						}
					}
				} catch (IOException e) {
					throw new UncheckedIOException(e);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			});
	}

	public void addAll(List<ConfigurationHolder> holders) {
		holders.forEach(this::add);
	}

	public void add(ConfigurationHolder holder) {

		// We may know the state of the Configuration.
		ConfigurationCopy compateConfig = holder.getConfiguration();
		Optional<ConfigurationHolder> preStoresConfig = holders.stream()
			.filter(c -> {
				return c.getConfiguration()
					.getPid()
					.endsWith(compateConfig.getPid());
			})
			.findAny();

		// if we have a state, we can ignore later ons
		if (!preStoresConfig.isPresent()) {
			holders.add(holder);
		}

	}

}
