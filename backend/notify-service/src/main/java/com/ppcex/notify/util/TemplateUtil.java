package com.ppcex.notify.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 模板处理工具类
 */
@Slf4j
public class TemplateUtil {

    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\$\\{(\\w+)\\}");

    /**
     * 处理模板变量
     *
     * @param template     模板内容
     * @param variables    变量映射
     * @return 处理后的内容
     */
    public static String processTemplate(String template, Map<String, Object> variables) {
        if (!StringUtils.hasText(template)) {
            return template;
        }

        if (variables == null || variables.isEmpty()) {
            return template;
        }

        try {
            Matcher matcher = VARIABLE_PATTERN.matcher(template);
            StringBuffer result = new StringBuffer();

            while (matcher.find()) {
                String variableName = matcher.group(1);
                Object value = variables.get(variableName);

                if (value != null) {
                    matcher.appendReplacement(result, String.valueOf(value));
                } else {
                    // 如果变量不存在，保持原样
                    matcher.appendReplacement(result, matcher.group());
                }
            }

            matcher.appendTail(result);
            return result.toString();

        } catch (Exception e) {
            log.error("处理模板变量失败", e);
            return template;
        }
    }
}