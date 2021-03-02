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

import static org.osgi.test.common.inject.FieldInjector.findAnnotatedFields;
import static org.osgi.test.common.inject.FieldInjector.setField;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.util.Dictionary;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.osgi.framework.BundleContext;
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
import org.osgi.test.junit5.service.ServiceExtension;

public class ConfigurationExtension
	implements BeforeEachCallback, ParameterResolver, BeforeAllCallback, AfterAllCallback, AfterEachCallback {

	private static final String					STORE_CONFIGURATION_KEY	= "store.configurationAdmin";

	private static BlockingConfigurationHandler	blockingConfigHandler;

	static BundleContext bc(ExtensionContext extensionContext) {
		return BundleContextExtension.getBundleContext(extensionContext);
	}

	static ConfigurationAdmin ca(ExtensionContext extensionContext) {
		return ServiceExtension
			.getServiceConfiguration(ConfigurationAdmin.class, "", new String[0], 0, 0, extensionContext)
			.getService();
	}

	@Override
	public void beforeEach(ExtensionContext extensionContext) throws Exception {

		storeConfigCopy(extensionContext);
		handleInjectConfigAnnotationOnFields(extensionContext);
		handleAnnotationsOnActiveElement(extensionContext);
	}

	private void handleInjectConfigAnnotationOnFields(ExtensionContext extensionContext) throws Exception {
		List<Field> fields = findAnnotatedFields(extensionContext.getRequiredTestClass(), InjectConfiguration.class);
		for (Field field : fields) {
			handleInjectConfigAnnotationOnField(extensionContext, field);
		}
	}

	private void handleInjectConfigAnnotationOnField(ExtensionContext extensionContext, Field field) throws Exception {
		assertValidFieldCandidate(field);

		ConfigurationAdmin ca = ca(extensionContext);
		TargetType targetType = TargetType.of(field);
		InjectConfiguration injectConfiguration = field.getAnnotation(InjectConfiguration.class);

		Configuration ic = ConfigUtil.getConfigsByServicePid(ca, injectConfiguration.value(),
			injectConfiguration.timeout());

		Object objectToInject = transformConfigToTargetType(ic, targetType);

		setField(field, extensionContext.getRequiredTestInstance(), objectToInject);
	}

	private void storeConfigCopy(ExtensionContext extensionContext) throws Exception {

		List<Configuration> configurations = ConfigUtil.getAllConfigurations(ca(extensionContext));
		List<ConfigurationCopy> configurationCopies = ConfigUtil.cloneConfigurations(configurations);

		extensionContext.getStore(Namespace.create(ConfigurationExtension.class, extensionContext.getUniqueId()))
			.put(STORE_CONFIGURATION_KEY, configurationCopies);

	}

	Configuration handleWithConfiguration(WithConfiguration configAnnotation, ConfigurationAdmin configurationAdmin,
		BlockingConfigurationHandler blockingConfigHandler)
		throws ParameterResolutionException, IllegalArgumentException {

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
			throw new ParameterResolutionException("ConfigurationAdmin could not be found", e);
		}

	}

	Configuration handleWithFactoryConfiguration(WithFactoryConfiguration configAnnotation,
		ConfigurationAdmin configurationAdmin) throws ParameterResolutionException, IllegalArgumentException {

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
			throw new ParameterResolutionException("ConfigurationAdmin could not be found", e);
		}

	}

	static void assertValidFieldCandidate(Field field) {
		if (Modifier.isFinal(field.getModifiers()) || Modifier.isPrivate(field.getModifiers())
			|| Modifier.isStatic(field.getModifiers())) {
			throw new ExtensionConfigurationException("@" + WithConfiguration.class.getSimpleName() + " field ["
				+ field.getName() + "] must not be final, private or static.");
		}
	}

	@Override
	public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
		throws ParameterResolutionException {

		ConfigurationAdmin ca = ca(extensionContext);
		try {
			Optional<InjectConfiguration> injectConfiguration = parameterContext
				.findAnnotation(InjectConfiguration.class);

			TargetType targetType = TargetType.of(parameterContext.getParameter());

			if (injectConfiguration.isPresent()) {

				InjectConfiguration ic = injectConfiguration.get();

				Configuration configuration = null;
				boolean valueUsed = false;
				boolean withConfigUsed = false;
				boolean withFactoryConfigUsed = false;
				if (!ic.value()
					.equals(Property.NOT_SET)) {

					configuration = ConfigUtil.getConfigsByServicePid(ca(extensionContext), ic.value(), ic.timeout());

					valueUsed = true;
				} else if (!ic.withConfig()
					.pid()
					.equals(Property.NOT_SET)) {

					if (valueUsed) {
						throw new IllegalArgumentException(
							"@InjectConfiguration - only one of the Fields `value`, `withConfig` or `withFactoryConfig` could be used.");
					}
					WithConfiguration wc = ic.withConfig();
					configuration = handleWithConfiguration(wc, ca, blockingConfigHandler);
					withConfigUsed = true;
				} else if (!ic.withFactoryConfig()
					.factoryPid()
					.equals(Property.NOT_SET)) {

					if (valueUsed || withConfigUsed) {
						throw new IllegalArgumentException(
							"@InjectConfiguration - only one of the Fields `value`, `withConfig` or `withFactoryConfig` could be used.");
					}

					WithFactoryConfiguration wc = ic.withFactoryConfig();
					configuration = handleWithFactoryConfiguration(wc, ca);
					withFactoryConfigUsed = true;
				}
				if (!valueUsed && !withConfigUsed && !withFactoryConfigUsed) {
					throw new IllegalArgumentException(
						"@InjectConfiguration - one of the Fields `value`, `withConfig` or `withFactoryConfig` must be used.");
				}
				return transformConfigToTargetType(configuration, targetType);
			}
			return null;
		} catch (Exception e) {

			throw new ParameterResolutionException("Could not get Configuration from Configuration-Admin", e);
		}
	}

	private Object transformConfigToTargetType(Configuration configuration, TargetType targetType) throws Exception {

		if (targetType.matches(Configuration.class)) {
			return configuration;
		} else if (targetType.matches(Optional.class, Configuration.class)) {
			return Optional.ofNullable(configuration);
		} else if (targetType.matches(Map.class, String.class, Object.class)) {
			return Dictionaries.asMap(configuration.getProperties());
		} else if (targetType.matches(Dictionary.class, String.class, Object.class)) {
			return configuration.getProperties();
		}
		throw new ParameterResolutionException("Bad Parameter-Type");
	}

	@Override
	public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
		throws ParameterResolutionException {

		if (parameterContext.isAnnotated(InjectConfiguration.class)) {
			Parameter parameter = parameterContext.getParameter();
			if (parameter.getType()
				.isAssignableFrom(Configuration.class)
				|| parameter.getType()
					.isAssignableFrom(Optional.class)
					&& (((ParameterizedType) parameter.getParameterizedType()).getActualTypeArguments()[0]
						.equals(Configuration.class))
				|| parameter.getType()
					.isAssignableFrom(Map.class)
				|| parameter.getType()
					.isAssignableFrom(Dictionary.class)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void beforeAll(ExtensionContext extensionContext) throws Exception {

		BlockingConfigurationHandlerImpl blockingConfigHandlerImpl = new BlockingConfigurationHandlerImpl();
		bc(extensionContext).registerService(ConfigurationListener.class, blockingConfigHandlerImpl, null);
		blockingConfigHandler = blockingConfigHandlerImpl;
		storeConfigCopy(extensionContext);
		handleAnnotationsOnActiveElement(extensionContext);

	}

	private void handleAnnotationsOnActiveElement(ExtensionContext extensionContext) {

		handleWithConfigurations(extensionContext);
		handleWithConfiguration(extensionContext);
		handleWithFactoryConfigurations(extensionContext);
		handleWithFactoryConfiguration(extensionContext);

	}

	private void handleWithFactoryConfiguration(ExtensionContext extensionContext) {
		extensionContext.getElement()
			.ifPresent((element) -> {
				WithFactoryConfiguration factoryConfigAnnotation = element
					.getAnnotation(WithFactoryConfiguration.class);
				if (factoryConfigAnnotation != null) {
					handleWithFactoryConfiguration(factoryConfigAnnotation, ca(extensionContext));
				}
			});
	}

	private void handleWithFactoryConfigurations(ExtensionContext extensionContext) {
		extensionContext.getElement()
			.ifPresent((element) -> {
				WithFactoryConfigurations factoryConfigAnnotations = element
					.getAnnotation(WithFactoryConfigurations.class);
				if (factoryConfigAnnotations != null) {
					Stream.of(factoryConfigAnnotations.value())
						.forEachOrdered((factoryConfigAnnotation) -> {
							handleWithFactoryConfiguration(factoryConfigAnnotation, ca(extensionContext));
						});
				}
			});
	}

	private void handleWithConfiguration(ExtensionContext extensionContext) {
		extensionContext.getElement()
			.ifPresent((element) -> {
				WithConfiguration configAnnotation = element.getAnnotation(WithConfiguration.class);
				if (configAnnotation != null) {
					handleWithConfiguration(configAnnotation, ca(extensionContext), blockingConfigHandler);
				}
			});
	}

	private void handleWithConfigurations(ExtensionContext extensionContext) {
		extensionContext.getElement()
			.ifPresent((element) -> {
				WithConfigurations configAnnotations = element.getAnnotation(WithConfigurations.class);
				if (configAnnotations != null) {
					Stream.of(configAnnotations.value())
						.forEachOrdered((configAnnotation) -> {
							handleWithConfiguration(configAnnotation, ca(extensionContext), blockingConfigHandler);
						});
				}
			});
	}

	@Override
	public void afterEach(ExtensionContext extensionContext) throws Exception {
		reset(extensionContext);

	}

	@Override
	public void afterAll(ExtensionContext extensionContext) throws Exception {

		reset(extensionContext);
	}

	private void reset(ExtensionContext extensionContext) throws Exception {
		@SuppressWarnings("unchecked")
		List<ConfigurationCopy> copys = getStore(extensionContext).get(STORE_CONFIGURATION_KEY, List.class);
		ConfigUtil.resetConfig(blockingConfigHandler, ca(extensionContext), copys);
		getStore(extensionContext).remove(STORE_CONFIGURATION_KEY, List.class);
	}

	static Store getStore(ExtensionContext extensionContext) {
		return extensionContext
			.getStore(Namespace.create(ConfigurationExtension.class, extensionContext.getUniqueId()));
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
