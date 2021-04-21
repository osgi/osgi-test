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

package org.osgi.test.common.context;

import static java.util.stream.Collectors.toMap;
import static org.osgi.framework.Bundle.ACTIVE;
import static org.osgi.framework.Bundle.STARTING;
import static org.osgi.framework.Bundle.STOPPING;
import static org.osgi.test.common.exceptions.ConsumerWithException.asConsumer;
import static org.osgi.test.common.exceptions.ConsumerWithException.asConsumerIgnoreException;

import java.io.InputStream;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.BundleListener;
import org.osgi.framework.FrameworkListener;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceObjects;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.test.common.exceptions.Exceptions;

public class CloseableBundleContext implements AutoCloseable, InvocationHandler {
	private static final Consumer<ServiceRegistration<?>>					unregisterService				= asConsumerIgnoreException(
		ServiceRegistration::unregister);
	private static final Consumer<AutoCloseable>							autoclose						= asConsumer(
		AutoCloseable::close);
	private static final Predicate<Bundle>									installed						= bundle -> (bundle
		.getState() & Bundle.UNINSTALLED) != Bundle.UNINSTALLED;
	private static final Consumer<Bundle>									uninstallBundle					= asConsumer(
		Bundle::uninstall);
	static final ClassLoader												PROXY_CLASS_LOADER				= CloseableBundleContext.class
		.getClassLoader();

	private final Bundle													bundle;
	private final Set<ServiceRegistration<?>>								regs							= Collections
		.synchronizedSet(Collections.newSetFromMap(new IdentityHashMap<>()));
	private final Set<FrameworkListener>									fwListeners						= Collections
		.synchronizedSet(Collections.newSetFromMap(new IdentityHashMap<>()));
	private final Set<ServiceListener>										sListeners						= Collections
		.synchronizedSet(Collections.newSetFromMap(new IdentityHashMap<>()));
	private final Set<BundleListener>										bListeners						= Collections
		.synchronizedSet(Collections.newSetFromMap(new IdentityHashMap<>()));
	private final Set<Bundle>												bundlesToBeUninstalledOnClose	= Collections
		.synchronizedSet(new HashSet<>());
	private final Map<ServiceReference<?>, Integer>							services						= Collections
		.synchronizedMap(new HashMap<>());
	private final Set<ServiceObjects<?>>									serviceobjects					= Collections
		.synchronizedSet(Collections.newSetFromMap(new IdentityHashMap<>()));

	private static final Map<Method, BiFunction<Object, Object[], Object>>	methods;
	static {
		methods = Arrays.stream(BundleContext.class.getMethods())
			.collect(toMap(Function.identity(), method -> {
				try {
					return invoker(CloseableBundleContext.class.getMethod(method.getName(), method.getParameterTypes()),
						CloseableBundleContext::closeableBundleContext);
				} catch (NoSuchMethodException e) {
					return invoker(method, CloseableBundleContext::realBundleContext);
				}
			}));
		try {
			methods.put(AutoCloseable.class.getMethod("close"), CloseableBundleContext::delegatedClose);
			methods.put(Object.class.getMethod("toString"), CloseableBundleContext::delegatedToString);
			methods.put(Object.class.getMethod("hashCode"), CloseableBundleContext::delegatedHashCode);
			methods.put(Object.class.getMethod("equals", Object.class), CloseableBundleContext::delegatedEquals);
		} catch (NoSuchMethodException e) {
			throw Exceptions.duck(e);
		}
	}

	static BiFunction<Object, Object[], Object> invoker(Method method, Function<? super Object, Object> mapper) {
		try {
			MethodHandle mh = MethodHandles.publicLookup()
				.unreflect(method);
			if (Modifier.isStatic(method.getModifiers())) {
				return (Object proxy, Object[] args) -> {
					try {
						return mh.invokeWithArguments(args);
					} catch (Throwable e) {
						throw Exceptions.duck(e);
					}
				};
			} else {
				return (Object proxy, Object[] args) -> {
					try {
						return mh.bindTo(mapper.apply(proxy))
							.invokeWithArguments(args);
					} catch (Throwable e) {
						throw Exceptions.duck(e);
					}
				};
			}
		} catch (Exception e) {
			throw Exceptions.duck(e);
		}
	}

