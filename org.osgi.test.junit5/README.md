# org.osgi.test.junit5

This artifact provides support classes for OSGi testing with [JUnit 5](https://junit.org/junit5/) including JUnit 5 Extensions.

## Setup an embedded Testframework with `FrameworkExtension`

`org.osgi.test` itself assumes running inside an OSGI Framework and you can setup one 
using [BND](https://github.com/bndtools/bnd), [Tycho](https://github.com/eclipse/tycho/), [Pax Exam](https://github.com/ops4j/org.ops4j.pax.exam2) or other techniques but this often requires carefull setup 
and often mean all your tests run in the same setup or you spread them over different 
test-artifacts.

With the `FrameworkExtension` it is possible to start a so called [Connect-Framework](http://docs.osgi.org/specification/osgi.core/8.0.0/framework.connect.html) 
that is directly feed from the classpath of your test, this makes it suitable for test-cases 
where you have a small set of bundles (even though the number is not limited in any 
way), want to run different configurations or run directly from your current module 
(e.g. the usual maven setup where the test-code is next to your code and executed by 
maven-surefire as part of that build).

First you need to consider some things:

- you are responsible for setting up what makes your Framework, the good news is that 
you often do not need setup all bundles, just those required for your test
- all bundles must be on the classpath of your test, either as a jar or as a folder
- even though your test will see a full Framework and thus can register services, use 
declarative services and so on, all bundles share the same classloader. This has advantages 
(e.g. your test can easily interact with all code in the framework) but also limit 
the usage of some OSGi feature, e.g. you can't use the same bundle in different versions
- because of this, lazy activation of bundles do not work and they will always be activated 
beforehands

### Configure the framework

The framework is confugured using a builder, lets assume you only need your test and 
will setup everything else using `org.osgi.test` (e.g. installing other bundles, register 
services, see below) then you can use this and you are done.

```java
@RegisterExtension
static FrameworkExtension framework = FrameworkExtension.builder()
		.build();
```

### adding a bundle from the classpath

Often one wants additional stuff, so you can load it from the classpath of your test 
(e.g. adding it as a maven dependency):

```java
static FrameworkExtension framework = FrameworkExtension.builder()
		.withBundle("org.xerial.sqlite-jdbc")
		.build();
```

you simply pass the bundle name of the bundle and you are done.

If you like, you can even mark your bundle as beeing started:
```java
static FrameworkExtension framework = FrameworkExtension.builder()
		.withBundle("org.xerial.sqlite-jdbc", true)
		.build();
```

### Inspect the state of the framework

The extension provides some usefull methods to give you insights into the state
of your framework:

- `framework.printBundles(System.out::println)` will print an overview of all installed 
bundles and their state
- `framework.printServices(System.out::println)` will print an overview of all installed 
services
- `framework.printComponents(System.out::println)` will print an overview of all declarative 
  services components and their state
- `framework.printFrameworkState(System.out::println)` will print bundes, services 
and components
-  `framework.getFrameworkEvents` return the `FrameworkEvents` for this framework that 
could be used to inspect any `FrameworkEvent`s that occuring while starting or running 
the embedded framework.

### Export additional packages

In most cases your bundles under test will require additional packages (e.g. from APIs) 
and you can of course simply provide the API bundle as well to fullfill this requirements:

```java
static FrameworkExtension framework = FrameworkExtension.builder()
		.withBundle("org.xerial.sqlite-jdbc", true)
		.withBundle("org.osgi.service.jdbc")
		.build();
```
depending how good you shape your system (and how good shaped the libraries are you 
are using), this can become a headache as those bundles might require additional packages 
or capabilities.
As an alternative you can export arbitrary packages from your test-probe, this could 
also be used to make some things aviable from within your test-probe even though it 
is not a bundle at all:

```java
static FrameworkExtension framework = FrameworkExtension.builder()
		.withBundle("org.xerial.sqlite-jdbc", true)
		.exportPackage("org.osgi.service.jdbc", "1.0.0")
		.build();
```
 

## Testing with `BundleContext`

There are a number of operations that can be performed with or on the OSGi `BundleContext`. However, this is a 
low level API that risks exposing other tests to side effects resulting from these operations. Managing these side effects can results in large amounts of boiler plate code.

### `BundleContextExtension`

The `BundleContextExtension` is designed to help in these scenarios by giving access to an instance of the BundleContext that is context aware and results in the necessary cleanup when a test scope 
ends. The following cleanup is performed at the end of each test scope:

- bundles installed with `installBundle` are uninstalled
- services obtained from `getService` are returned
- `ServiceObjects` instances obtained from `getServiceObjects` are returned
- services registered by `registerService` are unregistered
- `BundleListener`s registered with `addBundleListener` are removed
- `FrameworkListener`s registered with `addFrameworkListener` are removed
- `ServiceListener`s registered with `addServiceListener` are removed (this includes closing `ServiceTracker`s created using the `BundleContext`)

Scope is inherited - for example, any services that are registered at the class-level 
scope (eg, in a `@BeforeAll` callback) will be visible to all tests in the class as 
well as to all tests in all nested test classes, and then cleaned up after all the class'
tests have run. On the other hand, changes made during the execution of an individual
test will only be visible for the duration of that individual test. Similarly, `@Nested`-annotated 
inner test classes will be able to see changes made in their enclosing class' `@BeforeAll` 
and `@BeforeEach` methods.

#### Declarative Registration of `BundleContextExtension`

The `BundleContextExtension` can be applied *declaratively* to a test class using the JUnit5 `@ExtendWith` annotation.

```java
@ExtendWith(BundleContextExtension.class)
public class MyTest {
    // ...
}
```

*See the JUnit5 documentation for more information on [Declarative Extension Registration](https://junit.org/junit5/docs/current/user-guide/#extensions-registration-declarative).*

#### Programmatic Registration of `BundleContextExtension`

The `BundleContextExtension` can be applied *programmatically* using a public field of the test class annotated with the JUnit5 `@RegisterExtension` annotation.

```java
@RegisterExtension
public BundleContextExtension bundleContextExtension = new BundleContextExtension();
```

*See the JUnit5 documentation for more information on [Programmatic Extension Registration](https://junit.org/junit5/docs/current/user-guide/#extensions-registration-programmatic).*

#### Obtaining `BundleContext` Instances

Now that the extension is in place, a `BundleContext` instance can be injected into a non-private,
non-final field annotated with `@InjectBundleContext`: 

```java
@InjectBundleContext
BundleContext bundleContext;
```

or from a likewise-annotated test method or lifecycle method parameter:

```java
@BeforeAll
public static void beforeAll(
    @InjectBundleContext BundleContext classScopeBundleContext) {
    // changes made here will persist until the afterAll phase completes
}

@BeforeEach
public void beforeEach(
    @InjectBundleContext BundleContext testScopeBundleContext) {
    // changes made here will persist until the afterEach phase completes
}

@Test
public void testWithBundleContext(
    @InjectBundleContext BundleContext testScopeBundleContext) {
    // changes made here will persist until the afterEach phase completes
}
```

#### Test Utility `BundleInstaller`

In OSGi testing there are many scenarios that require installing pre-built bundles. The [Bnd](https://bnd.bndtools.org/) tool has support for easily building and embedding bundles within bundles. As a matter of convenience the `BundleInstaller` utility was designed to simplify the task of finding and installing such embedded bundles. 

The `BundleInstaller` utility provides three convenience methods:

- `Bundle installBundle(String pathToEmbeddedJar)` 
- `Bundle installBundle(String pathToEmbeddedJar, boolean startBundle)` 
- `BundleContext getBundleContext()`

...to simplify these use cases. The `installBundle` methods use the `findEntries` method from the Bundle API to locate embedded bundles.

An instance of this utility can be injected into a field, test or lifecycle method (much like the `BundleContext`) 
using the `@InjectBundleInstaller`.

```java
@InjectBundleInstaller
BundleInstaller bundleInstaller;
```

or

```
@Test
public void testWithBundleInstaller(
    @InjectBundleInstaller BundleInstaller bundleInstaller) {
    // ...
}
```

## Testing with OSGi Services

Testing OSGi services can prove to be tricky business involving a lot of state management.

#### `ServiceExtension`

The `ServiceExtension` was designed to help alleviate this complexity by offering a mechanism to declare which service or set of services to use and how they should behave within the context of a test and then to clean up when the test ends.

The following criteria can be defined for any service or set of services:

- a service type for the service
- a cardinality declaring the minimum number of required services
- a filter expression to match available services
- a timeout within which a service or set of services must arrive before the test fails

#### Declarative Registration of `ServiceExtension`

The `ServiceExtension` can be applied *declaratively* to a test class using the JUnit5 `@ExtendWith` annotation.

```java
@ExtendWith(ServiceExtension.class)
public class MyTest {
    // ...
}
```

*See the JUnit5 documentation for more information on [Declarative Extension Registration](https://junit.org/junit5/docs/current/user-guide/#extensions-registration-declarative).*

#### Programmatic Registration of `ServiceExtension`

The `ServiceExtension` can be applied *programmatically* using a field of the test class annotated with the JUnit5 `@RegisterExtension` annotation.

```java
@RegisterExtension
public ServiceExtension serviceExtension = new ServiceExtension();
```

*See the JUnit5 documentation for more information on [Programmatic Extension Registration](https://junit.org/junit5/docs/current/user-guide/#extensions-registration-programmatic).*

#### Service Injection

With the extension in place, service instances can be injected into a non-private, non-static field annotated with `@InjectService`

```java
@InjectService
LogService logService;
```

or from a likewise annotated test method parameter

```java
@Test
public void testWithLogService(
    @InjectService LogService logService) {
    // ...
}
```

#### Multi-cardinality

If the type of the field/parameter is of type `java.util.List<S>` then the value will be a list of services of type `S` where `S` must not be a generic type.

```java
@InjectService
List<LogService> logServices;
```

or from a likewise annotated test method parameter

```java
@Test
public void testWithLogServices(
    @InjectService List<LogService> logServices) {
    // ...
}
```

The list of services is provided in natural order of their service references. Tests are free to manipulate the list.

### Introspection

The type `org.osgi.test.common.service.ServiceAware` provides several introspection methods related to tracking services.

If the type of the field/parameter is of type `org.osgi.test.common.service.ServiceAware<S>` then the value will be an instance of that type where `S` is the service type and `S` must not be a generic type.

```java
@InjectService
ServiceAware<LogService> lsServiceAware;
```

or from a likewise annotated test method parameter

```java
@Test
public void testWithLogServices(
    @InjectService ServiceAware<LogService> lsServiceAware) {
    // ...
}
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
@Test
public void testWithLogServices(
    @InjectService(cardinality = 2) List<LogService> logServices) {
    // ...
}
```

```java
@InjectService(cardinality = 2)
ServiceAware<LogService> lsServiceAware;
```

*The default cardinality is `1`.*

##### Cardinality Zero (0)

Setting the cardinality to `0` allows for the test to continue immediately without waiting. This usage is most interesting used in conjunction with `ServiceAware` fields/parameters which allows for a live view of the underlying tracker essentially giving a managed service tracker to use in tests. It must be noted that field or parameter types other than `ServiceAware` are very likely to be `null` or empty since potentially no value was available to populate them.

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