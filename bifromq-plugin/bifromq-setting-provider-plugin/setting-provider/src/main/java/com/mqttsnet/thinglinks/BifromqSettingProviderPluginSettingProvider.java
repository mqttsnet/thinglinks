/*
 * Copyright (c) 2024. The BifroMQ Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package com.mqttsnet.thinglinks;

import com.baidu.bifromq.plugin.settingprovider.ISettingProvider;
import com.baidu.bifromq.plugin.settingprovider.Setting;
import com.mqttsnet.thinglinks.config.PluginConfig;
import lombok.extern.slf4j.Slf4j;
import org.pf4j.Extension;

/**
 * -----------------------------------------------------------------------------
 * File Name: BifromqSettingProviderPluginSettingProvider
 * -----------------------------------------------------------------------------
 * Description:
 * <a href="https://bifromq.apache.org/docs/plugin/setting_provider/intro/">...</a>
 * 自定义运行时变更的设置项
 * <p>
 * 1. 实现ISettingProvider接口
 * 2. 通过@Extension注解标记为插件
 * 3. 实现provide方法，根据setting和tenantId返回对应的设置项
 * 4. 解析处理setting和tenantId，返回对应的设置项
 * <p>
 * <b>⚠️ 部署前提(必读):必须在 BifroMQ JVM 启动参数加 {@code -Dsetting_provide_init_value=true}。</b>
 * <p>
 * {@code setting_provide_init_value} 默认 false ── 此时 Setting 在 cache miss 时直接用内核初值,
 * 根本不调用本插件的 {@link #provide}。后果:本插件配的 {@code debugModeEnabled / maxTopicFiltersPerSub /
 * maxTopicFiltersPerInbox} 在 broker 启动建 pipeline 时全部被内核默认值(false / 10 / 100)覆盖,等于没生效。
 * 典型表现:{@code DebugModeEnabled} 实际为 false → 内核不采集 {@code PingReq} 高频事件 → 设备心跳链路断在源头
 * (event-collector 收不到 PING_REQ,下游 mqs 写不了 last_heartbeat_time)。
 * <p>
 * 开启 {@code -Dsetting_provide_init_value=true} 后,cache miss 才回调 {@link #provide} 取本插件的值,
 * 整套自定义 Setting(含 PingReq 所需的 DebugMode)才真正生效。
 * 见 <a href="https://bifromq.apache.org/docs/plugin/setting_provider/intro/">官方 Setting Provider 文档</a>。
 * <p>
 * <b>性能铁律:</b>开启上述参数后,{@link #provide} 会在 cache miss 时由 BifroMQ 工作线程同步调用,
 * 因此本方法必须只读内存配置后立即返回,严禁 DB / RPC / 缓存查询等阻塞调用,否则会拖垮整个 broker;
 * 无法快速判定时 {@code return null}(让该 Setting 维持当前值,把决策逻辑异步化)。
 * <p>
 * <b>4.0 升级备注:</b>上述 {@code -Dsetting_provide_init_value=true} 与 {@code -D<SettingName>} 覆盖初值机制
 * 在 4.0.0-incubating 仍适用;升级主要改动是包路径 {@code com.baidu.bifromq} → {@code org.apache.bifromq}。
 * <p>
 * -----------------------------------------------------------------------------
 *
 * @author xiaonannet
 * @version 1.0
 * -----------------------------------------------------------------------------
 * Revision History:
 * Date         Author          Version     Description
 * --------      --------     -------   --------------------
 * 2024/2/23       xiaonannet        1.0        Initial creation
 * -----------------------------------------------------------------------------
 * @email
 * @date 2024/2/23 15:36
 */

@Slf4j
@Extension
public final class BifromqSettingProviderPluginSettingProvider implements ISettingProvider {


    private final PluginConfig pluginConfig;

    /**
     * 构造函数，通过 {@link BifromqSettingProviderContext} 初始化配置。
     *
     * @param context {@link BifromqSettingProviderContext} 插件的上下文，包含配置信息。
     */
    public BifromqSettingProviderPluginSettingProvider(BifromqSettingProviderContext context) {
        this.pluginConfig = context.getPluginConfig();
    }


