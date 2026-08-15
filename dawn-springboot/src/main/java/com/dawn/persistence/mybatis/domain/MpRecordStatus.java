package com.dawn.persistence.mybatis.domain;

import com.baomidou.mybatisplus.annotation.EnumValue;

public enum MpRecordStatus {
    DRAFT(0),
    ACTIVE(1),
    ARCHIVED(2);

    @EnumValue
    private final int code;

    MpRecordStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
