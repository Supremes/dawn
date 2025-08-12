package com.dawn.filter;


import com.dawn.model.dto.UserDetailsDTO;
import com.dawn.service.TokenService;
import com.dawn.util.SecurityUtil;
import com.dawn.util.UserUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.naming.AuthenticationException;
import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Objects;

@Slf4j
@Component
@SuppressWarnings("all")
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter{

    @Autowired
    public TokenService tokenService;

    @Autowired
    public AuthenticationEntryPoint authenticationEntryPoint;

    @SneakyThrows
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) {
        UserDetailsDTO userDetailsDTO = tokenService.getUserDetailDTO(request);
        // 在携带token访问的情况下，authentication对象为null
        if (Objects.nonNull(userDetailsDTO) && Objects.isNull(UserUtil.getAuthentication())) {
            tokenService.renewToken(userDetailsDTO);
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(userDetailsDTO, null, userDetailsDTO.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        }

        // 无论是否token是否有效，不影响后续filter的执行。因为在后续的FilterSecurityInterceptor中会进行权限验证
        // 若访问的URL不需要权限，则直接放行
        // 若访问的URL需要权限，则会在FilterSecurityInterceptor中进行权限验证。目前仅有admin开头的URL需要权限
        filterChain.doFilter(request, response);
    }
}
