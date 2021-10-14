# org.osgi.test.junit5.listeners.log.osgi

This artifact provides a JUnit Jupiter `TestExecutionListener` implementation that logs test output through the OSGi Log Service. It registers a new logger for each log service that becomes available.

* Failing tests are logged as errors.
* Aborted and skipped tests are logged as warnings.
* Passing tests are logged as info messages.
* Everything else (test run start, test start, dynamic test registered, test run end) is logged as a debug message.

## Usage

These instructions are for running tests using Bnd and the JUnit Platform tester. It is theoretically possible to use the OSGiLogListener in a non-Bnd OSGi environment, but this has not been tested.

Under Bnd, you need to do the following:

1. Add the `log.osgi` bundle to the `-runrequires` of your test bnd(run) file.
2. Ensure that you are using the junit-platform tester (`-tester: biz.aQute.tester.junit-platform`).
3. Resolve your bnd(run) file.
4. Launch your tests.

The tests will run and the test output will be redirected through the OSGi Log Service.

