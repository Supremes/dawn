-- ======================================
-- Dawn Blog SQL 优化建议
-- ======================================
USE dawn;

-- ======================================
-- 1. 添加索引优化查询性能
-- ======================================

-- 文章表优化索引
CREATE INDEX idx_article_status_delete_time ON t_article(status, is_delete, create_time DESC);
CREATE INDEX idx_article_user_category ON t_article(user_id, category_id);
CREATE INDEX idx_article_featured_top ON t_article(is_featured, is_top, create_time DESC);

-- 评论表优化索引
CREATE INDEX idx_comment_topic_type_review ON t_comment(topic_id, type, is_review, create_time DESC);
CREATE INDEX idx_comment_user_time ON t_comment(user_id, create_time DESC);

-- 文章标签关联表索引
CREATE INDEX idx_article_tag_article ON t_article_tag(article_id);
CREATE INDEX idx_article_tag_tag ON t_article_tag(tag_id);

-- 操作日志表索引（按时间查询频繁）
CREATE INDEX idx_operation_log_time ON t_operation_log(create_time DESC);
CREATE INDEX idx_operation_log_user ON t_operation_log(user_id, create_time DESC);

-- 用户认证表索引
CREATE INDEX idx_user_auth_login_time ON t_user_auth(last_login_time DESC);

-- 访问统计表索引
CREATE INDEX idx_unique_view_date ON t_unique_view(create_time DESC);

-- 定时任务日志索引
CREATE INDEX idx_job_log_job_time ON t_job_log(job_id, create_time DESC);
CREATE INDEX idx_job_log_status ON t_job_log(status, create_time DESC);

-- ======================================
-- 2. 外键约束建议（可选）
-- ======================================

-- 文章相关外键
-- ALTER TABLE t_article ADD CONSTRAINT fk_article_user FOREIGN KEY (user_id) REFERENCES t_user_info(id);
-- ALTER TABLE t_article ADD CONSTRAINT fk_article_category FOREIGN KEY (category_id) REFERENCES t_category(id);

-- 文章标签关联外键
-- ALTER TABLE t_article_tag ADD CONSTRAINT fk_article_tag_article FOREIGN KEY (article_id) REFERENCES t_article(id);
-- ALTER TABLE t_article_tag ADD CONSTRAINT fk_article_tag_tag FOREIGN KEY (tag_id) REFERENCES t_tag(id);

-- 评论相关外键
-- ALTER TABLE t_comment ADD CONSTRAINT fk_comment_user FOREIGN KEY (user_id) REFERENCES t_user_info(id);

-- 用户角色关联外键
-- ALTER TABLE t_user_role ADD CONSTRAINT fk_user_role_user FOREIGN KEY (user_id) REFERENCES t_user_info(id);
-- ALTER TABLE t_user_role ADD CONSTRAINT fk_user_role_role FOREIGN KEY (role_id) REFERENCES t_role(id);

-- ======================================
-- 3. 表结构优化建议
-- ======================================

-- 统一字符集排序规则（建议使用 utf8mb4_general_ci）
-- ALTER TABLE t_job CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
-- ALTER TABLE t_job_log CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
-- ALTER TABLE t_exception_log CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

-- ======================================
-- 4. 分区表建议（针对大数据量表）
-- ======================================

-- 操作日志表按月分区（如果数据量很大）
/*
ALTER TABLE t_operation_log 
PARTITION BY RANGE (YEAR(create_time) * 100 + MONTH(create_time)) (
    PARTITION p202501 VALUES LESS THAN (202502),
    PARTITION p202502 VALUES LESS THAN (202503),
    PARTITION p202503 VALUES LESS THAN (202504),
    PARTITION p202504 VALUES LESS THAN (202505),
    PARTITION p202505 VALUES LESS THAN (202506),
    PARTITION p202506 VALUES LESS THAN (202507),
    PARTITION p202507 VALUES LESS THAN (202508),
    PARTITION p202508 VALUES LESS THAN (202509),
    PARTITION p202509 VALUES LESS THAN (202510),
    PARTITION p202510 VALUES LESS THAN (202511),
    PARTITION p202511 VALUES LESS THAN (202512),
    PARTITION p202512 VALUES LESS THAN (202601),
    PARTITION pmax VALUES LESS THAN MAXVALUE
);
*/

-- ======================================
-- 5. 清理和维护脚本
-- ======================================

