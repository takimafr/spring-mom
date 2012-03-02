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
package com.excilys.spring.mom.client;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.excilys.spring.mom.annotation.MOMMappingConsum;
import com.excilys.spring.mom.parser.MOMResponseBinaryParser;
import com.excilys.spring.mom.parser.MOMResponseJSONParser;
import com.excilys.spring.mom.parser.MOMResponseParser;
import com.excilys.spring.mom.parser.MOMResponseStringParser;

/**
 * @author dvilleneuve
 * 
 */
public class MOMMethodHandler implements Comparable<MOMMethodHandler> {
	private final Method method;
	private final Object instance;
	private final MOMMappingConsum consum;
	private final MOMResponseParser parser;

	public MOMMethodHandler(Method method, Object instance, MOMMappingConsum consum) {
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

	private MOMResponseParser getParser(MOMMappingConsum consum) {
		switch (consum) {
		case BINARY:
			return new MOMResponseBinaryParser();
		case JSON: {
			Class<?>[] parameterTypes = method.getParameterTypes();
			if (parameterTypes.length >= 1) {
				return new MOMResponseJSONParser(parameterTypes[0]);
			}
		}
		}

		return new MOMResponseStringParser();
	}

	public Method getMethod() {
		return method;
	}

	public Object getInstance() {
		return instance;
	}

	public MOMMappingConsum getConsum() {
		return consum;
	}

	public MOMResponseParser getParser() {
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
		MOMMethodHandler other = (MOMMethodHandler) obj;
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
	public int compareTo(MOMMethodHandler o) {
		return 0;
	}
}