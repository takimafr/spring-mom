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

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.excilys.spring.mom.annotation.MOMAttribute;
import com.excilys.spring.mom.annotation.MOMMappingConsum;
import com.excilys.spring.mom.parser.MOMResponseBinaryParser;
import com.excilys.spring.mom.parser.MOMResponseJSONAttributesParser;
import com.excilys.spring.mom.parser.MOMResponseJSONParser;
import com.excilys.spring.mom.parser.MOMResponseParser;
import com.excilys.spring.mom.parser.MOMResponseParsingException;
import com.excilys.spring.mom.parser.MOMResponseStringParser;
import com.excilys.spring.mom.parser.ParameterInfo;

/**
 * Encapsulates information about a bean method consisting of a {@linkplain #getMethod() method} and an
 * {@linkplain #getInstance() instance}. Provides convenient access to method parameters, the method return value,
 * method annotations.
 * <p/>
 * The class is used to invoke mapped methods after parsed input datas by a {@link MOMResponseParser parser}.
 * 
 * @author dvilleneuve
 * @see MOMResponseParser
 */
public class MOMMethodHandler implements Comparable<MOMMethodHandler> {

	private static final Logger LOGGER = LoggerFactory.getLogger(MOMMethodHandler.class);

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

	/**
	 * Invoke the mapped method with data received by the MOMClient. The input will be parsed according to
	 * <code>@MOMAttribue.consum</code>.
	 * 
	 * @param data
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	public Object invoke(byte[] data) throws IllegalArgumentException, IllegalAccessException,
			InvocationTargetException {
		try {
			Object[] parsedData = parser.parse(data);
			return method.invoke(instance, parsedData);
		} catch (MOMResponseParsingException e) {
			LOGGER.error("Can't invoke the MOM method", e);
		}
		return null;
	}

	/**
	 * Return an instance of <code>MOMResponseParser</code> according to the value of <code>consum</code> an method
	 * parameters. The possible returned instance are the following :
	 * <ul>
	 * <li>if <code>consum</code> is BINARY : <code>MOMResponseBinaryParser</code></li>
	 * <li>if <code>consum</code> is TEXT (default value) : <code>MOMResponseStringParser</code></li>
	 * <li>if <code>consum</code> is JSON :
	 * <ul>
	 * <li>if there are no parameters : <code>MOMResponseStringParser</code></li>
	 * <li>if there are at least one parameter :
	 * <ul>
	 * <li>if there are no <code>@MOMAttribute</code> annotation : <code>MOMResponseJSONParser</code></li>
	 * <li>if there are not <code>@MOMAttribute</code> annotation on each parameters :
	 * <code>MOMResponseStringParser</code></li>
	 * <li>if there are <code>@MOMAttribute</code> annotation on each parameters :
	 * <code>MOMResponseJSONAttributesParser</code></li>
	 * </ul>
	 * </li>
	 * </ul>
	 * </li>
	 * </ul>
	 * 
	 * @param consum
	 */
	private MOMResponseParser getParser(MOMMappingConsum consum) {
		switch (consum) {
			case BINARY:
				return new MOMResponseBinaryParser();
			case JSON: {
				Class<?>[] parameterTypes = method.getParameterTypes();
				if (parameterTypes.length == 0) {
					// If there are no parameters, we can't parse the input to bind it
					break;
				}

				int i = 0;
				Annotation[][] parameterAnnotations = method.getParameterAnnotations();
				ParameterInfo[] parameterValues = new ParameterInfo[parameterAnnotations.length];

				// Search all annotations of each parameters
				for (Annotation[] parameterAnnotation : parameterAnnotations) {
					// Looking for @MOMAttribute annotation of the current parameter
					for (Annotation annotation : parameterAnnotation) {
						if (annotation instanceof MOMAttribute) {
							MOMAttribute momAnnotation = (MOMAttribute) annotation;
							ParameterInfo parameterInfo = new ParameterInfo(momAnnotation.value(),
									momAnnotation.encoding());
							parameterValues[i++] = parameterInfo;
						}
					}
				}

				// If no @MOMAttribute annotation has been found, then, launch classic MOMResponsJSONParser
				if (i == 0) {
					return new MOMResponseJSONParser(parameterTypes[0]);
				} else if (parameterValues.length != parameterTypes.length) {
					LOGGER.error("You can't use @MOMAttribute only on few parameters of the method '{}'", method);
					break;
				}

				return new MOMResponseJSONAttributesParser(parameterValues);
			}
			case STRING:
				return new MOMResponseStringParser();
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