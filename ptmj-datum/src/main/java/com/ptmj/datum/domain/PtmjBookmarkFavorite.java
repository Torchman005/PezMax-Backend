package com.ptmj.datum.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 书签收藏对象 ptmj_bookmark_favorite
 */
public class PtmjBookmarkFavorite extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 书签ID */
    private Long bookmarkId;

    /** 收藏用户ID */
    private Long userId;

    public void setBookmarkId(Long bookmarkId)
    {
        this.bookmarkId = bookmarkId;
    }

    public Long getBookmarkId()
    {
        return bookmarkId;
    }

    public void setUserId(Long userId)
    {
        this.userId = userId;
    }

    public Long getUserId()
    {
        return userId;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("bookmarkId", getBookmarkId())
            .append("userId", getUserId())
            .toString();
    }
}
