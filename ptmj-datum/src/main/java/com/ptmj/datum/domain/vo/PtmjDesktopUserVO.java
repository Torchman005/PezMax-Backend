package com.ptmj.datum.domain.vo;

/**
 * 桌面用户VO
 * @author zac
 * @date 2026-04-08 19:59
 */
public class PtmjDesktopUserVO {
    /** 用户账号 */
    private String userName;

    /** 头像地址 */
    private String avatar;


    public PtmjDesktopUserVO() {
    }

    public PtmjDesktopUserVO(String userName, String avatar) {
        this.userName = userName;
        this.avatar = avatar;
    }

    /**
     * 获取
     * @return userName
     */
    public String getUserName() {
        return userName;
    }

    /**
     * 设置
     * @param userName
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * 获取
     * @return avatar
     */
    public String getAvatar() {
        return avatar;
    }

    /**
     * 设置
     * @param avatar
     */
    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String toString() {
        return "PtmjDesktopUserVO{userName = " + userName + ", avatar = " + avatar + "}";
    }
}
