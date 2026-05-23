package com.ruoyi.web.controller.datum;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ptmj.datum.domain.PtmjBookmark;
import com.ptmj.datum.service.IPtmjBookmarkService;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;

/**
 * @author Lzj
 *
 * 外部书签Controller
 */
@RestController
@RequestMapping("/datum/bookmark")
public class PtmjBookmarkController extends BaseController
{
    @Autowired
    private IPtmjBookmarkService ptmjBookmarkService;

    /**
     * 上传书签封面图到 MinIO
     */
    @Log(title = "书签封面上传", businessType = BusinessType.OTHER)
    @PostMapping("/uploadCover")
    public AjaxResult uploadCover(@RequestParam("file") MultipartFile file,
                                  @RequestParam("resourceType") String resourceType,
                                  @RequestParam(value = "collection", required = false) String collection,
                                  @RequestParam(value = "bookmarkId", required = false) Long bookmarkId,
                                  @RequestParam(value = "bookmarkName", required = false) String bookmarkName) throws Exception
    {
        Map<String, String> result = ptmjBookmarkService.uploadAndSaveBookmarkCover(file, resourceType, collection, bookmarkId, bookmarkName);
        return success(result);
    }

    /**
     * 新增书签
     */
    @Log(title = "外部书签", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody PtmjBookmark ptmjBookmark)
    {
        return toAjax(ptmjBookmarkService.insertPtmjBookmark(ptmjBookmark));
    }

    /**
     * 根据ID查询书签详情
     */
    @Anonymous
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id)
    {
        return success(ptmjBookmarkService.selectPtmjBookmarkById(id));
    }

    /**
     * 查询书签列表
     */
    @Anonymous
    @GetMapping("/list")
    public TableDataInfo list(PtmjBookmark ptmjBookmark)
    {
        startPage();
        List<PtmjBookmark> list = ptmjBookmarkService.selectPtmjBookmarkList(ptmjBookmark);
        return getDataTable(list);
    }

    /**
     * 修改书签
     */
    @Log(title = "外部书签", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody PtmjBookmark ptmjBookmark)
    {
        return toAjax(ptmjBookmarkService.updatePtmjBookmark(ptmjBookmark));
    }

    /**
     * 删除书签
     */
    @Log(title = "外部书签", businessType = BusinessType.DELETE)
    @DeleteMapping("/{id}")
    public AjaxResult remove(@PathVariable Long id)
    {
        return toAjax(ptmjBookmarkService.deletePtmjBookmarkById(id));
    }
}
