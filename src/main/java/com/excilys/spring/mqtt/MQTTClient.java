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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.albin.mqtt.MqttListener;
import com.albin.mqtt.NettyClient;

/**
 * @author dvilleneuve
 * 
 */
@Service
public class MQTTClient {
	private static final Logger LOGGER = LoggerFactory.getLogger(MQTTClient.class);

	private final Map<String, Set<MQTTMethodHandler>> topicMethodHandlers;
	private final String hostname;
	private final int port;

	private List<MQTTClientListener> clientListeners;
	private NettyClient nettyClient;

	public MQTTClient(String hostname, int port, String clientId) {
		this(hostname, port, clientId, true);
	}

	/**
	 * Create an instance of MQTTClient for a specific {@code hostname} and
	 * {@code port}. The {@code clientId} is also passed as argument. The
	 * {@code autoconnect} field is used to automaticaly connect to the mqtt
	 * broker.
	 * 
	 * @param hostname
	 *            of the mqtt broker
	 * @param port
	 *            of the mqtt broker
	 * @param clientId
	 *            of the mqtt client
	 * @param autoconnect
	 */
	public MQTTClient(String hostname, int port, String clientId, boolean autoconnect) {
		this.topicMethodHandlers = new HashMap<String, Set<MQTTMethodHandler>>();
		this.hostname = hostname;
		this.port = port;

		this.clientListeners = new LinkedList<MQTTClientListener>();

		this.nettyClient = new NettyClient(clientId);
		this.nettyClient.setListener(new MQTTClientInternalListener());

		if (autoconnect) {
			connect();
		}
	}

	/**
	 * Connect to the MQTT broker
	 */
	public void connect() {
		LOGGER.info("Connecting to {}:{}...", hostname, port);
		nettyClient.connect(hostname, port);
	}

	/**
	 * Disconnect from the MQTT broker
	 */
	public void disconnect() {
		LOGGER.info("Disconnect...");
		nettyClient.disconnect();
	}

	/**
	 * Publish a new message on a specific topic to the MQTT broker. Each
	 * connected device which subscribed to this topic will receive this
	 * message.
	 * 
	 * @param topic
	 * @param message
	 */
	public void publish(String topic, String message) {
		LOGGER.debug("Send to {} : {}...", topic, message);
		nettyClient.publish(topic, message);
	}

	/**
	 * Send a ping request to the MQTT broket just to say 'Hey, I'm alive'.
	 */
	public void ping() {
		nettyClient.ping();
	}

	/**
	 * Add a mqtt listener to the client
	 * 
	 * @param clientListener
	 */
	public void addClientListener(MQTTClientListener clientListener) {
		clientListeners.add(clientListener);
	}

	/**
	 * Remove a mqtt listener to the client
	 * 
	 * @param clientListener
	 */
	public void removeClientListener(MQTTClientListener clientListener) {
		clientListeners.remove(clientListener);
	}

	/**
	 * Register a listener to a specific topic.
	 * 
	 * @param topic
	 */
	void addTopicListener(String topic, MQTTMethodHandler mqttMethodHandler) {
		Set<MQTTMethodHandler> methodHandlers = topicMethodHandlers.get(topic);

		if (methodHandlers == null) {
			methodHandlers = new TreeSet<MQTTMethodHandler>();
			topicMethodHandlers.put(topic, methodHandlers);
			nettyClient.subscribe(topic);

			LOGGER.info("Subscribed to topic {}", topic);
		}

		methodHandlers.add(mqttMethodHandler);
	}

	/**
	 * Handle the MQTT events and dispatch them to the messaging listener
	 * according to the concerned topic.
	 * 
	 * @author dvilleneuve
	 * 
	 */
	private final class MQTTClientInternalListener implements MqttListener {
		@Override
		public void disconnected() {
			LOGGER.info("MQTTClient disconnected");

			for (MQTTClientListener clientListener : clientListeners) {
				clientListener.disconnected();
			}
		}

		@Override
		public void publishArrived(String topic, byte[] data) {
			Set<MQTTMethodHandler> methodHandlers = topicMethodHandlers.get(topic);

			LOGGER.info("publishArrived on '{}'", topic);

			for (MQTTMethodHandler methodHandler : methodHandlers) {
				if (methodHandler != null && methodHandler.getMethod() != null && methodHandler.getInstance() != null) {
					try {
						methodHandler.invoke(data);
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					} catch (InvocationTargetException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
}
