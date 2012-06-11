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
package com.excilys.spring.mom.parser;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.datatype.joda.JodaModule;

/**
 * Concrete class implemented {@link MOMResponseParser MOMResponseParser}.
 * <p/>
 * This implementation parse data as a JSON string and try to map the result object to the method paramater.
 * 
 * @author dvilleneuve
 * @see MOMResponseParser
 */
public class MOMResponseJSONParser implements MOMResponseParser {

	private static final Logger LOGGER = LoggerFactory.getLogger(MOMResponseJSONParser.class);

	private final Class<?> bindClass;

	static {
		ObjectMapperSingleton.INSTANCE.getMapper().registerModule(new JodaModule());
	}

	public MOMResponseJSONParser(Class<?> bindClasses) {
		this.bindClass = bindClasses;
	}

	@Override
	public Object[] parse(byte[] data) {

		if (data.length == 0) {
			return null;
		}

		try {
			return new Object[] { ObjectMapperSingleton.INSTANCE.getMapper().readValue(data, bindClass) };
		} catch (JsonProcessingException e) {
			LOGGER.warn("Unable to parse the json string : {}", new String(data), e);
		} catch (IOException e) {
			LOGGER.warn("Unable to parse the json string", e);
		}
		return null;
	}
}
