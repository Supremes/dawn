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
@Schema(description = "照片信息")
public class PhotoInfoVO {

    @NotNull(message = "照片id不能为空")
    @Schema(description = "照片id", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer id;

    @NotBlank(message = "照片名不能为空")
    @Schema(description = "照片名", requiredMode = Schema.RequiredMode.REQUIRED)
    private String photoName;

    @Schema(description = "照片描述")
    private String photoDesc;

}
