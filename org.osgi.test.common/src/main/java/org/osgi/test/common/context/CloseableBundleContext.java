package org.osgi.test.common.context;
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

import java.io.InputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.Dictionary;
import java.util.IdentityHashMap;
import java.util.Set;

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

public class CloseableBundleContext implements AutoCloseable, InvocationHandler {

	private final BundleContext					bundleContext;
	private final Class<?>						host;
	private final Set<ServiceRegistration<?>>	regs		= Collections
		.synchronizedSet(Collections.newSetFromMap(new IdentityHashMap<>()));
	private final Set<FrameworkListener>		fwListeners	= Collections
		.synchronizedSet(Collections.newSetFromMap(new IdentityHashMap<>()));
	private final Set<ServiceListener>			sListeners	= Collections
		.synchronizedSet(Collections.newSetFromMap(new IdentityHashMap<>()));
	private final Set<BundleListener>			bListeners	= Collections
		.synchronizedSet(Collections.newSetFromMap(new IdentityHashMap<>()));
	private final Set<Bundle>					bundles		= Collections
		.synchronizedSet(Collections.newSetFromMap(new IdentityHashMap<>()));
	private final Set<ServiceReference<?>>		services	= Collections
		.synchronizedSet(Collections.newSetFromMap(new IdentityHashMap<>()));
	private final Set<ServiceObjects<?>>		serviceobjects	= Collections
		.synchronizedSet(Collections.newSetFromMap(new IdentityHashMap<>()));

	public static BundleContext proxy(Class<?> host, BundleContext bundleContext) {
		return (BundleContext) Proxy.newProxyInstance(host.getClassLoader(), new Class<?>[] {
			BundleContext.class, AutoCloseable.class
		}, new CloseableBundleContext(host, bundleContext));
	}

	public CloseableBundleContext(Class<?> host, BundleContext bundleContext) {
		this.host = host;
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

	@Override
	public void close() {
		bundles.stream()
			.filter(this::installed)
			.forEach(this::uninstall);
		services.forEach(this::ungetService);
		serviceobjects.stream()
			.map(AutoCloseable.class::cast)
			.forEach(this::autoclose);
		regs.forEach(ServiceRegistration::unregister);
		bListeners.forEach(bundleContext::removeBundleListener);
		sListeners.forEach(bundleContext::removeServiceListener);
		fwListeners.forEach(bundleContext::removeFrameworkListener);
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
		services.add(reference);
		return service;
	}

	public <S> ServiceObjects<S> getServiceObjects(ServiceReference<S> reference) {
		final ServiceObjects<S> so = bundleContext.getServiceObjects(reference);
		ServiceObjects<S> serviceObjects = ClosableServiceObjects.proxy(host, so);
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

	private void autoclose(AutoCloseable autoCloseable) {
		try {
			autoCloseable.close();
		} catch (Exception be) {
			throwsUnchecked(be);
		}
	}

	private boolean installed(Bundle bundle) {
		return (bundle.getState() & Bundle.UNINSTALLED) != Bundle.UNINSTALLED;
	}

	private void uninstall(Bundle bundle) {
		try {
			bundle.uninstall();
		} catch (BundleException be) {
			throwsUnchecked(be);
		}
	}

	private void ungetService(ServiceReference<?> reference) {
		while (bundleContext.ungetService(reference)) {}
	}

	@SuppressWarnings("unchecked")
	private static <E extends Throwable> void throwsUnchecked(Throwable throwable) throws E {
		throw (E) throwable;
	}

	private static class ClosableServiceObjects<S> implements AutoCloseable, InvocationHandler {
		private final ServiceObjects<S>			so;
		private final Set<S>			instances	= Collections
			.synchronizedSet(Collections.newSetFromMap(new IdentityHashMap<>()));

		@SuppressWarnings("unchecked")
		public static <S> ServiceObjects<S> proxy(Class<?> host, ServiceObjects<S> so) {
			return (ServiceObjects<S>) Proxy.newProxyInstance(host.getClassLoader(), new Class<?>[] {
				ServiceObjects.class, AutoCloseable.class
			}, new ClosableServiceObjects<>(so));
		}

		public ClosableServiceObjects(ServiceObjects<S> so) {
			this.so = so;
		}

		@Override
		public void close() {
			instances.forEach(so::ungetService);
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
					Method ourMethod = getClass().getMethod(method.getName(), method.getParameterTypes());

					return ourMethod.invoke(this, args);
				} catch (NoSuchMethodException t) {
					return method.invoke(so, args);
				}
			}

			throw new IllegalArgumentException();
		}

		@SuppressWarnings("unused")
		public S getService() {
			S service = so.getService();
			instances.add(service);
			return service;
		}

		@SuppressWarnings("unused")
		public void ungetService(S service) {
			instances.remove(service);
			so.ungetService(service);
		}

	}

}
