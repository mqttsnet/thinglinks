package com.mqttsnet.thinglinks.protocolAnalysis.tcpServer.gb32960.service;

import io.netty.channel.ChannelHandlerContext;

/**
 * @Description: gb32960实时数据解析接口
 * @Author: ShiHuan Sun
 * @E-mail: 13733918655@163.com
 * @Website: http://thinglinks.mqttsnet.com
 * @CreateDate: 2021/11/15$ 18:30$
 * @UpdateUser: ShiHuan Sun
 * @UpdateDate: 2021/11/15$ 18:30$
 * @UpdateRemark: 修改内容
 * @Version: 1.0
 */

public interface GB32960DataParseService {

    /**
     * 实时数据解析并返回数据
     *
     * @param readDatas
     */
    String realTimeDataParseAndPushData(ChannelHandlerContext ctx, String readDatas) throws Exception;
}
