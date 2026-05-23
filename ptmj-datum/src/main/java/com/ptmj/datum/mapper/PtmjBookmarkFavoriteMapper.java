package com.ptmj.datum.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.ptmj.datum.domain.PtmjBookmark;
import com.ptmj.datum.domain.PtmjBookmarkFavorite;

/**
 * 书签收藏Mapper接口
 */
@Mapper
public interface PtmjBookmarkFavoriteMapper
{
    public List<PtmjBookmarkFavorite> selectPtmjBookmarkFavoriteList(PtmjBookmarkFavorite ptmjBookmarkFavorite);

    public List<PtmjBookmark> selectFavoriteBookmarkList(Long userId);

    public int insertPtmjBookmarkFavorite(PtmjBookmarkFavorite ptmjBookmarkFavorite);

    public int deleteByUserIdAndBookmarkId(@Param("userId") Long userId, @Param("bookmarkId") Long bookmarkId);

    public long countByUserId(Long userId);
}
