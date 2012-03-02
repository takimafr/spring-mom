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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * @author dvilleneuve
 * 
 */
@Service
public abstract class MOMClient {
	protected static final Logger LOGGER = LoggerFactory.getLogger(MOMClient.class);

	private final List<MOMClientListener> clientListeners;
	private final Map<String, Set<MOMMethodHandler>> topicMethodHandlers;
	private final String hostname;
	private final int port;

	public MOMClient(String hostname, int port) {
		this(hostname, port, true);
	}

	/**
	 * Create an instance of MQTTClient for a specific {@code hostname} and
	 * {@code port}. The {@code clientId} is also passed as argument. The
	 * {@code autoconnect} field is used to automaticaly connect to the mom
	 * broker.
	 * 
	 * @param hostname
	 *            of the mom broker
	 * @param port
	 *            of the mom broker
	 * @param autoconnect
	 */
	public MOMClient(String hostname, int port, boolean autoconnect) {
		this.clientListeners = new LinkedList<MOMClientListener>();
		this.topicMethodHandlers = new HashMap<String, Set<MOMMethodHandler>>();
		this.hostname = hostname;
		this.port = port;
	}

	/**
	 * Connect to the MQTT broker
	 */
	public abstract void connect();

	/**
	 * Disconnect from the MQTT broker
	 */
	public abstract void disconnect();

	/**
	 * Subscribe by registering a listener to a specific topic.
	 * 
	 * @param topic
	 */
	public void subscribe(String topic, MOMMethodHandler momMethodHandler) {
		Set<MOMMethodHandler> methodHandlers = topicMethodHandlers.get(topic);

		if (methodHandlers == null) {
			methodHandlers = new TreeSet<MOMMethodHandler>();
			topicMethodHandlers.put(topic, methodHandlers);
		}

		methodHandlers.add(momMethodHandler);
	}

	/**
	 * Unsubscribe by unregistering all listener for a specific topic.
	 * 
	 * @param topic
	 */
	public void unsubscribe(String topic) {
		topicMethodHandlers.remove(topic);
	}

	/**
	 * Publish a new message on a specific topic to the MQTT broker. Each
	 * connected device which subscribed to this topic will receive this
	 * message.
	 * 
	 * @param topic
	 * @param message
	 */
	public abstract void publish(String topic, String message);

	/**
	 * Publish a stream on a specific topic to the MQTT broker. Each connected
	 * device which subscribed to this topic will receive this message.
	 * 
	 * @param topic
	 * @param data
	 */
	public abstract void publish(String topic, byte[] data);

	/**
	 * Send a ping request to the MQTT broket just to say 'Hey, I'm alive'.
	 */
	public abstract void ping();
	
	/**
	 * Check if the client is connected to the server
	 */
	public abstract boolean isConnected();

	/**
	 * Add a MOM listener to the client. This is sued
	 * 
	 * @param clientListener
	 */
	public void addClientListener(MOMClientListener clientListener) {
		clientListeners.add(clientListener);
	}

	/**
	 * Remove a MOM listener to the client.
	 * 
	 * @param clientListener
	 */
	public void removeClientListener(MOMClientListener clientListener) {
		clientListeners.remove(clientListener);
	}

	List<MOMClientListener> getClientListeners() {
		return clientListeners;
	}

	Map<String, Set<MOMMethodHandler>> getTopicMethodHandlers() {
		return topicMethodHandlers;
	}

	String getHostname() {
		return hostname;
	}

	int getPort() {
		return port;
	}

}
