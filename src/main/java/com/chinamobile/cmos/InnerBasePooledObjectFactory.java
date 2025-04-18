package com.chinamobile.cmos;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
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

class InnerBasePooledObjectFactory extends BasePooledObjectFactory<InnerSmsClient> {
	private static final Logger logger = LoggerFactory.getLogger(InnerBasePooledObjectFactory.class);
	
	private EndpointEntity entity;
	private final MessageReceiver receiver;
	
	InnerBasePooledObjectFactory(EndpointEntity entity ,  MessageReceiver receiver){
		this.entity = entity;
		this.receiver = receiver;
	}
	@Override
	public InnerSmsClient create() throws Exception {
		EndpointEntity tempEntity = buildEndpointEntity();
		InnerSmsClient innerSmsClient = new InnerSmsClient(tempEntity,tempEntity.getWindow());
		return innerSmsClient;
	}

	@Override
	public PooledObject<InnerSmsClient> wrap(InnerSmsClient obj) {
		return new DefaultPooledObject<InnerSmsClient>(obj);
	}

	public void activateObject(PooledObject<InnerSmsClient> p) throws Exception {
		InnerSmsClient innerSmsClient = p.getObject();
		innerSmsClient.open();
	}

	public boolean validateObject(PooledObject<InnerSmsClient> p) {
		InnerSmsClient innerSmsClient = p.getObject();
		return innerSmsClient.isConnected();
	}

	public void destroyObject(PooledObject<InnerSmsClient> p) throws Exception {
		InnerSmsClient innerSmsClient = p.getObject();
		innerSmsClient.close();
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
		List<BusinessHandlerInterface> originHandlers = null;
		if (innerEntity.getBusinessHandlerSet() == null) {
			innerEntity.setBusinessHandlerSet(new ArrayList<BusinessHandlerInterface>());
		}else {
			//如果设置有业务的定制handler。要复制出来添加到innerEntity里
			originHandlers = innerEntity.getBusinessHandlerSet();
			innerEntity.setBusinessHandlerSet(new ArrayList<BusinessHandlerInterface>());
			innerEntity.getBusinessHandlerSet().addAll(originHandlers);
		}

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
				return "__preRandom924__AddResponseSenderHandler";
			}
		};
		innerEntity.getBusinessHandlerSet().add(responseHandler);
		
		if(receiver != null) {
			AbstractBusinessHandler receiverHandlerAdder =new AbstractBusinessHandler() {
				@Override
				public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
					ctx.pipeline().addLast(new AbstractBusinessHandler() {

						public void channelRead(final ChannelHandlerContext ctx, Object msg) throws Exception {
							try {
								if(receiver!=null)
									receiver.receive((BaseMessage) msg);
							}catch(Exception e) {
								logger.warn("Exception : receiver {}",msg.toString(),e);
							}
						}

						@Override
						public String name() {
							return "__preRandom924__ReceiverHandler";
						}
					});
					ctx.pipeline().remove(this);
				}

				@Override
				public String name() {
					return "__preRandom924__AddReceiverHandler";
				}
			};
			innerEntity.getBusinessHandlerSet().add(receiverHandlerAdder);
		}
	
		return innerEntity;
	}
	
	public EndpointEntity getEntity() {
		return entity;
	}
}
