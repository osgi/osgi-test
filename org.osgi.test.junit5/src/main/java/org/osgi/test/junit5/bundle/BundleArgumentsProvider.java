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

import static java.util.stream.Collectors.toList;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.support.AnnotationConsumer;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.test.junit5.context.BundleContextExtension;

public class BundleArgumentsProvider implements ArgumentsProvider, AnnotationConsumer<BundleSource> {
	private BundleSource source;

	@Override
	public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
		BundleContext bundleContext = BundleContextExtension.getBundleContext(context);
		Stream<Bundle> bundles = Arrays.stream(bundleContext.getBundles())
			.filter(Objects::nonNull)
			.filter(bundle -> (bundle.getState() & source.stateMask()) != 0);
		String[] symbolicNamePatterns = source.symbolicNamePattern();
		if (symbolicNamePatterns.length > 0) {
			List<Pattern> patterns = Arrays.stream(symbolicNamePatterns)
				.map(Pattern::compile)
				.collect(toList());
			bundles = bundles.filter(bundle -> patterns.stream()
				.anyMatch(pattern -> pattern.matcher(bundle.getSymbolicName())
					.matches()));
		}
		String headerFilter = source.headerFilter();
		if (!headerFilter.isEmpty()) {
			Filter filter = bundleContext.createFilter(headerFilter);
			bundles = bundles.filter(bundle -> filter.match(bundle.getHeaders()));
		}
		return bundles.map(Arguments::of);
	}

	@Override
	public void accept(BundleSource source) {
		this.source = source;
	}
}
