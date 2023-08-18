# 技术问题请加QQ群
![qq 20180420170449](https://user-images.githubusercontent.com/7598107/39042453-6fcfaac0-44bd-11e8-94bf-101c8dad8400.png)

群名称：cmppGate短信
<br/>群   号：770738500

# 一个基于[SMSGate框架](https://github.com/Lihuanghe/SMSGate) 的纯发送短信客户端库，使用简单



# How To Use

```xml
<dependency>
  <groupId>com.chinamobile.cmos</groupId>
  <artifactId>sms-client</artifactId>
  <version>0.0.9</version>
</dependency>
```

- `如何创建smsClient对象`
	
  通过builder模式，为同一个账号创建唯一一个实例对象，不能重复创建。 smsClient对象创建后会自行管理多个tcp连接，如果重复创建Client对象，就会造成总连接数超过最大值。

- `如何发送短信？`

  参考test包里的测试用例 ：
  
```java
		String uri = "cmpp://127.0.0.1:17890?username=test01&password=1qaz2wsx&version=32&spcode=10086&msgsrc=test01&serviceid=000000&window=32&maxchannel=1";

		//通过builder创建一个Client对象，同一个通道账号只用保持一个smsClient实例。可以使用Spring注册为单例Bean。或者单例模式
		SmsClientBuilder builder = new SmsClientBuilder();
//		EndpointEntity client =  builder.createEndpointEntity(uri);
		final SmsClient smsClient = builder.uri(uri) // 保持空闲连接，以便能接收上行或者状态报告消息
				.receiver(new MessageReceiver() {
					public void receive(BaseMessage message) {
//						System.out.println(message.toString());
					}
				}).build();
				
		
		Future future = null;
		
		//发送5000条短信
		for (int i = 0; i < 1; i++) {
			 future = executor.submit(new Runnable() {

				public void run() {
					//new 一个短信 Request对象
					CmppSubmitRequestMessage msg = new CmppSubmitRequestMessage();
					msg.setDestterminalId(String.valueOf(13800138000));
					msg.setSrcId(String.valueOf(10699802323));
					msg.setLinkID("0000");
					msg.setMsgContent("老师好，Hello World");
					msg.setRegisteredDelivery((short) 1);
					msg.setServiceId("ssss");
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

  参考test包里的测试用例 ： 创建 Builder的时候注册 `MessageReceiver`类。
  
  `SGIP`协议是特例，因为该协议要求开启一个Server端口，网关作为客户端连上来推送上行短信和状态报告。

- `使用 http 或者 socks 代理`

  SmsGate支持HTTP、SOCKS代理以方便在使用代理访问服务器的情况。代理设置方式：

```
	// 无username 和 password 可写为  http://ipaddress:port
	client.setProxy("http://username:password@ipaddress:port");  //http代理
	client.setProxy("socks4://username:password@ipaddress:port");  //socks4代理
	client.setProxy("socks5://username:password@ipaddress:port");  //socks5代理

```

# TODO




