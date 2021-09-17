package com.chinamobile.cmos;

import com.zx.sms.codec.cmpp.msg.CmppDeliverRequestMessage;
import com.zx.sms.codec.cmpp.msg.CmppDeliverResponseMessage;
import com.zx.sms.codec.sgip12.msg.SgipDeliverRequestMessage;
import com.zx.sms.codec.sgip12.msg.SgipDeliverResponseMessage;
import com.zx.sms.codec.smgp.msg.SMGPDeliverMessage;
import com.zx.sms.codec.smgp.msg.SMGPDeliverRespMessage;
import com.zx.sms.codec.smpp.msg.DeliverSm;
import com.zx.sms.codec.smpp.msg.DeliverSmResp;
import com.zx.sms.handler.api.AbstractBusinessHandler;

import io.netty.channel.ChannelHandlerContext;

class ResponseSenderHandler extends AbstractBusinessHandler {

	public void channelRead(final ChannelHandlerContext ctx, Object msg) throws Exception {
		if (msg instanceof CmppDeliverRequestMessage) {
			CmppDeliverRequestMessage req = (CmppDeliverRequestMessage) msg;
			CmppDeliverResponseMessage responseMessage = new CmppDeliverResponseMessage(req.getHeader().getSequenceId());
			responseMessage.setResult(0);
			responseMessage.setMsgId(req.getMsgId());
			ctx.channel().writeAndFlush(responseMessage);
		} else if (msg instanceof SMGPDeliverMessage) {
			SMGPDeliverMessage req = (SMGPDeliverMessage) msg;
			SMGPDeliverRespMessage resp = new SMGPDeliverRespMessage();
			resp.setSequenceNo(req.getSequenceNo());
			resp.setMsgId(req.getMsgId());
			resp.setStatus(0);

			ctx.channel().writeAndFlush(resp);
		} else if (msg instanceof SgipDeliverRequestMessage) {
			SgipDeliverRequestMessage req = (SgipDeliverRequestMessage) msg;
			SgipDeliverResponseMessage resp = new SgipDeliverResponseMessage(req.getHeader());
			resp.setResult((short) 0);
			resp.setTimestamp(req.getTimestamp());
			ctx.channel().writeAndFlush(resp);
		} else if (msg instanceof DeliverSm) {
			DeliverSm req = (DeliverSm) msg;
			DeliverSmResp resp = req.createResponse();
			ctx.channel().writeAndFlush(resp);
		}
		ctx.fireChannelRead(msg);
	}

	@Override
	public String name() {
		return "ResponseSenderHandler";
	}

}
