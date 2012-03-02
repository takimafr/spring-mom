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

import java.util.Set;

import com.albin.mqtt.MqttListener;
import com.albin.mqtt.NettyClient;

/**
 * A concrete sub-class of {@link MOMClient MOMClient} for the <a
 * href="http://www.ibm.com/developerworks/webservices/library/ws-mqtt/index.html">MQTT protocol</a>.
 * <p/>
 * <b>NOTE :</b> This class doesn't support wilcards on topic yet.
 * 
 * @author dvilleneuve
 * @see MOMClient
 */
public class MOMClientMqtt extends MOMClient {

	private NettyClient client;

	/**
	 * Create an instance of MOMClientMqtt for a specific {@code hostname} and {@code port}, which will auto-connect to
	 * the server when an instance is created. The {@code clientId} is used by Mqtt server to identify each client.
	 * 
	 * @param hostname
	 * @param port
	 * @param clientId
	 * @see MOMClient
	 */
	public MOMClientMqtt(String hostname, int port, String clientId) {
		this(hostname, port, clientId, true);
	}

	/**
	 * Create an instance of MOMClientMqtt for a specific {@code hostname} and {@code port}. If {@code autoconnect}
	 * parameter is true, the instance will auto-connect when it's created. The {@code clientId} is used by Mqtt server
	 * to identify each client.
	 * 
	 * @param hostname
	 * @param port
	 * @param clientId
	 * @param autoconnect
	 * @see MOMClient
	 */
	public MOMClientMqtt(String hostname, int port, String clientId, boolean autoconnect) {
		super(hostname, port, autoconnect);

		client = new NettyClient(clientId);
		client.setListener(new MqttClientInternalListener());

		if (autoconnect) {
			connect();
		}

	}

	@Override
	public void connect() {
		LOGGER.info("Connecting to {}:{}...", getHostname(), getPort());
		client.connect(getHostname(), getPort());
	}

	@Override
	public void disconnect() {
		LOGGER.info("Disconnect...");
		client.disconnect();
	}

	@Override
	public void subscribe(String topic, MOMMethodHandler momMethodHandler) {
		super.subscribe(topic, momMethodHandler);
		client.subscribe(topic);
	}

	@Override
	public void unsubscribe(String topic) {
		super.unsubscribe(topic);
		client.unsubscribe(topic);
	}

	@Override
	public void publish(String topic, String message) {
		LOGGER.debug("Send to {} : {}...", topic, message);
		client.publish(topic, message);
	}

	@Override
	public void publish(String topic, byte[] data) {
		LOGGER.debug("Send {} bytes to {}...", topic, data.length);
		client.publish(topic, new String(data));
	}

	@Override
	public void ping() {
		client.ping();
	}

	@Override
	public boolean isConnected() {
		return client.isConnected();
	}

	/**
	 * Handle the MQTT events and dispatch them to the messaging listener according to the concerned topic.
	 * 
	 * @author dvilleneuve
	 * 
	 */
	private final class MqttClientInternalListener implements MqttListener {
		@Override
		public void disconnected() {
			LOGGER.info("MQTTClient disconnected");

			for (MOMClientListener clientListener : getClientListeners()) {
				clientListener.disconnected();
			}
		}

		@Override
		public void publishArrived(String topic, byte[] data) {
			Set<MOMMethodHandler> methodHandlers = getTopicMethodHandlers().get(topic);

			for (MOMMethodHandler methodHandler : methodHandlers) {
				if (methodHandler != null && methodHandler.getMethod() != null && methodHandler.getInstance() != null) {
					try {
						methodHandler.invoke(data);
					} catch (Exception e) {
						LOGGER.error("Can't invoke method", e);
					}
				}
			}
		}
	}

}
