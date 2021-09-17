package com.chinamobile.cmos;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.marre.sms.SmsMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zx.sms.BaseMessage;
import com.zx.sms.LongSMSMessage;
import com.zx.sms.codec.cmpp.wap.LongMessageFrame;
import com.zx.sms.codec.cmpp.wap.LongMessageFrameHolder;
import com.zx.sms.connect.manager.AbstractClientEndpointConnector;
import com.zx.sms.connect.manager.EndpointEntity;
import com.zx.sms.session.AbstractSessionStateManager;

import io.netty.channel.ChannelFuture;
import io.netty.util.concurrent.Promise;

class InnerSmsClient {
	private static final Logger logger = LoggerFactory.getLogger(InnerSmsClient.class);
	private EndpointEntity entity;
	private boolean connected = false;
	private AbstractClientEndpointConnector connector;

	InnerSmsClient(EndpointEntity entity) {
		this.entity = entity;
		this.connector = entity.getSingletonConnector();
	}

	private <T extends BaseMessage> List<Promise<T>> syncWriteLongMsgToEntity(T msg) throws Exception {
		if (msg instanceof LongSMSMessage) {
			LongSMSMessage<T> lmsg = (LongSMSMessage<T>) msg;
			if (!lmsg.isReport()) {
				// 长短信拆分
				SmsMessage msgcontent = lmsg.getSmsMessage();
				List<LongMessageFrame> frameList = LongMessageFrameHolder.INS.splitmsgcontent(msgcontent);

				// 保证同一条长短信，通过同一个tcp连接发送
				List<T> msgs = new ArrayList<T>();
				for (LongMessageFrame frame : frameList) {
					T basemsg = (T) lmsg.generateMessage(frame);
					msgs.add(basemsg);
				}
				return synwrite(msgs);
			}
		}

		return synwrite(msg);
	}

	private <T extends BaseMessage> List<Promise<T>> synwrite(List<T> msgs) throws Exception {
		AbstractSessionStateManager session = ((LoginResponseWaiter)connector).session();
		if (session == null) {
			// 连接已关闭
			close();
			if (!open())
				return null;
			session = ((LoginResponseWaiter)connector).session();
			if (session == null)
				return null;
		}

		List<Promise<T>> arrPromise = new ArrayList<Promise<T>>();
		for (BaseMessage msg : msgs) {
			arrPromise.add(session.writeMessagesync(msg));
		}
		return arrPromise;
	}

	private <T extends BaseMessage> List<Promise<T>> synwrite(BaseMessage msg) throws Exception {
		AbstractSessionStateManager session = ((LoginResponseWaiter)connector).session();
		if (session == null) {
			// 连接已关闭
			close();
			if (!open())
				return null;
			session = ((LoginResponseWaiter)connector).session();
			if (session == null)
				return null;
		}
		List<Promise<T>> arrPromise = new ArrayList<Promise<T>>();
		arrPromise.add(session.writeMessagesync(msg));
		return arrPromise;
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

	public boolean open() throws Exception {

		if (connected)
			return connected;
		ChannelFuture future = connector.open();
		try {
			connected = (0 == ((LoginResponseWaiter)connector).responseResult());
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
