package com.ptmj.datum.service;

import java.util.List;
import com.ptmj.datum.domain.PtmjBookmark;
import com.ptmj.datum.domain.PtmjBookmarkFavorite;

/**
 * 书签收藏Service接口
 */
public interface IPtmjBookmarkFavoriteService
{
    public List<PtmjBookmarkFavorite> selectPtmjBookmarkFavoriteList(PtmjBookmarkFavorite ptmjBookmarkFavorite);

    public List<PtmjBookmark> selectFavoriteBookmarkList(Long userId, Integer pageNum, Integer pageSize);

    public int insertPtmjBookmarkFavorite(PtmjBookmarkFavorite ptmjBookmarkFavorite);

    public int deleteByUserIdAndBookmarkId(Long userId, Long bookmarkId);
}
