package io.netty.handler.traffic;

import org.junit.Test;

import io.netty.util.concurrent.GlobalEventExecutor;

public class TestTrafficCounter {

	@Test
	public void test() throws InterruptedException {
		TrafficCounter trafficCounter = new TrafficCounter(GlobalEventExecutor.INSTANCE, "test", 100);
		trafficCounter.start();
		for (int i = 0; i < 400; i++) {
			long now = TrafficCounter.milliSecondFromNano();  
			
			//set the TrafficCounter Logger level debug
			long wait = trafficCounter.writeTimeToWait(1, 20, 15000, now);
			Thread.sleep(2);
		}
	}
}
