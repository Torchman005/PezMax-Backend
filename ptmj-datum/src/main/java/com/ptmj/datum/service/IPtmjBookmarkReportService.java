package com.ptmj.datum.service;

import java.util.List;
import com.ptmj.datum.domain.PtmjBookmarkReport;

/**
 * 书签举报Service接口
 *
 * @author pk
 * @date 2026-05-21
 */
public interface IPtmjBookmarkReportService
{
    public PtmjBookmarkReport selectPtmjBookmarkReportByReportId(Long reportId);

    public List<PtmjBookmarkReport> selectPtmjBookmarkReportList(PtmjBookmarkReport ptmjBookmarkReport);

    public int insertPtmjBookmarkReport(PtmjBookmarkReport ptmjBookmarkReport);

    public int updatePtmjBookmarkReport(PtmjBookmarkReport ptmjBookmarkReport);

    public int deletePtmjBookmarkReportByReportIds(Long[] reportIds);

    public int deletePtmjBookmarkReportByReportId(Long reportId);

    public String handleBookmarkReport(Long reportId, String result, String remark);
}
