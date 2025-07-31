package com.aurora.handler;

import com.aurora.model.dto.ResourceRoleDTO;
import com.aurora.mapper.RoleMapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.SecurityConfig;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.access.intercept.FilterInvocationSecurityMetadataSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.List;

@Component
public class FilterInvocationSecurityMetadataSourceImpl implements FilterInvocationSecurityMetadataSource {


    @Autowired
    private RoleMapper roleMapper;

    private static List<ResourceRoleDTO> resourceRoleList;

    @PostConstruct
    private void loadResourceRoleList() {
        resourceRoleList = roleMapper.listResourceRoles();
    }

    public void clearDataSource() {
        resourceRoleList = null;
    }

    @Override
    public Collection<ConfigAttribute> getAttributes(Object object) throws IllegalArgumentException {
        if (CollectionUtils.isEmpty(resourceRoleList)) {
            this.loadResourceRoleList();
        }
        FilterInvocation fi = (FilterInvocation) object;
        String method = fi.getRequest().getMethod();
        String url = fi.getRequest().getRequestURI();
        AntPathMatcher antPathMatcher = new AntPathMatcher();
        
        // 对于公开访问的路径，返回特殊的配置属性，表示允许匿名访问
        if (antPathMatcher.match("/actuator/**", url) ||
            antPathMatcher.match("/swagger-ui/**", url) ||
            antPathMatcher.match("/swagger-resources/**", url) ||
            antPathMatcher.match("/v2/api-docs", url) ||
            antPathMatcher.match("/webjars/**", url) ||
            antPathMatcher.match("/users/login", url) ||
            antPathMatcher.match("/users/register", url) ||
            antPathMatcher.match("/users/code", url)) {
            return SecurityConfig.createList("ROLE_ANONYMOUS");
        }
        
        for (ResourceRoleDTO resourceRoleDTO : resourceRoleList) {
            if (antPathMatcher.match(resourceRoleDTO.getUrl(), url) && resourceRoleDTO.getRequestMethod().equals(method)) {
                List<String> roleList = resourceRoleDTO.getRoleList();
                if (CollectionUtils.isEmpty(roleList)) {
                    return SecurityConfig.createList("disable");
                }
                return SecurityConfig.createList(roleList.toArray(new String[]{}));
            }
        }
        return null;
    }

    @Override
    public Collection<ConfigAttribute> getAllConfigAttributes() {
        return null;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return FilterInvocation.class.isAssignableFrom(clazz);
    }
}
