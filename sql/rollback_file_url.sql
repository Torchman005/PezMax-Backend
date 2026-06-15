-- =====================================================
-- 回滚 file_url 变更（因为 MinIO 文件还在旧路径）
-- =====================================================

-- 设置变量
SET @SCHOOL_NAME = '齐鲁工业大学';
SET @BUCKET_NAME = 'ptmj';

-- =====================================================
-- 1. 回滚前验证
-- =====================================================
SELECT 
    COUNT(*) AS 总记录数,
    COUNT(CASE WHEN file_url LIKE CONCAT('%/', @SCHOOL_NAME, '/%') THEN 1 END AS 包含学校名的记录数
FROM ptmj_file
WHERE del_flag = 0;

-- =====================================================
-- 2. 执行回滚
-- =====================================================
-- 使用 REGEXP_REPLACE 移除学校名
UPDATE ptmj_file
SET file_url = REGEXP_REPLACE(
    file_url,
    CONCAT('(/', @BUCKET_NAME, '/[^/]+/)', @SCHOOL_NAME, '/'),
    '$1'
)
WHERE del_flag = 0
  AND file_url LIKE CONCAT('%/', @SCHOOL_NAME, '/%');

-- 检查更新行数
SELECT ROW_COUNT() AS 已回滚记录数;

-- =====================================================
-- 3. 回滚后验证
-- =====================================================
SELECT 
    file_id,
    file_name,
    file_url
FROM ptmj_file
WHERE del_flag = 0
LIMIT 10;
