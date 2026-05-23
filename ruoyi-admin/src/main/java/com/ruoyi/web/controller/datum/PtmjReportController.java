package com.ruoyi.web.controller.datum;


import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ptmj.datum.domain.PtmjReport;
import com.ptmj.datum.service.IPtmjReportService;

/**
 * 举报Controller
 *
 * @author pk
 * @date 2026-04-02
 */
@RestController
@RequestMapping("/datum/report")
public class PtmjReportController extends BaseController
{
    @Autowired
    private IPtmjReportService ptmjReportService;

    /**
     * 查询举报列表
     */
//    @PreAuthorize("@ss.hasPermi('datum:report:list')")
    @GetMapping("/list")
    public TableDataInfo list(PtmjReport ptmjReport)
    {
        startPage();
        List<PtmjReport> list = ptmjReportService.selectPtmjReportList(ptmjReport);
        return getDataTable(list);
    }

    /**
     * 导出举报列表
     */
    @PreAuthorize("@ss.hasPermi('datum:report:export')")
    @Log(title = "举报", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public AjaxResult export(PtmjReport ptmjReport)
    {
        List<PtmjReport> list = ptmjReportService.selectPtmjReportList(ptmjReport);
        ExcelUtil<PtmjReport> util = new ExcelUtil<PtmjReport>(PtmjReport.class);
        return util.exportExcel(list, "举报数据");
    }

    /**
     * 获取举报详细信息
     */
    @PreAuthorize("@ss.hasPermi('datum:report:query')")
    @GetMapping(value = "/{reportId}")
    public AjaxResult getInfo(@PathVariable("reportId") Long reportId)
    {
        return success(ptmjReportService.selectPtmjReportByReportId(reportId));
    }

    /**
     * 新增举报（通常由用户端调用，这里不强制权限）
     */
//    @PreAuthorize("@ss.hasPermi('datum:report:add')")
    @Log(title = "举报", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody PtmjReport ptmjReport)
    {
        // 实际项目中需要校验当前登录用户，避免伪造举报人
        return toAjax(ptmjReportService.insertPtmjReport(ptmjReport));
    }

    /**
     * 修改举报
     */
    @PreAuthorize("@ss.hasPermi('datum:report:edit')")
    @Log(title = "举报", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody PtmjReport ptmjReport)
    {
        return toAjax(ptmjReportService.updatePtmjReport(ptmjReport));
    }

    /**
     * 删除举报
     */
    @PreAuthorize("@ss.hasPermi('datum:report:remove')")
    @Log(title = "举报", businessType = BusinessType.DELETE)
    @DeleteMapping("/{reportIds}")
    public AjaxResult remove(@PathVariable Long[] reportIds)
    {
        return toAjax(ptmjReportService.deletePtmjReportByReportIds(reportIds));
    }

    /**
     * 获取举报处理时间线及进度
     * 用户端依据举报ID，查看举报流程的时间戳以及当前状态
     */
    @GetMapping("/timeline/{reportId}")
    public AjaxResult getTimeline(@PathVariable("reportId") Long reportId)
    {
        PtmjReport report = ptmjReportService.selectPtmjReportByReportId(reportId);
        if (report == null)
        {
            return error("未找到相关举报记录");
        }

        AjaxResult ajax = AjaxResult.success();
        // 举报的用户ID
        ajax.put("userId", report.getUserId());
        // 举报文件的ID
        ajax.put("fileId", report.getFileId());
        // 审核状态（0:未审核，1:审核通过/属实，2:审核驳回/不属实）
        ajax.put("result", report.getResult());

        // 构造时间线列表记录过程时间戳
        List<Map<String, Object>> timeline = new ArrayList<>();

        // 第一步：用户提交举报的时间点（必须存在）
        Map<String, Object> submitNode = new HashMap<>();
        submitNode.put("nodeName", "提交举报");
        submitNode.put("timestamp", report.getCreateTime());
        submitNode.put("status", "1"); // 已完成
        timeline.add(submitNode);

        // 第二步：审核处理时间点（如果不是“未审核”则表已处理）
        if (!"0".equals(report.getResult()) && report.getUpdateTime() != null)
        {
            Map<String, Object> auditNode = new HashMap<>();
            auditNode.put("nodeName", "1".equals(report.getResult()) ? "审核通过(举报属实)" : "审核驳回(举报不属实)");
            auditNode.put("timestamp", report.getUpdateTime());
            auditNode.put("status", "1"); // 已完成
            timeline.add(auditNode);
        }
        else
        {
            Map<String, Object> pendingNode = new HashMap<>();
            pendingNode.put("nodeName", "等待管理员审核");
            pendingNode.put("timestamp", null);
            pendingNode.put("status", "0"); // 未处理/进行中
            timeline.add(pendingNode);
        }

        ajax.put("timeline", timeline);
        return ajax;
    }

    /**
     * 处理举报（审核+封禁）/**
     *  *
     *  * @author lzt
     *  * @date 2026-04-13
     *
     * @param reportId 举报ID
     * @param result 审核结果（1-属实，2-不属实）
     * @param banUser 是否封禁上传用户（布尔值，true/false）
     */
    @PreAuthorize("@ss.hasPermi('datum:report:handle')")
    @Log(title = "举报", businessType = BusinessType.UPDATE)
    @PostMapping("/handle")
    public AjaxResult handle(@RequestParam("reportId") Long reportId,
                             @RequestParam("result") String result,
                             @RequestParam(value = "banUser", defaultValue = "false") boolean banUser)
    {
        // 参数校验
        if (reportId == null || reportId <= 0)
        {
            return error("举报ID无效");
        }
        if (!"1".equals(result) && !"2".equals(result))
        {
            return error("审核结果只能为1（属实）或2（不属实）");
        }
        if ("1".equals(result) && banUser)
        {
            // 可选：增加二次确认或提示
        }
        String msg = ptmjReportService.handleReport(reportId, result, null, banUser);
        return success(msg);
    }

    /**
     * 审核举报（前端管理端）
     */
    @PreAuthorize("@ss.hasPermi('datum:report:audit')")
    @Log(title = "举报审核", businessType = BusinessType.UPDATE)
    @PutMapping("/audit")
    public AjaxResult audit(@RequestBody PtmjReport ptmjReport)
    {
        Long reportId = ptmjReport.getReportId();
        String result = ptmjReport.getResult();
        if (reportId == null || reportId <= 0)
        {
            return error("举报ID无效");
        }
        if (!"1".equals(result) && !"2".equals(result))
        {
            return error("审核结果只能为1（属实）或2（不属实）");
        }
        String msg = ptmjReportService.handleReport(reportId, result, ptmjReport.getRemark(), false);
        return success(msg);
    }
}

