package com.chinamobile.cmos.test;

import java.nio.charset.Charset;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chinamobile.cmos.MessageReceiver;
import com.chinamobile.cmos.SmsClient;
import com.chinamobile.cmos.SmsClientBuilder;
import com.zx.sms.BaseMessage;
import com.zx.sms.codec.cmpp.msg.CmppSubmitRequestMessage;
import com.zx.sms.codec.cmpp.msg.CmppSubmitResponseMessage;
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

		client.setMaxChannels((short) 1);
		client.setVersion((short) 0x30);
		client.setRetryWaitTimeSec((short) 30);
		client.setUseSSL(false);
//		client.setWriteLimit(100);
		client.setReSendFailMsg(false);
		client.setSupportLongmsg(SupportLongMessage.BOTH);
		SmsClientBuilder builder = new SmsClientBuilder();
		final SmsClient smsClient = builder.entity(client).receiver(new MessageReceiver() {

			public void receive(BaseMessage message) {
				logger.debug(message.toString());
				
			}}).build();
		Future future = null;
		for (int i = 0; i < 100000; i++) {
			 future = executor.submit(new Runnable() {

				public void run() {
					CmppSubmitRequestMessage msg = new CmppSubmitRequestMessage();
					msg.setDestterminalId(String.valueOf(System.nanoTime()));
					msg.setSrcId(String.valueOf(System.nanoTime()));
					msg.setLinkID("0000");
					msg.setMsgContent("老师好，接工信部投诉");
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
