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
package com.excilys.spring.mom.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.excilys.spring.mom.parser.MOMResponseParser;

/**
 * Annotation for mapping MOM requests onto specific handler methods.
 * 
 * @author dvilleneuve
 * @see MOMController
 * @see MOMAttribute
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
public @interface MOMMapping {

	/**
	 * The topic for wich this method will be mapped.
	 * 
	 * @return
	 */
	String topic() default "*";

	/**
	 * This optional argument say to spring-mom how received data have to be parsed before calling the handler method.
	 * Default parsing is {@link MOMMappingConsum MOMMappingConsum.STRING} which just bind the result to method
	 * parameter as a string.
	 * <p/>
	 * If consumes is JSON, spring-mom will try to parse data as JSON string and then bind the results to parameters
	 * according to a specific strategy (defined in {@link MOMResponseParser#getParser()}. If {@link MOMAttribute
	 * MOMAttribute} annotations is set for each parameters, the api will try to bind JSON values to the parameter
	 * according to the value of the {@link MOMAttribute MOMAttribute} annotaiton.
	 * 
	 * @return
	 * @see MOMMappingConsum
	 * @see MOMAttribute
	 * @see MOMResponseParser
	 */
	MOMMappingConsum consumes() default MOMMappingConsum.STRING;

}
