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

package org.osgi.test.assertj.feature;

import static org.assertj.core.api.Assertions.not;

import java.util.Objects;

import org.assertj.core.api.Condition;
import org.osgi.util.feature.Feature;
import org.osgi.util.feature.FeatureArtifact;
import org.osgi.util.feature.FeatureBundle;
import org.osgi.util.feature.FeatureConfiguration;
import org.osgi.util.feature.FeatureExtension;
import org.osgi.util.feature.ID;

public interface FeaturesConditions {

	interface FeatureArtifactConditions {

		static Condition<FeatureArtifact> iDNull() {
			return new Condition<>(f -> f.getID() == null, "ID <null>");
		}

	}

	interface FeatureBundleConditions {

		static Condition<FeatureBundle> metadataEmpty() {
			return new Condition<>(f -> f.getMetadata()
				.isEmpty(), "metadata empty");
		}

		static Condition<FeatureBundle> metadataNull() {
			return new Condition<>(f -> f.getMetadata() == null, "metadata <null>");
		}

	}

	interface FeatureConditions extends FeatureArtifactConditions {

		static Condition<Feature> complete() {
			return new Condition<Feature>(Feature::isComplete, "complete");
		}

		static Condition<Feature> description(String description) {
			return new Condition<Feature>(f -> Objects.equals(f.getDescription(), description), "description <%s>",
				description);
		}

		static Condition<Feature> descriptionMatches(String pattern) {
			return new Condition<Feature>(f -> Objects.nonNull(f.getDescription()) && f.getDescription()
				.matches(pattern), "description match <%s>", pattern);
		}

		static Condition<Feature> descriptionNull() {
			return new Condition<>(f -> f.getDescription() == null, "description <null>");
		}

		static Condition<Feature> license(String license) {
			return new Condition<Feature>(f -> Objects.equals(f.getLicense(), license), "license <%s>", license);
		}

		static Condition<Feature> licenseMatches(String pattern) {
			return new Condition<Feature>(f -> Objects.nonNull(f.getLicense()) && f.getLicense()
				.matches(pattern), "license match <%s>", pattern);
		}

		static Condition<Feature> licenseNull() {
			return new Condition<>(f -> f.getLicense() == null, "license <null>");
		}

		static Condition<Feature> name(String name) {
			return new Condition<Feature>(f -> Objects.equals(f.getName(), name), "name <%s>", name);
		}

		static Condition<Feature> nameMatches(String pattern) {
			return new Condition<Feature>(f -> Objects.nonNull(f.getName()) && f.getName()
				.matches(pattern), "name match <%s>", pattern);
		}

		static Condition<Feature> nameNull() {
			return new Condition<>(f -> f.getName() == null, "name <null>");
		}

		static Condition<Feature> notComplete() {
			return not(complete()).describedAs("not complete");
		}

		static Condition<Feature> vendor(String vendor) {
			return new Condition<Feature>(f -> Objects.equals(f.getVendor(), vendor), "vendor <%s>", vendor);
		}

		static Condition<Feature> vendorMatches(String pattern) {
			return new Condition<Feature>(f -> Objects.nonNull(f.getVendor()) && f.getVendor()
				.matches(pattern), "vendor match <%s>", pattern);
		}

		static Condition<Feature> vendorNull() {
			return new Condition<>(f -> f.getVendor() == null, "vendor <null>");
		}

		//
		// static Condition<Feature> categoriesNull() {
		// return new Condition<>(f -> f.getCategories() ==
		// null, "categories must be null");
		// }
		//
		// static Condition<Feature> categories(String categories) {
		// return new Condition<Feature>(f ->
		// f.getCategories()
		// ,categories), "categories must be %s", categories);
		// }
		//
		// static Condition<Feature> categoriesMatches(String pattern) {
		// return new Condition<Feature>(f ->
		// f.getCategories()
		// .matches(pattern), "categories must match %s", pattern);
		// }

		//
		// static Condition<Feature> copyrightNull() {
		// return new Condition<>(f -> f.getCopyright() ==
		// null, "copyright must be null");
		// }
		//
		// static Condition<Feature> copyright(String copyright) {
		// return new Condition<Feature>(f ->
		// f.getCopyright()
		// ,copyright), "copyright must be %s", copyright);
		// }
		//
		// static Condition<Feature> copyrightMatches(String pattern) {
		// return new Condition<Feature>(f ->
		// f.getCopyright()
		// .matches(pattern), "copyright must match %s", pattern);
		// }

		//
		// static Condition<Feature> docURLNull() {
		// return new Condition<>(f -> f.getFocURL() == null, "docURL must be
		// null");
		// }
		//
		// static Condition<Feature> docURL(String docURL) {
		// return new Condition<Feature>(f -> f.getDocURL()
		// ,docURL), "docURL must be %s", docURL);
		// }
		//
		// static Condition<Feature> docURLMatches(String pattern) {
		// return new Condition<Feature>(f -> f.getDocURL()
		// .matches(pattern), "docURL must match %s", pattern);
		// }

