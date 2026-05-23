package com.ptmj.datum.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ptmj.datum.mapper.PtmjFileFavoriteMapper;
import com.ptmj.datum.domain.PtmjFileFavorite;
import com.ptmj.datum.service.IPtmjFileFavoriteService;

/**
 * 试卷收藏Service业务层处理
 *
 * @author pk
 * @date 2026-04-02
 */
@Service
public class PtmjFileFavoriteServiceImpl implements IPtmjFileFavoriteService
{
    @Autowired
    private PtmjFileFavoriteMapper ptmjFileFavoriteMapper;

    /**
     * 查询试卷收藏
     *
     * @param fileId 试卷收藏主键
     * @return 试卷收藏
     */
    @Override
    public PtmjFileFavorite selectPtmjFileFavoriteByFileId(Long fileId)
    {
        return ptmjFileFavoriteMapper.selectPtmjFileFavoriteByFileId(fileId);
    }

    /**
     * 查询试卷收藏列表
     *
     * @param ptmjFileFavorite 试卷收藏
     * @return 试卷收藏
     */
    @Override
    public List<PtmjFileFavorite> selectPtmjFileFavoriteList(PtmjFileFavorite ptmjFileFavorite)
    {
        return ptmjFileFavoriteMapper.selectPtmjFileFavoriteList(ptmjFileFavorite);
    }

    /**
     * 新增试卷收藏
     *
     * @param ptmjFileFavorite 试卷收藏
     * @return 结果
     */
    @Override
    public int insertPtmjFileFavorite(PtmjFileFavorite ptmjFileFavorite)
    {
        return ptmjFileFavoriteMapper.insertPtmjFileFavorite(ptmjFileFavorite);
    }

    /**
     * 修改试卷收藏
     *
     * @param ptmjFileFavorite 试卷收藏
     * @return 结果
     */
    @Override
    public int updatePtmjFileFavorite(PtmjFileFavorite ptmjFileFavorite)
    {
        return ptmjFileFavoriteMapper.updatePtmjFileFavorite(ptmjFileFavorite);
    }

    /**
     * 批量删除试卷收藏
     *
     * @param fileIds 需要删除的试卷收藏主键
     * @return 结果
     */
    @Override
    public int deletePtmjFileFavoriteByFileIds(Long[] fileIds)
    {
        return ptmjFileFavoriteMapper.deletePtmjFileFavoriteByFileIds(fileIds);
    }

    /**
     * 删除试卷收藏信息
     *
     * @param fileId 试卷收藏主键
     * @return 结果
     */
    @Override
    public int deletePtmjFileFavoriteByFileId(Long fileId)
    {
        return ptmjFileFavoriteMapper.deletePtmjFileFavoriteByFileId(fileId);
    }
}
