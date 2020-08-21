/*
 * Copyright (c) OSGi Alliance (2020). All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.osgi.test.junit5.cm;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Array;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.test.common.annotation.config.ConfigEntry;
import org.osgi.test.common.annotation.config.ConfigEntry.Scalar;
import org.osgi.test.common.annotation.config.ConfigEntry.Type;
import org.osgi.test.common.annotation.config.WithConfiguration;

public class ConfigUtil {

	public static boolean isDictionaryWithNotSetMarker(Dictionary<String, Object> dictionary) {
		if (dictionary.size() == 1) {
			if (dictionary.keys()
				.nextElement()
				.equals(WithConfiguration.NOT_SET)) {
				return true;
			}
		}
		return false;
	}

	public static Configuration getConfigsByServicePid(ConfigurationAdmin ca, String pid) throws Exception {
		return getConfigsByServicePid(ca, pid, 0l);
	}

	public static Configuration getConfigsByServicePid(ConfigurationAdmin ca, String pid, long timeout)
		throws Exception {
		return getConfigsByPid(ca, Constants.SERVICE_PID, pid, timeout);
	}

	private static Configuration getConfigsByPid(ConfigurationAdmin ca, String pid_key, String value, long timeout)
		throws Exception {
		String filter = String.format("(%s=%s)", pid_key, value);
		Configuration[] configurations = null;
		final Instant endTime = Instant.now()
			.plusMillis(timeout);
		do {

			configurations = ca.listConfigurations(filter);
			Thread.sleep(50l);
		} while (configurations == null && !endTime.isBefore(Instant.now()));

		if (configurations == null || configurations.length == 0) {
			return null;
		} else {
			return configurations[0];
		}
	}

	static List<Configuration> getAllConfigurations(ConfigurationAdmin ca) throws IOException, InvalidSyntaxException {

		Configuration[] cs = ca.listConfigurations(null);
		if (cs == null) {
			return Collections.emptyList();
		}

		return Stream.of(cs)
			.collect(Collectors.toList());
	}

	static List<ConfigurationCopy> cloneConfigurations(List<Configuration> configurations) {
		return configurations.stream()
			.map((c) -> {
				return ConfigurationCopy.of(c);
			})
			.collect(Collectors.toList());

	}

	static void resetConfig(BlockingConfigurationHandler timeoutListener, ConfigurationAdmin ca,
		List<ConfigurationCopy> copys) throws Exception {

		List<ConfigurationCopy> leftOvers = new ArrayList<ConfigurationCopy>(copys);
		List<Configuration> configurations = ConfigUtil.getAllConfigurations(ca);

		configurations.stream()
			.forEach((conf) -> {
				boolean match = copys.stream()
					.anyMatch((copy) -> {
						if (Objects.equals(conf.getPid(), copy.getPid())) {
							try {
								timeoutListener.update(conf, copy.getProperties(), 3000);
							} catch (IOException e) {
								throw new UncheckedIOException(e);
							} catch (InterruptedException e) {
								throw new RuntimeException(e);
							}
							leftOvers.remove(copy);
							return true;
						} else {
							return false;
						}
					});
				try {
					if (!match) {
						String pid = conf.getPid();
						timeoutListener.delete(conf, 3000);

					}
				} catch (IOException e) {
					throw new UncheckedIOException(e);
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			});
		leftOvers.stream()
			.forEach((copy) -> {
				try {
					Configuration conf = null;
					if (copy.getFactoryPid() != null) {
						String name = copy.getPid()
							.substring(copy.getFactoryPid()
								.length() + 1);
						conf = ca.getFactoryConfiguration(copy.getFactoryPid(), name, copy.getBundleLocation());
					} else {
						conf = ca.getConfiguration(copy.getPid(), copy.getBundleLocation());
					}

					try {
						timeoutListener.update(conf, copy.getProperties(), 3000);

					} catch (IOException e) {
						throw new UncheckedIOException(e);
					} catch (InterruptedException e) {
						throw new RuntimeException(e);
					}

				} catch (IOException e) {
					throw new UncheckedIOException(e);
				}
			});

	}

	public static <K, V> Dictionary<K, V> copy(Dictionary<K, V> dictionary) {
		Hashtable<K, V> copy = new Hashtable<>();
		Enumeration<K> keys = dictionary.keys();
		while (keys.hasMoreElements()) {
			K key = keys.nextElement();
			copy.put(key, dictionary.get(key));
		}
		return copy;
	}

	public static Dictionary<String, Object> of(ConfigEntry[] entrys) {
		Hashtable<String, Object> dictionary = new Hashtable<>();
		for (ConfigEntry entry : entrys) {
			dictionary.put(entry.key(), toValue(entry));
		}
		return dictionary;
	}

	private static Object toValue(ConfigEntry entry) {

		boolean primitive = entry.type()
			.equals(Type.PrimitiveArray);
		Object array = createArray(entry.scalar(), primitive, entry.value().length);
		int i = 0;
		for (String v : entry.value()) {
			Object val = null;

			if (v != null) {
				switch (entry.scalar()) {
					case Boolean :
						Boolean booleanValue = Boolean.valueOf(v);
						val = primitive ? booleanValue.booleanValue() : booleanValue;
						break;

					case Byte :
						Byte byteVal = Byte.valueOf(v);
						val = primitive ? byteVal.byteValue() : byteVal;
						break;

					case Character :
						char charVal = v.charAt(0);
						val = primitive ? charVal : new Character(charVal);
						break;

					case Double :
						Double doubleVal = Double.valueOf(v);
						val = primitive ? doubleVal.doubleValue() : doubleVal;
						break;

					case Float :
						Float floatVal = Float.valueOf(v);
						val = primitive ? floatVal.floatValue() : floatVal;
						break;

					case Integer :
						Integer integerVal = Integer.valueOf(v);
						val = primitive ? integerVal.intValue() : integerVal;
						break;

					case Long :
						Long longVal = Long.valueOf(v);
						val = primitive ? longVal.longValue() : longVal;
						break;

					case Short :
						Short shortVal = Short.valueOf(v);
						val = primitive ? shortVal.shortValue() : shortVal;
						break;

					case String :
						val = v;
						break;
				}
			}

			if (Type.Scalar.equals(entry.type())) {
				return val;
			} else {
				Array.set(array, i++, val);
			}
		}
		if (entry.type()
			.equals(Type.Array)
			|| entry.type()
				.equals(Type.PrimitiveArray)) {
			return array;
		} else if (entry.type()
			.equals(Type.Collection)) {

			return Arrays.asList((Object[]) array);
		}

		return null;
	}

	private static Object createArray(Scalar scalar, boolean primitive, int length) {

		switch (scalar) {
			case Boolean :
				if (primitive) {
					return new boolean[length];
				} else {
					return new Boolean[length];
				}

			case Byte :
				if (primitive) {
					return new byte[length];
				} else {
					return new Byte[length];
				}

			case Character :
				if (primitive) {
					return new char[length];
				} else {
					return new Character[length];
				}

			case Double :
				if (primitive) {
					return new double[length];
				} else {
					return new Double[length];
				}

			case Float :
				if (primitive) {
					return new int[length];
				} else {
					return new Float[length];
				}

			case Integer :
				if (primitive) {
					return new int[length];
				} else {
					return new Integer[length];
				}

			case Long :
				if (primitive) {
					return new long[length];
				} else {
					return new Long[length];
				}

			case Short :
				if (primitive) {
					return new short[length];
				} else {
					return new Short[length];
				}

			case String :
				if (primitive) {
					throw new IllegalArgumentException(
						"@ConfigEntry Could not be Scalar=String and type=ÜrimitiveArray at the same time");
				} else {
					return new String[length];
				}
		}
		return null;
	}

}
