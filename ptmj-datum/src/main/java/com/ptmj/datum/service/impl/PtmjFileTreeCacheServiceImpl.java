package com.ptmj.datum.service.impl;

import com.alibaba.fastjson2.JSON;
import com.ptmj.datum.domain.FileTreeNode;
import com.ptmj.datum.domain.PtmjFile;
import com.ptmj.datum.mapper.PtmjFileMapper;
import com.ptmj.datum.service.PtmjFileTreeCacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class PtmjFileTreeCacheServiceImpl implements PtmjFileTreeCacheService {
    @Autowired
    private PtmjFileMapper fileMapper;

    //@Autowired
    private RedisTemplate<String, String> redisTemplate;

    private static final String CACHE_KEY = "ptmj:file:tree";
    private static final long CACHE_EXPIRE_HOURS = 2;

    /**
     * 获取文件树
     */
    public String getFileTree() {
        // 1. 从 Redis 读取（返回 JSON 字符串）
        String cached = redisTemplate.opsForValue().get(CACHE_KEY);
        if (cached != null) {
            return cached;
        }

        // 2. 查数据库构建树
        List<FileTreeNode> tree = buildTreeFromDb();

        // 3. 转 JSON 存入 Redis
        String treeJson = JSON.toJSONString(tree);
        redisTemplate.opsForValue().set(CACHE_KEY, treeJson, CACHE_EXPIRE_HOURS, TimeUnit.HOURS);

        return treeJson;
    }

    /**
     * 构建两层树：类型→科目+文件
     */
    private List<FileTreeNode> buildTreeFromDb() {
        PtmjFile query = new PtmjFile();
        query.setFileStatus(1L); // 只查审核通过的
        query.setDelFlag(0L);// del_flag 数据库默认是 0，查询时可不传
        List<PtmjFile> files = fileMapper.selectPtmjFileList(query);
        List<FileTreeNode> root = new ArrayList<>();

        // 按类型分组
        Map<Long, List<PtmjFile>> typeGroup = files.stream()
                .collect(Collectors.groupingBy(PtmjFile::getFileType));

        long idCounter = 1;
        for (Map.Entry<Long, List<PtmjFile>> typeEntry : typeGroup.entrySet()) {
            FileTreeNode typeNode = new FileTreeNode();
            typeNode.setId(idCounter++);
            typeNode.setLabel(getTypeLabel(typeEntry.getKey()));
            typeNode.setChildren(new ArrayList<>());

            // 按科目分组
            Map<String, List<PtmjFile>> subjectGroup = typeEntry.getValue().stream()
                    .collect(Collectors.groupingBy(PtmjFile::getFileSubject));

            for (Map.Entry<String, List<PtmjFile>> subjectEntry : subjectGroup.entrySet()) {
                FileTreeNode subjectNode = new FileTreeNode();
                subjectNode.setId(idCounter++);
                subjectNode.setLabel(subjectEntry.getKey());
                subjectNode.setChildren(new ArrayList<>());

                // 添加文件节点（只存 fileId + url）
                for (PtmjFile file : subjectEntry.getValue()) {
                    FileTreeNode fileNode = new FileTreeNode();
                    fileNode.setId(file.getFileId());
                    fileNode.setLabel(file.getFileName());
                    fileNode.setFileId(file.getFileId());
                    fileNode.setUrl(file.getFileUrl()); // 公开桶 URL
                    subjectNode.getChildren().add(fileNode);
                }

                typeNode.getChildren().add(subjectNode);
            }

            root.add(typeNode);
        }

        return root;
    }

    private String getTypeLabel(Long type) {
        return switch (type.intValue()) {
            case 1 -> "历年期中";
            case 2 -> "历年来期末";
            case 3 -> "资料";
            case 4 -> "补考题";
            case 5 -> "其他学校";
            default -> "其他";
        };
    }

    /**
     * 清除缓存
     */
    public void clearCache() {
        redisTemplate.delete(CACHE_KEY);
    }
}
