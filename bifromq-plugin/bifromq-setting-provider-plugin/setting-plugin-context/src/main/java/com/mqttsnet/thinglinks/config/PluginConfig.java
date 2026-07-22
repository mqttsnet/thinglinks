package com.mqttsnet.thinglinks.config;

import lombok.Data;

/**
 * PluginConfig 用于封装 BifroMQ Setting Provider 插件的所有可配置参数。
 * <p>
 * 配置加载优先级（高 → 低）：
 * <ol>
 *     <li>运行时覆盖文件：{@code ./conf/standalone.yml}</li>
 *     <li>插件内置配置：{@code classpath:/config.yaml}</li>
 * </ol>
 * <p>
 * 配置项与 BifroMQ 的 {@code Setting} 枚举对应，
 * 在 {@code BifromqSettingProviderPluginSettingProvider#provide} 方法中使用。
 *
 * @author mqttsnet
 * @see com.mqttsnet.thinglinks.util.ConfigUtil#getPluginConfig()
 */
@Data
public class PluginConfig {

    /**
     * 是否启用调试模式（Enables or disables debug mode）。
     * <p>
     * 对应 {@code Setting.DebugModeEnabled}，开启后 BifroMQ 会输出更详细的调试日志。
     * <ul>
     *     <li>类型：Boolean</li>
     *     <li>BifroMQ 默认值：false</li>
     *     <li>插件默认值：true</li>
     * </ul>
     *
     * @see <a href="https://bifromq.apache.org/docs/3.3.x/plugin/setting_provider/tenantsetting/">BifroMQ Tenant Settings</a>
     */
    private Boolean debugModeEnabled;

    /**
     * 单次 SUBSCRIBE 报文允许的最大 topic filter 数量（Maximum topic filters per subscription）。
     * <p>
     * 对应 {@code Setting.MaxTopicFiltersPerSub}，用于限制客户端单次订阅请求中
     * 可以携带的 topic filter 条目数，防止客户端一次性订阅过多主题。
     * <ul>
     *     <li>类型：Integer</li>
     *     <li>BifroMQ 默认值：10</li>
     *     <li>插件默认值：100</li>
     * </ul>
     *
     * @see <a href="https://bifromq.apache.org/docs/3.3.x/plugin/setting_provider/tenantsetting/">BifroMQ Tenant Settings</a>
     */
    private Integer maxTopicFiltersPerSub = 100;

    /**
     * 单个客户端 inbox 允许的 topic filter 总数（Maximum topic filters per inbox）。
     * <p>
     * 对应 {@code Setting.MaxTopicFiltersPerInbox}，用于限制单个客户端
     * 的收件箱中可以累计保留的 topic filter 总条目数，防止单客户端占用过多路由资源。
     * <ul>
     *     <li>类型：Integer</li>
     *     <li>BifroMQ 默认值：100</li>
     *     <li>插件默认值：500</li>
     * </ul>
     *
     * @see <a href="https://bifromq.apache.org/docs/3.3.x/plugin/setting_provider/tenantsetting/">BifroMQ Tenant Settings</a>
     */
    private Integer maxTopicFiltersPerInbox = 500;
}
