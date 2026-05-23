package com.ptmj.datum.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 外部书签对象 ptmj_bookmark
 */
public class PtmjBookmark extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;

    /** 所属用户ID */
    private Long userId;

    /** 目标链接 */
    @Excel(name = "目标链接")
    private String url;

    /** 标题 */
    @Excel(name = "标题")
    private String title;

    /** 描述 */
    @Excel(name = "描述")
    private String description;

    /** 封面图 */
    @Excel(name = "封面图")
    private String coverImage;

    /** 学科/分类 */
    @Excel(name = "学科")
    private String subject;

    /** 资源类型 */
    @Excel(name = "资源类型")
    @JsonAlias("resource_type")
    private String resourceType;

    /** 所属专栏 */
    @Excel(name = "所属专栏")
    private String collection;

    /** 关联用户信息 */
    private PtmjUser user;

    /** 状态：0-默认，1-启用，2-停用（可按业务扩展） */
    private Long status;

    /** 删除标记：0-未删除，1-已删除 */
    private Long delFlag;

    /** 统一模糊搜索关键字，匹配标题或描述 */
    private String keyword;

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getId()
    {
        return id;
    }

    public void setUserId(Long userId)
    {
        this.userId = userId;
    }

    public Long getUserId()
    {
        return userId;
    }

    public void setUrl(String url)
    {
        this.url = url;
    }

    public String getUrl()
    {
        return url;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public String getTitle()
    {
        return title;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getDescription()
    {
        return description;
    }

    public void setCoverImage(String coverImage)
    {
        this.coverImage = coverImage;
    }

    public String getCoverImage()
    {
        return coverImage;
    }

    public void setSubject(String subject)
    {
        this.subject = subject;
    }

    public String getSubject()
    {
        return subject;
    }

    public void setResourceType(String resourceType)
    {
        this.resourceType = resourceType;
    }

    public String getResourceType()
    {
        return resourceType;
    }

    public void setCollection(String collection)
    {
        this.collection = collection;
    }

    public String getCollection()
    {
        return collection;
    }

    public void setUser(PtmjUser user)
    {
        this.user = user;
    }

    public PtmjUser getUser()
    {
        return user;
    }

    public void setStatus(Long status)
    {
        this.status = status;
    }

    public Long getStatus()
    {
        return status;
    }

    public void setDelFlag(Long delFlag)
    {
        this.delFlag = delFlag;
    }

    public Long getDelFlag()
    {
        return delFlag;
    }

    public void setKeyword(String keyword)
    {
        this.keyword = keyword;
    }

    public String getKeyword()
    {
        return keyword;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("userId", getUserId())
            .append("url", getUrl())
            .append("title", getTitle())
            .append("description", getDescription())
            .append("coverImage", getCoverImage())
            .append("subject", getSubject())
            .append("resourceType", getResourceType())
            .append("collection", getCollection())
            .append("status", getStatus())
            .append("delFlag", getDelFlag())
            .append("keyword", getKeyword())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