	public static BundleContext proxy(Bundle bundle) {
		return (BundleContext) Proxy.newProxyInstance(PROXY_CLASS_LOADER, new Class<?>[] {
			BundleContext.class, AutoCloseable.class
		}, new CloseableBundleContext(bundle));
	}

	public CloseableBundleContext(Bundle bundle) {
		this.bundle = bundle;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		BiFunction<Object, Object[], Object> invoker = methods.get(method);
		if (invoker == null) {
			throw new IllegalArgumentException();
		}
		return invoker.apply(proxy, args);
	}

	private static CloseableBundleContext closeableBundleContext(Object proxy) {
		InvocationHandler invocationHandler;
		try {
			invocationHandler = Proxy.getInvocationHandler(proxy);
		} catch (IllegalArgumentException e) {
			return null;
		}
		if (invocationHandler instanceof CloseableBundleContext) {
			return (CloseableBundleContext) invocationHandler;
		}
		return null;
	}

	private static BundleContext realBundleContext(Object proxy) {
		CloseableBundleContext closeableBundleContext = closeableBundleContext(proxy);
		if (closeableBundleContext == null) {
			return null;
		}
		Bundle real = closeableBundleContext.bundle;
		while ((closeableBundleContext = closeableBundleContext(real)) != null) {
			real = closeableBundleContext.bundle;
		}
		if (((STARTING | ACTIVE | STOPPING) & real.getState()) == 0)
			throw new IllegalStateException("The bundle " + real + " is not started");
		return real.getBundleContext();
	}

	private static Void delegatedClose(Object proxy, Object[] args) {
		closeableBundleContext(proxy).close();
		return null;
	}

	@Override
	public void close() {
		BundleContext bundleContext = getBundleContext();
		bundlesToBeUninstalledOnClose.stream()
			.filter(installed)
			.forEach(uninstallBundle);
		bundlesToBeUninstalledOnClose.clear();

		services.forEach((reference, useCount) -> {
			for (int i = useCount; i > 0; i--) {
				bundleContext.ungetService(reference);
			}
		});
		services.clear();

		serviceobjects.stream()
			.map(AutoCloseable.class::cast)
			.forEach(autoclose);
		serviceobjects.clear();

		regs.forEach(unregisterService);
		regs.clear();

		bListeners.forEach(bundleContext::removeBundleListener);
		bListeners.clear();

		sListeners.forEach(bundleContext::removeServiceListener);
		sListeners.clear();

		fwListeners.forEach(bundleContext::removeFrameworkListener);
		fwListeners.clear();
	}

	private static String delegatedToString(Object proxy, Object[] args) {
		return "CloseableBundleContext[" + System.identityHashCode(proxy) + "]:" + realBundleContext(proxy).toString();
	}

	private static int delegatedHashCode(Object proxy, Object[] args) {
		return realBundleContext(proxy).hashCode();
	}

	private static boolean delegatedEquals(Object proxy, Object[] args) {
		BundleContext bundleContext = realBundleContext(proxy);
		BundleContext real = realBundleContext(args[0]);
		if (real != null) {
			return bundleContext.equals(real);
		}
		return bundleContext.equals(args[0]);
	}

	public Bundle installBundle(String location, InputStream input) throws BundleException {
		BundleContext bundleContext = getBundleContext();
		Bundle bundle = bundleContext.getBundle(location);
		// Check whether the Bundle already exists because we should only add
		// new bundles to the bundlesToBeUninstalledOnClose-Set of this
		// CloseableBundleContext. On close all Bundles in this Set will be
		// uninstalled. We are not allowed to uninstall bundles, that exist
		// before. Same in installBundle(String location)
		if (bundle == null) {
			bundle = bundleContext.installBundle(location, input);
			bundlesToBeUninstalledOnClose.add(bundle);
		}
		return bundle;
	}

	private BundleContext getBundleContext() {
		if (((STARTING | ACTIVE | STOPPING) & bundle.getState()) == 0)
			throw new IllegalStateException("The bundle " + bundle + " is not started");
		return bundle.getBundleContext();
	}

