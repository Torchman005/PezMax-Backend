-- =====================================================
-- PezMax 数据库路径升级脚本
-- 功能：在 file_url 路径中添加学校名层级
-- 原路径: /ptmj/{学科}/{类型}/{年份}/{文件名}
-- 新路径: /ptmj/{学科}/{学校名}/{类型}/{年份}/{文件名}
-- =====================================================

-- 设置变量
SET @SCHOOL_NAME = '齐鲁工业大学';
SET @BUCKET_NAME = 'ptmj';

-- =====================================================
-- 1. 更新前验证：统计需要更新的记录数
-- =====================================================
SELECT 
    COUNT(*) AS 需要更新的记录数,
    COUNT(CASE WHEN file_url NOT LIKE CONCAT('%/', @SCHOOL_NAME, '/%') THEN 1 END) AS 不包含学校名的记录数,
    COUNT(CASE WHEN file_url LIKE CONCAT('%/', @SCHOOL_NAME, '/%') THEN 1 END) AS 已包含学校名的记录数
FROM ptmj_file
WHERE del_flag = 0;

-- 查看需要更新的样本数据
SELECT 
    file_id,
    file_name,
    file_url AS 原URL
FROM ptmj_file
WHERE del_flag = 0
  AND file_url NOT LIKE CONCAT('%/', @SCHOOL_NAME, '/%')
LIMIT 10;

-- =====================================================
-- 2. 执行更新操作
-- =====================================================
-- 开启事务（可选，建议在生产环境使用）
-- START TRANSACTION;

-- 使用 REGEXP_REPLACE 进行精确替换
-- 模式说明：匹配 /ptmj/后面跟任意非/字符的部分，然后插入学校名
UPDATE ptmj_file
SET file_url = REGEXP_REPLACE(
    file_url,
    CONCAT('(/', @BUCKET_NAME, '/)([^/]+/)'),
    CONCAT('$1$2', @SCHOOL_NAME, '/')
)
WHERE del_flag = 0
  AND file_url LIKE CONCAT('%/', @BUCKET_NAME, '/%')
  AND file_url NOT LIKE CONCAT('%/', @SCHOOL_NAME, '/%');

-- 检查更新行数
SELECT ROW_COUNT() AS 已更新记录数;

-- 提交事务（如果开启了事务）
-- COMMIT;

-- =====================================================
-- 3. 更新后验证
-- =====================================================
-- 统计更新后的数据
SELECT 
    COUNT(*) AS 总记录数,
    COUNT(CASE WHEN file_url LIKE CONCAT('%/', @SCHOOL_NAME, '/%') THEN 1 END) AS 包含学校名的记录数,
    COUNT(CASE WHEN file_url NOT LIKE CONCAT('%/', @SCHOOL_NAME, '/%') THEN 1 END) AS 不包含学校名的记录数
FROM ptmj_file
WHERE del_flag = 0;

-- 查看更新后的样本数据
SELECT 
    file_id,
    file_name,
    file_url AS 新URL
FROM ptmj_file
WHERE del_flag = 0
LIMIT 10;

-- 查找可能更新失败的记录（仍然不包含学校名的）
SELECT 
    file_id,
    file_name,
    file_url
FROM ptmj_file
WHERE del_flag = 0
  AND file_url NOT LIKE CONCAT('%/', @SCHOOL_NAME, '/%');

-- =====================================================
-- 回滚脚本（如需回滚，取消以下注释执行）
-- =====================================================
-- SET @SCHOOL_NAME = '齐鲁工业大学';
-- SET @BUCKET_NAME = 'ptmj';
-- 
-- UPDATE ptmj_file
-- SET file_url = REGEXP_REPLACE(
--     file_url,
--     CONCAT('(/', @BUCKET_NAME, '/)([^/]+/)', @SCHOOL_NAME, '/'),
--     '$1$2'
-- )
-- WHERE del_flag = 0
--   AND file_url LIKE CONCAT('%/', @SCHOOL_NAME, '/%');

