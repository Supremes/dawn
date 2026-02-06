package com.dawn.handler;

import com.alibaba.fastjson.JSON;
import com.dawn.constant.CommonConstant;
import com.dawn.model.vo.ResultVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedCredentialsNotFoundException;
import org.springframework.security.web.authentication.session.SessionAuthenticationException;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@Component
public class AuthenticationEntryPointImpl implements AuthenticationEntryPoint {
    
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        String uri = request.getRequestURI();
        log.warn("认证异常 - URI: {}, 异常类型: {}, 消息: {}", uri, authException.getClass().getSimpleName(), authException.getMessage());
        
        String message = resolveExceptionMessage(authException);
        int code = resolveExceptionCode(authException);
        
        response.setContentType(CommonConstant.APPLICATION_JSON);
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write(JSON.toJSONString(ResultVO.fail(code, message)));
    }
    
    /**
     * 根据异常类型返回对应的错误消息
     */
    private String resolveExceptionMessage(AuthenticationException exception) {
        if (exception instanceof BadCredentialsException) {
            return "用户名或密码错误";
        } else if (exception instanceof UsernameNotFoundException) {
            return "用户不存在";
        } else if (exception instanceof AccountExpiredException) {
            return "账户已过期，请联系管理员";
        } else if (exception instanceof LockedException) {
            return "账户已被锁定，请联系管理员";
        } else if (exception instanceof DisabledException) {
            return "账户已被禁用，请联系管理员";
        } else if (exception instanceof CredentialsExpiredException) {
            return "密码已过期，请修改密码";
        } else if (exception instanceof InsufficientAuthenticationException) {
            return "认证级别不足，请重新登录";
        } else if (exception instanceof AuthenticationCredentialsNotFoundException) {
            return "未提供认证凭证，请先登录";
        } else if (exception instanceof SessionAuthenticationException) {
            return "会话异常，可能已在其他地方登录";
        } else if (exception instanceof PreAuthenticatedCredentialsNotFoundException) {
            return "预认证凭证缺失";
        } else if (exception instanceof AuthenticationServiceException) {
            return "认证服务异常，请稍后重试";
        }
        return "用户未登录，请先登录";
    }
    
    /**
     * 根据异常类型返回对应的错误码
     */
    private int resolveExceptionCode(AuthenticationException exception) {
        if (exception instanceof BadCredentialsException) {
            return 40101;  // 凭证错误
        } else if (exception instanceof UsernameNotFoundException) {
            return 40102;  // 用户不存在
        } else if (exception instanceof AccountExpiredException) {
            return 40103;  // 账户过期
        } else if (exception instanceof LockedException) {
            return 40104;  // 账户锁定
        } else if (exception instanceof DisabledException) {
            return 40105;  // 账户禁用
        } else if (exception instanceof CredentialsExpiredException) {
            return 40106;  // 密码过期
        } else if (exception instanceof InsufficientAuthenticationException) {
            return 40107;  // 认证级别不足
        } else if (exception instanceof SessionAuthenticationException) {
            return 40108;  // 会话异常
        }
        return 40100;  // 默认未认证
    }
}
