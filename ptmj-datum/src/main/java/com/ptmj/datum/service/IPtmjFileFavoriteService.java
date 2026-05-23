package com.ptmj.datum.service;

import java.util.List;
import com.ptmj.datum.domain.PtmjFileFavorite;

/**
 * 试卷收藏Service接口
 *
 * @author pk
 * @date 2026-04-02
 */
public interface IPtmjFileFavoriteService
{
    /**
     * 查询试卷收藏
     *
     * @param fileId 试卷收藏主键
     * @return 试卷收藏
     */
    public PtmjFileFavorite selectPtmjFileFavoriteByFileId(Long fileId);

    /**
     * 查询试卷收藏列表
     *
     * @param ptmjFileFavorite 试卷收藏
     * @return 试卷收藏集合
     */
    public List<PtmjFileFavorite> selectPtmjFileFavoriteList(PtmjFileFavorite ptmjFileFavorite);

    /**
     * 新增试卷收藏
     *
     * @param ptmjFileFavorite 试卷收藏
     * @return 结果
     */
    public int insertPtmjFileFavorite(PtmjFileFavorite ptmjFileFavorite);

    /**
     * 修改试卷收藏
     *
     * @param ptmjFileFavorite 试卷收藏
     * @return 结果
     */
    public int updatePtmjFileFavorite(PtmjFileFavorite ptmjFileFavorite);

    /**
     * 批量删除试卷收藏
     *
     * @param fileIds 需要删除的试卷收藏主键集合
     * @return 结果
     */
    public int deletePtmjFileFavoriteByFileIds(Long[] fileIds);

    /**
     * 删除试卷收藏信息
     *
     * @param fileId 试卷收藏主键
     * @return 结果
     */
    public int deletePtmjFileFavoriteByFileId(Long fileId);
}
