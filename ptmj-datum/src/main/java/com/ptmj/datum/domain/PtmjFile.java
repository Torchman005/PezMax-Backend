package com.ptmj.datum.domain;


import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 试卷文件对象 ptmj_file
 *
 * @author pk
 * @date 2026-04-02
 */
public class PtmjFile extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 文件ID，主键 */
    private Long fileId;

    /** 上传用户ID */
    private Long userId;

    /** 文件名称 */
    @Excel(name = "文件名称")
    private String fileName;

    /** 文件URL */
    @Excel(name = "文件URL")
    private String fileUrl;

    /** 文件大小（字节） */
    @Excel(name = "文件大小", readConverterExp = "字=节")
    private Long fileSize;

    /** 文件格式，如：pdf、doc、docx、zip等 */
    @Excel(name = "文件格式，如：pdf、doc、docx、zip等")
    private String fileFormat;

    /** 文件年份，如：2024 */
    @Excel(name = "文件年份，如：2024")
    private Long fileYear;

    /** 文件类型：1-期末，2-期中，3-资料，4-补考，5-其他学校 */
  @Excel(name = "文件类型：1-期末，2-期中，3-资料，4-补考，5-其他学校")
  private Long fileType;

  /** 学校名称 */
  @Excel(name = "学校名称")
  private String fileSchool;

  /** 科目 */
  @Excel(name = "科目")
  private String fileSubject;

    /** 审核人 */
    @Excel(name = "审核人")
    private String reviewer;

    /** 文件状态：0-未审核，1-通过，2-未通过，3-被举报 */
    @Excel(name = "文件状态：0-未审核，1-通过，2-未通过，3-被举报")
    private Long fileStatus;

    /** 删除标记：0-未删除，1-已删除 */
    private Long delFlag;

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

    public void setFileName(String fileName)
    {
        this.fileName = fileName;
    }

    public String getFileName()
    {
        return fileName;
    }

    public void setFileUrl(String fileUrl)
    {
        this.fileUrl = fileUrl;
    }

    public String getFileUrl()
    {
        return fileUrl;
    }

    public void setFileSize(Long fileSize)
    {
        this.fileSize = fileSize;
    }

    public Long getFileSize()
    {
        return fileSize;
    }

    public void setFileFormat(String fileFormat)
    {
        this.fileFormat = fileFormat;
    }

    public String getFileFormat()
    {
        return fileFormat;
    }

    public void setFileYear(Long fileYear)
    {
        this.fileYear = fileYear;
    }

    public Long getFileYear()
    {
        return fileYear;
    }

    public void setFileType(Long fileType)
    {
        this.fileType = fileType;
    }

    public Long getFileType()
    {
        return fileType;
    }

    public void setFileSchool(String fileSchool)
    {
        this.fileSchool = fileSchool;
    }

    public String getFileSchool()
    {
        return fileSchool;
    }

    public void setFileSubject(String fileSubject)
    {
        this.fileSubject = fileSubject;
    }

    public String getFileSubject()
    {
        return fileSubject;
    }

    public void setReviewer(String reviewer)
    {
        this.reviewer = reviewer;
    }

    public String getReviewer()
    {
        return reviewer;
    }

    public void setFileStatus(Long fileStatus)
    {
        this.fileStatus = fileStatus;
    }

    public Long getFileStatus()
    {
        return fileStatus;
    }

    public void setDelFlag(Long delFlag)
    {
        this.delFlag = delFlag;
    }

    public Long getDelFlag()
    {
        return delFlag;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
                .append("fileId", getFileId())
                .append("userId", getUserId())
                .append("fileName", getFileName())
                .append("fileUrl", getFileUrl())
                .append("fileSize", getFileSize())
                .append("fileFormat", getFileFormat())
                .append("fileYear", getFileYear())
                .append("fileType", getFileType())
                .append("fileSchool", getFileSchool())
                .append("fileSubject", getFileSubject())
            .append("reviewer", getReviewer())
            .append("fileStatus", getFileStatus())
            .append("delFlag", getDelFlag())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .append("remark", getRemark())
                .toString();
    }
}

