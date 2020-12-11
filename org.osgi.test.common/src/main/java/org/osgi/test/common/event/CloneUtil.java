/*
 * Copyright (c) OSGi Alliance (2020). All Rights Reserved.
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
package org.osgi.test.common.event;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceReference;

public class CloneUtil {
	public static ServiceEvent clone(ServiceEvent serviceEvent) {
		return new ServiceEvent(serviceEvent.getType(), clone(serviceEvent.getServiceReference()));
	}

	public static BundleEvent clone(BundleEvent bundleEvent) {
		return new BundleEvent(bundleEvent.getType(), clone(bundleEvent.getBundle()), clone(bundleEvent.getOrigin()));
	}

	public static FrameworkEvent clone(FrameworkEvent frameworkEvent) {
		return new FrameworkEvent(frameworkEvent.getType(), clone(frameworkEvent.getBundle()),
			frameworkEvent.getThrowable());
	}

	public static ServiceReference<?> clone(ServiceReference<?> serviceReference) {
		Map<String, Object> props = new HashMap<>();
		if (serviceReference.getPropertyKeys() != null) {
			for (String key : serviceReference.getPropertyKeys()) {
				props.put(key, serviceReference.getProperty(key));
			}
		}
		Bundle bundle = clone(serviceReference.getBundle());
		Bundle[] usingBundles = serviceReference.getUsingBundles() == null ? null
			: Stream.of(serviceReference.getUsingBundles())
				.map(b -> clone(b))
				.toArray(Bundle[]::new);
		ServiceReference<?> serviceReferenceProxyInstance = (ServiceReference<?>) Proxy
			.newProxyInstance(ServiceReference.class.getClassLoader(), new Class[] {
				ServiceReference.class
			}, new InvocationHandler() {
				@Override
				public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
					if (method.getName()
						.equals("getProperty")) {
						return props.get(args[0]);
					}
					if (method.getName()
						.equals("getPropertyKeys")) {
						return props.keySet()
							.stream()
							.toArray(String[]::new);
					}
					if (method.getName()
						.equals("getBundle")) {
						return bundle;
					}
					if (method.getName()
						.equals("getUsingBundles")) {
						return usingBundles;
					}
					return method.invoke(proxy, args);
				}
			});
		return serviceReferenceProxyInstance;
	}

	public static Bundle clone(Bundle bundle) {
		if (bundle == null) {
			return null;
		}
		int state = bundle.getState();
		long lastModified = bundle.getLastModified();
		Bundle bundleProxyInstance = (Bundle) Proxy.newProxyInstance(Bundle.class.getClassLoader(), new Class[] {
			Bundle.class
		}, new InvocationHandler() {
			@Override
			public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
				if (method.getName()
					.equals("getState")) {
					return state;
				}
				if (method.getName()
					.equals("getLastModified")) {
					return lastModified;
				}
				return method.invoke(proxy, args);
			}
		});
		return bundleProxyInstance;
	}
}
