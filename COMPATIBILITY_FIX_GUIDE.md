# 🔧 紧急修复：MinIO 旧文件访问问题

## ❌ 问题

部署成功了，但是历史文件访问不到！因为：
- 我们更新了数据库中的 `file_url`（添加了学校名
- 但是 **MinIO 中的实际文件还在旧路径**！

| 位置 | 路径 |
|------|------|
| 数据库（已更新） | `/ptmj/科目/齐鲁工业大学/类型/年份/文件` ❌ |
| MinIO（未动） | `/ptmj/科目/类型/年份/文件` ✅ |

---

## ✅ 解决方案

### 第一步：立即回滚数据库

```bash
# 1. SSH 登录服务器
ssh ubuntu@your-server-ip

# 2. 进入项目目录
cd /home/ubuntu/PezMax/pezmax-backend

# 3. 执行回滚 SQL
mysql -u root -p ptmj-platform < sql/rollback_file_url.sql
```

或者手动执行 SQL：
```sql
-- 回滚 file_url，移除学校名
UPDATE ptmj_file
SET file_url = REGEXP_REPLACE(
    file_url,
    '(/ptmj/[^/]+/)齐鲁工业大学/',
    '$1'
)
WHERE del_flag = 0
  AND file_url LIKE '%/齐鲁工业大学/%';
```

---

### 第二步：确认回滚成功

```sql
-- 检查几个样本
SELECT file_id, file_name, file_url 
FROM ptmj_file 
WHERE del_flag = 0 
LIMIT 10;
```

确认 URL 中**没有** "齐鲁工业大学" 了。

---

### 第三步：保留学校字段功能，但保持路径兼容

好消息是，我们不需要完全回滚代码！只需要调整策略：

| 项目 | 策略 |
|------|------|
| **历史文件** | `file_url` 保持原路径，`file_school` 设置为 "齐鲁工业大学" |
| **新文件** | `file_url` 使用新路径（带学校名，`file_school` 正常存储 |
| **文件树显示** | 使用 `file_school` 字段显示层级，不依赖 URL |

---

## 📋 完整操作步骤

### 1. 立即执行回滚

```bash
# 在服务器上执行
cd /home/ubuntu/PezMax/pezmax-backend
mysql -u root -p'123456' ptmj-platform < sql/rollback_file_url.sql
```

### 2. 验证历史文件可以访问

随便找几个旧文件的 URL，确认可以在浏览器打开。

### 3. 新文件功能保持不变

新上传的文件会自动使用新路径（带学校名，这没问题，因为它们是新上传到 MinIO 新路径的。

---

## 🔮 未来改进（可选

如果将来想把历史文件也迁移到新路径：

### 方案A：脚本迁移 MinIO 文件（谨慎
编写一个脚本：
1. 读取所有历史文件的旧路径
2. 在 MinIO 中复制到新路径
3. 更新数据库的 file_url

### 方案B：保持双路径（推荐
保持现状：
- 历史文件在旧路径
- 新文件在新路径
- 依靠 file_school 字段来做文件树展示

这样最安全，没有数据丢失风险。

---

## 🎯 总结

✅ **已创建的文件：**
- `sql/rollback_file_url.sql` - 回滚脚本
- `COMPATIBILITY_FIX_GUIDE.md` - 本文档

✅ **立即执行：**
1. 运行 `sql/rollback_file_url.sql` 回滚数据库
2. 测试历史文件可以访问
3. 新功能继续使用，无需修改代码
