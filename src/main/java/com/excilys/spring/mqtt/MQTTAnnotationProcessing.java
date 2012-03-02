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

import java.lang.reflect.Method;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import com.excilys.spring.mqtt.annotation.MQTTController;
import com.excilys.spring.mqtt.annotation.MQTTMapping;
import com.excilys.spring.mqtt.annotation.MQTTMappingConsum;

/**
 * @author dvilleneuve
 * 
 */
@Component
public class MQTTAnnotationProcessing implements BeanPostProcessor, Ordered {

	@Autowired
	private MQTTClient mqttClient;

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
		MQTTController classAnnotation = clazz.getAnnotation(MQTTController.class);

		if (classAnnotation != null) {
			ReflectionUtils.doWithMethods(clazz, new ReflectionUtils.MethodCallback() {
				public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {
					MQTTMapping annotation = method.getAnnotation(MQTTMapping.class);

					if (annotation != null) {
						String topic = annotation.topic();
						if (applicationContext != null) {
							topic = applicationContext.getBeanFactory().resolveEmbeddedValue(topic);
						}

						MQTTMappingConsum consum = annotation.consumes();

						mqttClient.addTopicListener(topic, new MQTTMethodHandler(method, bean, consum));
					}
				}
			});
		}

		return bean;
	}
}
