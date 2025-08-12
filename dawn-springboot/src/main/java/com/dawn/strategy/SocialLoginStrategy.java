package com.dawn.strategy;


import com.dawn.model.dto.UserInfoDTO;

public interface SocialLoginStrategy {

    UserInfoDTO login(String data);

}
