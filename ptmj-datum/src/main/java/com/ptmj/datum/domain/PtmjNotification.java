package com.ptmj.datum.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 通知对象 ptmj_notification
 * 
 * @author ruoyi
 * @date 2026-04-09
 */
public class PtmjNotification extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 通知主键 */
    private Long notifyId;

    /** 通知类型（1版本更新 2系统故障 3系统维护 4资料下架 5日常滚动） */
    @Excel(name = "通知类型", readConverterExp = "1=版本更新,2=系统故障,3=系统维护,4=资料下架,5=日常滚动")
    private String notifyType;

    /** 通知标题 */
    @Excel(name = "通知标题")
    private String title;

    /** 通知正文 */
    @Excel(name = "通知正文")
    private String content;

    /** 配置状态（0启用 1禁用） */
    @Excel(name = "配置状态", readConverterExp = "0=启用,1=禁用")
    private String status;

    /** 排序/优先级（越大越优先弹出） */
    @Excel(name = "排序/优先级", readConverterExp = "越=大越优先弹出")
    private Long sort;

    /** 展示形态（0弹窗 1滚动字幕） */
    @Excel(name = "展示形态", readConverterExp = "0=弹窗,1=滚动字幕")
    private String displayMode;

    /** 故障开始时间（仅类型2） */
    @Excel(name = "故障开始时间", readConverterExp = "仅=类型2")
    private Date faultStartTime;

    /** 故障结束时间（可为空，一直到管理员手动关；仅类型2） */
    @Excel(name = "故障结束时间", readConverterExp = "可=为空，一直到管理员手动关；仅类型2")
    private Date faultEndTime;

    /** 维护开始时间（仅类型3） */
    @Excel(name = "维护开始时间", readConverterExp = "仅=类型3")
    private Date maintenanceStartTime;

    /** 维护结束时间（可为空，一直到管理员手动关；仅类型3） */
    @Excel(name = "维护结束时间", readConverterExp = "可=为空，一直到管理员手动关；仅类型3")
    private Date maintenanceEndTime;

    /** 维护提前提醒分钟数（仅类型3） */
    @Excel(name = "维护提前提醒分钟数", readConverterExp = "仅=类型3")
    private Long remindBeforeMinutes;

    /** 接收被举报下架通知用户的id（即上传表上传用户 id；仅类型4） */
    @Excel(name = "接收被举报下架通知用户的id", readConverterExp = "即=上传表上传用户,i=d；仅类型4")
    private Long uploadUserId;

    /** 被举报下架的资料的id（关联资料表id；仅类型4） */
    @Excel(name = "被举报下架的资料的id", readConverterExp = "关=联资料表id；仅类型4")
    private Long materialId;

    /** 保存下架资料的标题，防止原资料删除后无法展示（仅类型4） */
    @Excel(name = "保存下架资料的标题，防止原资料删除后无法展示", readConverterExp = "仅=类型4")
    private String materialTitleSnapshot;

    /** 滚动日常通知展示开始时间，可为空即立即开始（仅类型5） */
    @Excel(name = "滚动日常通知展示开始时间，可为空即立即开始", readConverterExp = "仅=类型5")
    private Date publishStart;

    /** 滚动日常通知展示结束时间，可为空，一直到管理员手动关（仅类型5） */
    @Excel(name = "滚动日常通知展示结束时间，可为空，一直到管理员手动关", readConverterExp = "仅=类型5")
    private Date publishEnd;

    /** 滚动时间间隔（仅类型5） */
    @Excel(name = "滚动时间间隔", readConverterExp = "仅=类型5")
    private Long scrollTimeInterval;

    public void setNotifyId(Long notifyId) 
    {
        this.notifyId = notifyId;
    }

    public Long getNotifyId() 
    {
        return notifyId;
    }

    public void setNotifyType(String notifyType) 
    {
        this.notifyType = notifyType;
    }

    public String getNotifyType() 
    {
        return notifyType;
    }

    public void setTitle(String title) 
    {
        this.title = title;
    }

    public String getTitle() 
    {
        return title;
    }

    public void setContent(String content) 
    {
        this.content = content;
    }

    public String getContent() 
    {
        return content;
    }

    public void setStatus(String status) 
    {
        this.status = status;
    }

    public String getStatus() 
    {
        return status;
    }

    public void setSort(Long sort) 
    {
        this.sort = sort;
    }

    public Long getSort() 
    {
        return sort;
    }

    public void setDisplayMode(String displayMode) 
    {
        this.displayMode = displayMode;
    }

    public String getDisplayMode() 
    {
        return displayMode;
    }

    public void setFaultStartTime(Date faultStartTime) 
    {
        this.faultStartTime = faultStartTime;
    }

    public Date getFaultStartTime() 
    {
        return faultStartTime;
    }

    public void setFaultEndTime(Date faultEndTime) 
    {
        this.faultEndTime = faultEndTime;
    }

    public Date getFaultEndTime() 
    {
        return faultEndTime;
    }

    public void setMaintenanceStartTime(Date maintenanceStartTime) 
    {
        this.maintenanceStartTime = maintenanceStartTime;
    }

    public Date getMaintenanceStartTime() 
    {
        return maintenanceStartTime;
    }

    public void setMaintenanceEndTime(Date maintenanceEndTime) 
    {
        this.maintenanceEndTime = maintenanceEndTime;
    }

    public Date getMaintenanceEndTime() 
    {
        return maintenanceEndTime;
    }

    public void setRemindBeforeMinutes(Long remindBeforeMinutes) 
    {
        this.remindBeforeMinutes = remindBeforeMinutes;
    }

    public Long getRemindBeforeMinutes() 
    {
        return remindBeforeMinutes;
    }

    public void setUploadUserId(Long uploadUserId) 
    {
        this.uploadUserId = uploadUserId;
    }

    public Long getUploadUserId() 
    {
        return uploadUserId;
    }

    public void setMaterialId(Long materialId) 
    {
        this.materialId = materialId;
    }

    public Long getMaterialId() 
    {
        return materialId;
    }

    public void setMaterialTitleSnapshot(String materialTitleSnapshot) 
    {
        this.materialTitleSnapshot = materialTitleSnapshot;
    }

    public String getMaterialTitleSnapshot() 
    {
        return materialTitleSnapshot;
    }

    public void setPublishStart(Date publishStart) 
    {
        this.publishStart = publishStart;
    }

    public Date getPublishStart() 
    {
        return publishStart;
    }

    public void setPublishEnd(Date publishEnd) 
    {
        this.publishEnd = publishEnd;
    }

    public Date getPublishEnd() 
    {
        return publishEnd;
    }

    public void setScrollTimeInterval(Long scrollTimeInterval) 
    {
        this.scrollTimeInterval = scrollTimeInterval;
    }

    public Long getScrollTimeInterval() 
    {
        return scrollTimeInterval;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("notifyId", getNotifyId())
            .append("notifyType", getNotifyType())
            .append("title", getTitle())
            .append("content", getContent())
            .append("status", getStatus())
            .append("sort", getSort())
            .append("displayMode", getDisplayMode())
            .append("faultStartTime", getFaultStartTime())
            .append("faultEndTime", getFaultEndTime())
            .append("maintenanceStartTime", getMaintenanceStartTime())
            .append("maintenanceEndTime", getMaintenanceEndTime())
            .append("remindBeforeMinutes", getRemindBeforeMinutes())
            .append("uploadUserId", getUploadUserId())
            .append("materialId", getMaterialId())
            .append("materialTitleSnapshot", getMaterialTitleSnapshot())
            .append("publishStart", getPublishStart())
            .append("publishEnd", getPublishEnd())
            .append("scrollTimeInterval", getScrollTimeInterval())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
