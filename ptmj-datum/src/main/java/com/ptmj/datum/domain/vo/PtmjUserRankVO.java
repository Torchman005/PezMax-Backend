package com.ptmj.datum.domain.vo;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 * 用户排行榜 VO
 * 用于存储 Redis 中的用户排行数据（ID、昵称、头像、上传次数）
 *
 * @author pk
 */
public class PtmjUserRankVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 用户ID */
    private Long userId;

    /** 用户昵称 */
    private String userName;

    /** 用户头像 */
    private String avatar;

    /** 上传次数/积分 */
    private Long count;

    /** 备注信息 */
    private String remark;

    /** 注册时间 */
    private Date createTime;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PtmjUserRankVO that = (PtmjUserRankVO) o;
        return Objects.equals(userId, that.userId) &&
                Objects.equals(userName, that.userName) &&
                Objects.equals(avatar, that.avatar) &&
                Objects.equals(count, that.count) &&
                Objects.equals(remark, that.remark) &&
                Objects.equals(createTime, that.createTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, userName, avatar, count, remark, createTime);
    }

    @Override
    public String toString() {
        return "PtmjUserRankVO{" +
                "userId=" + userId +
                ", userName='" + userName + '\'' +
                ", avatar='" + avatar + '\'' +
                ", count=" + count +
                ", remark='" + remark + '\'' +
                ", createTime=" + createTime +
                '}';
    }
}
