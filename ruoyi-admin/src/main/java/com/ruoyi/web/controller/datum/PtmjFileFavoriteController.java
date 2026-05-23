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
import com.ptmj.datum.domain.PtmjFile;
import com.ptmj.datum.domain.PtmjFileFavorite;
import com.ptmj.datum.mapper.PtmjFileFavoriteMapper;
import com.ptmj.datum.service.IPtmjDesktopFileFavoriteService;
import com.ptmj.datum.service.IPtmjFileFavoriteService;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 试卷收藏Controller
 *
 * @author pk
 * @date 2026-04-02
 */
@RestController
@RequestMapping("/datum")
public class PtmjFileFavoriteController extends BaseController
{
    @Autowired
    private IPtmjFileFavoriteService ptmjFileFavoriteService;

    @Autowired
    private IPtmjDesktopFileFavoriteService ptmjDesktopFileFavoriteService;

    @Autowired
    private PtmjFileFavoriteMapper ptmjFileFavoriteMapper;

    /**
     * 查询试卷收藏列表
     */
//    @PreAuthorize("@ss.hasPermi('datum:favorite:list')")
    @GetMapping("/favorite/list")
    public TableDataInfo list(PtmjFileFavorite ptmjFileFavorite)
    {
        startPage();
        List<PtmjFileFavorite> list = ptmjFileFavoriteService.selectPtmjFileFavoriteList(ptmjFileFavorite);
        return getDataTable(list);
    }

    /**
     * 导出试卷收藏列表
     */
    @PreAuthorize("@ss.hasPermi('datum:favorite:export')")
    @Log(title = "试卷收藏", businessType = BusinessType.EXPORT)
    @PostMapping("/favorite/export")
    public void export(HttpServletResponse response, PtmjFileFavorite ptmjFileFavorite)
    {
        List<PtmjFileFavorite> list = ptmjFileFavoriteService.selectPtmjFileFavoriteList(ptmjFileFavorite);
        ExcelUtil<PtmjFileFavorite> util = new ExcelUtil<PtmjFileFavorite>(PtmjFileFavorite.class);
        util.exportExcel(response, list, "试卷收藏数据");
    }

    /**
     * 获取试卷收藏详细信息
     */
//    @PreAuthorize("@ss.hasPermi('datum:favorite:query')")
    @GetMapping(value = "/favorite/{fileId}")
    public AjaxResult getInfo(@PathVariable("fileId") Long fileId)
    {
        return success(ptmjFileFavoriteService.selectPtmjFileFavoriteByFileId(fileId));
    }

    /**
     * 新增试卷收藏
     */
//    @PreAuthorize("@ss.hasPermi('datum:favorite:add')")
    @Log(title = "试卷收藏", businessType = BusinessType.INSERT)
    @PostMapping("/favorite")
    public AjaxResult add(@RequestBody PtmjFileFavorite ptmjFileFavorite)
    {
        return toAjax(ptmjFileFavoriteService.insertPtmjFileFavorite(ptmjFileFavorite));
    }

    /**
     * 修改试卷收藏
     */
//    @PreAuthorize("@ss.hasPermi('datum:favorite:edit')")
    @Log(title = "试卷收藏", businessType = BusinessType.UPDATE)
    @PutMapping("/favorite")
    public AjaxResult edit(@RequestBody PtmjFileFavorite ptmjFileFavorite)
    {
        return toAjax(ptmjFileFavoriteService.updatePtmjFileFavorite(ptmjFileFavorite));
    }

    /**
     * 删除试卷收藏
     */
//    @PreAuthorize("@ss.hasPermi('datum:favorite:remove')")
    @Log(title = "试卷收藏", businessType = BusinessType.DELETE)
    @DeleteMapping("/favorite/{fileIds}")
    public AjaxResult remove(@PathVariable Long[] fileIds)
    {
        return toAjax(ptmjFileFavoriteService.deletePtmjFileFavoriteByFileIds(fileIds));
    }

    /**
     * 查询桌面端用户试卷收藏列表
     */
    @GetMapping("/desktop/favorite/list/{userId}")
    public TableDataInfo desktopList(@PathVariable("userId") Long userId,
                                     @RequestParam(defaultValue = "1") Integer pageNum,
                                     @RequestParam(defaultValue = "10") Integer pageSize)
    {
        List<PtmjFile> ptmjFileList = ptmjDesktopFileFavoriteService.selectFileFavoriteList(userId, pageNum, pageSize);
        return getDataTable(ptmjFileList);
    }

    /**
     * 删除桌面端用户试卷收藏
     */
    @DeleteMapping("/desktop/favorite/{userId}/{fileId}")
    public AjaxResult desktopDelete(@PathVariable("userId") Long userId, @PathVariable("fileId") Long fileId)
    {
        if (!SecurityUtils.getUserId().equals(userId)) {
            return AjaxResult.error("不能删除其他用户的收藏记录");
        }
        return toAjax(ptmjFileFavoriteMapper.deleteByUserIdAndFileId(userId, fileId));
    }
}

