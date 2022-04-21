package com.mqttsnet.thinglinks.protocolAnalysis.tcpServer.gb32960.entity;

import lombok.Data;

/**
 * @Description: GB32960报文体数据模型
 * @Author: ShiHuan Sun
 * @E-mail: 13733918655@163.com
 * @Website: http://mqtts.net
 * @CreateDate: 2021/11/15$ 18:37$
 * @UpdateUser: ShiHuan Sun
 * @UpdateDate: 2021/11/15$ 18:37$
 * @UpdateRemark: 修改内容
 * @Version: 1.0
 */
@Data
public class GB32960MessageData {
    /**
     * 起始符
     */
    private String msgHead;


    /**
     *命令标识
     */
    private String msgCommand;

    /**
     *应答标识
     */
    private String msgResponse;

    /**
     *唯一识别码
     */
    private String uniqueIdentifier;


    /**
     *加密方式
     */
    private String encryption;


    /**
     *数据单元长度
     */
    private String dataCellLength;

    /**
     *数据单元
     */
    private String data;


    /**
     *校验码
     */
    private String checkCode;

}
