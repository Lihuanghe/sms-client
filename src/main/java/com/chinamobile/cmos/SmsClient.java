package com.chinamobile.cmos;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.pool2.impl.GenericObjectPool;

import com.zx.sms.BaseMessage;

import io.netty.util.concurrent.Promise;

public class SmsClient {

	private GenericObjectPool<InnerSmsClient> pool;
	SmsClient(GenericObjectPool<InnerSmsClient> pool) {
		this.pool = pool;
	}
	public BaseMessage send(BaseMessage msg) throws Exception {
		InnerSmsClient  client = pool.borrowObject();
		try {
			Promise<BaseMessage> promise =  client.send(msg);
			promise.awaitUninterruptibly();
			return promise.get();
		}finally {
			pool.returnObject(client);
		}
	}
	public BaseMessage send(BaseMessage msg,int timeOut) throws Exception {
		InnerSmsClient  client = pool.borrowObject();
		try {
			Promise<BaseMessage> promise = client.send(msg);
			promise.awaitUninterruptibly(timeOut,TimeUnit.MILLISECONDS);
			return promise.get();
		}finally {
			pool.returnObject(client);
		}
	}
	
	public BaseMessage sendRawMsg(BaseMessage msg) throws Exception {
		InnerSmsClient  client = pool.borrowObject();
		try {
			List<BaseMessage> list = new ArrayList();
			list.add(msg);
			Promise<BaseMessage> promise =  client.rawwrite(list);
			promise.awaitUninterruptibly();
			return promise.get();
		}finally {
			pool.returnObject(client);
		}
	}
	
	public Promise<BaseMessage> asyncSend(BaseMessage msg) throws Exception {
		InnerSmsClient  client = pool.borrowObject();
		try {
			return  client.send(msg);
		}finally {
			pool.returnObject(client);
		}
	}

	public boolean open()  throws Exception{
		InnerSmsClient  client = pool.borrowObject();
		try {
			return  client.open();
		}finally {
			pool.returnObject(client);
		}
	}
	
	public void close()  throws Exception{
		pool.close();
	}
}