		//
		// static Condition<Feature> scmNull() {
		// return new Condition<>(f -> f.getSCM() == null,
		// "scm must be null");
		// }
		//
		// static Condition<Feature> scm(String scm) {
		// return new Condition<Feature>(f -> f.getSCM()
		// ,scm), "scm must be %s", scm);
		// }
		//
		// static Condition<Feature> scmMatches(String pattern) {
		// return new Condition<Feature>(f -> f.getSCM()
		// .matches(pattern), "scm must match %s", pattern);
		// }
	}

	interface FeatureConfigurationConditions {

		static Condition<FeatureConfiguration> factoryConfiguration() {

			return new Condition<FeatureConfiguration>(f -> Objects.nonNull(f.getFactoryPid()), "factoryConfiguration");
		}

		static Condition<FeatureConfiguration> factoryPid(String text) {
			return new Condition<FeatureConfiguration>(f -> Objects.equals(f.getFactoryPid(), text), "factoryPid <%s>",
				text);
		}

		static Condition<FeatureConfiguration> pid(String text) {

			return new Condition<FeatureConfiguration>(f -> Objects.equals(f.getPid(), text), "pid <%s>", text);
		}
	}

	interface FeatureExtensionConditions {

		static Condition<FeatureExtension> json(String json) {
			return new Condition<FeatureExtension>(f -> Objects.equals(f.getJSON(), json), "json <%s>", json);
		}

		static Condition<FeatureExtension> jsonMatches(String pattern) {
			return new Condition<FeatureExtension>(f -> Objects.nonNull(f.getJSON()) && f.getJSON()
				.matches(pattern), "json match <%s>", pattern);
		}

		static Condition<FeatureExtension> jsonNull() {
			return new Condition<>(f -> f.getJSON() == null, "json <null>");
		}

		static Condition<FeatureExtension> kind(final FeatureExtension.Kind kind) {
			return new Condition<>(f -> Objects.equals(f.getKind(), kind), "kind <%s>", kind);
		}

		static Condition<FeatureExtension> kindMandantory() {
			return kind(FeatureExtension.Kind.MANDATORY);
		}

		static Condition<FeatureExtension> kindNull() {
			return kind(null);
		}

		static Condition<FeatureExtension> kindOptional() {
			return kind(FeatureExtension.Kind.OPTIONAL);
		}

		static Condition<FeatureExtension> kindTransient() {
			return kind(FeatureExtension.Kind.TRANSIENT);
		}

		static Condition<FeatureExtension> name(String name) {
			return new Condition<FeatureExtension>(f -> Objects.equals(f.getName(), name), "name <%s>", name);
		}

		static Condition<FeatureExtension> nameMatches(String pattern) {
			return new Condition<FeatureExtension>(f -> Objects.nonNull(f.getName()) && f.getName()
				.matches(pattern), "name match <%s>", pattern);
		}

		static Condition<FeatureExtension> nameNull() {
			return new Condition<>(f -> f.getName() == null, "name <null>");
		}

		static Condition<FeatureExtension> text(String text) {
			return new Condition<FeatureExtension>(f -> Objects.equals(f.getText(), text), "text <%s>", text);
		}

		static Condition<FeatureExtension> textMatches(String pattern) {
			return new Condition<FeatureExtension>(f -> Objects.nonNull(f.getText()) && f.getText()
				.matches(pattern), "text match <%s>", pattern);
		}

		static Condition<FeatureExtension> textNull() {
			return new Condition<>(f -> f.getText() == null, "json <null>");
		}

		static Condition<FeatureExtension> type(final FeatureExtension.Type type) {
			return kind(null);
		}

		static Condition<FeatureExtension> typeArtifacts() {
			return type(FeatureExtension.Type.ARTIFACTS);
		}

		static Condition<FeatureExtension> typeJson() {
			return type(FeatureExtension.Type.JSON);
		}

		static Condition<FeatureExtension> typeNull() {
			return type(null);
		}

		static Condition<FeatureExtension> typeText() {
			return type(FeatureExtension.Type.TEXT);
		}

	}

	interface IDConditions {
		static Condition<ID> artifactId(String artifactId) {
			return new Condition<ID>(f -> Objects.equals(f.getArtifactId(), artifactId), "artifactId <%s>", artifactId);
		}

		static Condition<ID> classifier(String classifier) {
			return new Condition<ID>(f -> Objects.equals(f.getClassifier(), classifier), "classifier <%s>", classifier);
		}

		static Condition<ID> groupId(String groupId) {
			return new Condition<ID>(f -> Objects.equals(f.getGroupId(), groupId), "groupId <%s>", groupId);
		}

		static Condition<ID> type(String type) {
			return new Condition<ID>(f -> Objects.equals(f.getType(), type), "type <%s>", type);
		}

		static Condition<ID> version(String version) {
			return new Condition<ID>(f -> Objects.equals(f.getVersion(), version), "version <%s>", version);
		}
	}

}
