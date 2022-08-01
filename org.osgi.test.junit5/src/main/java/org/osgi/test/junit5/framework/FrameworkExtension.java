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
package org.osgi.test.junit5.framework;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Objects;
import java.util.function.Consumer;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store;
import org.junit.platform.commons.PreconditionViolationException;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.runtime.ServiceComponentRuntime;
import org.osgi.service.component.runtime.dto.ComponentConfigurationDTO;
import org.osgi.service.component.runtime.dto.ComponentDescriptionDTO;
import org.osgi.service.component.runtime.dto.UnsatisfiedReferenceDTO;
import org.osgi.test.common.bitmaps.BundleState;

public class FrameworkExtension implements BeforeAllCallback, BeforeEachCallback, AfterEachCallback {

	private JUnit5ConnectFramework		connect;

	FrameworkExtension() {
	}

	public static FrameworkExtension create() {
		return new FrameworkExtension();
	}

	@Override
	public void beforeAll(ExtensionContext context) throws Exception {
		this.connect = getConnectFramework(context);
	}

	@Override
	public void beforeEach(ExtensionContext context) throws Exception {
		JUnit5FrameworkUtilHelper.threadHelper.set(connect);
	}

	@Override
	public void afterEach(ExtensionContext context) throws Exception {
		JUnit5FrameworkUtilHelper.threadHelper.set(null);
	}

	private JUnit5ConnectFramework getConnectFramework(ExtensionContext context) {
		Namespace namespace = Namespace.create(FrameworkExtension.class, context.getUniqueId());
		Store store = context.getStore(namespace);
		return store.getOrComputeIfAbsent("JUnit5ConnectFramework", key -> {
			try {
				return new JUnit5ConnectFramework(context.getRequiredTestClass(), context.getUniqueId());
			} catch (Exception e) {
				e.printStackTrace();
				throw new PreconditionViolationException("problem starting framework: " + e, e);
			}
		}, JUnit5ConnectFramework.class);
	}

	public FrameworkEvents getFrameworkEvents() {
		return connect.frameworkEvents;
	}

	/**
	 * Prints the current Framework bundles and their state to the given log
	 * consumer
	 *
	 * @param log for each line, this consumer will receive a string
	 */
	public void printBundles(Consumer<String> log) {
		Bundle[] bundles = connect.getBundles();
		log.accept("============ Framework Bundles ==================");
		Comparator<Bundle> bySymbolicName = Comparator.comparing(Bundle::getSymbolicName,
			String.CASE_INSENSITIVE_ORDER);
		Comparator<Bundle> byState = Comparator.comparingInt(Bundle::getState);
		Arrays.stream(bundles)
			.sorted(byState.thenComparing(bySymbolicName))
			.forEachOrdered(bundle -> {
				log.accept(toBundleState(bundle.getState()) + " | " + bundle.getSymbolicName() + " ("
					+ bundle.getVersion() + ")");
			});
	}

	/**
	 * Prints the current Framework bundles, registered services and components
	 * to the given log consumer
	 *
	 * @param log for each line, this consumer will receive a string
	 */
	public void printFrameworkState(Consumer<String> log) {
		printBundles(log);
		printComponents(log);
		printServices(log);
	}

	/**
	 * Prints the current Framework registered services to the given log
	 * consumer
	 *
	 * @param log for each line, this consumer will receive a string
	 */
	public void printServices(Consumer<String> log) {
		log.accept("============ Registered Services ==================");
		Arrays.stream(connect.getBundles())
			.map(bundle -> bundle.getRegisteredServices())
			.filter(Objects::nonNull)
			.flatMap(Arrays::stream)
			.forEach(reference -> {
				Object service = reference.getProperty(Constants.OBJECTCLASS);
				if (service instanceof Object[]) {
					Object[] objects = (Object[]) service;
					if (objects.length == 1) {
						service = objects[0];
					} else {
						service = Arrays.toString(objects);
					}
				}
				log.accept(service + " registered by " + reference.getBundle()
					.getSymbolicName() + " | " + reference.getProperties());
			});
	}

	/**
	 * Prints the current Framework bundles and their state to the given log
	 * consumer
	 *
	 * @param log for each line, this consumer will receive a string
	 */
	public void printComponents(Consumer<String> log) {
		BundleContext bc = connect.framework.getBundleContext();
		ServiceReference<ServiceComponentRuntime> reference = bc.getServiceReference(ServiceComponentRuntime.class);
		ServiceComponentRuntime componentRuntime;
		if (reference != null) {
			componentRuntime = bc.getService(reference);
		} else {
			componentRuntime = null;
		}
		if (componentRuntime != null) {
			log.accept("============ Framework Components ==================");
			Collection<ComponentDescriptionDTO> descriptionDTOs = componentRuntime.getComponentDescriptionDTOs();
			Comparator<ComponentConfigurationDTO> byComponentName = Comparator.comparing(dto -> dto.description.name,
				String.CASE_INSENSITIVE_ORDER);
			Comparator<ComponentConfigurationDTO> byComponentState = Comparator.comparingInt(dto -> dto.state);
			descriptionDTOs.stream()
				.flatMap(dto -> componentRuntime.getComponentConfigurationDTOs(dto)
					.stream())
				.sorted(byComponentState.thenComparing(byComponentName))
				.forEachOrdered(dto -> {
					if (dto.state == ComponentConfigurationDTO.FAILED_ACTIVATION) {
						log.accept(toComponentState(dto.state) + " | " + dto.description.name + " | " + dto.failure);
					} else {
						log.accept(toComponentState(dto.state) + " | " + dto.description.name);
					}
					for (int i = 0; i < dto.unsatisfiedReferences.length; i++) {
						UnsatisfiedReferenceDTO ref = dto.unsatisfiedReferences[i];
						log.accept("\t" + ref.name + " is missing");
					}
				});
			bc.ungetService(reference);
		} else {
			log.accept("No service component runtime installed (or started) in this framework!");
		}
	}

	private static String toComponentState(int state) {
		switch (state) {
			case ComponentConfigurationDTO.ACTIVE :
				return "ACTIVE ";
			case ComponentConfigurationDTO.FAILED_ACTIVATION :
				return "FAILED ";
			case ComponentConfigurationDTO.SATISFIED :
				return "SATISFIED ";
			case ComponentConfigurationDTO.UNSATISFIED_CONFIGURATION :
			case ComponentConfigurationDTO.UNSATISFIED_REFERENCE :
				return "UNSATISFIED";
			default :
				return String.valueOf(state);
		}
	}

	private static String toBundleState(int state) {
		return String.format("%-9s", BundleState.toString(state));
	}

}
