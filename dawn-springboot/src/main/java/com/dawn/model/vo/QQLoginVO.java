package com.dawn.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "qq登录信息")
public class QQLoginVO {

    @NotBlank(message = "openId不能为空")
    @Schema(description = "qq openId", requiredMode = Schema.RequiredMode.REQUIRED)
    private String openId;

    @NotBlank(message = "accessToken不能为空")
    @Schema(description = "qq accessToken", requiredMode = Schema.RequiredMode.REQUIRED)
    private String accessToken;

}
