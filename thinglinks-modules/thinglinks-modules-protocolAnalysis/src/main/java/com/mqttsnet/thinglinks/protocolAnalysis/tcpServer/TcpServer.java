package com.mqttsnet.thinglinks.protocolAnalysis.tcpServer;

import com.mqttsnet.thinglinks.protocolAnalysis.tcpServer.decoder.DecoderHandler;
import com.mqttsnet.thinglinks.protocolAnalysis.tcpServer.encoder.EncoderHandler;
import com.mqttsnet.thinglinks.protocolAnalysis.tcpServer.modbus.HeartBeatHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Description: TCP服务端
 * @Author: ShiHuan Sun
 * @E-mail: 13733918655@163.com
 * @CreateDate: 2022/04/20$ 16:33$
 * @UpdateUser: ShiHuan Sun
 * @UpdateDate: 2021/11/5$ 16:33$
 * @UpdateRemark: 修改内容
 * @Version: 1.0
 */
@Slf4j
@Component
@RefreshScope
public class TcpServer {

    @Value("${protocol.tcp-ip.port}")
    private int port;


    @PostConstruct
    public void startNetty() {
        new Thread(() -> {
            try {
                this.init();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
    public  void init(){
        log.info("正在启动TCP服务器……");
        NioEventLoopGroup boss = new NioEventLoopGroup();//主线程组
        NioEventLoopGroup work = new NioEventLoopGroup();//工作线程组
        deviceChannelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();//引导对象
            bootstrap.group(boss,work);//配置工作线程组
            bootstrap.channel(NioServerSocketChannel.class);//配置为NIO的socket通道
            bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {//绑定通道参数
                    ch.pipeline().addLast("HeartBeatHandler",new HeartBeatHandler());
                    ch.pipeline().addLast("logging",new LoggingHandler("DEBUG"));//设置log监听器，并且日志级别为debug，方便观察运行流程
                    ch.pipeline().addLast("encode",new EncoderHandler());//编码器。发送消息时候用
                    ch.pipeline().addLast("decode",new DecoderHandler());//解码器，接收消息时候用
                    ch.pipeline().addLast("handler",new DecoderHandler());//业务处理类，最终的消息会在这个handler中进行业务处理
                }
            });
            bootstrap.option(ChannelOption.SO_BACKLOG,20480);//缓冲区
            bootstrap.childOption(ChannelOption.SO_KEEPALIVE,true);//ChannelOption对象设置TCP套接字的参数，非必须步骤
            bootstrap.childOption(ChannelOption.RCVBUF_ALLOCATOR,new FixedRecvByteBufAllocator(65535));
            bootstrap.childOption(ChannelOption.TCP_NODELAY,true);
            bootstrap.childOption(ChannelOption.SO_REUSEADDR,true);
            ChannelFuture future = bootstrap.bind(port).sync();//使用了Future来启动线程，并绑定了端口
            log.info("启动TCP服务器启动成功,正在监听端口:{}",port);
            future.channel().closeFuture().sync();//以异步的方式关闭端口

        }catch (InterruptedException e) {
            log.info("启动出现异常:{}",e);
        }finally {
            work.shutdownGracefully();
            boss.shutdownGracefully();//出现异常后，关闭线程组
            log.info("TCP服务器关闭成功");
        }
    }
    private static ChannelGroup deviceChannelGroup;
    private static Map<String, ChannelId> deviceMap = new ConcurrentHashMap<>();
    /**
     * 设备接入
     */
    public static void deviceAdd(Channel channel) {
        deviceChannelGroup.add(channel);
    }
    /**
     * 设备移除
     */
    public static void deviceRemove(Channel channel) {
        deviceChannelGroup.remove(channel);
        removeDeviceChannelId(channel.id());
    }

    public static ChannelGroup deviceChannelGroup() {
        return deviceChannelGroup;
    }

    public static void putDeviceChannelId(String code, ChannelId channelId) {
        deviceMap.put(code, channelId);
    }

    public static void removeDeviceChannelId(ChannelId channelId) {
        deviceMap.entrySet().removeIf(item -> item.getValue().equals(channelId));
    }

    public static ChannelId deviceChannelId(String code) {
        return deviceMap.getOrDefault(code, null);
    }

    public static Channel deviceChannel(ChannelId channelId){
        return deviceChannelGroup.find(channelId);
    }
    public static Map<String,ChannelId> deviceMap(){
        return deviceMap;
    }
    /**
     * DEVICE 操作 结束
     */
}
