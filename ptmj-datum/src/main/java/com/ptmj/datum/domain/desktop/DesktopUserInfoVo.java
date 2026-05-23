package com.ptmj.datum.domain.desktop;

import lombok.Data;

/**
 * 桌面端当前登录用户返回对象
 * 撰写人：LYX
 */
public class DesktopUserInfoVo
{
    /**
     * 用户ID
     * 撰写人：LYX
     */
    private Long userId;

    /**
     * 用户名
     * 撰写人：LYX
     */
    private String userName;

    /**
     * 头像
     * 撰写人：LYX
     */
    private String avatar;

    /**
     * 账号状态
     * 撰写人：LYX
     */
    private String status;

    /**
     * 用户类型
     * 撰写人：LYX
     */
    private String userType;

    public Long getUserId()
    {
        return userId;
    }

    public void setUserId(Long userId)
    {
        this.userId = userId;
    }

    public String getUserName()
    {
        return userName;
    }

    public void setUserName(String userName)
    {
        this.userName = userName;
    }

    public String getAvatar()
    {
        return avatar;
    }

    public void setAvatar(String avatar)
    {
        this.avatar = avatar;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getUserType()
    {
        return userType;
    }

    public void setUserType(String userType)
    {
        this.userType = userType;
    }

    @Override
    public String toString() {
        return "DesktopUserInfoVo{" +
                "userId=" + userId +
                ", userName='" + userName + '\'' +
                ", avatar='" + avatar + '\'' +
                ", status='" + status + '\'' +
                ", userType='" + userType + '\'' +
                '}';
    }
}
