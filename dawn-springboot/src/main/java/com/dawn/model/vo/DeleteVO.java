package com.dawn.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeleteVO {

    @NotNull(message = "id不能为空")
    @Schema(description = "要删除的id", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Integer> ids;

    @NotNull(message = "状态值不能为空")
    @Schema(description = "删除状态", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer isDelete;
}
