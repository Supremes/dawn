package com.dawn.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "角色")
public class RoleVO {

    @Schema(description = "用户id")
    private Integer id;

    @NotBlank(message = "角色名不能为空")
    @Schema(description = "角色名", requiredMode = Schema.RequiredMode.REQUIRED)
    private String roleName;

    @Schema(description = "资源列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Integer> resourceIds;

    @Schema(description = "菜单列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Integer> menuIds;

}
