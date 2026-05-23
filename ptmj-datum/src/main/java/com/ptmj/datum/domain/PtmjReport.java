package com.ptmj.datum.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 举报对象 ptmj_report
 *
 * @author pk
 * @date 2026-04-02
 */
public class PtmjReport extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 举报id */
    private Long reportId;

    /** 被举报文件id */
    @Excel(name = "被举报文件id")
    private Long fileId;

    /** 举报用户id */
    @Excel(name = "举报用户id")
    private Long userId;

    /** 被举报原因 */
    @Excel(name = "被举报原因")
    private String reason;

    /** 审核结果（0未审核 1属实 2不属实） */
    @Excel(name = "审核结果", readConverterExp = "0=未审核,1=属实,2=不属实")
    private String result;

    public void setReportId(Long reportId)
    {
        this.reportId = reportId;
    }

    public Long getReportId()
    {
        return reportId;
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

    public void setReason(String reason)
    {
        this.reason = reason;
    }

    public String getReason()
    {
        return reason;
    }

    public void setResult(String result)
    {
        this.result = result;
    }

    public String getResult()
    {
        return result;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
                .append("reportId", getReportId())
                .append("fileId", getFileId())
                .append("userId", getUserId())
                .append("reason", getReason())
                .append("result", getResult())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .append("remark", getRemark())
                .toString();
    }
}
