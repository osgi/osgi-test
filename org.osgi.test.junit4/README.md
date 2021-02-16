# org.osgi.test.junit4

This artifact provides support classes for OSGi testing with [JUnit 4](https://junit.org/junit4/) including JUnit 4 Rules.

## Testing with `BundleContext`

There are a number of operations that can be performed with or on the OSGi `BundleContext`. However, this is low level API risk exposing tests to side effects resulting from these operations. Managing these side effects can results in large amounts of boiler plate code.

### `BundleContextRule`

The `BundleContextRule` is designed to help in these scenarios by giving access to an instance of the BundleContext that is context aware and results in the necessary cleanup when a test ends. The following cleanup is performed at the end of each test:

- bundles installed with `installBundle` are uninstalled
- services obtained from `getService` are returned
- `ServiceObjects` instances obtained from `getServiceObjects` are returned
- services registered by `registerService` are unregistered
- `BundleListener`s registered with `addBundleListener` are removed
- `FrameworkListener`s registered with `addFrameworkListener` are removed
- `ServiceListener`s registered with `addServiceListener` are removed (this includes closing `ServiceTracker`s created using the `BundleContext`)

#### Registration of `BundleContextRule`

The `BundleContextRule` is applied *programmatically* using a public field of the test class annotated with the JUnit4 `@Rule` annotation.

```java
@Rule
public BundleContextRule BundleContextRule = new BundleContextRule();
```

#### Obtaining `BundleContext` Instances

Now that the rule is in place, a `BundleContext` instance can be injected into a non-private, non-static field annotated with `@InjectBundleContext` 

```java
@InjectBundleContext
BundleContext bundleContext;
```

#### Test Utility `BundleInstaller`

In OSGi testing there are many scenarios that require installing pre-built bundles. The [Bnd](https://bnd.bndtools.org/) tool has support for easily building and embedding bundles within bundles. As a matter of convenience the `BundleInstaller` utility was designed to simplify the task of finding and installing such embedded bundles. 

The `BundleInstaller` utility provides three convenience methods 

- `Bundle installBundle(String pathToEmbeddedJar)` 
- `Bundle installBundle(String pathToEmbeddedJar, boolean startBundle)` 
- `BundleContext getBundleContext()`

to simplify these use cases. The `installBundle` methods use the `findEntries` method from the Bundle API to locate embedded bundles.

An instance of this utility can be injected much like the `BundleContext` using the `@InjectBundleInstaller`.

```java
@InjectBundleInstaller
BundleInstaller bundleInstaller;
```

## Testing with OSGi Services

Testing OSGi services can prove to be tricky business involving a lot of state management.

#### `ServiceRule`

The `ServiceRule` was designed to help alleviate this complexity by offering a mechanism to declare which service or set of services to use and how they should behave within the context of a test and then to clean up when the test ends.

The following criteria can be defined for any service or set of services:

- a service type for the service
- a cardinality declaring the minimum number of required services
- a filter expression to match available services
- a timeout within which a service or set of services must arrive before the test fails

#### Registration of `ServiceRule`

The `ServiceRule` can be applied *programmatically* using a public field of the test class annotated with the JUnit4 `@Rule` annotation.

```java
@Rule
public ServiceRule serviceRule = new ServiceRule();
```

#### Service Injection

With the rule in place, service instances can be injected into a non-private, non-static field annotated with `@InjectService`

```java
@InjectService
LogService logService;
```

#### Multi-cardinality

If the type of the field is of type `java.util.List<S>` then the value will be a list of services of type `S` where `S`  must not be a generic type.

```java
@InjectService
List<LogService> logServices;
```

The list of services is provided in natural order of their service references. Tests are free to manipulate the list.

### Introspection

The type `org.osgi.test.common.service.ServiceAware` provides several introspection methods related to tracking services.

If the type of the field is of type `org.osgi.test.common.service.ServiceAware<S>` then the value will be an instance of that type where `S` is the service type and `S` must not be a generic type.

```java
@InjectService
ServiceAware<LogService> lsServiceAware;
```

The instance is _live_ and will reflect the state of tracked services and the underlying service tracker. This allows for cases having zero cardinality and/or to dynamically register services in the test itself and observe them with the `ServiceAware` instance.

#### Service Cardinality

The cardinality can be specified to indicate a minimum number of expected services using the `@InjectService.cardinality` property:

```java
@InjectService(cardinality = 2)
// Gets the first service in ranking order (although it still waits for 2)
LogService logService;
```

```java
@InjectService(cardinality = 2)
// The more likely usage
List<LogService> logService;
```

```java
@InjectService(cardinality = 2)
ServiceAware<LogService> lsServiceAware;
```

*The default cardinality is `1`.*

##### Cardinality Zero (0)

Setting the cardinality to `0` allows for the test to continue immediately without waiting. This usage is most interesting used in conjunction with `ServiceAware` fields which allows for a live view of the underlying tracker essentially giving a managed service tracker to use in tests. It must be noted that field types other than `ServiceAware` are very likely to be `null` or empty since potentially no value was available to populate them.

```java
@InjectBundleContext
BundleContext bundleContext;
@InjectService(cardinality = 0)
ServiceAware<LogService> lsServiceAware;

@Test
public void testWithLogServices() {
    bundleContext.registerService(LogService.class, new MyLogService(), null);
    assertFalse(lsServiceAware.isEmpty());
}
```

#### Service Filter

Services can be filtered by declaring a filter expression using the `@InjectService.filter` property.

```java
@InjectService(filter = "(service.vendor=Acme Inc.)")
LogService	logService;
```

As a matter of convenience the `@InjectService.filterArguments` property can be used to provide format arguments where the `@InjectService.filter` property uses format syntax.

```java
@InjectService(
  filter = "(%s=%s)",
  filterArguments = {
    Constants.SERVICE_VENDOR,
		MyConstants.COMPANY_NAME
  }
)
List<LogService>	logServices;
```

#### Service Timeout

A timeout can be applied in order to constrain the length of time the extension will wait before declaring a test failure. The timeout can be specified using the `@InjectService.timeout` property.

```java
@InjectService(timeout = 1000)
LogService	logService;
```

The timeout will be cut short and the test will proceed when all other constraints of the `@InjectService` are satisfied. As such, if all constraints can be immediately satisfied no waiting will occur.

*The default timeout is `200` milliseconds.*