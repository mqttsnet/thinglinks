package net.mqtts.link.service.impl;


import net.mqtts.link.service.PasswordAuthentication;

/**
 * @Description: java类作用描述
 * @Author: ShiHuan Sun
 * @E-mail: 13733918655@163.com
 * @CreateDate: 2021/10/23$ 23:09$
 * @UpdateUser: ShiHuan Sun
 * @UpdateDate: 2021/10/23$ 23:09$
 * @UpdateRemark: 修改内容
 * @Version: 1.0
 */
public class PasswordAuthenticationImpl implements PasswordAuthentication {
    /**
     * 认证接口
     *
     * @param userName         用户名称
     * @param passwordInBytes  密钥
     * @param clientIdentifier 设备标志
     * @return 布尔
     */
    @Override
    public boolean auth(String userName, byte[] passwordInBytes, String clientIdentifier) {
        if (userName.equals("123")){
            return true;
        }
        return false;
    }
}
