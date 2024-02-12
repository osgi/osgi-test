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

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.osgi.framework.ServiceRegistration;
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
	implements BeforeEachCallback, ParameterResolver, BeforeAllCallback, AfterAllCallback, AfterEachCallback,
	BeforeTestExecutionCallback, AfterTestExecutionCallback {

	public ConfigurationExtension() {
		super(InjectConfiguration.class);
	}

	private static final String		STORE_CONFIG_HANDLER			= "store.config.handler";
	private static final String		STORE_CONFIG_HANDLER_REG		= "store.config.handler.reg";
	private static final String		STORE_CONFIGURATION_CLASS_KEY	= "store.configurationAdmin.class";
	private static final String		STORE_CONFIGURATION_BA_KEY		= "store.configurationAdmin.beforeAll";
	private static final String		STORE_CONFIGURATION_BE_KEY		= "store.configurationAdmin.beforeEach";
	private static final String		STORE_CONFIGURATION_TEST_KEY	= "store.configurationAdmin.test";

	private static ExtensionContext	DO_NOT_USE_CONTEXT;

	public static ConfigurationAdmin configurationAdmin(ExtensionContext extensionContext) {
		return ServiceExtension
			.getServiceConfiguration(ConfigurationAdmin.class, "", new String[0], 0, 0, extensionContext)
			.getService();
	}

	private static BlockingConfigurationHandler getBlockingConfigurationHandler(ExtensionContext extensionContext) {
		Store store = getStore(extensionContext.getRoot());
		BlockingConfigurationHandlerImpl impl = store.getOrComputeIfAbsent(STORE_CONFIG_HANDLER,
			y -> new BlockingConfigurationHandlerImpl(), BlockingConfigurationHandlerImpl.class);
		ServiceRegistration<?> svc = store.getOrComputeIfAbsent(STORE_CONFIG_HANDLER_REG,
			y -> BundleContextExtension.getBundleContext(extensionContext).registerService(ConfigurationListener.class, impl, null), ServiceRegistration.class);
		return impl;
	}

	@Override
	public void beforeAll(ExtensionContext extensionContext) throws Exception {
		DO_NOT_USE_CONTEXT = extensionContext.getRoot();
		List<ConfigurationHolder> list = handleAnnotationsOnActiveElement(extensionContext);
		storeConfigCopy(extensionContext, list, STORE_CONFIGURATION_CLASS_KEY);
		storeConfigCopy(extensionContext, emptyList(), STORE_CONFIGURATION_BA_KEY);
	}

	@Override
	public void afterAll(ExtensionContext extensionContext) throws Exception {
		super.afterAll(extensionContext);
		clearConfigurations(extensionContext, STORE_CONFIGURATION_BA_KEY);
		clearConfigurations(extensionContext, STORE_CONFIGURATION_CLASS_KEY);
		ServiceRegistration<?> svc = getStore(extensionContext.getRoot()).remove(STORE_CONFIG_HANDLER_REG,
			ServiceRegistration.class);
		if (svc != null) {
			try {
				svc.unregister();
			} catch (IllegalStateException ise) {
				// Swallow this as it means the framework is stopping
			}
		}
	}

	// Overridden to avoid a breaking baseline change
	// The original logic was in the wrong place causing
	// @Test configurations to be created earlier than they
	// should have been
	@Override
	public void beforeEach(ExtensionContext extensionContext) throws Exception {
		storeConfigCopy(extensionContext, emptyList(), STORE_CONFIGURATION_BE_KEY);
		super.beforeEach(extensionContext);
	}

	@Override
	public void afterEach(ExtensionContext extensionContext) throws Exception {
		super.afterEach(extensionContext);
		clearConfigurations(extensionContext, STORE_CONFIGURATION_BE_KEY);
	}

	@Override
	public void beforeTestExecution(ExtensionContext extensionContext) throws Exception {
		List<ConfigurationHolder> list = handleAnnotationsOnActiveElement(extensionContext);
		storeConfigCopy(extensionContext, list, STORE_CONFIGURATION_TEST_KEY);
	}

	@Override
	public void afterTestExecution(ExtensionContext extensionContext) throws Exception {
		clearConfigurations(extensionContext, STORE_CONFIGURATION_TEST_KEY);
	}

	private void clearConfigurations(ExtensionContext extensionContext, String key) {
		ConfigCloseableResource resource = getStore(extensionContext).remove(key, ConfigCloseableResource.class);
		if (resource != null) {
			try {
				resource.close();
			} catch (Throwable e) {
				if (e instanceof RuntimeException) {
					throw (RuntimeException) e;
				} else {
					throw new RuntimeException("Failed to delete test configuration", e);
				}
			}
		}
	}

	private ConfigurationHolder handleWithConfiguration(ExtensionContext context, WithConfiguration configAnnotation,
		ConfigurationAdmin configurationAdmin) {
		try {
			Configuration configBefore = ConfigUtil.getConfigsByServicePid(configurationAdmin, configAnnotation.pid(),
				0l);

			Optional<ConfigurationCopy> copyOfBefore = createConfigurationCopy(configBefore);

			Configuration configuration;
			if (Property.NOT_SET.equals(configAnnotation.location())) {
				configuration = configurationAdmin.getConfiguration(configAnnotation.pid(), null);
			} else {
				configuration = configurationAdmin.getConfiguration(configAnnotation.pid(),
					configAnnotation.location());
			}

			updateConfigurationRespectNew(context, configuration, PropertiesConverter.of(configAnnotation.properties()),
				configBefore == null);

			return new ConfigurationHolder(ConfigurationCopy.of(configuration), copyOfBefore);
		} catch (Exception e) {
			throw new ParameterResolutionException(
				String.format("Unable to obtain Configuration for %s.", configAnnotation.pid()), e);
		}
	}

	private ConfigurationHolder handleWithFactoryConfiguration(ExtensionContext context,
		WithFactoryConfiguration configAnnotation, ConfigurationAdmin configurationAdmin) {

		try {
			Configuration configBefore = ConfigUtil.getConfigsByServicePid(configurationAdmin,
				configAnnotation.factoryPid() + "~" + configAnnotation.name(), 0l);

			Optional<ConfigurationCopy> copyOfBefore = createConfigurationCopy(configBefore);

			Configuration configuration;
			if (Property.NOT_SET.equals(configAnnotation.location())) {
				configuration = configurationAdmin.getFactoryConfiguration(configAnnotation.factoryPid(),
					configAnnotation.name(), null);
			} else {
				configuration = configurationAdmin.getFactoryConfiguration(configAnnotation.factoryPid(),
					configAnnotation.name(), configAnnotation.location());
			}

			updateConfigurationRespectNew(context, configuration, PropertiesConverter.of(configAnnotation.properties()),
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
				configurationHolder = handleWithConfiguration(extensionContext, injectConfiguration.withConfig(),
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
				configurationHolder = handleWithFactoryConfiguration(extensionContext,
					injectConfiguration.withFactoryConfig(), configurationAdmin(extensionContext));
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
			storeConfigCopy(extensionContext, configurationHolder, getFinestScopeKey(extensionContext));
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

	private String getFinestScopeKey(ExtensionContext extensionContext) {
		Store store = getStore(extensionContext);
		if (store.get(STORE_CONFIGURATION_TEST_KEY) != null) {
			return STORE_CONFIGURATION_TEST_KEY;
		} else if (store.get(STORE_CONFIGURATION_BE_KEY) != null) {
			return STORE_CONFIGURATION_BE_KEY;
		} else if (store.get(STORE_CONFIGURATION_BA_KEY) != null) {
			return STORE_CONFIGURATION_BA_KEY;
		} else {
			throw new IllegalStateException("Unable to determine the current injection scope");
		}
	}

	private List<ConfigurationHolder> handleAnnotationsOnActiveElement(ExtensionContext extensionContext) {
		ConfigurationAdmin ca = configurationAdmin(extensionContext);
		return extensionContext.getElement()
			.map(AnnotationUtil::findAllConfigAnnotations)
			.orElse(emptyList())
			.stream()
			.flatMap(annotation -> handleConfiguration(extensionContext, annotation, ca).stream())
			.collect(Collectors.toList());
	}

	private List<ConfigurationHolder> handleConfiguration(ExtensionContext extensionContext, Annotation an,
		ConfigurationAdmin ca) {

		ArrayList<ConfigurationHolder> configHolders = new ArrayList<>();
		if (an instanceof WithConfigurations) {

			WithConfigurations withConfigurations = (WithConfigurations) an;
			for (WithConfiguration factoryConfigAnnotation : withConfigurations.value()) {
				configHolders.add(handleWithConfiguration(extensionContext, factoryConfigAnnotation, ca));

			}

		} else if (an instanceof WithConfiguration) {

			configHolders.add(handleWithConfiguration(extensionContext, (WithConfiguration) an, ca));

		} else if (an instanceof WithFactoryConfigurations) {

			WithFactoryConfigurations withFactoryConfigurations = (WithFactoryConfigurations) an;
			for (WithFactoryConfiguration factoryConfigAnnotation : withFactoryConfigurations.value()) {
				configHolders.add(handleWithFactoryConfiguration(extensionContext, factoryConfigAnnotation, ca));
			}

		} else if (an instanceof WithFactoryConfiguration) {

			configHolders.add(handleWithFactoryConfiguration(extensionContext, (WithFactoryConfiguration) an, ca));
		}
		return configHolders;
	}

	static Store getStore(ExtensionContext extensionContext) {
		return extensionContext
			.getStore(Namespace.create(ConfigurationExtension.class, extensionContext.getUniqueId()));
	}

	private void storeConfigCopy(ExtensionContext extensionContext, List<ConfigurationHolder> configurationHolders,
		String key) {
		ConfigCloseableResource ccr = getStore(extensionContext).getOrComputeIfAbsent(key,
			k -> new ConfigCloseableResource(extensionContext, getBlockingConfigurationHandler(extensionContext)),
			ConfigCloseableResource.class);
		ccr.addAll(configurationHolders);
	}

	private void storeConfigCopy(ExtensionContext extensionContext, ConfigurationHolder configurationHolder,
		String key) {
		storeConfigCopy(extensionContext, singletonList(configurationHolder), key);
	}

	/**
	 * This should not be used by external clients and will be removed in
	 * future.
	 *
	 * @param configurationToBeUpdated
	 * @param newConfigurationProperties
	 * @param isNewConfiguration
	 * @throws InterruptedException
	 * @throws IOException
	 */
	@Deprecated
	public void updateConfigurationRespectNew(Configuration configurationToBeUpdated,
		Dictionary<String, Object> newConfigurationProperties, boolean isNewConfiguration)
		throws InterruptedException, IOException {
		updateConfigurationRespectNew(DO_NOT_USE_CONTEXT, configurationToBeUpdated, newConfigurationProperties,
			isNewConfiguration);
	}

	private void updateConfigurationRespectNew(ExtensionContext extensionContext,
		Configuration configurationToBeUpdated, Dictionary<String, Object> newConfigurationProperties,
		boolean isNewConfiguration) throws InterruptedException, IOException {
		if (configurationToBeUpdated != null) {
			BlockingConfigurationHandler blockingConfigHandler = getBlockingConfigurationHandler(extensionContext);
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
