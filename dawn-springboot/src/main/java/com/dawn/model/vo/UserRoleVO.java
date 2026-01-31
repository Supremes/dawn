package com.dawn.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "用户权限")
public class UserRoleVO {

    @NotNull(message = "id不能为空")
    @Schema(description = "用户信息id")
    private Integer userInfoId;

    @NotBlank(message = "昵称不能为空")
    @Schema(description = "昵称")
    private String nickname;

    @NotNull(message = "用户角色不能为空")
    @Schema(description = "角色id集合")
    private List<Integer> roleIds;

}
