package com.chinamobile.cmos;

import java.util.concurrent.ExecutionException;

import com.zx.sms.session.AbstractSessionStateManager;

interface LoginResponseWaiter {
	int responseResult() throws InterruptedException, ExecutionException;
	AbstractSessionStateManager session();
}
