# 技术问题请加QQ群
![qq 20180420170449](https://user-images.githubusercontent.com/7598107/39042453-6fcfaac0-44bd-11e8-94bf-101c8dad8400.png)

群名称：cmppGate短信
<br/>群   号：770738500

# How To Use

- `如何发送短信？`

  参考test包里的测试用例 ：
  
```java
		CMPPClientEndpointEntity client = new CMPPClientEndpointEntity();
		client.setId("client");
		// client.setLocalhost("127.0.0.1");
		// client.setLocalport(65521);
		client.setHost("127.0.0.1");   //服务器IP
		client.setPort(37890);        //端口
		client.setChartset(Charset.forName("utf-8"));
		client.setGroupName("test");
		client.setUserName("901783");  //企业代码
		client.setPassword("ICP001");  //密码

		client.setMaxChannels((short) 2);  //最大连接数
		client.setVersion((short) 0x20);   //协议版本
		client.setWriteLimit(20);   //每个连接的最大发送速度，单位 拆分短信后 条/秒
		client.setReSendFailMsg(false);
		client.setSupportLongmsg(SupportLongMessage.BOTH);
		
		//SmsClientBuilder 适合和单例 ，一个通道账号只build一次
		SmsClientBuilder builder = new SmsClientBuilder();
		final SmsClient smsClient = builder.entity(client).receiver(new MessageReceiver() {
			public void receive(BaseMessage message) {
				logger.info("receive: {} ",message.toString());
			}}).build();
		Future future = null;
		
		//发送5000条短信
		for (int i = 0; i < 5000; i++) {
			 future = executor.submit(new Runnable() {

				public void run() {
					//new 一个短信 Request对象
					CmppSubmitRequestMessage msg = new CmppSubmitRequestMessage();
					msg.setDestterminalId(String.valueOf(System.nanoTime()));
					msg.setSrcId(String.valueOf(System.nanoTime()));
					msg.setLinkID("0000");
					msg.setMsgContent("老师好，接工信部投诉");
					msg.setRegisteredDelivery((short) 1);
					msg.setServiceId("10086");
					CmppSubmitResponseMessage response;
					try {
					//调用send方法发送
						response = (CmppSubmitResponseMessage) smsClient.send(msg);
						
						//收到Response消息
					} catch (Exception e) {
						logger.info("send ", e);
					}
				}
				
			});
		}
```
- `如何发送长短信？`

  默认已经处理好长短信了，就像发送普通短信一样。

- `如何接收短信？`

  参考test包里的测试用例 ： 创建 Builder的时候注册 `MessageReceiver`类

- `使用 http 或者 socks 代理`

  SmsGate支持HTTP、SOCKS代理以方便在使用代理访问服务器的情况。代理设置方式：

```
	// 无username 和 password 可写为  http://ipaddress:port
	client.setProxy("http://username:password@ipaddress:port");  //http代理
	client.setProxy("socks4://username:password@ipaddress:port");  //socks4代理
	client.setProxy("socks5://username:password@ipaddress:port");  //socks5代理

```
