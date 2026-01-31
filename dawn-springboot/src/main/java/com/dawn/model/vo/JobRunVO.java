package com.dawn.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobRunVO {

    @Schema(description = "任务id", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer id;

    @Schema(description = "任务组别", requiredMode = Schema.RequiredMode.REQUIRED)
    private String jobGroup;
}
