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
public class JobSearchVO {

    @Schema(name = "任务名称", description = "jobName", required = true, type = "String")
    private String jobName;

    @Schema(name = "任务组别", description = "jobGroup", required = true, type = "String")
    private String jobGroup;

    @Schema(name = "任务状态", description = "status", required = true, type = "Integer")
    private Integer status;
}
