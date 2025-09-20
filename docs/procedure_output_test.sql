-- 存储过程输出测试示例

-- 1. 简单查询输出
DELIMITER //
CREATE PROCEDURE TestSimpleOutput()
BEGIN
    SELECT 'Hello World' AS message, NOW() AS current_time;
END //
DELIMITER ;

-- 测试调用：CALL TestSimpleOutput();
-- 输出：一个结果集包含 message 和 current_time 字段


-- 2. OUT参数输出
DELIMITER //  
CREATE PROCEDURE TestOutParameter(OUT result_count INT, OUT result_message VARCHAR(100))
BEGIN
    SELECT COUNT(*) INTO result_count FROM t_user_info;
    SET result_message = CONCAT('找到 ', result_count, ' 个用户');
END //
DELIMITER ;

-- 测试调用：
-- CALL TestOutParameter(@count, @message);
-- SELECT @count, @message;


-- 3. 多结果集输出
DELIMITER //
CREATE PROCEDURE TestMultipleResultSets()
BEGIN
    -- 结果集1：用户统计
    SELECT 
        COUNT(*) as total_users,
        COUNT(CASE WHEN is_disable = 0 THEN 1 END) as active_users,
        COUNT(CASE WHEN is_disable = 1 THEN 1 END) as disabled_users
    FROM t_user_info;
    
    -- 结果集2：文章统计  
    SELECT 
        COUNT(*) as total_articles,
        COUNT(CASE WHEN status = 1 THEN 1 END) as published_articles,
        COUNT(CASE WHEN status = 3 THEN 1 END) as draft_articles
    FROM t_article;
    
    -- 结果集3：最新用户
    SELECT id, nickname, email, create_time 
    FROM t_user_info 
    ORDER BY create_time DESC 
    LIMIT 5;
END //
DELIMITER ;

-- 测试调用：CALL TestMultipleResultSets();
-- 输出：三个独立的结果集


-- 4. 条件输出
DELIMITER //
CREATE PROCEDURE TestConditionalOutput(IN user_id INT)
BEGIN
    DECLARE user_count INT DEFAULT 0;
    
    SELECT COUNT(*) INTO user_count FROM t_user_info WHERE id = user_id;
    
    IF user_count > 0 THEN
        -- 输出用户详情
        SELECT 
            u.id,
            u.nickname,
            u.email,
            u.create_time,
            COUNT(a.id) as article_count
        FROM t_user_info u
        LEFT JOIN t_article a ON u.id = a.user_id
        WHERE u.id = user_id
        GROUP BY u.id;
        
        -- 输出状态消息
        SELECT 'SUCCESS' as status, '用户信息查询成功' as message;
    ELSE
        -- 输出错误消息
        SELECT 'ERROR' as status, '用户不存在' as message;
    END IF;
END //
DELIMITER ;

-- 测试调用：
-- CALL TestConditionalOutput(1);  -- 存在的用户
-- CALL TestConditionalOutput(999); -- 不存在的用户


-- 5. 异常处理输出
DELIMITER //
CREATE PROCEDURE TestExceptionOutput(IN test_type VARCHAR(20))
BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        SELECT 'EXCEPTION' as status, '发生SQL异常，操作已回滚' as message;
        RESIGNAL;
    END;
    
    START TRANSACTION;
    
    CASE test_type
        WHEN 'success' THEN
            INSERT INTO t_operation_log (opt_module, opt_desc, opt_method, request_param)
            VALUES ('TEST', '测试操作', 'POST', '{}');
            SELECT 'SUCCESS' as status, '操作成功' as message;
            
        WHEN 'error' THEN  
            -- 故意触发异常
            INSERT INTO t_user_info (id) VALUES (NULL);
            
        ELSE
            SELECT 'UNKNOWN' as status, '未知的测试类型' as message;
    END CASE;
    
    COMMIT;
END //
DELIMITER ;

-- 测试调用：
-- CALL TestExceptionOutput('success'); -- 正常输出
-- CALL TestExceptionOutput('error');   -- 异常输出
-- CALL TestExceptionOutput('unknown'); -- 其他输出


-- 6. 动态SQL输出
DELIMITER //
CREATE PROCEDURE TestDynamicOutput(IN table_name VARCHAR(64))
BEGIN
    SET @sql = CONCAT('SELECT COUNT(*) as record_count FROM ', table_name);
    PREPARE stmt FROM @sql;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
    
    SELECT CONCAT('表 ', table_name, ' 的记录统计完成') as message;
END //
DELIMITER ;

-- 测试调用：
-- CALL TestDynamicOutput('t_user_info');
-- CALL TestDynamicOutput('t_article');


-- 清理测试存储过程
-- DROP PROCEDURE IF EXISTS TestSimpleOutput;
-- DROP PROCEDURE IF EXISTS TestOutParameter;  
-- DROP PROCEDURE IF EXISTS TestMultipleResultSets;
-- DROP PROCEDURE IF EXISTS TestConditionalOutput;
-- DROP PROCEDURE IF EXISTS TestExceptionOutput;
-- DROP PROCEDURE IF EXISTS TestDynamicOutput;