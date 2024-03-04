package com.mqttsnet.basic.protocol.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @program: thinglinks-util-pro
 * @description: 协议Topic变量提取工具类
 * RegexVariableExtractor is a utility class that provides methods to extract
 * specific variables (version and deviceId) from a given input string.
 * @packagename: com.mqttsnet.basic.utils.protocol
 * @author: ShiHuan Sun
 * @e-mainl: 13733918655@163.com
 * @date: 2023-05-07 14:10
 **/
public class ProtocolRegexTopicVariableExtractorUtils {

    /**
     * Extracts the version and deviceId variables from the given input string.
     *
     * @param input The input string to extract the variables from.
     * @return A Map containing the extracted version and deviceId variables.
     */
    public static Map<String, String> extractVariables(String input) {
        // Define the regex pattern to match the version and deviceId variables
        String pattern = "/([^/]+)/devices/([^/]+)";

        // Compile the regex pattern
        Pattern compiledPattern = Pattern.compile(pattern);
        // Match the input string against the compiled pattern
        Matcher matcher = compiledPattern.matcher(input);

        // Create a map to store the extracted variables
        Map<String, String> variables = new HashMap<>();

        // If the input string matches the pattern, extract the version and deviceId variables
        if (matcher.find()) {
            variables.put("version", matcher.group(1));
            variables.put("deviceId", matcher.group(2));
        }

        // Return the map containing the extracted variables
        return variables;
    }
}
