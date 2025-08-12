package com.dawn.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmailDTO {

    private String email;

    private String subject;

    private Map<String, Object> commentMap;

    private String template;

    private String verificationCode;    // 邮件中包含的验证码

}
