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

	private ExecutorService executor =  Executors.newFixedThreadPool(100);
	@Test
	public void testcmpp() throws Exception {
		CMPPClientEndpointEntity client = new CMPPClientEndpointEntity();
		client.setId("client");
		// client.setLocalhost("127.0.0.1");
		// client.setLocalport(65521);
		client.setHost("127.0.0.1");
		client.setPort(37890);
		client.setChartset(Charset.forName("utf-8"));
		client.setGroupName("test");
		client.setUserName("901783");
		client.setPassword("ICP001");

		client.setMaxChannels((short) 10);
		client.setVersion((short) 0x30);
		client.setRetryWaitTimeSec((short) 30);
		client.setUseSSL(false);
		client.setWriteLimit(50);
		client.setReSendFailMsg(false);
		client.setSupportLongmsg(SupportLongMessage.BOTH);
		SmsClientBuilder builder = new SmsClientBuilder();
		final SmsClient smsClient = builder.entity(client).keepAllIdleConnection()
				.receiver(new MessageReceiver() {

			public void receive(BaseMessage message) {
				logger.info("receive : {}",message.toString());
			}}).build();
		Future future = null;
		for (int i = 0; i < 50; i++) {
			 future = executor.submit(new Runnable() {

				public void run() {
					try {
						Thread.sleep(RandomUtils.nextInt(100, 300));
					} catch (InterruptedException e1) {
					}
					CmppSubmitRequestMessage msg = new CmppSubmitRequestMessage();
					msg.setDestterminalId(String.valueOf(System.nanoTime()));
					msg.setSrcId(String.valueOf(System.nanoTime()));
					msg.setLinkID("0000");
					msg.setMsgContent(new SmsTextMessage("老师好，CmppSubmitRequestMessage msg = new CmppSubmitRequestMessage();接工信部投诉",new SmsDcs((byte)8,70)));
					msg.setRegisteredDelivery((short) 0);
					msg.setServiceId("10086");
					try {
						smsClient.send(msg);
					} catch (Exception e) {
						logger.info("send ", e);
					}
				}
				
			});
		}
		future.get();
		Thread.sleep(5000000);
		
	}
}
