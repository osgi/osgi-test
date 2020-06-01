# OSGi Testing Support

Testing support for OSGi

## Purpose

This project provides a set of bundles which contain useful and helpful classes for testing of OSGi API.

### [org.osgi.test.common][1]

This artifact includes common utility classes which are useful in all testing scenarios.

### [org.osgi.test.assertj][2]

This artifact provides support classes for OSGi testing with [AssertJ](https://github.com/joel-costigliola/assertj-core) including custom assertions.

### [org.osgi.test.junit4][3]

This artifact provides support classes for OSGi testing with [JUnit 4](https://junit.org/junit4/) including JUnit 4 Rules.

### [org.osgi.test.junit5][4]

This artifact provides support classes for OSGi testing with [JUnit 5](https://junit.org/junit5/) including JUnit 5 Extensions.

## Building

We use Maven to build and the repo includes `mvnw`.
You can use your system `mvn` but we require a recent version.

- `./mvnw clean install` - Assembles and tests the project

[![Build Status](https://github.com/osgi/osgi-test/workflows/CI%20Build/badge.svg)](https://github.com/osgi/osgi-test/actions?query=workflow%3A%22CI%20Build%22)

## Code Coverage

We use JaCoCo to collect code coverage statistics. Thes are uploaded to [Codecov](https://codecov.io/github/osgi/osgi-test) or you can view the results by running `mvnw verify`
on your local machine.

[![Coverage Status](https://img.shields.io/codecov/c/github/osgi/osgi-test.svg)](https://codecov.io/github/osgi/osgi-test)



## Repository

Currently OSGi Testing artifacts are available as snapshots from the Sonatype OSS repository:

[https://oss.sonatype.org/content/repositories/snapshots/](https://oss.sonatype.org/content/repositories/snapshots/)

## Future work

See the [open issues](https://github.com/osgi/osgi-test/issues) for the list of outstanding TODOs.

## License

This program and the accompanying materials are made available under the terms of the Apache License, Version 2.0 which is available at <https://www.apache.org/licenses/LICENSE-2.0>.

## Contributing

Want to hack? There are [instructions](CONTRIBUTING.md) to get you
started.

They are probably not perfect, please let us know if anything feels
wrong or incomplete.

## Acknowledgments

This project uses the [Bnd Maven Plugins](https://github.com/bndtools/bnd) to build.

[1]: org.osgi.test.common/README.md
[2]: org.osgi.test.assertj/README.md
[3]: org.osgi.test.junit4/README.md
[4]: org.osgi.test.junit5/README.md
