package org.osgi.test.assertj.bundle;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.osgi.framework.Bundle.ACTIVE;
import static org.osgi.framework.Bundle.INSTALLED;
import static org.osgi.framework.Bundle.RESOLVED;
import static org.osgi.framework.Bundle.STARTING;
import static org.osgi.framework.Bundle.STOPPING;
import static org.osgi.framework.Bundle.UNINSTALLED;
import static org.osgi.test.common.dictionary.Dictionaries.asDictionary;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.net.URL;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.osgi.framework.Bundle;
import org.osgi.framework.Version;
import org.osgi.framework.wiring.BundleRevision;
import org.osgi.test.assertj.testutil.AbstractAssertTest;

@ExtendWith(SoftAssertionsExtension.class)
public class BundleAssertTest extends AbstractAssertTest<BundleAssert, Bundle> {

	public BundleAssertTest() {
		super(BundleAssert::assertThat);
	}

	public static final long	WAIT_TIME	= 2L;

	BundleRevision				revision;
	Map<String, String>			headers;

	@BeforeEach
	public void setUp() {
		Bundle b = mock(Bundle.class);
		revision = mock(BundleRevision.class);
		headers = new HashMap<>();
		when(b.adapt(BundleRevision.class)).thenReturn(revision);
		when(b.getHeaders()).thenReturn(asDictionary(headers));
		when(b.getVersion()).thenReturn(Version.parseVersion("1.2.3"));
		setActual(b);
	}

	@Test
	public void isFragment(SoftAssertions softly) throws Exception {
		this.softly = softly;

		assertPassing("isNot", x -> aut.isNotFragment(), null);
		assertFailing("is", x -> aut.isFragment(), null)
			.hasMessageMatching("(?si).*to be a fragment.*but it was not.*");

		when(revision.getTypes()).thenReturn(BundleRevision.TYPE_FRAGMENT);
		headers.put("Fragment-Host", "my.host");
		assertPassing("is", x -> aut.isFragment(), null);
		assertFailing("isNot", x -> aut.isNotFragment(), null)
			.hasMessageMatching("(?si).*not.*be a fragment.*but it was.*host.*my\\.host.*");
	}

	@Test
	public void hasPermission(SoftAssertions softly) throws Exception {
		this.softly = softly;
		when(actual.hasPermission(anyString())).thenReturn(true);

		assertPassing("has", aut::hasPermission, "aString");
		final Object o = new Object();
		assertFailing("has", aut::hasPermission, o)
			.hasMessageMatching("(?si).*have permission.*" + o + ".*but it did not.*");

		assertPassing("doesNotHave", aut::doesNotHavePermission, o);
		assertFailing("doesNotHave", aut::doesNotHavePermission, "aString")
			.hasMessageMatching("(?si).*not.*have permission.*aString.*but it did.*");
	}

	@Test
	public void hasBundleId(SoftAssertions softly) {
		this.softly = softly;
		when(actual.getBundleId()).thenReturn(2L);

		assertEqualityAssertion("bundle ID", aut::hasBundleId, 2L, 4L);
	}

	@Test
	public void hasLocation(SoftAssertions softly) {
		this.softly = softly;
		when(actual.getLocation()).thenReturn("file://my/path");

		assertEqualityAssertion("location", aut::hasLocation, "file://my/path", "some.other.location");

		when(actual.getLocation()).thenReturn(null);

		assertEqualityAssertion("location", aut::hasLocation, null, "some.other.location");
	}

	@Test
	public void hasLocationThat(SoftAssertions softly) {
		this.softly = softly;
		when(actual.getLocation()).thenReturn("file://my/path");

		assertChildAssertion("location", aut::hasLocationThat, actual::getLocation);
	}

	@Test
	public void hasSymbolicName(SoftAssertions softly) {
		this.softly = softly;
		when(actual.getSymbolicName()).thenReturn("a.bundle.name");

		assertEqualityAssertion("symbolic name", aut::hasSymbolicName, "a.bundle.name", "some.other.name");

		when(actual.getSymbolicName()).thenReturn(null);

		assertEqualityAssertion("symbolic name", aut::hasSymbolicName, null, "some.other.name");
	}

	@Test
	public void hasSymbolicNameThat(SoftAssertions softly) {
		this.softly = softly;
		when(actual.getSymbolicName()).thenReturn("a.bundle.name");

		assertChildAssertion("symbolic name", aut::hasSymbolicNameThat, actual::getSymbolicName);
	}

