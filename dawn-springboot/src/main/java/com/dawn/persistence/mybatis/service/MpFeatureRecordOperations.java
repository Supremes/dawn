package com.dawn.persistence.mybatis.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.dawn.persistence.mybatis.domain.MpFeatureRecord;
import com.dawn.persistence.mybatis.domain.MpRecordStatus;

import java.util.List;

public interface MpFeatureRecordOperations {

    MpFeatureRecord create(String name, MpRecordStatus status);

    boolean createBatch(List<String> names);

    IPage<MpFeatureRecord> pageByStatus(MpRecordStatus status, long current, long size);

    boolean rename(Long id, String name);

    boolean softDelete(Long id);
}
