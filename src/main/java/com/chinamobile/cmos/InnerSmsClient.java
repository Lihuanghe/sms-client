package com.chinamobile.cmos;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zx.sms.BaseMessage;
import com.zx.sms.LongSMSMessage;
import com.zx.sms.common.util.ChannelUtil;
import com.zx.sms.connect.manager.AbstractClientEndpointConnector;
import com.zx.sms.connect.manager.EndpointEntity;
import com.zx.sms.session.AbstractSessionStateManager;

import io.netty.util.concurrent.Promise;

class InnerSmsClient {
	private static final Logger logger = LoggerFactory.getLogger(InnerSmsClient.class);
	private EndpointEntity entity;
	private volatile boolean connected = false;
	private AbstractClientEndpointConnector connector;
	
	InnerSmsClient(EndpointEntity entity,int window) {
		this.entity = entity;
		this.entity.setWindow(window);
		this.connector = entity.getSingletonConnector();
	}

	private <T extends BaseMessage> List<Promise<T>> syncWriteLongMsgToEntity(T msg) throws Exception {
		if (msg instanceof LongSMSMessage) {
			LongSMSMessage<T> lmsg = (LongSMSMessage<T>) msg;
			if (!lmsg.isReport()) {
			
				List<T> msgs = ChannelUtil.splitLongSmsMessage(entity, msg);
				
				return synwrite(msgs);
			}
		}

		return synwrite(msg);
	}

	private <T extends BaseMessage> List<Promise<T>> synwrite(List<T> msgs) throws Exception {
		AbstractSessionStateManager session = ((LoginResponseWaiter) connector).session();
		if (session == null) {
			// 连接已关闭
			close();
			if (!open())
				return null;
			session = ((LoginResponseWaiter) connector).session();
			if (session == null)
				return null;
		}

		List<Promise<T>> arrPromise = new ArrayList<Promise<T>>();
		for (BaseMessage msg : msgs) {
			Promise<T> future = session.writeMessagesync(msg);
			arrPromise.add(future);
		}
		return arrPromise;
	}

	private <T extends BaseMessage> List<Promise<T>> synwrite(BaseMessage msg) throws Exception {
		AbstractSessionStateManager session = ((LoginResponseWaiter) connector).session();
		if (session == null) {
			// 连接已关闭
			close();
			if (!open())
				return null;
			session = ((LoginResponseWaiter) connector).session();
			if (session == null)
				return null;
		}
		List<Promise<T>> arrPromise = new ArrayList<Promise<T>>();
		Promise<T> future =session.writeMessagesync(msg);
		arrPromise.add(future);
		return arrPromise;
	}
	
	public <T extends BaseMessage> Promise<T> rawwrite(List<T> msgs) throws Exception {
		if (connected) {
			List<Promise<T>> promises =  synwrite(msgs);
			if (promises == null) {
				throw new IOException("connection usable.");
			}
			Promise<T> promise = promises.get(promises.size() - 1);
			return promise;
		} else {
			throw new IOException("connection usable.");
		}
	}

	public Promise<BaseMessage> send(BaseMessage msg) throws Exception {
		if (connected) {
			List<Promise<BaseMessage>> promises = syncWriteLongMsgToEntity(msg);
			if (promises == null) {
				throw new IOException("connection usable.");
			}
			Promise<BaseMessage> promise = promises.get(promises.size() - 1);
			return promise;
		} else {
			throw new IOException("connection usable.");
		}
	}
	
	public List<Promise<BaseMessage>> sendAndWaitAllReponse(BaseMessage msg) throws Exception {
		if (connected) {
			List<Promise<BaseMessage>> promises = syncWriteLongMsgToEntity(msg);
			if (promises == null) {
				throw new IOException("connection usable.");
			}
			return promises;
		} else {
			throw new IOException("connection usable.");
		}
	}

	public boolean open() throws Exception {

		if (isConnected())
			return true;
		connector.open();
		try {
			connected = (0 == ((LoginResponseWaiter) connector).responseResult());
			return connected;
		} catch (Exception e) {
			logger.warn("", e);
		}
		return false;

	}

	public void close() throws Exception {
		connector.close();
		connected = false;
	}

	public boolean isConnected() {
		connected = ((LoginResponseWaiter) connector).session() != null;
		return connected;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("InnerSmsClient [connected=");
		builder.append(connected);
		builder.append("]");
		builder.append(entity.toString());
		return builder.toString();
	}
}
