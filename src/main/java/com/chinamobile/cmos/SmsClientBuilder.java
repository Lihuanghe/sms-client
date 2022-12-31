package com.chinamobile.cmos;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chinamobile.cmos.protocol.ProtocolProcessor;
import com.chinamobile.cmos.protocol.ProtocolProcessorFactory;
import com.zx.sms.connect.manager.EndpointEntity;
import com.zx.sms.connect.manager.EndpointEntity.ChannelType;

public class SmsClientBuilder {
	private static final Logger logger = LoggerFactory.getLogger(SmsClientBuilder.class);
	private EndpointEntity entity;
	private SmsClient client;
	private GenericObjectPool<InnerSmsClient> pool;
	private GenericObjectPoolConfig config;
	private MessageReceiver receiver;
	private boolean hasBuild = false;
	private boolean keepAllIdleConnection = false;
	private int window = 16;

	public SmsClient build() {
		if (hasBuild)
			return client;
		if (config == null) {
			config = new GenericObjectPoolConfig();
			//连接空闲超过此时间，并且空闲个数大于最大空闲连接数，连接收回
			config.setSoftMinEvictableIdleTimeMillis(30000);
			config.setMinIdle(1); //至少保留1个连接
		}
		
		//这个配置影响断点续连 ，连接空闲超过此时间强制关闭回收连接
		config.setMinEvictableIdleTimeMillis(-1); 
		
		//打开个这配置，相当于开启断连检查
		config.setTestWhileIdle(true);
		//这个时间影响断点续连的检查时间，时长越长发现断连越晚
		config.setTimeBetweenEvictionRunsMillis(5000);  
		
		if (entity == null || StringUtils.isBlank(entity.getId()))
			return null;

		int maxChannel = entity.getMaxChannels();

		if (maxChannel > 0) {
			config.setMaxTotal(maxChannel);
			config.setMaxIdle(maxChannel);
			if(keepAllIdleConnection) {
				//保留所有空闲连接
				config.setMinIdle(maxChannel); 
			}
		}

		pool = new GenericObjectPool<InnerSmsClient>(new InnerBasePooledObjectFactory(this.entity, receiver) , config);
		hasBuild = true;
		client = new SmsClient(pool);
		return client;
	}

	public SmsClientBuilder config(GenericObjectPoolConfig config) {
		this.config = config;
		return this;
	}

	public SmsClientBuilder entity(EndpointEntity entity) {

		this.entity = entity;
		return this;
	}
	
	public SmsClientBuilder uri(String uri) throws Exception {
		EndpointEntity entity = createEndpointEntity(uri);
		this.entity = entity;
		return this;
	}
	
	public SmsClientBuilder keepAllIdleConnection() {
		this.keepAllIdleConnection = true;
		return this;
	}

	public SmsClientBuilder receiver(MessageReceiver receiver) {
		this.receiver = receiver;
		return this;
	}
	
	public SmsClientBuilder window(int window) {
		this.window = window;
		return this;
	}
	
	public EndpointEntity createEndpointEntity(String str_uri) throws ClassNotFoundException, InstantiationException, IllegalAccessException, InvocationTargetException {
		URI uri = URI.create(str_uri);
		String protocol = uri.getScheme();
		Map<String, String> queryMap = queryToMap(uri.getQuery());
		ProtocolProcessor p = ProtocolProcessorFactory.build(protocol);
		
		EndpointEntity e = p.buildClient(queryMap);
		
		BeanUtils.copyProperties(e, queryMap);
		
		String proxy = queryMap.get("proxy");
		String id = queryMap.get("id");
		String maxchannel = queryMap.get("maxchannel");
		Integer maxc = Integer.parseInt((StringUtils.isBlank(maxchannel)?"1":maxchannel));
		
		
		e.setId(StringUtils.isBlank(id)?"client":id);
		e.setHost(uri.getHost());
		e.setPort(uri.getPort());
		e.setValid(true);
		e.setChannelType(ChannelType.DUPLEX);
		e.setMaxChannels((short) maxc.shortValue());
		e.setProxy(proxy);
		
		return e;
	}
	
	private static Map<String, String> queryToMap(String query) {
		if (StringUtils.isBlank(query))
			return null;
		Map<String, String> result = new HashMap();
		String[] parameters = query.split("&");
		if (parameters.length > 0) {
			for (String pairs : parameters) {
				if (StringUtils.isBlank(pairs))
					continue;
				String[] kv = pairs.split("=");
				if (kv.length > 1) {
					result.put(kv[0], kv[1]);
				} else {
					result.put(kv[0], "");
				}
			}
		}
		return result;
	}

}
