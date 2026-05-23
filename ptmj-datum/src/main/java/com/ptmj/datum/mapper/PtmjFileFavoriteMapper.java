package com.ptmj.datum.mapper;

import java.util.List;
import com.ptmj.datum.domain.PtmjFileFavorite;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 试卷收藏Mapper接口
 *
 * @author pk
 * @date 2026-04-02
 */
@Mapper
public interface PtmjFileFavoriteMapper
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
     * 删除试卷收藏
     *
     * @param fileId 试卷收藏主键
     * @return 结果
     */
    public int deletePtmjFileFavoriteByFileId(Long fileId);

    /**
     * 批量删除试卷收藏
     *
     * @param fileIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deletePtmjFileFavoriteByFileIds(Long[] fileIds);

    /**
     * 删除某用户的单条收藏记录
     *
     * @param userId 用户ID
     * @param fileId 文件ID
     * @return 结果
     * @Author zac
     */
    public int deleteByUserIdAndFileId(@Param("userId") Long userId, @Param("fileId") Long fileId);

    /**
     * 统计某用户的收藏条数
     * @param userId 用户ID
     * @return 收藏条数
     * @Author zac
     */
    public long countByUserId(Long userId);
}
