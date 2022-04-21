package com.mqttsnet.thinglinks.protocolAnalysis.tcpServer.encoder;

import com.mqttsnet.thinglinks.protocolAnalysis.tcpServer.protocol.ProtocolModel;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;
@Slf4j
public class EncoderHandler extends MessageToByteEncoder {

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
        ProtocolModel protocol = (ProtocolModel) msg;
        out.writeBytes(protocol.getData());
        log.info("数据编码成功{}",out);
    }
}
