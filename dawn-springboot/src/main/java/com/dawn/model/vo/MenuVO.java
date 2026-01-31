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
@Schema(description = "菜单")
public class MenuVO {

    @Schema(description = "菜单id")
    private Integer id;

    @NotBlank(message = "菜单名不能为空")
    @Schema(description = "菜单名", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @NotBlank(message = "菜单icon不能为空")
    @Schema(description = "菜单icon", requiredMode = Schema.RequiredMode.REQUIRED)
    private String icon;

    @NotBlank(message = "路径不能为空")
    @Schema(description = "路径", requiredMode = Schema.RequiredMode.REQUIRED)
    private String path;

    @NotBlank(message = "组件不能为空")
    @Schema(description = "组件", requiredMode = Schema.RequiredMode.REQUIRED)
    private String component;

    @NotNull(message = "排序不能为空")
    @Schema(description = "排序")
    private Integer orderNum;

    @Schema(description = "父id")
    private Integer parentId;

    @Schema(description = "是否隐藏")
    private Integer isHidden;

}
