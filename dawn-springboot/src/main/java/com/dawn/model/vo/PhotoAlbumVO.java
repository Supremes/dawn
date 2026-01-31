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
@Schema(description = "相册")
public class PhotoAlbumVO {

    @Schema(description = "相册id", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer id;

    @NotBlank(message = "相册名不能为空")
    @Schema(description = "相册名", requiredMode = Schema.RequiredMode.REQUIRED)
    private String albumName;

    @Schema(description = "相册描述")
    private String albumDesc;

    @NotBlank(message = "相册封面不能为空")
    @Schema(description = "相册封面", requiredMode = Schema.RequiredMode.REQUIRED)
    private String albumCover;

    @Schema(description = "状态值", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer status;

}
