package com.dawn.persistence.mybatis.domain;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;

import java.time.LocalDateTime;

@TableName(value = "t_mp_feature_record", autoResultMap = true)
public class MpFeatureRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private MpRecordStatus status;

    @Version
    private Integer version;

    @TableLogic(value = "0", delval = "1")
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;

    public MpFeatureRecord() {
    }

    public MpFeatureRecord(String name, MpRecordStatus status) {
        rename(name);
        this.status = status == null ? MpRecordStatus.DRAFT : status;
        this.version = 0;
        this.deleted = 0;
    }

    public void rename(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("记录名称不能为空");
        }
        this.name = name;
    }

    public void changeStatus(MpRecordStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("记录状态不能为空");
        }
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public MpRecordStatus getStatus() {
        return status;
    }

    public Integer getVersion() {
        return version;
    }

    public Integer getDeleted() {
        return deleted;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }
}
