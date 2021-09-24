package com.chinamobile.cmos;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;

import com.zx.sms.connect.manager.cmpp.CMPPClientEndpointConnector;
import com.zx.sms.connect.manager.cmpp.CMPPClientEndpointEntity;
import com.zx.sms.session.AbstractSessionStateManager;
import com.zx.sms.session.cmpp.SessionLoginManager;
import com.zx.sms.session.cmpp.SessionState;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.GlobalEventExecutor;

class InnerCmppEndpointConnector extends CMPPClientEndpointConnector implements LoginResponseWaiter{
	
	private DefaultPromise<Integer> loginResponseFuture ;
	private AtomicReference<AbstractSessionStateManager> atomicReference = new AtomicReference<AbstractSessionStateManager>();
	
	public InnerCmppEndpointConnector(CMPPClientEndpointEntity e) {
		super(e);
	}
	
	public ChannelFuture open() throws Exception{
		loginResponseFuture = new DefaultPromise(GlobalEventExecutor.INSTANCE);
		ChannelFuture connectFuture =  super.open();
		connectFuture.addListener(new GenericFutureListener<ChannelFuture>(){

			public void operationComplete(ChannelFuture f) throws Exception {
				if(!f.isSuccess()) {
					loginResponseFuture.tryFailure(f.cause());
				}
			}
		});
		return connectFuture;
	}

	protected void doinitPipeLine(ChannelPipeline pipeline) {
		super.doinitPipeLine(pipeline);
		
		SessionLoginManager handler =(SessionLoginManager) pipeline.get("sessionLoginManager");
		pipeline.replace(handler, "sessionLoginManager", new SessionLoginManager(getEndpointEntity()) {
			public void channelInactive(ChannelHandlerContext ctx) throws Exception{
				if(!loginResponseFuture.isDone())
					loginResponseFuture.tryFailure(new IOException("login Failed.") );
				atomicReference.set(null);
				super.channelInactive(ctx);
			}
			protected  int validServermsg(Object message) {
				int status = super.validServermsg(message);
				if(!loginResponseFuture.isDone() && status!=0)
					loginResponseFuture.tryFailure(new IOException("login Failed.status = " + status) );
				return status;
			};
			public void userEventTriggered(final ChannelHandlerContext ctx, Object evt) throws Exception {
				if (evt == SessionState.Connect) {
					atomicReference.set((AbstractSessionStateManager)ctx.pipeline().get("sessionStateManager"));
					loginResponseFuture.trySuccess(0);
				}
			}
		});
	}

	public int responseResult() throws InterruptedException, ExecutionException {
		 return loginResponseFuture.get();
	}

	public AbstractSessionStateManager session() {
		return atomicReference.get();
	}
}
