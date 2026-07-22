package com.mqttsnet.thinglinks.service.script.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 规则脚本「执行日志收集器」── 作为 {@code log} 绑定注入脚本。
 *
 * <p>脚本里 {@code log.info("...")} / {@code log.warn(...)} / {@code log.debug(...)} / {@code log.error(...)}
 * (或通用 {@code log.log("...")}):
 * <ul>
 *   <li>写入 rule 服务日志(logger 名 {@code groovy.script})── 运行时可观测;</li>
 *   <li>同时收集到内存,经执行结果回显给「在线调试」面板 ── 写脚本不再是黑盒。</li>
 * </ul>
 *
 * <p>上限 {@value #MAX_LINES} 行,超出截断,防脚本刷屏拖垮内存 / 响应体。
 * 每次执行 new 一个实例(非线程共享),无并发问题。
 *
 * @author mqttsnet
 */
public class ScriptLogCollector {

    private static final Logger SCRIPT_LOG = LoggerFactory.getLogger("groovy.script");
    private static final int MAX_LINES = 200;

    private final List<String> logs = new ArrayList<>();

    public void info(Object msg) {
        append("INFO", msg);
    }

    public void warn(Object msg) {
        append("WARN", msg);
    }

    public void debug(Object msg) {
        append("DEBUG", msg);
    }

    public void error(Object msg) {
        append("ERROR", msg);
    }

    /** 通用入口:脚本可直接 {@code log.log("...")}。 */
    public void log(Object msg) {
        append("INFO", msg);
    }

    private void append(String level, Object msg) {
        String text = msg == null ? "null" : String.valueOf(msg);
        SCRIPT_LOG.info("[script] {} {}", level, text);
        int size = logs.size();
        if (size < MAX_LINES) {
            logs.add(level + " " + text);
        } else if (size == MAX_LINES) {
            logs.add("... (日志超过 " + MAX_LINES + " 行,已截断)");
        }
    }

    /** 收集到的日志(只读),供执行结果回显。 */
    public List<String> getLogs() {
        return Collections.unmodifiableList(logs);
    }
}
