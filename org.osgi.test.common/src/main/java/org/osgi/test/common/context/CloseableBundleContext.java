/*
 * Copyright (c) OSGi Alliance (2019). All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.osgi.test.common.context;

import static org.osgi.test.common.exceptions.ConsumerWithException.asConsumer;
import static org.osgi.test.common.exceptions.ConsumerWithException.asConsumerIgnoreException;

import java.io.InputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
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
	private static final Consumer<ServiceRegistration<?>>	unregisterService	= asConsumerIgnoreException(
		ServiceRegistration::unregister);
	private static final Consumer<AutoCloseable>			autoclose			= asConsumer(AutoCloseable::close);
	private static final Predicate<Bundle>					installed			= bundle -> (bundle.getState()
		& Bundle.UNINSTALLED) != Bundle.UNINSTALLED;
	private static final Consumer<Bundle>					uninstallBundle		= asConsumer(Bundle::uninstall);
	static final ClassLoader								PROXY_CLASS_LOADER	= CloseableBundleContext.class
		.getClassLoader();

	private final BundleContext								bundleContext;
	private final Set<ServiceRegistration<?>>				regs				= Collections
		.synchronizedSet(Collections.newSetFromMap(new IdentityHashMap<>()));
	private final Set<FrameworkListener>					fwListeners			= Collections
		.synchronizedSet(Collections.newSetFromMap(new IdentityHashMap<>()));
	private final Set<ServiceListener>						sListeners			= Collections
		.synchronizedSet(Collections.newSetFromMap(new IdentityHashMap<>()));
	private final Set<BundleListener>						bListeners			= Collections
		.synchronizedSet(Collections.newSetFromMap(new IdentityHashMap<>()));
	private final Set<Bundle>								bundles				= Collections
		.synchronizedSet(new HashSet<>());
	private final Map<ServiceReference<?>, Integer>			services			= Collections
		.synchronizedMap(new HashMap<>());
	private final Set<ServiceObjects<?>>					serviceobjects		= Collections
		.synchronizedSet(Collections.newSetFromMap(new IdentityHashMap<>()));

	public static BundleContext proxy(BundleContext bundleContext) {
		return (BundleContext) Proxy.newProxyInstance(PROXY_CLASS_LOADER, new Class<?>[] {
			BundleContext.class, AutoCloseable.class
		}, new CloseableBundleContext(bundleContext));
	}

	public CloseableBundleContext(BundleContext bundleContext) {
		this.bundleContext = bundleContext;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		if (method.getDeclaringClass()
			.equals(AutoCloseable.class)) {
			close();
			return null;
		}
		if (method.getDeclaringClass()
			.equals(BundleContext.class)) {

			try {
				Method ourMethod = getClass().getMethod(method.getName(), method.getParameterTypes());

				return ourMethod.invoke(this, args);
			} catch (NoSuchMethodException t) {
				return method.invoke(bundleContext, args);
			}
		}
		if (method.getDeclaringClass()
			.equals(Object.class)) {
			switch (method.getName()) {
				case "toString" :
					return delegatedToString(proxy);
				case "hashCode" :
					return bundleContext.hashCode();
				case "equals" :
					return bundleContext.equals(args[0]);
			}
		}

		throw new IllegalArgumentException();
	}

	public static void close(BundleContext bundleContext) {
		CloseableBundleContext cbc = (CloseableBundleContext) Proxy.getInvocationHandler(bundleContext);
		cbc.close();
	}

	@Override
	public void close() {
		bundles.stream()
			.filter(installed)
			.forEach(uninstallBundle);
		bundles.clear();

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

	public String delegatedToString(Object proxy) {
		return "CloseableBundleContext[" + System.identityHashCode(proxy) + "]:" + bundleContext.toString();
	}

	public Bundle installBundle(String location, InputStream input) throws BundleException {
		Bundle bundle = bundleContext.installBundle(location, input);
		bundles.add(bundle);
		return bundle;
	}

	public Bundle installBundle(String location) throws BundleException {
		Bundle bundle = bundleContext.installBundle(location);
		bundles.add(bundle);
		return bundle;
	}

	public void addServiceListener(ServiceListener listener, String filter) throws InvalidSyntaxException {
		bundleContext.addServiceListener(listener, filter);
		sListeners.add(listener);
	}

	public void addServiceListener(ServiceListener listener) {
		bundleContext.addServiceListener(listener);
		sListeners.add(listener);
	}

	public void removeServiceListener(ServiceListener listener) {
		bundleContext.removeServiceListener(listener);
		sListeners.remove(listener);
	}

	public void addBundleListener(BundleListener listener) {
		bundleContext.addBundleListener(listener);
		bListeners.add(listener);
	}

	public void removeBundleListener(BundleListener listener) {
		bundleContext.removeBundleListener(listener);
		bListeners.remove(listener);
	}

	public void addFrameworkListener(FrameworkListener listener) {
		bundleContext.addFrameworkListener(listener);
		fwListeners.add(listener);
	}

	public void removeFrameworkListener(FrameworkListener listener) {
		bundleContext.removeFrameworkListener(listener);
		fwListeners.remove(listener);
	}

	public <S> S getService(ServiceReference<S> reference) {
		S service = bundleContext.getService(reference);
		Integer count = services.merge(reference, 1, (oldValue, dummy) -> oldValue + 1);
		return service;
	}

	public <S> ServiceObjects<S> getServiceObjects(ServiceReference<S> reference) {
		final ServiceObjects<S> so = bundleContext.getServiceObjects(reference);
		ServiceObjects<S> serviceObjects = ClosableServiceObjects.proxy(so);
		serviceobjects.add(serviceObjects);
		return serviceObjects;
	}

	public ServiceRegistration<?> registerService(String[] clazzes, Object service, Dictionary<String, ?> properties) {
		ServiceRegistration<?> reg = bundleContext.registerService(clazzes, service, properties);
		regs.add(reg);
		return reg;
	}

	public ServiceRegistration<?> registerService(String clazz, Object service, Dictionary<String, ?> properties) {
		ServiceRegistration<?> reg = bundleContext.registerService(clazz, service, properties);
		regs.add(reg);
		return reg;
	}

	public <S> ServiceRegistration<S> registerService(Class<S> clazz, S service, Dictionary<String, ?> properties) {
		ServiceRegistration<S> reg = bundleContext.registerService(clazz, service, properties);
		regs.add(reg);
		return reg;
	}

	public <S> ServiceRegistration<S> registerService(Class<S> clazz, ServiceFactory<S> factory,
		Dictionary<String, ?> properties) {
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
			bundleContext.ungetService(reference);
			return true;
		}
		return false;
	}

	private static class ClosableServiceObjects<S> implements AutoCloseable, InvocationHandler {
		private final ServiceObjects<S>	so;
		private final Map<S, Integer>	instances	= Collections.synchronizedMap(new IdentityHashMap<>());

		@SuppressWarnings("unchecked")
		public static <S> ServiceObjects<S> proxy(ServiceObjects<S> so) {
			return (ServiceObjects<S>) Proxy.newProxyInstance(PROXY_CLASS_LOADER, new Class<?>[] {
				ServiceObjects.class, AutoCloseable.class
			}, new ClosableServiceObjects<>(so));
		}

		public ClosableServiceObjects(ServiceObjects<S> so) {
			this.so = so;
		}

		@Override
		public void close() {
			instances.forEach((service, useCount) -> {
				for (int i = useCount; i > 0; i--) {
					so.ungetService(service);
				}
			});
			instances.clear();
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			if (method.getDeclaringClass()
				.equals(AutoCloseable.class)) {
				close();
				return null;
			}
			if (method.getDeclaringClass()
				.equals(ServiceObjects.class)) {

				try {
					try {
						Method ourMethod = getClass().getMethod(method.getName(), method.getParameterTypes());

						return ourMethod.invoke(this, args);
					} catch (NoSuchMethodException t) {
						return method.invoke(so, args);
					}
				} catch (InvocationTargetException e) {
					throw Exceptions.duck(e.getCause());
				}
			}
			if (method.getDeclaringClass()
				.equals(Object.class)) {
				switch (method.getName()) {
					case "toString" :
						return delegatedToString(proxy);
					case "hashCode" :
						return so.hashCode();
					case "equals" :
						return so.equals(args[0]);
				}
			}

			throw new IllegalArgumentException();
		}

		public String delegatedToString(Object proxy) {
			return "CloseableServiceObjects[" + System.identityHashCode(proxy) + "]:" + so.toString();
		}

		@SuppressWarnings("unused")
		public S getService() {
			S service = so.getService();
			instances.merge(service, 1, (oldValue, dummy) -> oldValue + 1);
			return service;
		}

		@SuppressWarnings("unused")
		public void ungetService(S service) {
			instances.compute(service, (key, oldValue) -> {
				if (oldValue == null) {
					throw new AssertionError("Attempt to ungetService " + service
						+ " but there are no outstanding references to this object");
				}
				return oldValue == 1 ? null : oldValue - 1;
			});
			so.ungetService(service);
		}
	}
}
