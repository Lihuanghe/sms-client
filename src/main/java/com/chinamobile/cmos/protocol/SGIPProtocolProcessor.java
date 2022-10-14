package com.chinamobile.cmos.protocol;

import java.util.Map;

import com.zx.sms.connect.manager.EndpointEntity;
import com.zx.sms.connect.manager.sgip.SgipClientEndpointEntity;

public class SGIPProtocolProcessor implements ProtocolProcessor {

	@Override
	public EndpointEntity buildClient(Map<String, String> queryMap) {
		SgipClientEndpointEntity sgip = new SgipClientEndpointEntity();
		String userName = queryMap.get("username");
		String pass = queryMap.get("password");
		String nodeid = queryMap.get("nodeid");
		sgip.setLoginName(userName);
		sgip.setLoginPassowrd(pass);
		sgip.setNodeId(Long.valueOf(nodeid));
		return sgip;
	}

}
