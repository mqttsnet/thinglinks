package com.mqttsnet.thinglinks.protocolAnalysis.tcpServer.decoder;

import com.mqttsnet.thinglinks.common.core.utils.bytes.ByteCastUtil;
import com.mqttsnet.thinglinks.protocolAnalysis.tcpServer.gb32960.service.GB32960DataParseService;
import com.mqttsnet.thinglinks.protocolAnalysis.tcpServer.protocol.ProtocolModel;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;


@Slf4j
@Component
public class DecoderHandler extends SimpleChannelInboundHandler {
    private static DecoderHandler DecoderHandler;

    @Autowired
    private GB32960DataParseService gb32960DataParseService;

    @PostConstruct
    public void init() {
        DecoderHandler = this;
        DecoderHandler.gb32960DataParseService = this.gb32960DataParseService;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buffer = (ByteBuf) msg;
        if (buffer == null) {
            log.warn("[ShowRoom]-[GB32960]-["+ctx.channel().remoteAddress()+"]-接收到客户端遥测数据为空");
            return;
        }
        String string= ByteBufUtil.hexDump(buffer).toUpperCase();
        log.info("[接收数据]-[GB32960]-["+ctx.channel().remoteAddress()+"]"+string);
        String pushData = DecoderHandler.gb32960DataParseService.realTimeDataParseAndPushData(ctx,string);
        log.info("[发送数据]-[GB32960]-["+ctx.channel().remoteAddress()+"]"+pushData);
        this.replyMessage(ctx, ByteCastUtil.hexToAscii(pushData));

        buffer.clear();
    }
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        log.info("netty-->TCP客户端服务上线：" + channel.remoteAddress());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        try {
            Channel channel = ctx.channel();
            log.info("netty-->TCP客户端服务下线：" + channel.remoteAddress());
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    /**
     * 回复消息
     * @param ctx
     * @param message
     */
    public void replyMessage(ChannelHandlerContext ctx, String message){
        ProtocolModel res = new ProtocolModel();
        res.setData(message.getBytes());
        ctx.writeAndFlush(res);
    }
}
