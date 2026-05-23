package com.ruoyi.web.controller.datum;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ptmj.datum.domain.PtmjBookmark;
import com.ptmj.datum.domain.PtmjBookmarkFavorite;
import com.ptmj.datum.service.IPtmjBookmarkFavoriteService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;

/**
 * 书签收藏Controller
 */
@RestController
@RequestMapping("/datum")
public class PtmjBookmarkFavoriteController extends BaseController
{
    @Autowired
    private IPtmjBookmarkFavoriteService ptmjBookmarkFavoriteService;

    /**
     * 查询当前用户书签收藏关系
     */
    @GetMapping("/bookmark/favorite/list")
    public TableDataInfo list(PtmjBookmarkFavorite ptmjBookmarkFavorite)
    {
        startPage();
        List<PtmjBookmarkFavorite> list = ptmjBookmarkFavoriteService.selectPtmjBookmarkFavoriteList(ptmjBookmarkFavorite);
        return getDataTable(list);
    }

    /**
     * 新增书签收藏
     */
    @Log(title = "书签收藏", businessType = BusinessType.INSERT)
    @PostMapping("/bookmark/favorite")
    public AjaxResult add(@RequestBody PtmjBookmarkFavorite ptmjBookmarkFavorite)
    {
        return toAjax(ptmjBookmarkFavoriteService.insertPtmjBookmarkFavorite(ptmjBookmarkFavorite));
    }

    /**
     * 查询桌面端用户收藏的书签列表
     */
    @GetMapping("/desktop/bookmark/favorite/list/{userId}")
    public TableDataInfo desktopList(@PathVariable("userId") Long userId,
                                     @RequestParam(defaultValue = "1") Integer pageNum,
                                     @RequestParam(defaultValue = "10") Integer pageSize)
    {
        List<PtmjBookmark> list = ptmjBookmarkFavoriteService.selectFavoriteBookmarkList(userId, pageNum, pageSize);
        return getDataTable(list);
    }

    /**
     * 删除桌面端用户书签收藏
     */
    @Log(title = "书签收藏", businessType = BusinessType.DELETE)
    @DeleteMapping("/desktop/bookmark/favorite/{userId}/{bookmarkId}")
    public AjaxResult desktopDelete(@PathVariable("userId") Long userId, @PathVariable("bookmarkId") Long bookmarkId)
    {
        return toAjax(ptmjBookmarkFavoriteService.deleteByUserIdAndBookmarkId(userId, bookmarkId));
    }
}
