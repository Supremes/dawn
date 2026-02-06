package com.dawn.handler;

import com.alibaba.fastjson.JSON;
import com.dawn.constant.CommonConstant;
import com.dawn.model.vo.ResultVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.AuthorizationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.csrf.CsrfException;
import org.springframework.security.web.csrf.InvalidCsrfTokenException;
import org.springframework.security.web.csrf.MissingCsrfTokenException;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

@Slf4j
@Component
public class AccessDeniedHandlerImpl implements AccessDeniedHandler {
    
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException {
        String uri = request.getRequestURI();
        String username = Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(Authentication::getName)
                .orElse("anonymous");
        log.warn("授权异常 - 用户: {}, URI: {}, 异常类型: {}, 消息: {}", 
                username, uri, accessDeniedException.getClass().getSimpleName(), accessDeniedException.getMessage());
        
        String message = resolveExceptionMessage(accessDeniedException);
        int code = resolveExceptionCode(accessDeniedException);
        
        response.setContentType(CommonConstant.APPLICATION_JSON);
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.getWriter().write(JSON.toJSONString(ResultVO.fail(code, message)));
    }
    
    /**
     * 根据异常类型返回对应的错误消息
     */
    private String resolveExceptionMessage(AccessDeniedException exception) {
        if (exception instanceof MissingCsrfTokenException) {
            return "CSRF 令牌缺失，请刷新页面重试";
        } else if (exception instanceof InvalidCsrfTokenException) {
            return "CSRF 令牌无效，请刷新页面重试";
        } else if (exception instanceof CsrfException) {
            return "CSRF 验证失败";
        } else if (exception instanceof AuthorizationServiceException) {
            return "授权服务异常，请稍后重试";
        }
        return "权限不足，无法访问该资源";
    }
    
    /**
     * 根据异常类型返回对应的错误码
     */
    private int resolveExceptionCode(AccessDeniedException exception) {
        if (exception instanceof MissingCsrfTokenException) {
            return 40301;  // CSRF 令牌缺失
        } else if (exception instanceof InvalidCsrfTokenException) {
            return 40302;  // CSRF 令牌无效
        } else if (exception instanceof CsrfException) {
            return 40303;  // CSRF 验证失败
        } else if (exception instanceof AuthorizationServiceException) {
            return 40304;  // 授权服务异常
        }
        return 40300;  // 默认权限不足
    }
}
