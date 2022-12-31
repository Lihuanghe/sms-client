package com.chinamobile.cmos;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.pool2.impl.GenericObjectPool;

import com.zx.sms.BaseMessage;
import com.zx.sms.connect.manager.EndpointEntity;

import io.netty.util.concurrent.Promise;

public class SmsClient {

	private GenericObjectPool<InnerSmsClient> pool;
	
	SmsClient(GenericObjectPool<InnerSmsClient> pool) {
		this.pool = pool;
	}

	public BaseMessage send(BaseMessage msg) throws Exception {
		InnerSmsClient client = pool.borrowObject();
		Promise<BaseMessage> promise;
		try {
			promise = client.send(msg);
		} finally {
			pool.returnObject(client);
		}
		promise.awaitUninterruptibly();
		return promise.get();
	}

	public BaseMessage send(BaseMessage msg, int timeOut) throws Exception {
		InnerSmsClient client = pool.borrowObject();
		Promise<BaseMessage> promise;
		try {
			promise = client.send(msg);
		} finally {
			pool.returnObject(client);
		}
		promise.awaitUninterruptibly(timeOut, TimeUnit.MILLISECONDS);
		return promise.get();
	}

	public BaseMessage sendRawMsg(BaseMessage msg) throws Exception {
		InnerSmsClient client = pool.borrowObject();

		Promise<BaseMessage> promise;
		try {
			List<BaseMessage> list = new ArrayList();
			list.add(msg);
			promise = client.rawwrite(list);

		} finally {
			pool.returnObject(client);
		}
		promise.awaitUninterruptibly();
		return promise.get();
	}

	public Promise<BaseMessage> asyncSend(BaseMessage msg) throws Exception {
		InnerSmsClient client = pool.borrowObject();
		try {
			return client.send(msg);
		} finally {
			pool.returnObject(client);
		}
	}
	
	public List<Promise<BaseMessage>> sendAndWaitAllResponse(BaseMessage msg) throws Exception {
		InnerSmsClient client = pool.borrowObject();
		try {
			return client.sendAndWaitAllReponse(msg);
		} finally {
			pool.returnObject(client);
		}
	}
	

	public boolean open() throws Exception {
		InnerSmsClient client = pool.borrowObject();
		try {
			return client.open();
		} finally {
			pool.returnObject(client);
		}
	}

	public void close() throws Exception {
		pool.close();
	}

	public EndpointEntity getEntity() {
		return  ((InnerBasePooledObjectFactory)pool.getFactory()).getEntity();
	}
	
}
