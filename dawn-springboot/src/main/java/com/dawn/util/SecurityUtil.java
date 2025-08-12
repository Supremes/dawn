package com.dawn.util;

import org.springframework.util.AntPathMatcher;

public class SecurityUtil {
    /**
     * 检查给定的URL是否匹配公开访问的路径
     * @param url 要检查的URL
     * @return 如果URL匹配公开访问的路径，则返回true，否则返回false
     */
    public static boolean isMatched(String url) {
        AntPathMatcher antPathMatcher = new AntPathMatcher();
        return antPathMatcher.match("/actuator/**", url) ||
                antPathMatcher.match("/swagger-ui/**", url) ||
                antPathMatcher.match("/swagger-resources/**", url) ||
                antPathMatcher.match("/v2/api-docs", url) ||
                antPathMatcher.match("/webjars/**", url) ||
                antPathMatcher.match("/users/login", url);
    }
}
