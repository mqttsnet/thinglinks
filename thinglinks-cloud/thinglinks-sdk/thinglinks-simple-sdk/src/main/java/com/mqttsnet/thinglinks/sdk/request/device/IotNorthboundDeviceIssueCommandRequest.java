package com.mqttsnet.thinglinks.sdk.request.device;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Description:
 * 北向API-下发设备命令请求
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026/01/22
 */
@Data
public class IotNorthboundDeviceIssueCommandRequest {

    /**
     * 串行命令列表（串行执行，按顺序依次下发）
     * 与parallel字段二选一，serial优先
     */
    private List<CommandItem> serial;

    /**
     * 并行命令列表（并行执行，同时下发）
     * 与serial字段二选一，serial优先
     */
    private List<CommandItem> parallel;

    /**
     * 命令下发请求项
     */
    @Data
    public static class CommandItem {

        /**
         * 设备标识（"all" 表示所有设备,对齐 BizConstant.ALL 全平台通配字面值）
         * @mock SN-2025-ABCD-5678
         */
        private String deviceIdentification;

        /**
         * 产品标识
         * @mock PROD_001
         */
        private String productIdentification;

        /**
         * 消息类型（固定值 'cloudReq'，表示平台下发的请求）
         * @mock cloudReq
         */
        private String msgType;

        /**
         * 服务编码
         * @mock service001
         */
        private String serviceCode;

        /**
         * 命令编码
         * @mock command001
         */
        private String cmd;

        /**
         * 命令参数
         * @mock {"power":"on"}
         */
        private Map<String, Object> params;
    }

}
