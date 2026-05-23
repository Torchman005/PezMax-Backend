package com.ptmj.datum.domain;
import lombok.Data;

import java.util.List;


//前端预览文件要树状结构的json，这里这个就是每一个节点————范光友
@Data
public class FileTreeNode {
    private Long id;               // 节点 ID
    private String label;          // 显示名称
    private Long fileId;           // 文件 ID（叶子节点）
    private String url;            // 文件 URL（叶子节点）
    private List<FileTreeNode> children; // 子节点
}
