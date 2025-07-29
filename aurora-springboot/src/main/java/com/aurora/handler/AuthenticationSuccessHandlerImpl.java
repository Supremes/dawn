package com.aurora.handler;

import com.alibaba.fastjson.JSON;
import com.aurora.constant.CommonConstant;
import com.aurora.model.dto.UserDetailsDTO;
import com.aurora.model.dto.UserInfoDTO;
import com.aurora.entity.UserAuth;
import com.aurora.mapper.UserAuthMapper;
import com.aurora.service.TokenService;
import com.aurora.util.BeanCopyUtil;
import com.aurora.util.UserUtil;
import com.aurora.model.vo.ResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;


@Component
public class AuthenticationSuccessHandlerImpl implements AuthenticationSuccessHandler {

    @Autowired
    private UserAuthMapper userAuthMapper;

    @Autowired
    private TokenService tokenService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        UserDetailsDTO userDetailsDTO = (UserDetailsDTO) authentication.getPrincipal();
        UserInfoDTO userLoginDTO = BeanCopyUtil.copyObject(userDetailsDTO, UserInfoDTO.class);
        
        // 生成JWT token
        String token = tokenService.createToken(userDetailsDTO);
        userLoginDTO.setToken(token);
        
        // 返回登录成功响应
        response.setContentType(CommonConstant.APPLICATION_JSON);
        response.getWriter().write(JSON.toJSONString(ResultVO.ok(userLoginDTO)));
        
        // 异步更新用户登录信息
        updateUserInfo(userDetailsDTO);
    }

    @Async
    public void updateUserInfo(UserDetailsDTO userDetailsDTO) {
        UserAuth userAuth = UserAuth.builder()
                .id(userDetailsDTO.getId())
                .ipAddress(userDetailsDTO.getIpAddress())
                .ipSource(userDetailsDTO.getIpSource())
                .lastLoginTime(userDetailsDTO.getLastLoginTime())
                .build();
        userAuthMapper.updateById(userAuth);
    }
}
