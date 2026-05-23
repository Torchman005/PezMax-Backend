package com.ruoyi.web.controller.datum;


import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ptmj.datum.domain.PtmjSecurity;
import com.ptmj.datum.service.IPtmjSecurityService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 用户密保Controller
 *
 * @author pk
 * @date 2026-04-02
 */
@RestController
@RequestMapping("/datum/security")
public class PtmjSecurityController extends BaseController
{
    @Autowired
    private IPtmjSecurityService ptmjSecurityService;

    /**
     * 查询用户密保列表
     */
    @PreAuthorize("@ss.hasPermi('datum:security:list')")
    @GetMapping("/list")
    public TableDataInfo list(PtmjSecurity ptmjSecurity)
    {
        startPage();
        List<PtmjSecurity> list = ptmjSecurityService.selectPtmjSecurityList(ptmjSecurity);
        return getDataTable(list);
    }

    /**
     * 导出用户密保列表
     */
    @PreAuthorize("@ss.hasPermi('datum:security:export')")
    @Log(title = "用户密保", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, PtmjSecurity ptmjSecurity)
    {
        List<PtmjSecurity> list = ptmjSecurityService.selectPtmjSecurityList(ptmjSecurity);
        ExcelUtil<PtmjSecurity> util = new ExcelUtil<PtmjSecurity>(PtmjSecurity.class);
        util.exportExcel(response, list, "用户密保数据");
    }

    /**
     * 获取用户密保详细信息
     */
    @PreAuthorize("@ss.hasPermi('datum:security:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(ptmjSecurityService.selectPtmjSecurityById(id));
    }

    /**
     * 新增用户密保
     */
    @PreAuthorize("@ss.hasPermi('datum:security:add')")
    @Log(title = "用户密保", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody PtmjSecurity ptmjSecurity)
    {
        return toAjax(ptmjSecurityService.insertPtmjSecurity(ptmjSecurity));
    }

    /**
     * 修改用户密保
     */
    @PreAuthorize("@ss.hasPermi('datum:security:edit')")
    @Log(title = "用户密保", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody PtmjSecurity ptmjSecurity)
    {
        return toAjax(ptmjSecurityService.updatePtmjSecurity(ptmjSecurity));
    }

    /**
     * 删除用户密保
     */
    @PreAuthorize("@ss.hasPermi('datum:security:remove')")
    @Log(title = "用户密保", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(ptmjSecurityService.deletePtmjSecurityByIds(ids));
    }
}

