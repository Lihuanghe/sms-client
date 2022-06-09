package com.chinamobile.cmos.test;

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
import com.zx.sms.codec.sgip12.msg.SgipSubmitRequestMessage;
import com.zx.sms.connect.manager.EndpointEntity.ChannelType;
import com.zx.sms.connect.manager.sgip.SgipClientEndpointEntity;

public class TestSgipClient {
	private static final Logger logger = LoggerFactory.getLogger(TestSgipClient.class);

	private ExecutorService executor =  Executors.newFixedThreadPool(10);
	@Test
	public void testSgip() throws Exception {
		SgipClientEndpointEntity client = new SgipClientEndpointEntity();
		client.setId("sgipclient");
		client.setHost("127.0.0.1");
		client.setPort(16890);
		client.setLoginName("test01");
		client.setLoginPassowrd("1qaz2wsx");
		client.setChannelType(ChannelType.DUPLEX);
		client.setNodeId(3073100002L);
		client.setMaxChannels((short)10);
		client.setRetryWaitTimeSec((short)100);
		client.setUseSSL(false);
		client.setReSendFailMsg(false);
		client.setIdleTimeSec((short)120);
		SmsClientBuilder builder = new SmsClientBuilder();
		final SmsClient smsClient = builder.entity(client)
				.keepAllIdleConnection()
				.receiver(new MessageReceiver() {

			public void receive(BaseMessage message) {
				logger.info(message.toString());
			}}).build();
		Future future = null;
		for (int i = 0; i < 50000; i++) {
			 future = executor.submit(new Runnable() {

				public void run() {
					SgipSubmitRequestMessage requestMessage = new SgipSubmitRequestMessage();
					requestMessage.setSpnumber("10086");
					requestMessage.setUsernumber("13800138000");
					requestMessage.setMsgContent("老师好，接工信部投诉，烦请协");
					requestMessage.setReportflag((short)1);
					try {
						smsClient.asyncSend(requestMessage);
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
