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

import java.lang.reflect.Method;
import java.net.SocketException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import com.excilys.soja.client.exception.NotConnectedException;
import com.excilys.spring.mom.client.MOMClient;
import com.excilys.spring.mom.client.MOMMethodHandler;

/**
 * @author dvilleneuve
 * 
 */
@Component
public class MOMAnnotationProcessing implements BeanPostProcessor, Ordered {

	private static final Logger LOGGER = LoggerFactory.getLogger(MOMAnnotationProcessing.class);

	@Autowired
	private MOMClient momClient;

	@Autowired
	private ConfigurableApplicationContext applicationContext;

	@Override
	public int getOrder() {
		return LOWEST_PRECEDENCE;
	}

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}

	@Override
	public Object postProcessAfterInitialization(final Object bean, String beanName) throws BeansException {
		final Class<?> clazz = bean.getClass();
		MOMController classAnnotation = clazz.getAnnotation(MOMController.class);

		// If the bean is annotated with @MOMController
		if (classAnnotation != null) {
			LOGGER.debug("Found @MOMController annotated class : {}", clazz);

			ReflectionUtils.doWithMethods(clazz, new ReflectionUtils.MethodCallback() {
				public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {
					MOMMapping methodAnnotation = method.getAnnotation(MOMMapping.class);

					// If the method is annotated with @MOMMapping
					if (methodAnnotation != null) {
						String topic = resolveProperty(methodAnnotation.topic());
						MOMMappingConsum consum = methodAnnotation.consumes();

						LOGGER.debug("Configuring @MOMMapping({}) method {}", consum, method);

						try {
							momClient.subscribe(topic, new MOMMethodHandler(method, bean, consum));
						} catch (NotConnectedException e) {
							LOGGER.error("Can't subscribe to topic {}", topic, e);
						} catch (SocketException e) {
							LOGGER.error("Can't subscribe to topic {}", topic, e);
						}
					}
				}
			});
		}

		return bean;
	}

	/**
	 * Resolve the value parameter as a property formatted like <code>${my.property}</code>
	 * 
	 * @param value
	 * @return
	 */
	private String resolveProperty(String value) {
		if (applicationContext != null) {
			return applicationContext.getBeanFactory().resolveEmbeddedValue(value);
		}
		return value;
	}
}