	@Test
	public void hasEntry(SoftAssertions softly) throws Exception {
		this.softly = softly;
		URL url = Paths.get("/test")
			.toUri()
			.toURL();
		when(actual.getEntry("/my/entry")).thenReturn(url);

		assertPassing("has", aut::hasEntry, "/my/entry");
		assertFailing("has", aut::hasEntry, "/my/other/entry")
			.hasMessageMatching("(?si).*have entry.*/my/other/entry.*but it did not.*");

		assertPassing("doesNotHave", aut::doesNotHaveEntry, "/my/other/entry");
		assertFailing("doesNotHave", aut::doesNotHaveEntry, "/my/entry")
			.hasMessageMatching("(?si).*not.*have entry.*/my/entry.*but it did.*" + url + ".*");
	}

	@Test
	public void hasResource(SoftAssertions softly) throws Exception {
		this.softly = softly;
		URL url = Paths.get("/test")
			.toUri()
			.toURL();
		when(actual.getResource("/my/entry")).thenReturn(url);

		assertPassing("has", aut::hasResource, "/my/entry");
		assertFailing("has", aut::hasResource, "/my/other/entry")
			.hasMessageMatching("(?si).*have resource.*/my/other/entry.*but it did not.*");

		assertPassing("doesNotHave", aut::doesNotHaveResource, "/my/other/entry");
		assertFailing("doesNotHave", aut::doesNotHaveResource, "/my/entry")
			.hasMessageMatching("(?si).*not.*have resource.*/my/entry.*but it did.*" + url + ".*");
	}

	@Test
	public void hasLastModified(SoftAssertions softly) {
		this.softly = softly;
		when(actual.getLastModified()).thenReturn(10L);

		assertPassing(aut::hasLastModified, 10L);
		assertPassing(aut::hasLastModified, new Date(10L));
		assertPassing(aut::hasLastModified, Instant.ofEpochMilli(10L));
		assertFailing(aut::hasLastModified, 11L).hasMessageMatching("(?si).*have last modified.*11.*but it was.*10.*");
		assertFailing(aut::hasLastModified, new Date(11L))
			.hasMessageMatching("(?si).*have last modified.*11.*but it was.*10.*");
		assertFailing(aut::hasLastModified, Instant.ofEpochMilli(11L))
			.hasMessageMatching("(?si).*have last modified.*11.*but it was.*10.*");
	}

	@Test
	public void hasLastModifiedLongThat(SoftAssertions softly) {
		this.softly = softly;
		when(actual.getLastModified()).thenReturn(10L);

		assertChildAssertion("last modified", aut::hasLastModifiedLongThat, actual::getLastModified);
	}

	@Test
	public void hasLastModifiedDateThat(SoftAssertions softly) {
		this.softly = softly;
		when(actual.getLastModified()).thenReturn(10L);

		assertChildAssertion("last modified", aut::hasLastModifiedDateThat, () -> new Date(actual.getLastModified()));
	}

	@Test
	public void modifiedAsserts_withNonLongDateOrInstantExpected_throwIAE(SoftAssertions softly) throws Exception {
		this.softly = softly;
		when(actual.getLastModified()).thenReturn(10L);

		assertModifiedIAE("hasLastModified", aut::hasLastModified);
	}

