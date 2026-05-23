package com.ruoyi.web.controller.datum;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ptmj.datum.domain.PtmjNotification;
import com.ptmj.datum.service.IPtmjNotificationService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 通知Controller
 * 
 * @author ruoyi
 * @date 2026-04-09
 */
@RestController
@RequestMapping("/system/notification")
public class PtmjNotificationController extends BaseController
{
    @Autowired
    private IPtmjNotificationService ptmjNotificationService;

    /**
     * 查询通知列表
     */
    @PreAuthorize("@ss.hasPermi('system:notification:list')")
    @GetMapping("/list")
    public TableDataInfo list(PtmjNotification ptmjNotification)
    {
        startPage();
        List<PtmjNotification> list = ptmjNotificationService.selectPtmjNotificationList(ptmjNotification);
        return getDataTable(list);
    }

    /**
     * 导出通知列表
     */
    @PreAuthorize("@ss.hasPermi('system:notification:export')")
    @Log(title = "通知", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, PtmjNotification ptmjNotification)
    {
        List<PtmjNotification> list = ptmjNotificationService.selectPtmjNotificationList(ptmjNotification);
        ExcelUtil<PtmjNotification> util = new ExcelUtil<PtmjNotification>(PtmjNotification.class);
        util.exportExcel(response, list, "通知数据");
    }

    /**
     * 获取通知详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:notification:query')")
    @GetMapping(value = "/{notifyId}")
    public AjaxResult getInfo(@PathVariable("notifyId") Long notifyId)
    {
        return success(ptmjNotificationService.selectPtmjNotificationByNotifyId(notifyId));
    }

    /**
     * 新增通知
     */
    @PreAuthorize("@ss.hasPermi('system:notification:add')")
    @Log(title = "通知", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody PtmjNotification ptmjNotification)
    {
        return toAjax(ptmjNotificationService.insertPtmjNotification(ptmjNotification));
    }

    /**
     * 修改通知
     */
    @PreAuthorize("@ss.hasPermi('system:notification:edit')")
    @Log(title = "通知", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody PtmjNotification ptmjNotification)
    {
        return toAjax(ptmjNotificationService.updatePtmjNotification(ptmjNotification));
    }

    /**
     * 删除通知
     */
    @PreAuthorize("@ss.hasPermi('system:notification:remove')")
    @Log(title = "通知", businessType = BusinessType.DELETE)
	@DeleteMapping("/{notifyIds}")
    public AjaxResult remove(@PathVariable Long[] notifyIds)
    {
        return toAjax(ptmjNotificationService.deletePtmjNotificationByNotifyIds(notifyIds));
    }
    /**
     * lxq
     * 获取用户端需要以弹窗形式展示的通知列表
     */
    @GetMapping("/user/popup")
    public AjaxResult getUserPopupNotifications(@RequestParam Long userId) {
        List<PtmjNotification> notifications = ptmjNotificationService.getUserPopupNotifications(userId);
        return success(notifications);
    }
    /**
     * lxq
     * 获取用户端需要以滚动形式展示的通知列表
     */
    @GetMapping("/user/scroll")
    public AjaxResult getUserScrollNotifications() {
        List<PtmjNotification> notifications = ptmjNotificationService.getUserScrollNotifications();
        return success(notifications);
    }
}
