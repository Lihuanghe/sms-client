package com.chinamobile.cmos.test;

import org.junit.Test;

import com.chinamobile.cmos.protocol.ProtocolProcessor;
import com.chinamobile.cmos.protocol.ProtocolProcessorFactory;

import org.junit.Assert;

public class TestProtocolProcessorFactory {

	@Test
	public void testcmpp() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		ProtocolProcessor p = ProtocolProcessorFactory.build("cmpp");
		Assert.assertEquals("CMPPProtocolProcessor", p.getClass().getSimpleName());
		
		 p = ProtocolProcessorFactory.build("smpp");
		 Assert.assertEquals("SMPPProtocolProcessor", p.getClass().getSimpleName());
		 
		 p = ProtocolProcessorFactory.build("sgip");
		 Assert.assertEquals("SGIPProtocolProcessor", p.getClass().getSimpleName());
		 
		 p = ProtocolProcessorFactory.build("smgp");
		 Assert.assertEquals("SMGPProtocolProcessor", p.getClass().getSimpleName());
	}
}