	public Bundle installBundle(String location) throws BundleException {
		BundleContext bundleContext = getBundleContext();
		Bundle bundle = bundleContext.getBundle(location);
		// see installBundle(String location, InputStream input)
		if (bundle == null) {
			bundle = bundleContext.installBundle(location);
			bundlesToBeUninstalledOnClose.add(bundle);
		}
		return bundle;
	}

	public void addServiceListener(ServiceListener listener, String filter) throws InvalidSyntaxException {
		BundleContext bundleContext = getBundleContext();
		bundleContext.addServiceListener(listener, filter);
		sListeners.add(listener);
	}

	public void addServiceListener(ServiceListener listener) {
		BundleContext bundleContext = getBundleContext();
		bundleContext.addServiceListener(listener);
		sListeners.add(listener);
	}

	public void removeServiceListener(ServiceListener listener) {
		BundleContext bundleContext = getBundleContext();
		bundleContext.removeServiceListener(listener);
		sListeners.remove(listener);
	}

	public void addBundleListener(BundleListener listener) {
		BundleContext bundleContext = getBundleContext();
		bundleContext.addBundleListener(listener);
		bListeners.add(listener);
	}

	public void removeBundleListener(BundleListener listener) {
		BundleContext bundleContext = getBundleContext();
		bundleContext.removeBundleListener(listener);
		bListeners.remove(listener);
	}

	public void addFrameworkListener(FrameworkListener listener) {
		BundleContext bundleContext = getBundleContext();
		bundleContext.addFrameworkListener(listener);
		fwListeners.add(listener);
	}

	public void removeFrameworkListener(FrameworkListener listener) {
		BundleContext bundleContext = getBundleContext();
		bundleContext.removeFrameworkListener(listener);
		fwListeners.remove(listener);
	}

	public <S> S getService(ServiceReference<S> reference) {
		BundleContext bundleContext = getBundleContext();
		S service = bundleContext.getService(reference);
		Integer count = services.merge(reference, 1, (oldValue, dummy) -> oldValue + 1);
		return service;
	}

	public <S> ServiceObjects<S> getServiceObjects(ServiceReference<S> reference) {
		BundleContext bundleContext = getBundleContext();
		final ServiceObjects<S> so = bundleContext.getServiceObjects(reference);
		ServiceObjects<S> serviceObjects = CloseableServiceObjects.proxy(so);
		serviceobjects.add(serviceObjects);
		return serviceObjects;
	}

	public ServiceRegistration<?> registerService(String[] clazzes, Object service, Dictionary<String, ?> properties) {
		BundleContext bundleContext = getBundleContext();
		ServiceRegistration<?> reg = bundleContext.registerService(clazzes, service, properties);
		regs.add(reg);
		return reg;
	}

	public ServiceRegistration<?> registerService(String clazz, Object service, Dictionary<String, ?> properties) {
		BundleContext bundleContext = getBundleContext();
		ServiceRegistration<?> reg = bundleContext.registerService(clazz, service, properties);
		regs.add(reg);
		return reg;
	}

	public <S> ServiceRegistration<S> registerService(Class<S> clazz, S service, Dictionary<String, ?> properties) {
		BundleContext bundleContext = getBundleContext();
		ServiceRegistration<S> reg = bundleContext.registerService(clazz, service, properties);
		regs.add(reg);
		return reg;
	}

	public <S> ServiceRegistration<S> registerService(Class<S> clazz, ServiceFactory<S> factory,
		Dictionary<String, ?> properties) {
		BundleContext bundleContext = getBundleContext();
		ServiceRegistration<S> reg = bundleContext.registerService(clazz, factory, properties);
		regs.add(reg);
		return reg;
	}

	public boolean ungetService(ServiceReference<?> reference) {
		Integer count = services.compute(reference, (key, oldValue) -> {
			if ((oldValue == null) || (oldValue == 0)) {
				return null;
			}
			return oldValue - 1;
		});
		if (count != null) {
			getBundleContext()
				.ungetService(reference);
			return true;
		}
		return false;
	}
}
