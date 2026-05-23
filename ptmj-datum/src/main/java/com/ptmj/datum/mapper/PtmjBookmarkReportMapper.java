package com.ptmj.datum.mapper;

import java.util.List;
import com.ptmj.datum.domain.PtmjBookmarkReport;
import org.apache.ibatis.annotations.Mapper;

/**
 * 书签举报Mapper接口
 *
 * @author pk
 * @date 2026-05-21
 */
@Mapper
public interface PtmjBookmarkReportMapper
{
    public PtmjBookmarkReport selectPtmjBookmarkReportByReportId(Long reportId);

    public List<PtmjBookmarkReport> selectPtmjBookmarkReportList(PtmjBookmarkReport ptmjBookmarkReport);

    public int insertPtmjBookmarkReport(PtmjBookmarkReport ptmjBookmarkReport);

    public int updatePtmjBookmarkReport(PtmjBookmarkReport ptmjBookmarkReport);

    public int deletePtmjBookmarkReportByReportId(Long reportId);

    public int deletePtmjBookmarkReportByReportIds(Long[] reportIds);
}
