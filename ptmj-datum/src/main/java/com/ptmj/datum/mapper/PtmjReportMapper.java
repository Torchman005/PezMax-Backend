package com.ptmj.datum.mapper;

import java.util.List;
import com.ptmj.datum.domain.PtmjReport;
import org.apache.ibatis.annotations.Mapper;

/**
 * 举报Mapper接口
 *
 * @author pk
 * @date 2026-04-02
 */
@Mapper
public interface PtmjReportMapper
{
    /**
     * 查询举报1
     *
     * @param reportId 举报主键
     * @return 举报1
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
     * 删除举报
     *
     * @param reportId 举报主键
     * @return 结果
     */
    public int deletePtmjReportByReportId(Long reportId);

    /**
     * 批量删除举报
     *
     * @param reportIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deletePtmjReportByReportIds(Long[] reportIds);
}
