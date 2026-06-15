-- =====================================================
-- PezMax 数据库表结构升级脚本
-- 功能：为 ptmj_file 表添加 file_school 字段
-- =====================================================

-- 1. 添加 file_school 字段
ALTER TABLE ptmj_file 
ADD COLUMN file_school VARCHAR(100) 
COMMENT '学校名称' 
AFTER file_type;

-- 2. 为现有数据设置默认值（齐鲁工业大学）
UPDATE ptmj_file 
SET file_school = '齐鲁工业大学' 
WHERE file_school IS NULL OR file_school = '';

-- 3. 验证修改结果
DESC ptmj_file;

SELECT file_id, file_name, file_school, file_subject, file_url 
FROM ptmj_file 
LIMIT 10;

-- =====================================================
-- 创建学校名称索引（可选，用于优化查询性能）
-- =====================================================
CREATE INDEX idx_file_school ON ptmj_file(file_school);

