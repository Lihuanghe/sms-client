package com.chinamobile.cmos.protocol;

public class ProtocolProcessorFactory {

	public static ProtocolProcessor build(String protocol) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		Class<ProtocolProcessor> p = (Class<ProtocolProcessor>)Class.forName("com.chinamobile.cmos.protocol."+protocol.toUpperCase() +"ProtocolProcessor");
		return p.newInstance();
	}
}