-- 清理90天前的操作日志
DELIMITER //
CREATE PROCEDURE CleanOldOperationLogs()
BEGIN
    DELETE FROM t_operation_log 
    WHERE create_time < DATE_SUB(NOW(), INTERVAL 90 DAY);
    
    SELECT ROW_COUNT() AS deleted_rows;
END //
DELIMITER ;

-- 清理30天前的定时任务日志
DELIMITER //
CREATE PROCEDURE CleanOldJobLogs()
BEGIN
    DELETE FROM t_job_log 
    WHERE create_time < DATE_SUB(NOW(), INTERVAL 30 DAY);
    
    SELECT ROW_COUNT() AS deleted_rows;
END //
DELIMITER ;

-- 清理已删除的文章数据（物理删除）
DELIMITER //
CREATE PROCEDURE CleanDeletedArticles()
BEGIN
    -- 先删除关联的标签
    DELETE at FROM t_article_tag at
    INNER JOIN t_article a ON at.article_id = a.id
    WHERE a.is_delete = 1 AND a.update_time < DATE_SUB(NOW(), INTERVAL 30 DAY);
    
    -- 删除关联的评论
    DELETE FROM t_comment 
    WHERE topic_id IN (
        SELECT id FROM t_article 
        WHERE is_delete = 1 AND update_time < DATE_SUB(NOW(), INTERVAL 30 DAY)
    ) AND type = 1;
    
    -- 最后删除文章
    DELETE FROM t_article 
    WHERE is_delete = 1 AND update_time < DATE_SUB(NOW(), INTERVAL 30 DAY);
    
    SELECT ROW_COUNT() AS deleted_articles;
END //
DELIMITER ;

-- ======================================
-- 6. 性能监控查询
-- ======================================

-- 查看表大小和行数
CREATE VIEW v_table_stats AS
SELECT 
    TABLE_NAME as table_name,
    TABLE_ROWS as row_count,
    ROUND(DATA_LENGTH / 1024 / 1024, 2) as data_size_mb,
    ROUND(INDEX_LENGTH / 1024 / 1024, 2) as index_size_mb,
    ROUND((DATA_LENGTH + INDEX_LENGTH) / 1024 / 1024, 2) as total_size_mb
FROM information_schema.TABLES 
WHERE TABLE_SCHEMA = 'dawn'
ORDER BY (DATA_LENGTH + INDEX_LENGTH) DESC;

-- 查看慢查询相关设置
-- SHOW VARIABLES LIKE 'slow_query%';
-- SHOW VARIABLES LIKE 'long_query_time';

-- ======================================
-- 7. 数据备份建议脚本
-- ======================================

-- 每日备份脚本（仅结构和重要数据）
/*
#!/bin/bash
DATE=$(date +%Y%m%d)
BACKUP_DIR="/backup/dawn"
mkdir -p $BACKUP_DIR

# 备份结构
mysqldump -h localhost -u root -p --no-data dawn > $BACKUP_DIR/dawn_structure_$DATE.sql

# 备份重要数据（不包括日志表）
mysqldump -h localhost -u root -p --ignore-table=dawn.t_operation_log \
    --ignore-table=dawn.t_job_log --ignore-table=dawn.t_exception_log \
    dawn > $BACKUP_DIR/dawn_data_$DATE.sql

# 删除7天前的备份
find $BACKUP_DIR -name "*.sql" -mtime +7 -delete
*/

-- ======================================
-- 8. 优化配置建议
-- ======================================

/*
MySQL 配置优化建议（my.cnf）:

[mysqld]
# 基本设置
default-storage-engine = InnoDB
character-set-server = utf8mb4
collation-server = utf8mb4_general_ci

# 内存设置（根据服务器配置调整）
innodb_buffer_pool_size = 1G
key_buffer_size = 256M
query_cache_size = 128M

# 连接设置
max_connections = 500
max_connect_errors = 1000
wait_timeout = 600

# InnoDB 设置
innodb_file_per_table = 1
innodb_log_file_size = 256M
innodb_log_buffer_size = 64M
innodb_flush_log_at_trx_commit = 2

# 查询缓存
query_cache_type = 1
query_cache_limit = 8M

# 慢查询日志
slow_query_log = 1
slow_query_log_file = /var/log/mysql/slow.log
long_query_time = 2
*/

SELECT '✅ SQL优化建议已生成完成！' as message;
SELECT '建议根据实际业务需求和服务器配置调整相关参数' as note;
