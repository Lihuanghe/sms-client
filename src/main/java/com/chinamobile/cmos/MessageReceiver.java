package com.chinamobile.cmos;

import com.zx.sms.BaseMessage;

public interface MessageReceiver {
	void receive(BaseMessage message);
}
