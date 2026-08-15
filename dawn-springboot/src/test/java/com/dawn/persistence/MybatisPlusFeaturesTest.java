package com.dawn.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.exceptions.MybatisPlusException;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.dawn.persistence.mybatis.domain.MpFeatureRecord;
import com.dawn.persistence.mybatis.domain.MpRecordStatus;
import com.dawn.persistence.mybatis.mapper.MpFeatureRecordMapper;
import com.dawn.persistence.mybatis.service.MpFeatureRecordOperations;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.mybatis.spring.MyBatisSystemException;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(classes = MybatisPlusTestConfig.class)
@ActiveProfiles("mybatis-plus-test")
class MybatisPlusFeaturesTest {

    @Autowired
    private MpFeatureRecordMapper mapper;

    @Autowired
    private MpFeatureRecordOperations service;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanTable() {
        jdbcTemplate.update("DELETE FROM t_mp_feature_record");
    }

    @Test
    void enumAndLambdaWrapperUseMappedDatabaseValues() {
        MpFeatureRecord active = service.create("active-record", MpRecordStatus.ACTIVE);
        service.create("draft-record", MpRecordStatus.DRAFT);

        List<MpFeatureRecord> result = mapper.selectList(
                new LambdaQueryWrapper<MpFeatureRecord>()
                        .eq(MpFeatureRecord::getStatus, MpRecordStatus.ACTIVE)
                        .likeRight(MpFeatureRecord::getName, "active"));

        assertThat(result).extracting(MpFeatureRecord::getId).containsExactly(active.getId());
        assertThat(jdbcTemplate.queryForObject(
                "SELECT status FROM t_mp_feature_record WHERE id = ?",
                Integer.class,
                active.getId())).isEqualTo(MpRecordStatus.ACTIVE.getCode());
    }

    @Test
    void paginationReturnsRequestedSliceAndTotal() {
        service.createBatch(List.of("page-1", "page-2", "page-3", "page-4", "page-5"));

        IPage<MpFeatureRecord> page = service.pageByStatus(MpRecordStatus.DRAFT, 2, 2);

        assertThat(page.getTotal()).isEqualTo(5);
        assertThat(page.getPages()).isEqualTo(3);
        assertThat(page.getRecords()).extracting(MpFeatureRecord::getName)
                .containsExactly("page-3", "page-4");
    }

    @Test
    void automaticFillSetsInsertAndUpdateTimestamps() {
        MpFeatureRecord record = service.create("自动填充", MpRecordStatus.DRAFT);
        assertThat(record.getCreateTime()).isNotNull();
        assertThat(record.getUpdateTime()).isNull();

        boolean updated = service.rename(record.getId(), "自动填充已更新");
        MpFeatureRecord reloaded = mapper.selectById(record.getId());

        assertThat(updated).isTrue();
        assertThat(reloaded.getUpdateTime()).isNotNull();
    }

    @Test
    void serviceBatchPersistsEveryRecord() {
        boolean saved = service.createBatch(List.of("batch-a", "batch-b", "batch-c"));

        assertThat(saved).isTrue();
        assertThat(mapper.selectCount(null)).isEqualTo(3);
    }

    @Test
    void optimisticLockerRejectsStaleVersion() {
        MpFeatureRecord created = service.create("乐观锁", MpRecordStatus.DRAFT);
        MpFeatureRecord first = mapper.selectById(created.getId());
        MpFeatureRecord stale = mapper.selectById(created.getId());
        first.rename("第一次更新");
        stale.rename("过期更新");

        assertThat(mapper.updateById(first)).isEqualTo(1);
        assertThat(mapper.updateById(stale)).isZero();
        assertThat(mapper.selectById(created.getId()).getName()).isEqualTo("第一次更新");
    }

    @Test
    void tableLogicHidesSoftDeletedRows() {
        MpFeatureRecord record = service.create("逻辑删除", MpRecordStatus.ACTIVE);

        assertThat(service.softDelete(record.getId())).isTrue();

        assertThat(mapper.selectById(record.getId())).isNull();
        assertThat(jdbcTemplate.queryForObject(
                "SELECT deleted FROM t_mp_feature_record WHERE id = ?",
                Integer.class,
                record.getId())).isEqualTo(1);
    }

    @Test
    void blockAttackRejectsDeleteWithoutWhereClause() {
        service.create("禁止全表删除", MpRecordStatus.DRAFT);

        assertThatThrownBy(() -> mapper.delete(new QueryWrapper<>()))
                .isInstanceOf(MyBatisSystemException.class)
                .rootCause()
                .isInstanceOf(MybatisPlusException.class)
                .hasMessageContaining("Prohibition of table update operation");
        assertThat(mapper.selectCount(null)).isEqualTo(1);
    }
}
