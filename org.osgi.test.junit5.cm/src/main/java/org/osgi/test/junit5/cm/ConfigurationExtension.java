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

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ConfigurationListener;
import org.osgi.test.common.annotation.PropertiesConverter;
import org.osgi.test.common.annotation.Property;
import org.osgi.test.common.annotation.config.InjectConfiguration;
import org.osgi.test.common.annotation.config.WithConfiguration;
import org.osgi.test.common.annotation.config.WithConfigurations;
import org.osgi.test.common.annotation.config.WithFactoryConfiguration;
import org.osgi.test.common.annotation.config.WithFactoryConfigurations;
import org.osgi.test.common.dictionary.Dictionaries;
import org.osgi.test.common.inject.TargetType;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.inject.InjectingExtension;
import org.osgi.test.junit5.service.ServiceExtension;

public class ConfigurationExtension extends InjectingExtension<InjectConfiguration>
	implements BeforeEachCallback, ParameterResolver, BeforeAllCallback, AfterAllCallback, AfterEachCallback {

	public ConfigurationExtension() {
		super(InjectConfiguration.class);
	}

	private static final String					STORE_CONFIGURATION_KEY	= "store.configurationAdmin";

	private static BlockingConfigurationHandler	blockingConfigHandler;

	public static ConfigurationAdmin configurationAdmin(ExtensionContext extensionContext) {
		return ServiceExtension
			.getServiceConfiguration(ConfigurationAdmin.class, "", new String[0], 0, 0, extensionContext)
			.getService();
	}

	@Override
	public void beforeAll(ExtensionContext extensionContext) throws Exception {
		BlockingConfigurationHandlerImpl blockingConfigHandlerImpl = new BlockingConfigurationHandlerImpl();
		BundleContextExtension.getBundleContext(extensionContext)
			.registerService(ConfigurationListener.class, blockingConfigHandlerImpl, null);
		blockingConfigHandler = blockingConfigHandlerImpl;
		List<ConfigurationHolder> list = handleAnnotationsOnActiveElement(extensionContext);
		storeConfigCopy(extensionContext, list);
	}

	@Override
	public void beforeEach(ExtensionContext extensionContext) throws Exception {
		List<ConfigurationHolder> list = handleAnnotationsOnActiveElement(extensionContext);
		if (!isLifecyclePerClass(extensionContext)) {
			storeConfigCopy(extensionContext, list);
		}
		super.beforeEach(extensionContext);
	}

	ConfigurationHolder handleWithConfiguration(WithConfiguration configAnnotation,
		ConfigurationAdmin configurationAdmin) {
		try {
			Configuration configBefore = ConfigUtil.getConfigsByServicePid(configurationAdmin, configAnnotation.pid(),
				0l);

			Optional<ConfigurationCopy> copyOfBefore = createConfigurationCopy(configBefore);

			Configuration configuration;
			if (Property.NOT_SET.equals(configAnnotation.location())) {
				configuration = configurationAdmin.getConfiguration(configAnnotation.pid());
			} else {
				configuration = configurationAdmin.getConfiguration(configAnnotation.pid(),
					configAnnotation.location());
			}

			updateConfigurationRespectNew(configuration, PropertiesConverter.of(configAnnotation.properties()),
				configBefore == null);

			return new ConfigurationHolder(ConfigurationCopy.of(configuration), copyOfBefore);
		} catch (Exception e) {
			throw new ParameterResolutionException(
				String.format("Unable to obtain Configuration for %s.", configAnnotation.pid()), e);
		}
	}

	ConfigurationHolder handleWithFactoryConfiguration(WithFactoryConfiguration configAnnotation,
		ConfigurationAdmin configurationAdmin) {

		try {
			Configuration configBefore = ConfigUtil.getConfigsByServicePid(configurationAdmin,
				configAnnotation.factoryPid() + "~" + configAnnotation.name());

			Optional<ConfigurationCopy> copyOfBefore = createConfigurationCopy(configBefore);

			Configuration configuration;
			if (Property.NOT_SET.equals(configAnnotation.location())) {
				configuration = configurationAdmin.getFactoryConfiguration(configAnnotation.factoryPid(),
					configAnnotation.name());
			} else {
				configuration = configurationAdmin.getFactoryConfiguration(configAnnotation.factoryPid(),
					configAnnotation.name(), configAnnotation.location());
			}

			updateConfigurationRespectNew(configuration, PropertiesConverter.of(configAnnotation.properties()),
				configBefore == null);

			return new ConfigurationHolder(ConfigurationCopy.of(configuration), copyOfBefore);
		} catch (Exception e) {
			throw new ParameterResolutionException(
				String.format("Unable to obtain Configuration for %s.", configAnnotation.factoryPid()), e);
		}
	}

	private Optional<ConfigurationCopy> createConfigurationCopy(Configuration configBefore) {
		Optional<ConfigurationCopy> copyOfBefore;
		if (configBefore == null) {
			copyOfBefore = Optional.empty();
		} else {

			copyOfBefore = Optional.of(ConfigurationCopy.of(configBefore));
		}
		return copyOfBefore;
	}

	@Override
	protected boolean supportsType(TargetType targetType, ExtensionContext extensionContext)
		throws ParameterResolutionException {
		if (targetType.matches(Configuration.class)) {
			return true;
		}
		if (targetType.matches(Optional.class, Configuration.class)) {
			return true;
		}
		if (targetType.matches(Map.class, String.class, Object.class)) {
			return true;
		}
		if (targetType.matches(Dictionary.class, String.class, Object.class)) {
			return true;
		}
		throw new ParameterResolutionException(String.format(
			"Element %s has an unsupported type %s for annotation @%s. Supported types are: Configuration, Optional<Configuration>, Map<String,Object>, Dictionary<String,Object>.",
			targetType.getName(), targetType.getType()
				.getName(),
			annotation().getSimpleName()));
	}

	@Override
	protected int disallowedFieldModifiers() {
		return super.disallowedFieldModifiers() | Modifier.STATIC;
	}

	// Overridden to make baselining happy
	@Override
	public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
		throws ParameterResolutionException {
		return super.supportsParameter(parameterContext, extensionContext);
	}

	// Overridden to make baselining happy
	@Override
	public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
		return super.resolveParameter(parameterContext, extensionContext);
	}

	@Override
	protected Object resolveValue(TargetType targetType, InjectConfiguration injectConfiguration,
		ExtensionContext extensionContext) throws ParameterResolutionException {
		int option = 0;
		if (!injectConfiguration.value()
			.equals(Property.NOT_SET)) {
			option += 1;
		}
		if (!injectConfiguration.withConfig()
			.pid()
			.equals(Property.NOT_SET)) {
			option += 2;
		}
		if (!injectConfiguration.withFactoryConfig()
			.factoryPid()
			.equals(Property.NOT_SET)) {
			option += 4;
		}
		Configuration configuration = null;
		ConfigurationHolder configurationHolder = null;
		switch (option) {
			case 1 :
				try {
					configuration = ConfigUtil.getConfigsByServicePid(configurationAdmin(extensionContext),
						injectConfiguration.value(), injectConfiguration.timeout());

					if (configuration != null) {

						ConfigurationCopy cCopy = ConfigurationCopy.of(configuration);
						configurationHolder = new ConfigurationHolder(cCopy, Optional.of(cCopy));
					}

				} catch (Exception e) {
					throw new ParameterResolutionException(
						String.format("Unable to obtain Configuration for %s.", injectConfiguration.value()), e);
				}
				break;
			case 2 :
				configurationHolder = handleWithConfiguration(injectConfiguration.withConfig(),
					configurationAdmin(extensionContext));
				try {
					configuration = ConfigUtil.getConfigsByServicePid(configurationAdmin(extensionContext),
						configurationHolder.getConfiguration()
							.getPid());
				} catch (Exception e) {
					throw new ParameterResolutionException("Error while finding the Configuration.", e);

				}

				break;
			case 4 :
				configurationHolder = handleWithFactoryConfiguration(injectConfiguration.withFactoryConfig(),
					configurationAdmin(extensionContext));
				try {
					configuration = ConfigUtil.getConfigsByServicePid(configurationAdmin(extensionContext),
						configurationHolder.getConfiguration()
							.getPid());
				} catch (Exception e) {
					throw new ParameterResolutionException("Error while finding the Configuration.", e);
				}
				break;
			default :
				throw new ParameterResolutionException(
					String.format("@%s - one of the Fields `value`, `withConfig` or `withFactoryConfig` must be used.",
						annotation().getSimpleName()));
		}

		if (configurationHolder != null) {
			storeConfigCopy(extensionContext, configurationHolder);
		}

		if (targetType.matches(Configuration.class)) {
			return configuration;
		}
		if (targetType.matches(Optional.class, Configuration.class)) {
			return Optional.ofNullable(configuration);
		}
		if (targetType.matches(Map.class, String.class, Object.class)) {
			if (configuration == null) {
				return null;
			}
			return Dictionaries.asMap(configuration.getProperties());
		}
		if (targetType.matches(Dictionary.class, String.class, Object.class)) {
			if (configuration == null) {
				return null;
			}
			return configuration.getProperties();
		}

		throw new ParameterResolutionException(String.format(
			"Element %s has an unsupported type %s for annotation @%s. Supported types are: Configuration, Optional<Configuration>, Map<String,Object>, Dictionary<String,Object>.",
			targetType.getName(), targetType.getType()
				.getName(),
			annotation().getSimpleName()));
	}

	private List<ConfigurationHolder> handleAnnotationsOnActiveElement(ExtensionContext extensionContext) {
		List<ConfigurationHolder> list = new ArrayList<ConfigurationHolder>();
		list.addAll(handleWithConfigurations(extensionContext));
		list.addAll(handleWithConfiguration(extensionContext));
		list.addAll(handleWithFactoryConfigurations(extensionContext));
		list.addAll(handleWithFactoryConfiguration(extensionContext));
		return list;
	}

	private List<ConfigurationHolder> handleWithFactoryConfiguration(ExtensionContext extensionContext) {
		ConfigurationAdmin ca = configurationAdmin(extensionContext);
		List<ConfigurationHolder> list = new ArrayList<ConfigurationHolder>();
		extensionContext.getElement()
			.map(element -> element.getAnnotation(WithFactoryConfiguration.class))
			.ifPresent(
				factoryConfigAnnotation -> list.add(handleWithFactoryConfiguration(factoryConfigAnnotation, ca)));
		return list;
	}

	private List<ConfigurationHolder> handleWithFactoryConfigurations(ExtensionContext extensionContext) {
		ConfigurationAdmin ca = configurationAdmin(extensionContext);
		List<ConfigurationHolder> list = new ArrayList<ConfigurationHolder>();
		extensionContext.getElement()
			.map(element -> element.getAnnotation(WithFactoryConfigurations.class))
			.map(WithFactoryConfigurations::value)
			.ifPresent(factoryConfigAnnotations -> {
				for (WithFactoryConfiguration factoryConfigAnnotation : factoryConfigAnnotations) {
					list.add(handleWithFactoryConfiguration(factoryConfigAnnotation, ca));
				}
			});
		return list;

	}

	private List<ConfigurationHolder> handleWithConfiguration(ExtensionContext extensionContext) {
		ConfigurationAdmin ca = configurationAdmin(extensionContext);
		List<ConfigurationHolder> list = new ArrayList<ConfigurationHolder>();

		extensionContext.getElement()
			.map(element -> element.getAnnotation(WithConfiguration.class))
			.ifPresent(configAnnotation -> list.add(handleWithConfiguration(configAnnotation, ca)));
		return list;
	}

	private List<ConfigurationHolder> handleWithConfigurations(ExtensionContext extensionContext) {
		ConfigurationAdmin ca = configurationAdmin(extensionContext);
		List<ConfigurationHolder> list = new ArrayList<ConfigurationHolder>();
		extensionContext.getElement()
			.map(element -> element.getAnnotation(WithConfigurations.class))
			.map(WithConfigurations::value)
			.ifPresent((configAnnotations -> {
				for (WithConfiguration configAnnotation : configAnnotations) {
					list.add(handleWithConfiguration(configAnnotation, ca));
				}
			}));
		return list;
	}

	static Store getStore(ExtensionContext extensionContext) {
		return extensionContext
			.getStore(Namespace.create(ConfigurationExtension.class, extensionContext.getUniqueId()));
	}

	private void storeConfigCopy(ExtensionContext extensionContext, List<ConfigurationHolder> configurationHolders) {
		ConfigCloseableResource ccr = getStore(extensionContext).getOrComputeIfAbsent(STORE_CONFIGURATION_KEY,
			(key) -> new ConfigCloseableResource(extensionContext, blockingConfigHandler),
			ConfigCloseableResource.class);
		ccr.addAll(configurationHolders);
	}

	private void storeConfigCopy(ExtensionContext extensionContext, ConfigurationHolder configurationHolder) {
		List<ConfigurationHolder> list = new ArrayList<ConfigurationHolder>();
		list.add(configurationHolder);
		storeConfigCopy(extensionContext, list);
	}

	public void updateConfigurationRespectNew(Configuration configurationToBeUpdated,
		Dictionary<String, Object> newConfigurationProperties, boolean isNewConfiguration)
		throws InterruptedException, IOException {
		if (configurationToBeUpdated != null) {
			if (newConfigurationProperties != null
				&& !ConfigUtil.isDictionaryWithNotSetMarker(newConfigurationProperties)) {
				// has relevant Properties to update
				blockingConfigHandler.update(configurationToBeUpdated, newConfigurationProperties, 1000);
			} else if (isNewConfiguration) {
				// is new created Configuration. must be updated
				blockingConfigHandler.update(configurationToBeUpdated, Dictionaries.dictionaryOf(), 1000);
			}
		}
	}
}
