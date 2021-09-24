package com.chinamobile.cmos;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zx.sms.BaseMessage;
import com.zx.sms.connect.manager.EndpointEntity;
import com.zx.sms.connect.manager.cmpp.CMPPClientEndpointEntity;
import com.zx.sms.connect.manager.sgip.SgipClientEndpointEntity;
import com.zx.sms.connect.manager.smgp.SMGPClientEndpointEntity;
import com.zx.sms.connect.manager.smpp.SMPPClientEndpointEntity;
import com.zx.sms.handler.api.AbstractBusinessHandler;
import com.zx.sms.handler.api.BusinessHandlerInterface;

import io.netty.channel.ChannelHandlerContext;

public class SmsClientBuilder {
	private static final Logger logger = LoggerFactory.getLogger(SmsClientBuilder.class);
	private EndpointEntity entity;
	private SmsClient client;
	private GenericObjectPool<InnerSmsClient> pool;
	private GenericObjectPoolConfig config;
	private MessageReceiver receiver;
	private boolean hasBuild = false;
	private boolean keepAllIdleConnection = false;

	public SmsClient build() {
		if (hasBuild)
			return client;
		if (config == null) {
			config = new GenericObjectPoolConfig();
			//连接空闲超过此时间，并且空闲个数大于最大空闲连接数，连接收回
			config.setSoftMinEvictableIdleTimeMillis(30000);
			config.setMinIdle(2); //至少保留1个连接
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
			

		pool = new GenericObjectPool<InnerSmsClient>(new BasePooledObjectFactory<InnerSmsClient>() {

			@Override
			public InnerSmsClient create() throws Exception {
				EndpointEntity innerEntity = buildEndpointEntity();

				InnerSmsClient client = new InnerSmsClient(innerEntity);
				return client;
			}

			@Override
			public PooledObject<InnerSmsClient> wrap(InnerSmsClient obj) {
				return new DefaultPooledObject<InnerSmsClient>(obj);
			}

			public void activateObject(PooledObject<InnerSmsClient> p) throws Exception {
				InnerSmsClient client = p.getObject();
				client.open();
			}

			public boolean validateObject(PooledObject<InnerSmsClient> p) {
				InnerSmsClient client = p.getObject();
				return client.isConnected();
			}

			public void destroyObject(PooledObject<InnerSmsClient> p) throws Exception {
				InnerSmsClient client = p.getObject();
				client.close();
			}
		}, config);
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
	
	public SmsClientBuilder keepAllIdleConnection() {
		this.keepAllIdleConnection = true;
		return this;
	}

	public SmsClientBuilder receiver(MessageReceiver receiver) {
		this.receiver = receiver;
		return this;
	}

	private EndpointEntity buildEndpointEntity() {
		EndpointEntity innerEntity = null;
		if (entity instanceof CMPPClientEndpointEntity) {
			innerEntity = new CMPPClientEndpointEntity() {
				@Override
				protected InnerCmppEndpointConnector buildConnector() {

					return new InnerCmppEndpointConnector(this);
				}
			};
		} else if (entity instanceof SMPPClientEndpointEntity) {
			innerEntity = new SMPPClientEndpointEntity() {
				@Override
				protected InnerSMPPEndpointConnector buildConnector() {

					return new InnerSMPPEndpointConnector(this);
				}
			};
		} else if (entity instanceof SMGPClientEndpointEntity) {
			innerEntity = new SMGPClientEndpointEntity() {
				@Override
				protected InnerSMGPEndpointConnector buildConnector() {

					return new InnerSMGPEndpointConnector(this);
				}
			};
		} else if (entity instanceof SgipClientEndpointEntity) {
			innerEntity = new SgipClientEndpointEntity() {
				@Override
				protected InnerSgipEndpointConnector buildConnector() {

					return new InnerSgipEndpointConnector(this);
				}
			};
		}

		try {

			BeanUtils.copyProperties(innerEntity, entity);

		} catch (IllegalAccessException e) {
		} catch (InvocationTargetException e) {
		}

		innerEntity.setMaxChannels((short) 1);
		
		if (innerEntity.getBusinessHandlerSet() == null)
			innerEntity.setBusinessHandlerSet(new ArrayList<BusinessHandlerInterface>());

		AbstractBusinessHandler responseHandler = new AbstractBusinessHandler() {

			@Override
			public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
				ResponseSenderHandler handler = new ResponseSenderHandler();
				handler.setEndpointEntity(getEndpointEntity());
				ctx.pipeline().addAfter("sessionStateManager", handler.name(), handler);
				ctx.pipeline().remove(this);
			}

			@Override
			public String name() {
				return "AddResponseSenderHandler";
			}
		};
		AbstractBusinessHandler receiverHandlerAdder =new AbstractBusinessHandler() {
			@Override
			public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
				ctx.pipeline().addLast(new AbstractBusinessHandler() {

					public void channelRead(final ChannelHandlerContext ctx, Object msg) throws Exception {
						try {
							if(receiver!=null)
								receiver.receive((BaseMessage) msg);
						}catch(Exception e) {
							logger.warn("{}",msg);
						}
					}

					@Override
					public String name() {
						return "ReceiverHandler";
					}
				});
				ctx.pipeline().remove(this);
			}

			@Override
			public String name() {
				return "AddReceiverHandler";
			}
		};
		innerEntity.getBusinessHandlerSet().add(receiverHandlerAdder);
		innerEntity.getBusinessHandlerSet().add(responseHandler);
		return innerEntity;
	}
}
