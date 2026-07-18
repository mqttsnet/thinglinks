package com.mqttsnet.thinglinks.generator.config;

import com.mqttsnet.thinglinks.generator.enumeration.TplEnum;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Vben Web 端代码生成配置。
 *
 * @author mqttsnet
 * @date 2022/3/23 22:31
 */
@Data
@NoArgsConstructor
public class WebVbenConfig {
    /**
     * 格式化菜单文件名称
     */
    private String formatMenuName = "{}管理";

    /**
     * 前端生成页面样式模板
     */
    private TplEnum tpl = TplEnum.SIMPLE;

}
