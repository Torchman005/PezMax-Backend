package com.ptmj.datum.service;

import java.util.List;
import com.ptmj.datum.domain.PtmjReport;

/**
 * 举报Service接口
 *
 * @author pk
 * @date 2026-04-02
 */
public interface IPtmjReportService
{
    /**
     * 查询举报
     *
     * @param reportId 举报主键
     * @return 举报
     */
    public PtmjReport selectPtmjReportByReportId(Long reportId);

    /**
     * 查询举报列表
     *
     * @param ptmjReport 举报
     * @return 举报集合
     */
    public List<PtmjReport> selectPtmjReportList(PtmjReport ptmjReport);

    /**
     * 新增举报
     *
     * @param ptmjReport 举报
     * @return 结果
     */
    public int insertPtmjReport(PtmjReport ptmjReport);

    /**
     * 修改举报
     *
     * @param ptmjReport 举报
     * @return 结果
     */
    public int updatePtmjReport(PtmjReport ptmjReport);

    /**
     * 批量删除举报
     *
     * @param reportIds 需要删除的举报主键集合
     * @return 结果
     */
    public int deletePtmjReportByReportIds(Long[] reportIds);

    /**
     * 删除举报信息
     *
     * @param reportId 举报主键
     * @return 结果
     */
    public int deletePtmjReportByReportId(Long reportId);
    String handleReport(Long reportId, String result, String remark, boolean banUser);
}