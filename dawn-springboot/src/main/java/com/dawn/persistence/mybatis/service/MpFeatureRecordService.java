package com.dawn.persistence.mybatis.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dawn.persistence.mybatis.domain.MpFeatureRecord;
import com.dawn.persistence.mybatis.domain.MpRecordStatus;
import com.dawn.persistence.mybatis.mapper.MpFeatureRecordMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class MpFeatureRecordService
        extends ServiceImpl<MpFeatureRecordMapper, MpFeatureRecord>
        implements MpFeatureRecordOperations {

    @Override
    @Transactional
    public MpFeatureRecord create(String name, MpRecordStatus status) {
        MpFeatureRecord record = new MpFeatureRecord(name, status);
        save(record);
        return record;
    }

    @Override
    @Transactional
    public boolean createBatch(List<String> names) {
        List<MpFeatureRecord> records = names.stream()
                .map(name -> new MpFeatureRecord(name, MpRecordStatus.DRAFT))
                .toList();
        return saveBatch(records);
    }

    @Override
    public IPage<MpFeatureRecord> pageByStatus(
            MpRecordStatus status,
            long current,
            long size) {
        return page(
                new Page<>(current, size),
                new LambdaQueryWrapper<MpFeatureRecord>()
                        .eq(MpFeatureRecord::getStatus, status)
                        .orderByAsc(MpFeatureRecord::getId));
    }

    @Override
    @Transactional
    public boolean rename(Long id, String name) {
        MpFeatureRecord record = getById(id);
        if (record == null) {
            throw new IllegalArgumentException("记录不存在: " + id);
        }
        record.rename(name);
        return updateById(record);
    }

    @Override
    @Transactional
    public boolean softDelete(Long id) {
        return removeById(id);
    }
}
