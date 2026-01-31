package com.dawn.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "查询条件")
public class ConditionVO {

    @Schema(name = "current", description = "页码")
    private Long current;

    @Schema(name = "size", description = "条数")
    private Long size;

    @Schema(name = "keywords", description = "搜索内容")
    private String keywords;

    @Schema(name = "categoryId", description = "分类id")
    private Integer categoryId;

    @Schema(name = "tagId", description = "标签id")
    private Integer tagId;

    @Schema(name = "albumId", description = "相册id")
    private Integer albumId;

    @Schema(name = "loginType", description = "登录类型")
    private Integer loginType;

    @Schema(name = "type", description = "类型")
    private Integer type;

    @Schema(name = "status", description = "状态")
    private Integer status;

    @Schema(name = "startTime", description = "开始时间")
    private LocalDateTime startTime;

    @Schema(name = "endTime", description = "结束时间")
    private LocalDateTime endTime;

    @Schema(name = "isDelete", description = "是否删除")
    private Integer isDelete;

    @Schema(name = "isReview", description = "是否审核")
    private Integer isReview;

    @Schema(name = "isTop", description = "是否置顶")
    private Integer isTop;

    @Schema(name = "isFeatured", description = "是否推荐")
    private Integer isFeatured;


}
