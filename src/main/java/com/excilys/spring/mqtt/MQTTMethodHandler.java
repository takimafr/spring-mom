/**
 * Copyright 2010-2011 eBusiness Information, Groupe Excilys (www.excilys.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.excilys.spring.mqtt;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.excilys.spring.mqtt.annotation.MQTTMappingConsum;
import com.excilys.spring.mqtt.parser.MQTTResponseBinaryParser;
import com.excilys.spring.mqtt.parser.MQTTResponseJSONParser;
import com.excilys.spring.mqtt.parser.MQTTResponseParser;
import com.excilys.spring.mqtt.parser.MQTTResponseStringParser;

/**
 * @author dvilleneuve
 * 
 */
class MQTTMethodHandler implements Comparable<MQTTMethodHandler> {
	private final Method method;
	private final Object instance;
	private final MQTTMappingConsum consum;
	private final MQTTResponseParser parser;

	public MQTTMethodHandler(Method method, Object instance, MQTTMappingConsum consum) {
		this.method = method;
		this.instance = instance;
		this.consum = consum;
		this.parser = getParser(consum);
	}

	public Object invoke(byte[] data) throws IllegalArgumentException, IllegalAccessException,
			InvocationTargetException {
		Object parsedData = parser.parse(data);
		return method.invoke(instance, parsedData);
	}

	private MQTTResponseParser getParser(MQTTMappingConsum consum) {
		switch (consum) {
		case BINARY:
			return new MQTTResponseBinaryParser();
		case JSON: {
			Class<?>[] parameterTypes = method.getParameterTypes();
			if (parameterTypes.length >= 1) {
				return new MQTTResponseJSONParser(parameterTypes[0]);
			}
		}
		}

		return new MQTTResponseStringParser();
	}

	public Method getMethod() {
		return method;
	}

	public Object getInstance() {
		return instance;
	}

	public MQTTMappingConsum getConsum() {
		return consum;
	}

	public MQTTResponseParser getParser() {
		return parser;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((consum == null) ? 0 : consum.hashCode());
		result = prime * result + ((instance == null) ? 0 : instance.hashCode());
		result = prime * result + ((method == null) ? 0 : method.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MQTTMethodHandler other = (MQTTMethodHandler) obj;
		if (consum != other.consum)
			return false;
		if (instance == null) {
			if (other.instance != null)
				return false;
		} else if (!instance.equals(other.instance))
			return false;
		if (method == null) {
			if (other.method != null)
				return false;
		} else if (!method.equals(other.method))
			return false;
		return true;
	}

	@Override
	public int compareTo(MQTTMethodHandler o) {
		return 0;
	}
}