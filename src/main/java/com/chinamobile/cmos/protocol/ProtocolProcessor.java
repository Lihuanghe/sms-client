package com.chinamobile.cmos.protocol;

import java.util.Map;

import com.zx.sms.connect.manager.EndpointEntity;

public interface ProtocolProcessor {
	EndpointEntity buildClient(Map<String,String> queryMap);
}
