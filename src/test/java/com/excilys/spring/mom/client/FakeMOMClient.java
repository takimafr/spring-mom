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

/**
 * @author dvilleneuve
 * 
 */
public class FakeMOMClient extends MOMClient {

	/**
	 * @param hostname
	 * @param port
	 * @param autoconnect
	 */
	public FakeMOMClient(String hostname, int port, boolean autoconnect) {
		super(hostname, port, autoconnect);
	}

	@Override
	public void connect() {
	}

	@Override
	public void disconnect() {
	}

	@Override
	public void publish(String topic, String message) {
	}

	@Override
	public void publish(String topic, byte[] data) {
	}

	@Override
	public void ping() {
	}

	@Override
	public boolean isConnected() {
		return false;
	}

}
