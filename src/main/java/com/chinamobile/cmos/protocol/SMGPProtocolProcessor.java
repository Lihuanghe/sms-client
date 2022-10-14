package com.chinamobile.cmos.protocol;

import java.util.Map;

import com.zx.sms.connect.manager.EndpointEntity;
import com.zx.sms.connect.manager.smgp.SMGPClientEndpointEntity;

public class SMGPProtocolProcessor implements ProtocolProcessor {

	@Override
	public EndpointEntity buildClient(Map<String, String> queryMap) {
		SMGPClientEndpointEntity client = new SMGPClientEndpointEntity();
		String userName = queryMap.get("username");
		String pass = queryMap.get("password");
		String version = queryMap.get("version");
		client.setClientID(userName);
		client.setPassword(pass);
		client.setClientVersion(Integer.valueOf(version).byteValue());
		return client;
	}

}
