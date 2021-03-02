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
package org.osgi.test.junit5.bundle;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.support.AnnotationConsumer;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.test.junit5.context.BundleContextExtension;

public class BundleArgumentsProvider implements ArgumentsProvider, AnnotationConsumer<BundleSource> {

	private BundleSource bundleSource;

	@Override
	public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {

		BundleContext bundleContext = BundleContextExtension.getBundleContext(context);

		return filterBundleByAnnotation(bundleContext, bundleSource);
	}

	static Stream<Arguments> filterBundleByAnnotation(BundleContext bundleContext, BundleSource annotation)
		throws InvalidSyntaxException {

		String headerFilter = annotation.headerFilter();
		Filter filter = headerFilter.isEmpty() ? null : bundleContext.createFilter(headerFilter);
		Bundle[] bundles = bundleContext.getBundles();

		return Arrays.stream(bundles)
			.filter(Objects::nonNull)
			.filter((Bundle bundle) -> {
				if (annotation.symbolicNamePattern().length == 0) {
					return true;
				}
				return Arrays.stream(annotation.symbolicNamePattern())
					.anyMatch((symbolicNamePattern) -> bundle.getSymbolicName()
						.matches(symbolicNamePattern));
			})
			.filter((bundle) -> {
				return (bundle.getState() & annotation.stateMask()) != 0;
			})
			.filter((bundle) -> {
				if (filter == null) {
					return true;
				}
				return filter.match(bundle.getHeaders());
			})
			.map(Arguments::of);
	}

	@Override
	public void accept(BundleSource annotation) {
		this.bundleSource = annotation;
	}

}
