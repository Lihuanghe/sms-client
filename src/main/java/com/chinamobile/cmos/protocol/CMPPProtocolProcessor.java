package com.chinamobile.cmos.protocol;

import java.util.Map;

import com.zx.sms.connect.manager.EndpointEntity;
import com.zx.sms.connect.manager.cmpp.CMPPClientEndpointEntity;

public class CMPPProtocolProcessor implements ProtocolProcessor {

	@Override
	public EndpointEntity buildClient(Map<String, String> queryMap) {
    	CMPPClientEndpointEntity client = new CMPPClientEndpointEntity();
        String userName = queryMap.get("username");
        String pass = queryMap.get("password");
        String version = queryMap.get("version");
        String spcode = queryMap.get("spcode");
        String msgsrc = queryMap.get("msgsrc");
        String serviceid = queryMap.get("serviceid");
        
        client.setPassword(pass);
        client.setUserName(userName);
        client.setVersion(Integer.valueOf(version).shortValue());
        client.setSpCode(spcode);
        client.setMsgSrc(msgsrc);
        client.setServiceId(serviceid);
        return client;
	}

}
