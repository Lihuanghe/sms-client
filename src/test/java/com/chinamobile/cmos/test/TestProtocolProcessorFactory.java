package com.chinamobile.cmos.test;

import org.junit.Test;

import com.chinamobile.cmos.protocol.ProtocolProcessorFactory;

public class TestProtocolProcessorFactory {

	@Test
	public void testcmpp() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		ProtocolProcessorFactory.build("cmpp");
		ProtocolProcessorFactory.build("smpp");
		ProtocolProcessorFactory.build("sgip");
		ProtocolProcessorFactory.build("smgp");
	}
}