	private void assertModifiedIAE(String msg, Function<Object, BundleAssert> assertion) {
		softly.assertThatThrownBy(() -> assertion.apply(new Object()))
			.as(msg)
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("long")
			.hasMessageContaining("Instant")
			.hasMessageContaining("Date");
		softly.assertThatThrownBy(() -> assertion.apply(null))
			.as(msg + ":null")
			.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	public void hasVersion(SoftAssertions softly) {
		this.softly = softly;

		assertEqualityAssertion("version", aut::hasVersion, Version.valueOf("1.2.3"), Version.valueOf("2.3.4"));
		assertEqualityAssertion("version", aut::hasVersion, "1.2.3", "2.3.4");
	}

	@Test
	public void hasVersionThat(SoftAssertions softly) {
		this.softly = softly;

		assertChildAssertion("version", aut::hasVersionThat, actual::getVersion);
	}

	@Test
	public void hasVersion_withNullExpected(SoftAssertions softly) {
		this.softly = softly;

		assertEqualityAssertion("version", aut::hasVersion, Version.valueOf("1.2.3"), null);
	}

	@Test
	public void hasVersion_withNullActual(SoftAssertions softly) {
		this.softly = softly;
		when(actual.getVersion()).thenReturn(null);

		assertEqualityAssertion("version", aut::hasVersion, null, Version.valueOf("1.2.3"));
	}

	@Test
	public void versionAsserts_withNonStringOrVersionExpected_throwIAE(SoftAssertions softly) throws Exception {
		this.softly = softly;

		assertVersionIAE("hasVersion", aut::hasVersion);
	}

	private void assertVersionIAE(String msg, Function<Object, BundleAssert> assertion) {
		softly.assertThatThrownBy(() -> assertion.apply(new Object()))
			.as(msg)
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("Version")
			.hasMessageContaining("CharSequence")
			.hasMessageContaining("Object");
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"1.2.0", "0.0.1", "0.0.0", "1.2.2", "1.2.2.qualifier"
	})
	public void hasVersionGreaterThan_withPassingValues(String version, SoftAssertions softly) {
		this.softly = softly;

		softly().assertThatCode(() -> aut.hasVersionThat()
			.isGreaterThan(Version.valueOf(version)))
			.doesNotThrowAnyException();
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"1.2.3", "1.2.4", "2.0.0", "3.2.2", "1.2.3.qualifier"
	})
	public void hasVersionGreaterThan_withFailingValues(String version, SoftAssertions softly) {
		this.softly = softly;

		softly().assertThatThrownBy(() -> aut.hasVersionThat()
			.isGreaterThan(Version.valueOf(version)))
			.isInstanceOf(AssertionError.class)
			.hasMessageContaining(actual().toString())
			.hasMessageMatching("(?s).*version.*1[.]2[.]3.*greater than.*" + version + ".*");
	}

	@ParameterizedTest
	@StatesSource
	public void isInState(int passingState, SoftAssertions softly) {
		this.softly = softly;
		final int actualState = passingState | (passingState << 2);
		when(actual.getState()).thenReturn(actualState);

		int failingState = (passingState > 16) ? UNINSTALLED : passingState << 1;
		assertPassing(aut::isInState, passingState);
		assertFailing(aut::isInState, failingState).hasMessageMatching("(?si).*expect.*in state.*" + failingState + ":"
			+ stateToString(failingState) + ".*but was in state.*" + actualState + ":"
			+ Pattern.quote(stateMaskToString(actualState)) + ".*");
	}

	@ParameterizedTest
	@StatesSource
	public void isInState_withMultipleStates_throwsIAE(int expected, SoftAssertions softly) {
		when(actual.getState()).thenReturn(ACTIVE);

		final int mask = expected | (expected << 1);
		softly.assertThatThrownBy(() -> aut.isInState(mask))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageMatching("(?si).*" + mask + ".*isInStateMaskedBy.*");
	}

	public final static int UNKNOWN = 128;

	@ValueSource(ints = {
		ACTIVE, INSTALLED, RESOLVED, STARTING, STOPPING, UNINSTALLED, UNKNOWN
	})
	@Retention(RetentionPolicy.RUNTIME)
	static @interface StatesSource {}

	@ParameterizedTest
	@StatesSource
	public void isNotInState(int state, SoftAssertions softly) {
		this.softly = softly;
		when(actual.getState()).thenReturn(state);

		int passingState = state == ACTIVE ? RESOLVED : ACTIVE;
		AtomicReference<BundleAssert> retval = new AtomicReference<>();
		assertPassing(aut::isNotInState, passingState);
		assertFailing(aut::isNotInState, state)
			.hasMessageMatching("(?s).*not.* in state.*" + state + ".*" + stateToString(state) + ".*but it was.*");
	}

	@Test
	public void isInStateMaskedBy(SoftAssertions softly) {
		this.softly = softly;
		when(actual.getState()).thenReturn(ACTIVE);

		assertPassing(aut::isInStateMaskedBy, ACTIVE | INSTALLED);
		assertPassing(aut::isInStateMaskedBy, ACTIVE);
		assertPassing(aut::isInStateMaskedBy, ACTIVE | STOPPING | RESOLVED);
		assertFailing(aut::isInStateMaskedBy, INSTALLED)
			.hasMessageMatching(
				"(?si).*in one of states.*\\[2:INSTALLED\\].*but was in state.*32:ACTIVE.*");
		assertFailing(aut::isInStateMaskedBy, INSTALLED | STOPPING | RESOLVED).hasMessageMatching(
			"(?si).*in one of states.*\\Q[22:INSTALLED | RESOLVED | STOPPING]\\E.*but was in state.*32:ACTIVE.*");
		assertFailing(aut::isInStateMaskedBy, STOPPING | RESOLVED | STARTING).hasMessageMatching(
			"(?si).*in one of states.*\\Q[28:RESOLVED | STARTING | STOPPING]\\E.*but was in state.*32:ACTIVE.*");

		when(actual.getState()).thenReturn(RESOLVED);

		assertPassing(aut::isInStateMaskedBy, RESOLVED | INSTALLED);
		assertPassing(aut::isInStateMaskedBy, RESOLVED);
		assertPassing(aut::isInStateMaskedBy, ACTIVE | STOPPING | RESOLVED);
		assertFailing(aut::isInStateMaskedBy, INSTALLED)
			.hasMessageMatching("(?si).*in one of states.*\\[2:INSTALLED\\].*but was in state.*4:RESOLVED.*");
		assertFailing(aut::isInStateMaskedBy, INSTALLED | STOPPING | ACTIVE).hasMessageMatching(
			"(?si).*in one of states.*\\Q[50:INSTALLED | STOPPING | ACTIVE]\\E.*but was in state.*4:RESOLVED.*");
		assertFailing(aut::isInStateMaskedBy, STOPPING | ACTIVE | STARTING).hasMessageMatching(
			"(?si).*in one of states.*\\Q[56:STARTING | STOPPING | ACTIVE]\\E.*but was in state.*4:RESOLVED.*");
	}

	@ParameterizedTest
	@ValueSource(ints = {
		-23, 0, 64, 512
	})
	public void isInStateMask_throwsIAE_forInvalidMask(int mask, SoftAssertions softly) {
		when(actual.getState()).thenReturn(ACTIVE);

		softly.assertThatThrownBy(() -> aut.isInStateMaskedBy(mask))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining(Integer.toString(mask));
	}

	@Test
	public void isNotInStateMask(SoftAssertions softly) {
		this.softly = softly;
		when(actual.getState()).thenReturn(STARTING);

		assertPassing(aut::isNotInStateMask, ACTIVE | INSTALLED);
		assertPassing(aut::isNotInStateMask, ACTIVE);
		assertPassing(aut::isNotInStateMask, ACTIVE | STOPPING | RESOLVED);
		assertFailing(aut::isNotInStateMask, STARTING)
			.hasMessageMatching("(?si).*not.*in one of states.*\\Q[8:STARTING]\\E.*but was in state.*8:STARTING.*");
		assertFailing(aut::isNotInStateMask, INSTALLED | STARTING | STOPPING).hasMessageMatching(
			"(?si).*not.*in one of states.*\\Q[26:INSTALLED | STARTING | STOPPING]\\E.*but was in state.*8:STARTING.*");
		assertFailing(aut::isNotInStateMask, STOPPING | RESOLVED | STARTING).hasMessageMatching(
			"(?si).*not.*in one of states.*\\Q[28:RESOLVED | STARTING | STOPPING]\\E.*but was in state.*8:STARTING.*");

		when(actual.getState()).thenReturn(UNINSTALLED);

		assertPassing(aut::isNotInStateMask, RESOLVED | INSTALLED);
		assertPassing(aut::isNotInStateMask, RESOLVED);
		assertPassing(aut::isNotInStateMask, ACTIVE | STOPPING | RESOLVED);
		assertFailing(aut::isNotInStateMask, UNINSTALLED)
			.hasMessageMatching(
				"(?si).*not.*in one of states.*\\Q[1:UNINSTALLED]\\E.*but was in state.*1:UNINSTALLED.*");
		assertFailing(aut::isNotInStateMask, UNINSTALLED | STOPPING | ACTIVE).hasMessageMatching(
			"(?si).*not.*in one of states.*\\Q[49:UNINSTALLED | STOPPING | ACTIVE]\\E.*but was in state.*1:UNINSTALLED.*");
		assertFailing(aut::isNotInStateMask, STOPPING | UNINSTALLED | STARTING).hasMessageMatching(
			"(?si).*not.*in one of states.*\\Q[25:UNINSTALLED | STARTING | STOPPING]\\E.*but was in state.*1:UNINSTALLED.*");
	}

	@ParameterizedTest
	@ValueSource(ints = {
		-23, 0, 64, 512
	})
	public void isNotInStateMask_throwsIAE_forInvalidMask(int mask, SoftAssertions softly) {
		when(actual.getState()).thenReturn(ACTIVE);

		softly.assertThatThrownBy(() -> aut.isNotInStateMask(mask))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining(Integer.toString(mask));
	}

	private static final int[] STATES = {
		UNINSTALLED, INSTALLED, RESOLVED, STARTING, STOPPING, ACTIVE
	};

	private static final int	KNOWN_MASK		= UNINSTALLED | INSTALLED | RESOLVED | STARTING | STOPPING | ACTIVE;
	private static final int	UNKNOWN_MASK	= ~KNOWN_MASK;

	private static String stateMaskToString(int state) {
		Stream<String> bits = IntStream.of(STATES)
			.filter(x -> (x & state) != 0)
			.mapToObj(BundleAssertTest::stateToString);

		if ((state & UNKNOWN_MASK) != 0) {
			bits = Stream.concat(bits, Stream.of("UNKNOWN"));
		}
		return bits.collect(Collectors.joining(" | "));
	}

	private static String stateToString(int state) {
		switch (state) {
			case INSTALLED :
				return "INSTALLED";
			case RESOLVED :
				return "RESOLVED";
			case STARTING :
				return "STARTING";
			case ACTIVE :
				return "ACTIVE";
			case STOPPING :
				return "STOPPING";
			case UNINSTALLED :
				return "UNINSTALLED";
			default :
				return "UNKNOWN";
		}
	}

}