    /**
     * 根据指定的 {@link Setting} 和租户 ID，返回对应的租户级配置值。
     * <p>
     * 处理逻辑：
     * <ol>
     *     <li>优先匹配插件已显式配置的 Setting（从 {@link PluginConfig} 读取）</li>
     *     <li>未匹配的 Setting 回退到 BifroMQ 内置默认值 {@code setting.current(tenantId)}</li>
     * </ol>
     * <p>
     * <b>调用时机 + 性能铁律:</b>本方法由 BifroMQ 工作线程在 Setting cache miss 时同步调用
     * (前提是启用 {@code -Dsetting_provide_init_value=true},见类注释)。实现必须只读内存配置后立即返回,
     * 严禁 DB / RPC / 缓存查询等阻塞操作;无法快速判定时返回 {@code null} 让该 Setting 维持当前值。
     *
     * @param setting  BifroMQ 设定项枚举，参见 {@link Setting}
     * @param tenantId 租户 ID，用于租户级别的差异化配置
     * @param <R>      返回类型，由 Setting 枚举决定（Boolean / Integer / Long 等）
     * @return 该 Setting 对应的配置值
     */
    @Override
    public <R> R provide(Setting setting, String tenantId) {
        // ---- 调试模式开关（Enables or disables debug mode） ----
        // 对应 Setting.DebugModeEnabled（Boolean），BifroMQ 默认值: false
        // 开启后 BifroMQ 会采集 mqtt 心跳(PingReq)等高频事件并传给 event-collector,同时输出更详细调试日志。
        // ⚠️ 仅当 JVM 参数 -Dsetting_provide_init_value=true 时,本分支返回的 true 才会在 cache miss 被采用;
        //    否则内核直接用初值 false,本分支等于没生效(详见类注释“部署前提”)。
        // 配置来源：PluginConfig.debugModeEnabled（config.yaml / standalone.yml）
        // @see https://bifromq.apache.org/docs/plugin/setting_provider/intro/
        if (Setting.DebugModeEnabled.equals(setting)) {
            Boolean debugEnabled = checkDebugMode(tenantId);
            log.info("Debug mode for tenant {} is {}", tenantId, debugEnabled);
            return (R) debugEnabled;
        }

        // ---- 单次订阅最大 topic filter 数（Maximum topic filters per subscription） ----
        // 对应 Setting.MaxTopicFiltersPerSub（Integer），BifroMQ 默认值: 10
        // 限制客户端单次 SUBSCRIBE 报文中可携带的 topic filter 条目数
        // 配置来源：PluginConfig.maxTopicFiltersPerSub（config.yaml / standalone.yml）
        // @see https://bifromq.apache.org/docs/plugin/setting_provider/tenantsetting/
        if (Setting.MaxTopicFiltersPerSub.equals(setting)) {
            Integer maxPerSub = pluginConfig.getMaxTopicFiltersPerSub();
            log.debug("MaxTopicFiltersPerSub for tenant {} is {}", tenantId, maxPerSub);
            return (R) maxPerSub;
        }

        // ---- 单客户端 inbox 最大 topic filter 总数（Maximum topic filters per inbox） ----
        // 对应 Setting.MaxTopicFiltersPerInbox（Integer），BifroMQ 默认值: 100
        // 限制单个客户端收件箱中可累计保留的 topic filter 总条目数
        // 配置来源：PluginConfig.maxTopicFiltersPerInbox（config.yaml / standalone.yml）
        // @see https://bifromq.apache.org/docs/plugin/setting_provider/tenantsetting/
        if (Setting.MaxTopicFiltersPerInbox.equals(setting)) {
            Integer maxPerInbox = pluginConfig.getMaxTopicFiltersPerInbox();
            log.debug("MaxTopicFiltersPerInbox for tenant {} is {}", tenantId, maxPerInbox);
            return (R) maxPerInbox;
        }

        // ---- 未显式配置的 Setting，回退到 BifroMQ 内置默认值 ----
        // 完整 Setting 列表参见: https://bifromq.apache.org/docs/plugin/setting_provider/tenantsetting/
        // 如需对更多 Setting 进行租户级定制，可在上方添加对应的 if 分支
        return setting.current(tenantId);
    }

    /**
     * 检查指定租户的调试模式是否启用。
     * <p>
     * 当前实现从 {@link PluginConfig#getDebugModeEnabled()} 读取全局配置，
     * 若需要按租户维度差异化控制，可在此方法中扩展（如查询数据库或缓存）。
     *
     * @param tenantId 租户 ID
     * @return {@code true} 表示该租户启用调试模式；{@code false} 表示关闭
     */
    private boolean checkDebugMode(String tenantId) {
        // 从配置文件读取全局调试模式开关，默认为 true
        Boolean debugModeEnabled = pluginConfig.getDebugModeEnabled();
        return debugModeEnabled != null ? debugModeEnabled : true;
    }

    /**
     * 关闭设置提供者，执行必要的清理操作。
     */
    @Override
    public void close() {
        // 自定义关闭逻辑（如果需要）
        ISettingProvider.super.close();
    }
}
