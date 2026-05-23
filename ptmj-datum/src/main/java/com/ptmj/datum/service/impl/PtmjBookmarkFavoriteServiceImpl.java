package com.ptmj.datum.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.pagehelper.PageHelper;
import com.ptmj.datum.domain.PtmjBookmark;
import com.ptmj.datum.domain.PtmjBookmarkFavorite;
import com.ptmj.datum.mapper.PtmjBookmarkFavoriteMapper;
import com.ptmj.datum.mapper.PtmjBookmarkMapper;
import com.ptmj.datum.service.IPtmjBookmarkFavoriteService;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;

/**
 * 书签收藏Service业务层处理
 */
@Service
public class PtmjBookmarkFavoriteServiceImpl implements IPtmjBookmarkFavoriteService
{
    @Autowired
    private PtmjBookmarkFavoriteMapper ptmjBookmarkFavoriteMapper;

    @Autowired
    private PtmjBookmarkMapper ptmjBookmarkMapper;

    @Override
    public List<PtmjBookmarkFavorite> selectPtmjBookmarkFavoriteList(PtmjBookmarkFavorite ptmjBookmarkFavorite)
    {
        ptmjBookmarkFavorite.setUserId(SecurityUtils.getUserId());
        return ptmjBookmarkFavoriteMapper.selectPtmjBookmarkFavoriteList(ptmjBookmarkFavorite);
    }

    @Override
    public List<PtmjBookmark> selectFavoriteBookmarkList(Long userId, Integer pageNum, Integer pageSize)
    {
        if (!SecurityUtils.getUserId().equals(userId))
        {
            throw new ServiceException("不能查看其他用户的书签收藏");
        }
        Integer safePageNum = pageNum == null || pageNum <= 0 ? 1 : pageNum;
        Integer safePageSize = pageSize == null || pageSize <= 0 ? 10 : pageSize;
        PageHelper.startPage(safePageNum, safePageSize);
        List<PtmjBookmark> list = ptmjBookmarkFavoriteMapper.selectFavoriteBookmarkList(userId);
        return list == null ? new ArrayList<>() : list;
    }

    @Override
    public int insertPtmjBookmarkFavorite(PtmjBookmarkFavorite ptmjBookmarkFavorite)
    {
        if (ptmjBookmarkFavorite.getBookmarkId() == null)
        {
            throw new ServiceException("书签ID不能为空");
        }
        PtmjBookmark bookmark = ptmjBookmarkMapper.selectPtmjBookmarkById(ptmjBookmarkFavorite.getBookmarkId());
        if (bookmark == null || Long.valueOf(1L).equals(bookmark.getDelFlag()))
        {
            throw new ServiceException("书签不存在");
        }
        ptmjBookmarkFavorite.setUserId(SecurityUtils.getUserId());
        return ptmjBookmarkFavoriteMapper.insertPtmjBookmarkFavorite(ptmjBookmarkFavorite);
    }

    @Override
    public int deleteByUserIdAndBookmarkId(Long userId, Long bookmarkId)
    {
        if (!SecurityUtils.getUserId().equals(userId))
        {
            throw new ServiceException("不能删除其他用户的书签收藏");
        }
        return ptmjBookmarkFavoriteMapper.deleteByUserIdAndBookmarkId(userId, bookmarkId);
    }
}
