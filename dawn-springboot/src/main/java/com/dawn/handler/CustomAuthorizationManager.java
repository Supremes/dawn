package com.dawn.handler;

import com.dawn.model.dto.ResourceRoleDTO;
import com.dawn.mapper.RoleMapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * 自定义授权管理器 - 替代已弃用的 FilterSecurityInterceptor + AccessDecisionManager
 * 
 * Spring Security 6.x 推荐使用 AuthorizationManager 进行动态权限控制
 */
@Slf4j
@Component
public class CustomAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {

    @Autowired
    private RoleMapper roleMapper;

    private static List<ResourceRoleDTO> resourceRoleList;

    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    @PostConstruct
    private void loadResourceRoleList() {
        resourceRoleList = roleMapper.listResourceRoles();
        log.debug("加载资源角色列表完成，共 {} 条记录", resourceRoleList != null ? resourceRoleList.size() : 0);
    }

    /**
     * 清除缓存的资源角色列表，用于权限变更后刷新
     */
    public void clearDataSource() {
        resourceRoleList = null;
        log.debug("资源角色列表缓存已清除");
    }

    @Override
    public AuthorizationDecision check(Supplier<Authentication> authenticationSupplier, RequestAuthorizationContext context) {
        HttpServletRequest request = context.getRequest();
        String method = request.getMethod();
        String url = request.getRequestURI();

        // 懒加载资源角色列表
        if (CollectionUtils.isEmpty(resourceRoleList)) {
            this.loadResourceRoleList();
        }

        // 查找匹配的资源权限配置
        List<String> requiredRoles = findRequiredRoles(url, method);

        // 如果没有匹配的权限配置，放行
        if (requiredRoles == null) {
            if (!url.contains("/actuator/")) {
                // 排除监控端点的日志，避免过多无用日志干扰
                log.debug("URL: {} [{}] 无权限配置，放行", url, method);
            }
            return new AuthorizationDecision(true);
        }

        // 如果资源被禁用
        if (requiredRoles.contains("disable")) {
            log.warn("URL: {} [{}] 资源已禁用", url, method);
            return new AuthorizationDecision(false);
        }

        // 获取当前用户认证信息
        Authentication authentication = authenticationSupplier.get();

        // 未认证用户
        if (authentication == null || !authentication.isAuthenticated()) {
            log.debug("URL: {} [{}] 用户未认证", url, method);
            return new AuthorizationDecision(false);
        }

        // 获取用户拥有的角色
        List<String> userRoles = authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        // 检查用户是否拥有所需角色之一
        boolean granted = requiredRoles.stream().anyMatch(userRoles::contains);

        if (!granted) {
            log.warn("URL: {} [{}] 用户 {} 权限不足，需要角色: {}, 用户角色: {}",
                    url, method, authentication.getName(), requiredRoles, userRoles);
        }

        return new AuthorizationDecision(granted);
    }

    /**
     * 根据 URL 和请求方法查找所需角色
     */
    private List<String> findRequiredRoles(String url, String method) {
        for (ResourceRoleDTO resourceRoleDTO : resourceRoleList) {
            if (antPathMatcher.match(resourceRoleDTO.getUrl(), url)
                    && resourceRoleDTO.getRequestMethod().equals(method)) {
                List<String> roleList = resourceRoleDTO.getRoleList();
                if (CollectionUtils.isEmpty(roleList)) {
                    return List.of("disable");
                }
                return roleList;
            }
        }
        return null;
    }
}
