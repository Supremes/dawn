package com.dawn.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "说说对象")
public class TalkVO {

    @Schema(description = "说说id")
    private Integer id;

    @NotBlank(message = "说说内容不能为空")
    @Schema(description = "说说内容", requiredMode = Schema.RequiredMode.REQUIRED)
    private String content;

    @Schema(description = "说说图片")
    private String images;

    @NotNull(message = "置顶状态不能为空")
    @Schema(description = "置顶状态")
    private Integer isTop;

    @NotNull(message = "说说状态不能为空")
    @Schema(description = "说说状态")
    private Integer status;

}
