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
import org.osgi.test.common.exceptions.FunctionWithException;
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

	static ConfigurationAdmin configurationAdmin(ExtensionContext extensionContext) {
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
		storeConfigCopy(extensionContext);
		handleAnnotationsOnActiveElement(extensionContext);
	}

	@Override
	public void afterAll(ExtensionContext extensionContext) throws Exception {
		reset(extensionContext);
	}

	@Override
	public void beforeEach(ExtensionContext extensionContext) throws Exception {
		if (!isLifecyclePerClass(extensionContext)) {
			storeConfigCopy(extensionContext);
		}
		handleAnnotationsOnActiveElement(extensionContext);
		super.beforeEach(extensionContext);
	}

	@Override
	public void afterEach(ExtensionContext extensionContext) throws Exception {
		super.afterEach(extensionContext);
		reset(extensionContext);
	}

	Configuration handleWithConfiguration(WithConfiguration configAnnotation, ConfigurationAdmin configurationAdmin) {
		try {
			Configuration configBefore = ConfigUtil.getConfigsByServicePid(configurationAdmin, configAnnotation.pid(),
				0l);

			Configuration configuration;
			if (Property.NOT_SET.equals(configAnnotation.location())) {
				configuration = configurationAdmin.getConfiguration(configAnnotation.pid());
			} else {
				configuration = configurationAdmin.getConfiguration(configAnnotation.pid(),
					configAnnotation.location());
			}

			updateConfigurationRespectNew(configuration, PropertiesConverter.of(configAnnotation.properties()),
				configBefore == null);

			return configuration;
		} catch (Exception e) {
			throw new ParameterResolutionException(
				String.format("Unable to obtain Configuration for %s.", configAnnotation.pid()), e);
		}
	}

	Configuration handleWithFactoryConfiguration(WithFactoryConfiguration configAnnotation,
		ConfigurationAdmin configurationAdmin) {
		try {
			Configuration configBefore = ConfigUtil.getConfigsByServicePid(configurationAdmin,
				configAnnotation.factoryPid() + "~" + configAnnotation.name());

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

			return configuration;
		} catch (Exception e) {
			throw new ParameterResolutionException(
				String.format("Unable to obtain Configuration for %s.", configAnnotation.factoryPid()), e);
		}
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
		switch (option) {
			case 1 :
				try {
					configuration = ConfigUtil.getConfigsByServicePid(configurationAdmin(extensionContext),
						injectConfiguration.value(), injectConfiguration.timeout());
				} catch (Exception e) {
					throw new ParameterResolutionException(
						String.format("Unable to obtain Configuration for %s.", injectConfiguration.value()), e);
				}
				break;
			case 2 :
				configuration = handleWithConfiguration(injectConfiguration.withConfig(),
					configurationAdmin(extensionContext));
				break;
			case 4 :
				configuration = handleWithFactoryConfiguration(injectConfiguration.withFactoryConfig(),
					configurationAdmin(extensionContext));
				break;
			default :
				throw new ParameterResolutionException(
					String.format("@%s - one of the Fields `value`, `withConfig` or `withFactoryConfig` must be used.",
						annotation().getSimpleName()));
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

	private void handleAnnotationsOnActiveElement(ExtensionContext extensionContext) {
		handleWithConfigurations(extensionContext);
		handleWithConfiguration(extensionContext);
		handleWithFactoryConfigurations(extensionContext);
		handleWithFactoryConfiguration(extensionContext);
	}

	private void handleWithFactoryConfiguration(ExtensionContext extensionContext) {
		extensionContext.getElement()
			.map(element -> element.getAnnotation(WithFactoryConfiguration.class))
			.ifPresent(factoryConfigAnnotation -> handleWithFactoryConfiguration(factoryConfigAnnotation,
				configurationAdmin(extensionContext)));
	}

	private void handleWithFactoryConfigurations(ExtensionContext extensionContext) {
		extensionContext.getElement()
			.map(element -> element.getAnnotation(WithFactoryConfigurations.class))
			.map(WithFactoryConfigurations::value)
			.ifPresent(factoryConfigAnnotations -> {
				ConfigurationAdmin ca = configurationAdmin(extensionContext);
				for (WithFactoryConfiguration factoryConfigAnnotation : factoryConfigAnnotations) {
					handleWithFactoryConfiguration(factoryConfigAnnotation, ca);
				}
			});
	}

	private void handleWithConfiguration(ExtensionContext extensionContext) {
		extensionContext.getElement()
			.map(element -> element.getAnnotation(WithConfiguration.class))
			.ifPresent(
				configAnnotation -> handleWithConfiguration(configAnnotation, configurationAdmin(extensionContext)));
	}

	private void handleWithConfigurations(ExtensionContext extensionContext) {
		extensionContext.getElement()
			.map(element -> element.getAnnotation(WithConfigurations.class))
			.map(WithConfigurations::value)
			.ifPresent(configAnnotations -> {
				ConfigurationAdmin ca = configurationAdmin(extensionContext);
				for (WithConfiguration configAnnotation : configAnnotations) {
					handleWithConfiguration(configAnnotation, ca);
				}
			});
	}

	static Store getStore(ExtensionContext extensionContext) {
		return extensionContext
			.getStore(Namespace.create(ConfigurationExtension.class, extensionContext.getUniqueId()));
	}

	private void storeConfigCopy(ExtensionContext extensionContext) throws Exception {
		getStore(extensionContext).getOrComputeIfAbsent(STORE_CONFIGURATION_KEY,
			FunctionWithException.asFunction(key -> {
				List<Configuration> configurations = ConfigUtil
					.getAllConfigurations(configurationAdmin(extensionContext));
				List<ConfigurationCopy> configurationCopies = ConfigUtil.cloneConfigurations(configurations);
				return configurationCopies;
			}), List.class);
	}

	private void reset(ExtensionContext extensionContext) throws Exception {
		@SuppressWarnings("unchecked")
		List<ConfigurationCopy> configurationCopies = getStore(extensionContext).remove(STORE_CONFIGURATION_KEY,
			List.class);
		if (configurationCopies != null) {
			ConfigUtil.resetConfig(blockingConfigHandler, configurationAdmin(extensionContext), configurationCopies);
		}
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
