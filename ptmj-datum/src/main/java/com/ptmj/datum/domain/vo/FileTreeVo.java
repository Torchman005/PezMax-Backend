package com.ptmj.datum.domain.vo;

import java.util.ArrayList;
import java.util.List;
import com.ptmj.datum.domain.PtmjFile;

/**
 * 试卷文件树节点VO对象
 *
 * @author Luminous
 * @date 2026-04-15
 */
public class FileTreeVo {

    /**
     * 节点ID（可以是文件ID，也可以是虚拟目录的唯一标识）
     */
    private String id;

    /**
     * 展示的名称
     */
    private String label;

    /**
     * 节点类型: folder 目录, file 具体文件
     */
    private String type;

    /**
     * 具体文件对象（只有当type为file时才有值）
     */
    private PtmjFile fileInfo;

    /**
     * 子节点列表
     */
    private List<FileTreeVo> children = new ArrayList<>();

    public FileTreeVo() {
    }

    public FileTreeVo(String id, String label, String type) {
        this.id = id;
        this.label = label;
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public PtmjFile getFileInfo() {
        return fileInfo;
    }

    public void setFileInfo(PtmjFile fileInfo) {
        this.fileInfo = fileInfo;
    }

    public List<FileTreeVo> getChildren() {
        return children;
    }

    public void setChildren(List<FileTreeVo> children) {
        this.children = children;
    }
}