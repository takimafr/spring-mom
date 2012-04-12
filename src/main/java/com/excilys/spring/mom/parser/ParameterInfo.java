/**
###############################################################################
# Contributors:
#     Damien VILLENEUVE - initial API and implementation
###############################################################################
 */
package com.excilys.spring.mom.parser;

import com.excilys.spring.mom.annotation.MOMAttributeEncoding;

public class ParameterInfo {

	private String name;
	private MOMAttributeEncoding encoding;

	public ParameterInfo() {
	}

	/**
	 * @param name
	 * @param encoding
	 * @param clazz
	 */
	public ParameterInfo(String name, MOMAttributeEncoding encoding) {
		super();
		this.name = name;
		this.encoding = encoding;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public MOMAttributeEncoding getEncoding() {
		return encoding;
	}

	public void setEncoding(MOMAttributeEncoding encoding) {
		this.encoding = encoding;
	}

}