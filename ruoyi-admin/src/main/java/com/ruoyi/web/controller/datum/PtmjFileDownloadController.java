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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ptmj.datum.domain.PtmjFile;
import com.ptmj.datum.domain.PtmjFileDownload;
import com.ptmj.datum.service.IPtmjDesktopFileDownloadService;
import com.ptmj.datum.service.IPtmjFileDownloadService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.framework.manager.AsyncManager;
import java.util.TimerTask;
import com.ruoyi.common.utils.spring.SpringUtils;
import com.ptmj.datum.service.IPtmjFileService;

/**
 * 试卷下载Controller
 *
 * @author pk
 * @date 2026-04-02
 */
@RestController
@RequestMapping("/datum")
public class PtmjFileDownloadController extends BaseController
{
    @Autowired
    private IPtmjFileDownloadService ptmjFileDownloadService;

    @Autowired
    private IPtmjDesktopFileDownloadService ptmjDesktopFileDownloadService;

    // fxy 注入文件服务，用于查询文件地址
    @Autowired
    private IPtmjFileService ptmjFileService;

    /**
     * 查询试卷下载列表
     */
//    @PreAuthorize("@ss.hasPermi('datum:download:list')")
    @GetMapping("/download/list")
    public TableDataInfo list(PtmjFileDownload ptmjFileDownload)
    {
        startPage();
        List<PtmjFileDownload> list = ptmjFileDownloadService.selectPtmjFileDownloadList(ptmjFileDownload);
        return getDataTable(list);
    }

    /**
     * 导出试卷下载列表
     */
    @PreAuthorize("@ss.hasPermi('datum:download:export')")
    @Log(title = "试卷下载", businessType = BusinessType.EXPORT)
    @PostMapping("/download/export")
    public void export(HttpServletResponse response, PtmjFileDownload ptmjFileDownload)
    {
        List<PtmjFileDownload> list = ptmjFileDownloadService.selectPtmjFileDownloadList(ptmjFileDownload);
        ExcelUtil<PtmjFileDownload> util = new ExcelUtil<PtmjFileDownload>(PtmjFileDownload.class);
        util.exportExcel(response, list, "试卷下载数据");
    }

    /**
     * 获取试卷下载详细信息
     */
//    @PreAuthorize("@ss.hasPermi('datum:download:query')")
    @GetMapping(value = "/download/{downloadId}")
    public AjaxResult getInfo(@PathVariable("downloadId") Long downloadId)
    {
        return success(ptmjFileDownloadService.selectPtmjFileDownloadByDownloadId(downloadId));
    }

    /**
     * 新增试卷下载
     */
//    @PreAuthorize("@ss.hasPermi('datum:download:add')")
    @Log(title = "试卷下载", businessType = BusinessType.INSERT)
    @PostMapping("/download")
    public AjaxResult add(@RequestBody PtmjFileDownload ptmjFileDownload)
    {
        return toAjax(ptmjFileDownloadService.insertPtmjFileDownload(ptmjFileDownload));
    }

    /**
     * 修改试卷下载
     */
//    @PreAuthorize("@ss.hasPermi('datum:download:edit')")
    @Log(title = "试卷下载", businessType = BusinessType.UPDATE)
    @PutMapping("/download")
    public AjaxResult edit(@RequestBody PtmjFileDownload ptmjFileDownload)
    {
        return toAjax(ptmjFileDownloadService.updatePtmjFileDownload(ptmjFileDownload));
    }

    /**
     * 删除试卷下载
     */
//    @PreAuthorize("@ss.hasPermi('datum:download:remove')")
    @Log(title = "试卷下载", businessType = BusinessType.DELETE)
    @DeleteMapping("/download/{downloadIds}")
    public AjaxResult remove(@PathVariable Long[] downloadIds)
    {
        return toAjax(ptmjFileDownloadService.deletePtmjFileDownloadByDownloadIds(downloadIds));
    }

    /**
     * @author Fxy
     *
     * 下载试卷文件。
     *
     * 说明：
     * 1. 支持 fileId/fileUrl 二选一。
     * 2. 实际下载、对象路径解析、响应头设置统一交给下载服务层处理。
     */
//    @PreAuthorize("@ss.hasPermi('datum:download:query')")
    @Log(title = "试卷下载", businessType = BusinessType.OTHER)
    @GetMapping("/download/file")
    public void downloadFile(@RequestParam(value = "fileId", required = false) Long fileId,
                             @RequestParam(value = "fileUrl", required = false) String fileUrl,
                             HttpServletResponse response) throws Exception
    {
        // fxy 通过查询 fileUrl 并将 fileId 置空，绕过底层 Service 里的同步插入逻辑，改用下面的异步插入
        String actualUrl = fileUrl;
        if (fileId != null && com.ruoyi.common.utils.StringUtils.isEmpty(actualUrl)) {
            PtmjFile file = ptmjFileService.selectPtmjFileByFileId(fileId);
            if (file != null) {
                actualUrl = file.getFileUrl();
            }
        }
        
        // 传入 fileId=null 防止底层执行同步插入导致卡顿
        ptmjFileDownloadService.downloadFile(null, actualUrl, response);

        // fxy 异步无感记录用户下载行为到 ptmj_file_download 表中
        if (fileId != null) {
            try {
                // 必须在主线程中提前获取用户信息（子线程中无法获取 Request 上下文）
                final Long currentUserId = SecurityUtils.getUserId();
                final String currentUsername = SecurityUtils.getUsername();
                
                // 扔进若依的异步线程池执行，绝对不阻塞文件流
                AsyncManager.me().execute(new TimerTask() {
                    @Override
                    public void run() {
                        try {
                            PtmjFileDownload downloadRecord = new PtmjFileDownload();
                            downloadRecord.setFileId(fileId);
                            downloadRecord.setUserId(currentUserId);
                            downloadRecord.setCreatBy(currentUsername); 
                            downloadRecord.setCreatTime(DateUtils.getNowDate());
                            downloadRecord.setUpdateBy(currentUsername);
                            downloadRecord.setUpdateTime(DateUtils.getNowDate());
                            
                            // 从 Spring 容器中动态获取 Service 并执行插入
                            SpringUtils.getBean(IPtmjFileDownloadService.class).insertPtmjFileDownload(downloadRecord);
                        } catch (Exception innerE) {
                            logger.error("异步插入下载记录失败，fileId: " + fileId, innerE);
                        }
                    }
                });
            } catch (Exception e) {
                // 如果获取用户信息失败（如未登录），只记录日志，不影响下载
                logger.warn("未能获取当前用户信息，跳过记录下载流水", e);
            }
        }
    }

    /**
     * 查询桌面端用户试卷下载列表
     * zac
     */
    @GetMapping("/desktop/download/list/{userId}")
    public TableDataInfo desktopList(@PathVariable("userId") Long userId,
                                     @RequestParam(defaultValue = "1") Integer pageNum,
                                     @RequestParam(defaultValue = "10") Integer pageSize)
    {
        List<PtmjFile> ptmjFileList = ptmjDesktopFileDownloadService.selectPtmjDesktopFileDownloadList(userId, pageNum, pageSize);
        return getDataTable(ptmjFileList);
    }

    /**
     * 根据用户ID和试卷ID隐藏下载记录
     * zac
     */
    @DeleteMapping("/desktop/download/{userId}/{fileId}")
    public AjaxResult desktopDelete(@PathVariable("userId") Long userId, @PathVariable("fileId") Long fileId)
    {
        return toAjax(ptmjDesktopFileDownloadService.hideByUserIdAndFileId(userId, fileId));
    }
}

