package com.chinamobile.cmos.test;

import java.nio.charset.Charset;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.lang3.RandomUtils;
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
import com.zx.sms.connect.manager.EndpointEntity.SupportLongMessage;
import com.zx.sms.connect.manager.cmpp.CMPPClientEndpointEntity;

public class TestCmppClient {
	private static final Logger logger = LoggerFactory.getLogger(TestCmppClient.class);

	private ExecutorService executor =  Executors.newFixedThreadPool(10);
	@Test
	public void testcmpp() throws Exception {
		CMPPClientEndpointEntity client = new CMPPClientEndpointEntity();
		client.setId("client");
		// client.setLocalhost("127.0.0.1");
		// client.setLocalport(65521);
		client.setHost("127.0.0.1");
		client.setPort(17890);
		client.setChartset(Charset.forName("utf-8"));
		client.setGroupName("test");
		client.setUserName("test01");
		client.setPassword("1qaz2wsx");

		client.setMaxChannels((short)10);
		client.setVersion((short) 0x20);
		client.setRetryWaitTimeSec((short) 30);
		client.setCloseWhenRetryFailed(false);
		client.setMaxRetryCnt((short )1);
		client.setUseSSL(false);
//		client.setWriteLimit(100);
		client.setReSendFailMsg(false);
		client.setSupportLongmsg(SupportLongMessage.BOTH);
		SmsClientBuilder builder = new SmsClientBuilder();
		final SmsClient smsClient = builder.entity(client)
				.keepAllIdleConnection()  //保持空闲连接，以便能接收上行或者状态报告消息
				.window(32)             //设置发送窗口
				.receiver(new MessageReceiver() {

			public void receive(BaseMessage message) {
//				logger.info("receive : {}",message.toString());
			}}).build();
		Future future = null;
		for (int i = 0; i <100000; i++) {
			 future = executor.submit(new Runnable() {

				public void run() {

					CmppSubmitRequestMessage msg = new CmppSubmitRequestMessage();
					msg.setDestterminalId(String.valueOf(System.nanoTime()));
					msg.setSrcId(String.valueOf(System.nanoTime()));
					msg.setLinkID("0000");
					msg.setMsgContent(new SmsTextMessage("老师好，CmppSubmitRequestMessage msg = new CmppSubmitRequestMessage()",new SmsDcs((byte)8)));
					msg.setRegisteredDelivery((short) 1);
					msg.setServiceId("10086");
					try {
//						smsClient.send(msg);
						smsClient.asyncSend(msg);
//						Thread.yield();
					} catch (Exception e) {
						logger.info("send ", e);
					}
				}
				
			});
			 if(RandomUtils.nextInt(1,100)>50)future.get();
		}
		future.get();
		logger.error("=============finish============");
		Thread.sleep(5000000);
		
	}
}
