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
import com.zx.sms.codec.smpp.Address;
import com.zx.sms.codec.smpp.msg.SubmitSm;
import com.zx.sms.connect.manager.EndpointEntity.ChannelType;
import com.zx.sms.connect.manager.smpp.SMPPClientEndpointEntity;

public class TestSmppClient {
	private static final Logger logger = LoggerFactory.getLogger(TestSmppClient.class);

	private ExecutorService executor =  Executors.newFixedThreadPool(10);
	@Test
	public void testcmpp() throws Exception {
		SMPPClientEndpointEntity client = new SMPPClientEndpointEntity();
		client.setId("smppclient");
		client.setHost("127.0.0.1");
		client.setPort(27761);
		client.setSystemId("901782");
		client.setPassword("ICP");
		client.setChannelType(ChannelType.DUPLEX);

		client.setMaxChannels((short)2);
		client.setRetryWaitTimeSec((short)100);
		client.setUseSSL(false);
		client.setReSendFailMsg(false);
		client.setWriteLimit(20);
//		client.setReadLimit(200);
		SmsClientBuilder builder = new SmsClientBuilder();
		final SmsClient smsClient = builder.entity(client).receiver(new MessageReceiver() {

			public void receive(BaseMessage message) {
				logger.info("receive : {}",message.toString());
				
			}}).build();
		Future future = null;
		for (int i = 0; i < 5; i++) {
			 future = executor.submit(new Runnable() {

				public void run() {
					SubmitSm pdu = new SubmitSm();
					pdu.setRegisteredDelivery((byte)0);
			        pdu.setSourceAddress(new Address((byte)0,(byte)0,"10086"));
			        pdu.setDestAddress(new Address((byte)0,(byte)0,"13800138000"));
//			        pdu.setSmsMsg(new SmsTextMessage(content,SmsDcs.getGeneralDataCodingDcs(SmsAlphabet.GSM,SmsMsgClass.CLASS_UNKNOWN)));
			        pdu.setSmsMsg("SmsTextMessage");
					try {
						smsClient.send(pdu, 1000);
					} catch (Exception e) {
						logger.info("send ", e);
					}
				}
				
			});
		}
		future.get();
		Thread.sleep(5000);
		
	}
}
