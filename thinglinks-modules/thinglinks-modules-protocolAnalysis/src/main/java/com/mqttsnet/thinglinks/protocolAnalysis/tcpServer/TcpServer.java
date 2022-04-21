package com.mqttsnet.thinglinks.protocolAnalysis.tcpServer;

import com.mqttsnet.thinglinks.protocolAnalysis.tcpServer.decoder.DecoderHandler;
import com.mqttsnet.thinglinks.protocolAnalysis.tcpServer.encoder.EncoderHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;

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
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();//引导对象
            bootstrap.group(boss,work);//配置工作线程组
            bootstrap.channel(NioServerSocketChannel.class);//配置为NIO的socket通道
            bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {//绑定通道参数
                    ch.pipeline().addLast("logging",new LoggingHandler("DEBUG"));//设置log监听器，并且日志级别为debug，方便观察运行流程
                    ch.pipeline().addLast("encode",new EncoderHandler());//编码器。发送消息时候用
                    ch.pipeline().addLast("decode",new DecoderHandler());//解码器，接收消息时候用
                    ch.pipeline().addLast("handler",new DecoderHandler());//业务处理类，最终的消息会在这个handler中进行业务处理
                }
            });
            bootstrap.option(ChannelOption.SO_BACKLOG,20480);//缓冲区
            bootstrap.childOption(ChannelOption.SO_KEEPALIVE,true);//ChannelOption对象设置TCP套接字的参数，非必须步骤
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
}
