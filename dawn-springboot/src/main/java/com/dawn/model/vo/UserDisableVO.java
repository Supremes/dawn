package com.dawn.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "用户禁用状态")
public class UserDisableVO {

    @NotNull(message = "用户id不能为空")
    @Schema(description = "用户id")
    private Integer id;

    @NotNull(message = "用户禁用状态不能为空")
    @Schema(description = "用户禁用状态")
    private Integer isDisable;

}
