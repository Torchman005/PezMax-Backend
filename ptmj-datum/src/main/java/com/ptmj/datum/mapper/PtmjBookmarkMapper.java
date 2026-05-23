package com.ptmj.datum.mapper;

import java.util.List;
import com.ptmj.datum.domain.PtmjBookmark;

/**
 * 外部书签Mapper接口
 */
public interface PtmjBookmarkMapper
{
    /**
     * 查询外部书签
     *
     * @param id 主键
     * @return 外部书签
     */
    public PtmjBookmark selectPtmjBookmarkById(Long id);

    /**
     * 查询外部书签列表
     *
     * @param ptmjBookmark 外部书签
     * @return 外部书签集合
     */
    public List<PtmjBookmark> selectPtmjBookmarkList(PtmjBookmark ptmjBookmark);

    /**
     * 新增外部书签
     *
     * @param ptmjBookmark 外部书签
     * @return 结果
     */
    public int insertPtmjBookmark(PtmjBookmark ptmjBookmark);

    /**
     * 修改外部书签
     *
     * @param ptmjBookmark 外部书签
     * @return 结果
     */
    public int updatePtmjBookmark(PtmjBookmark ptmjBookmark);

    /**
     * 逻辑删除外部书签
     *
     * @param ptmjBookmark 外部书签
     * @return 结果
     */
    public int deletePtmjBookmarkById(PtmjBookmark ptmjBookmark);
}
