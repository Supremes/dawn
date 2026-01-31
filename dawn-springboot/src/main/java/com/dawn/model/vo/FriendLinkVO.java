package com.dawn.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "友链")
public class FriendLinkVO {

    @Schema(description = "友链id")
    private Integer id;

    @NotBlank(message = "链接名不能为空")
    @Schema(description = "友链名", requiredMode = Schema.RequiredMode.REQUIRED)
    private String linkName;

    @NotBlank(message = "链接头像不能为空")
    @Schema(description = "友链头像", requiredMode = Schema.RequiredMode.REQUIRED)
    private String linkAvatar;

    @NotBlank(message = "链接地址不能为空")
    @Schema(description = "友链地址", requiredMode = Schema.RequiredMode.REQUIRED)
    private String linkAddress;

    @NotBlank(message = "链接介绍不能为空")
    @Schema(description = "友链介绍", requiredMode = Schema.RequiredMode.REQUIRED)
    private String linkIntro;

}
