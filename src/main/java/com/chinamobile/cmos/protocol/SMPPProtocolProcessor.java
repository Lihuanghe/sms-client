package com.chinamobile.cmos.protocol;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.zx.sms.connect.manager.EndpointEntity;
import com.zx.sms.connect.manager.smpp.SMPPClientEndpointEntity;

public class SMPPProtocolProcessor implements ProtocolProcessor {

	@Override
	public EndpointEntity buildClient(Map<String, String> queryMap) {
		SMPPClientEndpointEntity client = new SMPPClientEndpointEntity();
		String userName = queryMap.get("username");
		String pass = queryMap.get("password");
		String version = queryMap.get("version");
		String servicetype = queryMap.get("servicetype");
		String addzero = queryMap.get("addzero");
		client.setSystemId(userName);
		client.setPassword(pass);
		client.setInterfaceVersion(Integer.valueOf(version).byteValue());
		if (StringUtils.isNoneBlank(servicetype))
			client.setSystemType(servicetype);

		if (StringUtils.isNoneBlank(addzero))
			client.setAddZeroByte(true);
		return client;
	}

}
