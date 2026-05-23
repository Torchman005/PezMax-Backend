package com.ptmj.datum.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 平台用户对象 ptmj_user
 *
 * @author pk
 * @date 2026-04-02

 */

public class PtmjUser extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 用户id（不对外显示） */
    private Long userId;

    /** 用户账号 */
    @Excel(name = "用户账号")
    private String userName;

    /** 密码 */
    // LYZ修改：密码字段仅允许写入，禁止接口响应序列化输出
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Excel(name = "密码")
    private String password;

    /** 头像地址 */
    @Excel(name = "头像地址")
    private String avatar;

    /** 上传文件数量 */
    @Excel(name = "上传文件数量")
    private Long count;

    /** 账号状态（0为封禁，1为正常） */
    @Excel(name = "账号状态", readConverterExp = "0=为封禁，1为正常")
    private String status;

    /** 创建者 */
    @Excel(name = "创建者")
    private String creatBy;


    public void setUserId(Long userId)
    {
        this.userId = userId;
    }

    public Long getUserId()
    {
        return userId;
    }

    public void setUserName(String userName)
    {
        this.userName = userName;
    }

    public String getUserName()
    {
        return userName;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public String getPassword()
    {
        return password;
    }

    public void setAvatar(String avatar)
    {
        this.avatar = avatar;
    }

    public String getAvatar()
    {
        return avatar;
    }

    public void setCount(Long count)
    {
        this.count = count;
    }

    public Long getCount()
    {
        return count;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getStatus()
    {
        return status;
    }

    public void setCreatBy(String creatBy)
    {
        this.creatBy = creatBy;
    }

    public String getCreatBy()
    {
        return creatBy;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
                .append("userId", getUserId())
                .append("userName", getUserName())
                .append("password", getPassword())
                .append("avatar", getAvatar())
                .append("count", getCount())
                .append("status", getStatus())
                .append("creatBy", getCreatBy())

                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .append("remark", getRemark())
                .toString();
    }
}
