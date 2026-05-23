package com.ptmj.datum.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 试卷下载对象 ptmj_file_download
 *
 * @author pk
 * @date 2026-04-02
 */
public class PtmjFileDownload extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** id */
    private Long downloadId;

    /** 文件id */
    private Long fileId;

    /** 下载的用户id */
    private Long userId;

    /** 创建者 */
    @Excel(name = "创建者")
    private String creatBy;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "创建时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date creatTime;

    public void setDownloadId(Long downloadId)
    {
        this.downloadId = downloadId;
    }

    public Long getDownloadId()
    {
        return downloadId;
    }

    public void setFileId(Long fileId)
    {
        this.fileId = fileId;
    }

    public Long getFileId()
    {
        return fileId;
    }

    public void setUserId(Long userId)
    {
        this.userId = userId;
    }

    public Long getUserId()
    {
        return userId;
    }

    public void setCreatBy(String creatBy)
    {
        this.creatBy = creatBy;
    }

    public String getCreatBy()
    {
        return creatBy;
    }

    public void setCreatTime(Date creatTime)
    {
        this.creatTime = creatTime;
    }

    public Date getCreatTime()
    {
        return creatTime;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
                .append("downloadId", getDownloadId())
                .append("fileId", getFileId())
                .append("userId", getUserId())
                .append("creatBy", getCreatBy())
                .append("creatTime", getCreatTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .append("remark", getRemark())
                .toString();
    }
}
