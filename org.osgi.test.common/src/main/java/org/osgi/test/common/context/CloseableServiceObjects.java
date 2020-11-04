package org.osgi.test.common.context;

import static java.util.stream.Collectors.toMap;
import static org.osgi.test.common.context.CloseableBundleContext.PROXY_CLASS_LOADER;
import static org.osgi.test.common.context.CloseableBundleContext.invoker;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.osgi.framework.ServiceObjects;
import org.osgi.test.common.exceptions.Exceptions;

public class CloseableServiceObjects<S> implements AutoCloseable, InvocationHandler {
	private static final Map<Method, BiFunction<Object, Object[], Object>> methods;
	static {
		methods = Arrays.stream(ServiceObjects.class.getMethods())
			.collect(toMap(Function.identity(), method -> {
				try {
					return invoker(
						CloseableServiceObjects.class.getMethod(method.getName(), method.getParameterTypes()),
						CloseableServiceObjects::closeableServiceObjects);
				} catch (NoSuchMethodException e) {
					return invoker(method, CloseableServiceObjects::realServiceObjects);
				}
			}));
		try {
			methods.put(AutoCloseable.class.getMethod("close"), CloseableServiceObjects::delegatedClose);
			methods.put(Object.class.getMethod("toString"), CloseableServiceObjects::delegatedToString);
			methods.put(Object.class.getMethod("hashCode"), CloseableServiceObjects::delegatedHashCode);
			methods.put(Object.class.getMethod("equals", Object.class),
				CloseableServiceObjects::delegatedEquals);
		} catch (NoSuchMethodException e) {
			throw Exceptions.duck(e);
		}
	}

	private final ServiceObjects<S>	serviceObjects;
	private final Map<S, Integer>	instances	= Collections.synchronizedMap(new IdentityHashMap<>());

	@SuppressWarnings("unchecked")
	public static <S> ServiceObjects<S> proxy(ServiceObjects<S> serviceObjects) {
		return (ServiceObjects<S>) Proxy.newProxyInstance(PROXY_CLASS_LOADER, new Class<?>[] {
			ServiceObjects.class, AutoCloseable.class
		}, new CloseableServiceObjects<>(serviceObjects));
	}

	public CloseableServiceObjects(ServiceObjects<S> serviceObjects) {
		this.serviceObjects = serviceObjects;
	}

	@Override
	public void close() {
		instances.forEach((service, useCount) -> {
			for (int i = useCount; i > 0; i--) {
				serviceObjects.ungetService(service);
			}
		});
		instances.clear();
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		BiFunction<Object, Object[], Object> invoker = methods.get(method);
		if (invoker == null) {
			throw new IllegalArgumentException();
		}
		return invoker.apply(proxy, args);
	}

	private static CloseableServiceObjects<?> closeableServiceObjects(Object proxy) {
		InvocationHandler invocationHandler;
		try {
			invocationHandler = Proxy.getInvocationHandler(proxy);
		} catch (IllegalArgumentException e) {
			return null;
		}
		if (invocationHandler instanceof CloseableServiceObjects) {
			return (CloseableServiceObjects<?>) invocationHandler;
		}
		return null;
	}

	private static ServiceObjects<?> realServiceObjects(Object proxy) {
		CloseableServiceObjects<?> closeableServiceObjects = closeableServiceObjects(proxy);
		if (closeableServiceObjects == null) {
			return null;
		}
		ServiceObjects<?> real = closeableServiceObjects.serviceObjects;
		while ((closeableServiceObjects = closeableServiceObjects(real)) != null) {
			real = closeableServiceObjects.serviceObjects;
		}
		return real;
	}

	private static Void delegatedClose(Object proxy, Object[] args) {
		closeableServiceObjects(proxy).close();
		return null;
	}

	private static String delegatedToString(Object proxy, Object[] args) {
		return "CloseableServiceObjects[" + System.identityHashCode(proxy) + "]:"
			+ realServiceObjects(proxy).toString();
	}

	private static int delegatedHashCode(Object proxy, Object[] args) {
		return realServiceObjects(proxy).hashCode();
	}

	private static boolean delegatedEquals(Object proxy, Object[] args) {
		ServiceObjects<?> serviceObjects = realServiceObjects(proxy);
		ServiceObjects<?> real = realServiceObjects(args[0]);
		if (real != null) {
			return serviceObjects.equals(real);
		}
		return serviceObjects.equals(args[0]);
	}

	@SuppressWarnings("unused")
	public S getService() {
		S service = serviceObjects.getService();
		instances.merge(service, 1, (oldValue, dummy) -> oldValue + 1);
		return service;
	}

	@SuppressWarnings("unused")
	public void ungetService(S service) {
		instances.compute(service, (key, oldValue) -> {
			if (oldValue == null) {
				throw new AssertionError(
					"Attempt to ungetService " + service + " but there are no outstanding references to this object");
			}
			return oldValue == 1 ? null : oldValue - 1;
		});
		serviceObjects.ungetService(service);
	}
}
