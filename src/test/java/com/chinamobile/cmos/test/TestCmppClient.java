package com.chinamobile.cmos.test;

import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.Test;
import org.marre.sms.SmsDcs;
import org.marre.sms.SmsTextMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chinamobile.cmos.MessageReceiver;
import com.chinamobile.cmos.SmsClient;
import com.chinamobile.cmos.SmsClientBuilder;
import com.zx.sms.BaseMessage;
import com.zx.sms.codec.cmpp.msg.CmppSubmitRequestMessage;

public class TestCmppClient {
	private static final Logger logger = LoggerFactory.getLogger(TestCmppClient.class);

	private ExecutorService executor =  Executors.newFixedThreadPool(10);
	@Test
	public void testcmpp() throws Exception {
		
		String uri = "cmpp://127.0.0.1:17890?username=test01&password=1qaz2wsx&version=32&spcode=10086&msgsrc=test01&serviceid=000000&maxchannel=1";
		
		SmsClientBuilder builder = new SmsClientBuilder();
//		EndpointEntity client =  builder.createEndpointEntity(uri);
		final SmsClient smsClient = builder.uri(uri)
				.keepAllIdleConnection()  //保持空闲连接，以便能接收上行或者状态报告消息
				.window(4)             //设置发送窗口
				.receiver(new MessageReceiver() {
			public void receive(BaseMessage message) {
				
			}}).build();
		Future future = null;
		smsClient.open();
		Thread.sleep(3000);
		for (int i = 0; i <100000; i++) {
			 future = executor.submit(new Runnable() {

				public void run() {

					CmppSubmitRequestMessage msg = new CmppSubmitRequestMessage();
					msg.setDestterminalId(String.valueOf(System.nanoTime()));
					msg.setSrcId(String.valueOf(System.nanoTime()));
					msg.setLinkID("0000");
					msg.setMsgContent(new SmsTextMessage("老师好，CmppSubmitRequestMessage msg = new CmppSubmitRequestMessage()",new SmsDcs((byte)8)));
					msg.setRegisteredDelivery((short) 0);
					msg.setServiceId("10086");
					try {
						Date time = new Date();
						BaseMessage res = smsClient.send(msg);
//						smsClient.asyncSend(msg);
//						logger.error("receive : {}-{}",DateFormatUtils.format(time, "HH:mm:ss.SSS") ,res.toString());
//						Thread.yield();
					} catch (Exception e) {
						logger.info("send ", e);
					}
				}
				
			});
			 
		}
		if(future!=null)future.get();
		logger.error("=============finish============");
		Thread.sleep(5000000);
		
	}
}
