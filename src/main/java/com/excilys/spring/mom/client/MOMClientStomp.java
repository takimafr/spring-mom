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

import java.util.Map;
import java.util.Set;

import net.ser1.stomp.Listener;
import net.ser1.stomp.client.Client;

/**
 * @author dvilleneuve
 * 
 */
public class MOMClientStomp extends MOMClient {

	private final String hostname;
	private final int port;
	private final String username;
	private final String password;
	private Client client;

	/**
	 * 
	 * @param hostname
	 * @param port
	 * @param clientId
	 */
	public MOMClientStomp(String hostname, int port, String username, String password) {
		this(hostname, port, username, password, true);
	}

	/**
	 * @param hostname
	 * @param port
	 * @param clientId
	 * @param autoconnect
	 */
	public MOMClientStomp(String hostname, int port, String username, String password, boolean autoconnect) {
		super(hostname, port, autoconnect);
		this.hostname = hostname;
		this.port = port;
		this.username = username;
		this.password = password;

		if (autoconnect) {
			connect();
		}
	}

	@Override
	public void connect() {
		LOGGER.info("Connecting to {}:{}...", getHostname(), getPort());
		try {
			client = new Client(hostname, port, username, password);
		} catch (Exception e) {
			LOGGER.error("Failed to connect to Stomp server '" + getHostname() + ":" + getPort() + "'", e);
		}
	}

	@Override
	public void disconnect() {
		if (!isConnected())
			return;

		LOGGER.info("Disconnect...");
		client.disconnect();
	}

	@Override
	public void subscribe(String topic, MOMMethodHandler momMethodHandler) {
		if (!isConnected())
			return;

		super.subscribe(topic, momMethodHandler);
		client.subscribe(topic, new MQTTClientInternalListener(topic));
	}

	@Override
	public void unsubscribe(String topic) {
		if (!isConnected())
			return;

		super.unsubscribe(topic);
		client.unsubscribe(topic);
	}

	/**
	 * Publish a new message on a specific topic to the MQTT broker. Each
	 * connected device which subscribed to this topic will receive this
	 * message.
	 * 
	 * @param topic
	 * @param message
	 */
	@Override
	public void publish(String topic, String message) {
		if (!isConnected())
			return;

		LOGGER.debug("Send to {} : {}...", topic, message);
		client.sendText(topic, message);
	}

	@Override
	public void publish(String topic, byte[] data) {
		if (!isConnected())
			return;

		LOGGER.debug("Send {} bytes to {}...", topic, data.length);
		client.sendText(topic, new String(data));
	}

	/**
	 * Send a ping request to the MQTT broket just to say 'Hey, I'm alive'.
	 */
	@Override
	public void ping() {
	}

	@Override
	public boolean isConnected() {
		return client != null && client.isConnected();
	}

	/**
	 * Handle the MQTT events and dispatch them to the messaging listener
	 * according to the concerned topic.
	 * 
	 * @author dvilleneuve
	 * 
	 */
	private final class MQTTClientInternalListener implements Listener {
		private final String topic;

		private MQTTClientInternalListener(String topic) {
			this.topic = topic;
		}

		@Override
		public void message(Map<String, String> headers, String body) {
			Set<MOMMethodHandler> methodHandlers = getTopicMethodHandlers().get(topic);

			LOGGER.info("publishArrived on '{}'", topic);

			for (MOMMethodHandler methodHandler : methodHandlers) {
				if (methodHandler != null && methodHandler.getMethod() != null && methodHandler.getInstance() != null) {
					try {
						methodHandler.invoke(body.getBytes());
					} catch (Exception e) {
						LOGGER.error("Can't invoke method", e);
					}
				}
			}
		}
	}

}
