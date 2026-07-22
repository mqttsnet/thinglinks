package com.mqttsnet.thinglinks.util;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.mqttsnet.basic.utils.StrPool;
import com.mqttsnet.basic.utils.topic.MqttTopicMatcher;
import lombok.extern.slf4j.Slf4j;

/**
 * Description:
 * ACL 匹配工具类测试类
 * 覆盖所有 MQTT 规范要求的边界情况
 *
 * @author mqttsnet
 * @version 1.0.4
 * @since 2025/6/14
 */
@Slf4j
public class AclMatcherUtilTest {

    public static void main(String[] args) {
        // 使用StrPool常量构造测试用例
        Map<String, List<String[]>> testCases = new LinkedHashMap<>();

        // 定义常量
        final String PLUS = StrPool.PLUS;
        final String HASH = StrPool.HASH;
        final String DOLLAR = StrPool.DOLLAR;
        final String SLASH = StrPool.SLASH;
        final String SPACE = StrPool.SPACE;
        final String EMPTY = StrPool.EMPTY;
        final String STAR = StrPool.STAR;

        // 1. 精确匹配
        testCases.put("sensors/temperature", Arrays.asList(
            new String[]{"sensors/temperature", "应该匹配"},
            new String[]{"sensors/humidity", "不应该匹配"},
            new String[]{"sensors/temperature/room1", "不应该匹配"},
            new String[]{"sensors/temperature" + SPACE, "不应该匹配（尾部空格）"},
            new String[]{SPACE + "sensors/temperature", "不应该匹配（前导空格）"}
        ));

        // 2. 单层通配符
        testCases.put("sensors/" + PLUS, Arrays.asList(
            new String[]{"sensors/temperature", "应该匹配"},
            new String[]{"sensors/humidity", "应该匹配"},
            new String[]{"sensors/temperature/room1", "不应该匹配"},
            new String[]{"sensors/", "不应该匹配（空层级）"},
            new String[]{"sensors/" + SPACE, "应该匹配（空格层级）"},
            new String[]{"sensors/" + SPACE + "/status", "不应该匹配（含空格层级）"},
            new String[]{"sensors/level1/level2", "不应该匹配（多层级）"}
        ));

        // 3. 多层通配符
        testCases.put("sensors/" + HASH, Arrays.asList(
            new String[]{"sensors/temperature", "应该匹配"},
            new String[]{"sensors/temperature/room1", "应该匹配"},
            new String[]{"sensors", "不应该匹配"},
            new String[]{"sensors/", "应该匹配（空层级）"},
            new String[]{"sensors/" + SPACE, "应该匹配（空格层级）"},
            new String[]{"sensors/" + SPACE + "/status", "应该匹配（含空格层级）"},
            new String[]{"sensors//status", "应该匹配（空层级）"},
            new String[]{"sensors/level1/level2/level3", "应该匹配（多层级）"}
        ));

        // 4. 混合通配符
        testCases.put("home/" + PLUS + "/sensors/" + HASH, Arrays.asList(
            new String[]{"home/floor1/sensors/temperature", "应该匹配"},
            new String[]{"home/floor2/sensors/humidity/room1", "应该匹配"},
            new String[]{"home/sensors/temperature", "不应该匹配"},
            new String[]{"home/" + SPACE + "/sensors/temperature", "应该匹配（含空格层级）"},
            new String[]{"home/level1/level2/sensors", "不应该匹配（多层通配符匹配空）"},
            new String[]{"home//sensors/temperature", "不应该匹配（空层级）"}
        ));

        // 5. 系统主题
        testCases.put(DOLLAR + "SYS/broker", Arrays.asList(
            new String[]{DOLLAR + "SYS/broker", "应该匹配"},
            new String[]{"SYS/broker", "不应该匹配"},
            new String[]{DOLLAR + "SYS/broker/status", "不应该匹配"},
            new String[]{SPACE + DOLLAR + "SYS/broker", "不应该匹配（前导空格）"},
            new String[]{DOLLAR + "SYS/broker" + SPACE, "不应该匹配（尾部空格）"},
            new String[]{DOLLAR + "SYS/broker" + SPACE + "/status", "不应该匹配（尾部空格）"}
        ));

        // 6. 空层级处理
        testCases.put("sensors//status", Arrays.asList(
            new String[]{"sensors//status", "应该匹配"},
            new String[]{"sensors/status", "不应该匹配"},
            new String[]{"sensors/" + SPACE + "/status", "不应该匹配（空格层级）"},
            new String[]{"sensors///status", "不应该匹配（多空层级）"}
        ));

        // 7. 边界情况
        testCases.put(HASH, Arrays.asList(
            new String[]{"any/topic", "应该匹配"},
            new String[]{EMPTY, "应该匹配"},
            new String[]{SPACE, "应该匹配（空格主题）"},
            new String[]{SLASH, "应该匹配（单斜杠）"},
            new String[]{SLASH + SLASH, "应该匹配（双斜杠）"},
            new String[]{SLASH + PLUS + SLASH + HASH, "应该匹配（通配符主题）"}
        ));

        testCases.put(PLUS, Arrays.asList(
            new String[]{"single", "应该匹配"},
            new String[]{EMPTY, "不应该匹配（空层级）"},
            new String[]{SPACE, "应该匹配（空格层级）"},
            new String[]{"multi/level", "不应该匹配"},
            new String[]{SLASH, "不应该匹配（分隔符）"}
        ));

        // 8. 多层级精确匹配
        testCases.put("a/b/c/d/e/f", Arrays.asList(
            new String[]{"a/b/c/d/e/f", "应该匹配"},
            new String[]{"a/b/c/d/e", "不应该匹配"},
            new String[]{"a/b/c/d/e/f/g", "不应该匹配"},
            new String[]{"a/b/c/d/e/f" + SPACE, "不应该匹配（尾部空格）"},
            new String[]{SPACE + "a/b/c/d/e/f", "不应该匹配（前导空格）"},
            new String[]{"a/b/c/d/e/f/", "不应该匹配（尾部分隔符）"}
        ));

        // 9. 特殊字符处理
        testCases.put("topic/with/special" + STAR + "chars", Arrays.asList(
            new String[]{"topic/with/special" + STAR + "chars", "应该匹配"},
            new String[]{"topic/with/special_chars", "不应该匹配"},
            new String[]{"topic/with/special*char", "不应该匹配（少字符）"},
            new String[]{"topic/with/special" + STAR + "char", "不应该匹配（少字符）"}
        ));

        // 10. 空格处理
        testCases.put(SPACE + "leading/space", Arrays.asList(
            new String[]{SPACE + "leading/space", "应该匹配"},
            new String[]{"leading/space", "不应该匹配"},
            new String[]{SPACE + SPACE + "leading/space", "不应该匹配（双前导空格）"}
        ));

        testCases.put("trailing/space" + SPACE, Arrays.asList(
            new String[]{"trailing/space" + SPACE, "应该匹配"},
            new String[]{"trailing/space", "不应该匹配"},
            new String[]{"trailing/space" + SPACE + SPACE, "不应该匹配（双尾部空格）"}
        ));

        testCases.put(SPACE + "space/in/middle" + SPACE, Arrays.asList(
            new String[]{SPACE + "space/in/middle" + SPACE, "应该匹配"},
            new String[]{"space/in/middle", "不应该匹配"},
            new String[]{SPACE + "space/in/middle", "不应该匹配（缺少尾部空格）"},
            new String[]{"space/in/middle" + SPACE, "不应该匹配（缺少前导空格）"}
        ));

        // 11. 混合空格和通配符
        testCases.put(SPACE + PLUS + SPACE + SLASH + SPACE + HASH + SPACE, Arrays.asList(
            new String[]{SPACE + PLUS + SPACE + SLASH + SPACE + HASH + SPACE, "应该匹配"},
            new String[]{PLUS + SLASH + HASH, "不应该匹配"},
            new String[]{SPACE + PLUS + SLASH + HASH + SPACE, "不应该匹配（缺少中间空格）"},
            new String[]{PLUS + SPACE + SLASH + SPACE + HASH, "不应该匹配（缺少前导和尾部空格）"}
        ));

        // 12. 超长主题测试
        StringBuilder longTopic = new StringBuilder();
        for (int i = 0; i < 50; i++) {
            longTopic.append("level").append(i).append(SLASH);
        }
        String longPattern = longTopic + PLUS;
        testCases.put(longPattern, Arrays.asList(
            new String[]{longTopic + "end", "应该匹配"},
            new String[]{longTopic.toString().substring(0, longTopic.length() - 1), "不应该匹配"},
            new String[]{longTopic + "end/extra", "不应该匹配"}
        ));

        // 13. 特殊边界情况
        testCases.put(EMPTY, Arrays.asList(
            new String[]{EMPTY, "应该匹配（空模式匹配空主题）"},
            new String[]{SPACE, "不应该匹配"},
            new String[]{SLASH, "不应该匹配"}
        ));

        testCases.put(SLASH, Arrays.asList(
            new String[]{SLASH, "应该匹配"},
            new String[]{EMPTY, "不应该匹配"},
            new String[]{SLASH + SLASH, "不应该匹配"}
        ));

        testCases.put(PLUS + SLASH + PLUS, Arrays.asList(
            new String[]{"a/b", "应该匹配"},
            new String[]{"a//b", "不应该匹配（空层级）"},
            new String[]{"a/b/c", "不应该匹配"},
            new String[]{"a" + SPACE + SLASH + "b", "应该匹配（含空格）"}
        ));

        // 运行测试
        int totalTests = 0;
        int passedTests = 0;
        List<String> failedTests = new ArrayList<>();

        for (Map.Entry<String, List<String[]>> entry : testCases.entrySet()) {
            String pattern = entry.getKey();
            System.out.println("\n测试模式: " + pattern);

            for (String[] test : entry.getValue()) {
                String topic = test[0];
                String expectedResult = test[1];
                boolean actual = MqttTopicMatcher.match(pattern, topic);
                boolean shouldMatch = expectedResult.startsWith("应该匹配");

                String result;
                if (actual == shouldMatch) {
                    result = "✓ 通过";
                    passedTests++;
                } else {
                    result = "✗ 失败";
                    failedTests.add(String.format("模式: %-30s 主题: %-40s 预期: %-15s 实际: %s",
                        pattern, topic, expectedResult, actual));
                }
                System.out.printf("%s 主题: %-40s 预期: %-15s 实际: %s%n",
                    result, topic, expectedResult, actual);

                totalTests++;
            }
        }

        // 打印总结
        System.out.println("\n========================================");
        System.out.printf("测试结果: %d/%d 通过 (%.2f%%)%n",
            passedTests, totalTests, (passedTests * 100.0 / totalTests));

        if (!failedTests.isEmpty()) {
            System.out.println("\n失败测试详情:");
            failedTests.forEach(System.out::println);
        }
    }
}
