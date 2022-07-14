package com.mqttsnet.thinglinks.protocolAnalysis.tcpServer.modbus;

import com.mqttsnet.thinglinks.protocolAnalysis.tcpServer.TcpServer;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
/**
 * 可使用Modbus Poll进行测试
 * */
@Slf4j
@ChannelHandler.Sharable
public class HeartBeatHandler extends ChannelInboundHandlerAdapter {
//    private RedisUtil redisUtil;
//    private HandlerService handlerService;
//
//    public HeartBeatHandler(RedisUtil redisUtil, HandlerService handlerService) {
//        this.redisUtil = redisUtil;
//        this.handlerService = handlerService;
//    }

    /**
    * 读取客户端传过来的数据
     * 发送到目标，比如redis，websocket，涛思数据库等
    * */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        //以下为示例代码，具体按实际功能需求来；
        String code = "具体获取code操作";
//        sendMessageToWebSocket(code, "发送消息");
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        log.info("接收到客户端信息完成");
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (cause instanceof Exception) {
            log.info("异常捕获");
            cause.printStackTrace();
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("CLIENT" + getRemoteAddress(ctx) + " 接入连接");
        TcpServer.deviceAdd(ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("CLIENT" + getRemoteAddress(ctx) + " 断开连接");
        TcpServer.deviceRemove(ctx.channel());
        ctx.close();
    }

    public static String getRemoteAddress(ChannelHandlerContext ctx) {
        return ctx.channel().remoteAddress().toString();
    }
}