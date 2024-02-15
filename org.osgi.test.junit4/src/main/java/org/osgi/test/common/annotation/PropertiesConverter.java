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
package org.osgi.test.common.annotation;

import static org.osgi.test.common.annotation.Property.NOT_SET;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Properties;

import org.osgi.test.common.annotation.Property.Scalar;
import org.osgi.test.common.annotation.Property.Type;

public class PropertiesConverter {

	public static Dictionary<String, Object> of(Property[] entrys) {
		Dictionary<String, Object> dictionary = new Hashtable<>();
		for (Property entry : entrys) {
			dictionary.put(entry.key(), toValue(entry));
		}
		return dictionary;
	}

	private static Object toValue(Property entry) {

		boolean primitive = entry.type()
			.equals(Type.PrimitiveArray);
		String[] value;
		if (NOT_SET.equals(entry.systemProperty())) {
			value = entry.value();
		} else {
			Properties sysProps = System.getProperties();
			if (sysProps.containsKey(entry.systemProperty())) {
				String prop = sysProps.getProperty(entry.systemProperty());
				value = entry.type() == Type.Scalar ? new String[] {
					prop
				} : prop.split(",");
			} else {
				value = entry.value();
			}
		}

		Object result = createArray(entry.scalar(), primitive, value.length);
		int i = 0;
		for (String v : value) {
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
						val = primitive ? charVal : Character.valueOf(charVal);
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
				result = val;
				break;
			} else {
				Array.set(result, i++, val);
			}
		}

		switch (entry.type()) {
			case Array :
				return result;
			case PrimitiveArray :
				return result;
			case Scalar :
				return result;
			case Collection :
				return Arrays.asList((Object[]) result);
			default :
				throw new RuntimeException("conversion error - unknown type");
		}

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
						"@Property Could not be Scalar=String and type=primitiveArray at the same time");
				} else {
					return new String[length];
				}
			default :
				throw new RuntimeException("conversion error - unknown type");
		}
	}

}
