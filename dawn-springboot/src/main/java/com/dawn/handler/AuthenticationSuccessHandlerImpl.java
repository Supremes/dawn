package com.dawn.handler;

import com.alibaba.fastjson.JSON;
import com.dawn.constant.CommonConstant;
import com.dawn.model.dto.UserDetailsDTO;
import com.dawn.model.dto.UserInfoDTO;
import com.dawn.entity.UserAuth;
import com.dawn.mapper.UserAuthMapper;
import com.dawn.service.TokenService;
import com.dawn.util.BeanCopyUtil;
import com.dawn.util.UserUtil;
import com.dawn.model.vo.ResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
