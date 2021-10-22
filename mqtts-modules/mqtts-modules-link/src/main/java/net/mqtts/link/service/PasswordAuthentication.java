package net.mqtts.link.service;

import io.github.quickmsg.common.StartUp;
import io.github.quickmsg.common.spi.DynamicLoader;

/**
 * @Description: 连接鉴权
 * @Author: ShiHuan Sun
 * @E-mail: 13733918655@163.com
 * @CreateDate: 2021/10/22$ 17:53$
 * @UpdateUser: ShiHuan Sun
 * @UpdateDate: 2021/10/22$ 17:53$
 * @UpdateRemark: 修改内容
 * @Version: 1.0
 */
public interface PasswordAuthentication extends StartUp {

    PasswordAuthentication INSTANCE = DynamicLoader.findFirst(PasswordAuthentication.class).orElse(null);

    /**
     * 认证接口
     *
     * @param userName        用户名称
     * @param passwordInBytes 密钥
     * @param clientIdentifier 设备标志
     * @return 布尔
     */
    boolean auth(String userName, byte[] passwordInBytes, String clientIdentifier);
}
