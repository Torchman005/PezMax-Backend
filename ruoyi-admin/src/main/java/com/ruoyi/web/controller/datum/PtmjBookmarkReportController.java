package com.ruoyi.web.controller.datum;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ptmj.datum.domain.PtmjBookmarkReport;
import com.ptmj.datum.service.IPtmjBookmarkReportService;

/**
 * 书签举报Controller
 *
 * @author pk
 * @date 2026-05-21
 */
@RestController
@RequestMapping("/datum/bookmarkReport")
public class PtmjBookmarkReportController extends BaseController
{
    @Autowired
    private IPtmjBookmarkReportService ptmjBookmarkReportService;

    @GetMapping("/list")
    public TableDataInfo list(PtmjBookmarkReport ptmjBookmarkReport)
    {
        startPage();
        List<PtmjBookmarkReport> list = ptmjBookmarkReportService.selectPtmjBookmarkReportList(ptmjBookmarkReport);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('datum:bookmarkReport:export')")
    @Log(title = "书签举报", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public AjaxResult export(PtmjBookmarkReport ptmjBookmarkReport)
    {
        List<PtmjBookmarkReport> list = ptmjBookmarkReportService.selectPtmjBookmarkReportList(ptmjBookmarkReport);
        ExcelUtil<PtmjBookmarkReport> util = new ExcelUtil<PtmjBookmarkReport>(PtmjBookmarkReport.class);
        return util.exportExcel(list, "书签举报数据");
    }

    @PreAuthorize("@ss.hasPermi('datum:bookmarkReport:query')")
    @GetMapping(value = "/{reportId}")
    public AjaxResult getInfo(@PathVariable("reportId") Long reportId)
    {
        return success(ptmjBookmarkReportService.selectPtmjBookmarkReportByReportId(reportId));
    }

    @Log(title = "书签举报", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody PtmjBookmarkReport ptmjBookmarkReport)
    {
        return toAjax(ptmjBookmarkReportService.insertPtmjBookmarkReport(ptmjBookmarkReport));
    }

    @PreAuthorize("@ss.hasPermi('datum:bookmarkReport:edit')")
    @Log(title = "书签举报", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody PtmjBookmarkReport ptmjBookmarkReport)
    {
        return toAjax(ptmjBookmarkReportService.updatePtmjBookmarkReport(ptmjBookmarkReport));
    }

    @PreAuthorize("@ss.hasPermi('datum:bookmarkReport:remove')")
    @Log(title = "书签举报", businessType = BusinessType.DELETE)
    @DeleteMapping("/{reportIds}")
    public AjaxResult remove(@PathVariable Long[] reportIds)
    {
        return toAjax(ptmjBookmarkReportService.deletePtmjBookmarkReportByReportIds(reportIds));
    }

    @GetMapping("/timeline/{reportId}")
    public AjaxResult getTimeline(@PathVariable("reportId") Long reportId)
    {
        PtmjBookmarkReport report = ptmjBookmarkReportService.selectPtmjBookmarkReportByReportId(reportId);
        if (report == null)
        {
            return error("未找到相关举报记录");
        }

        AjaxResult ajax = AjaxResult.success();
        ajax.put("userId", report.getUserId());
        ajax.put("bookmarkId", report.getBookmarkId());
        ajax.put("result", report.getResult());

        List<Map<String, Object>> timeline = new ArrayList<>();

        Map<String, Object> submitNode = new HashMap<>();
        submitNode.put("nodeName", "提交举报");
        submitNode.put("timestamp", report.getCreateTime());
        submitNode.put("status", "1");
        timeline.add(submitNode);

        if (!"0".equals(report.getResult()) && report.getUpdateTime() != null)
        {
            Map<String, Object> auditNode = new HashMap<>();
            auditNode.put("nodeName", "1".equals(report.getResult()) ? "审核通过(举报属实)" : "审核驳回(举报不属实)");
            auditNode.put("timestamp", report.getUpdateTime());
            auditNode.put("status", "1");
            timeline.add(auditNode);
        }
        else
        {
            Map<String, Object> pendingNode = new HashMap<>();
            pendingNode.put("nodeName", "等待管理员审核");
            pendingNode.put("timestamp", null);
            pendingNode.put("status", "0");
            timeline.add(pendingNode);
        }

        ajax.put("timeline", timeline);
        return ajax;
    }

    @PreAuthorize("@ss.hasPermi('datum:bookmarkReport:handle')")
    @Log(title = "书签举报处理", businessType = BusinessType.UPDATE)
    @PostMapping("/handle")
    public AjaxResult handle(@RequestParam("reportId") Long reportId,
                             @RequestParam("result") String result)
    {
        if (reportId == null || reportId <= 0)
        {
            return error("举报ID无效");
        }
        if (!"1".equals(result) && !"2".equals(result))
        {
            return error("审核结果只能为1（属实）或2（不属实）");
        }
        String msg = ptmjBookmarkReportService.handleBookmarkReport(reportId, result, null);
        return success(msg);
    }

    @PreAuthorize("@ss.hasPermi('datum:bookmarkReport:audit')")
    @Log(title = "书签举报审核", businessType = BusinessType.UPDATE)
    @PutMapping("/audit")
    public AjaxResult audit(@RequestBody PtmjBookmarkReport ptmjBookmarkReport)
    {
        Long reportId = ptmjBookmarkReport.getReportId();
        String result = ptmjBookmarkReport.getResult();
        if (reportId == null || reportId <= 0)
        {
            return error("举报ID无效");
        }
        if (!"1".equals(result) && !"2".equals(result))
        {
            return error("审核结果只能为1（属实）或2（不属实）");
        }
        String msg = ptmjBookmarkReportService.handleBookmarkReport(reportId, result, ptmjBookmarkReport.getRemark());
        return success(msg);
    }
}
