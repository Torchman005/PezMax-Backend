package com.ptmj.datum.service;

import java.util.List;
import com.ptmj.datum.domain.PtmjFileDownload;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 试卷下载Service接口
 *
 * @author pk
 * @date 2026-04-02
 */
public interface IPtmjFileDownloadService
{
    /**
     * 查询试卷下载
     *
     * @param downloadId 试卷下载主键
     * @return 试卷下载
     */
    public PtmjFileDownload selectPtmjFileDownloadByDownloadId(Long downloadId);

    /**
     * 查询试卷下载列表
     *
     * @param ptmjFileDownload 试卷下载
     * @return 试卷下载集合
     */
    public List<PtmjFileDownload> selectPtmjFileDownloadList(PtmjFileDownload ptmjFileDownload);

    /**
     * 流式下载文件
     *
     * @param fileId 文件ID，可选
     * @param fileUrl 文件地址，可选
     * @param response 响应对象
     * @throws Exception 下载异常
     */
    public void downloadFile(Long fileId, String fileUrl, HttpServletResponse response) throws Exception;

    /**
     * 新增试卷下载
     *
     * @param ptmjFileDownload 试卷下载
     * @return 结果
     */
    public int insertPtmjFileDownload(PtmjFileDownload ptmjFileDownload);

    /**
     * 修改试卷下载
     *
     * @param ptmjFileDownload 试卷下载
     * @return 结果
     */
    public int updatePtmjFileDownload(PtmjFileDownload ptmjFileDownload);

    /**
     * 批量删除试卷下载
     *
     * @param downloadIds 需要删除的试卷下载主键集合
     * @return 结果
     */
    public int deletePtmjFileDownloadByDownloadIds(Long[] downloadIds);

    /**
     * 删除试卷下载信息
     *
     * @param downloadId 试卷下载主键
     * @return 结果
     */
    public int deletePtmjFileDownloadByDownloadId(Long downloadId);
}
