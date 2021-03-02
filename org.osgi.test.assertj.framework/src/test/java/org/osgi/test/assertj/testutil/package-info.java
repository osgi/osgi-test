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

// It is not strictly necessary to export this package; however...
// It is not necessary for the test classes to be exported as the
// tester will find them through the Test-Cases header; however as we have placed them
// in the same package as the classes-under-test, and these packages are exported
// by our host bundle, it means that our test classes are in fact exported.
// Because the test classes inherit from classes in this package, bnd will generate
// a warning about the fact that this package is private. There is
// not much harm in exporting the package so we do so just to silence the bnd warning.
@org.osgi.annotation.bundle.Export
@org.osgi.annotation.versioning.Version("1.0.0")
package org.osgi.test.assertj.testutil;
