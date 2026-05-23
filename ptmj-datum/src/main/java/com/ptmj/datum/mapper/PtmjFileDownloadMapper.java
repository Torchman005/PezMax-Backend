package com.ptmj.datum.mapper;

import java.util.List;
import com.ptmj.datum.domain.PtmjFileDownload;
import org.apache.ibatis.annotations.Mapper;

/**
 * 试卷下载Mapper接口
 *
 * @author pk
 * @date 2026-04-02
 */
@Mapper
public interface PtmjFileDownloadMapper
{
    /**
     * 查询试卷下载
     *
     * @param downloadId 试卷下载主键
     * @return 试卷下载
     */
    public PtmjFileDownload selectPtmjFileDownloadByDownloadId(Long downloadId);

    /**
     * 查询试卷下载列表
     *
     * @param ptmjFileDownload 试卷下载
     * @return 试卷下载集合
     */
    public List<PtmjFileDownload> selectPtmjFileDownloadList(PtmjFileDownload ptmjFileDownload);

    /**
     * 新增试卷下载
     *
     * @param ptmjFileDownload 试卷下载
     * @return 结果
     */
    public int insertPtmjFileDownload(PtmjFileDownload ptmjFileDownload);

    /**
     * 修改试卷下载
     *
     * @param ptmjFileDownload 试卷下载
     * @return 结果
     */
    public int updatePtmjFileDownload(PtmjFileDownload ptmjFileDownload);

    /**
     * 删除试卷下载
     *
     * @param downloadId 试卷下载主键
     * @return 结果
     */
    public int deletePtmjFileDownloadByDownloadId(Long downloadId);

    /**
     * 批量删除试卷下载
     *
     * @param downloadIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deletePtmjFileDownloadByDownloadIds(Long[] downloadIds);

    /**
     * 统计某用户「展示中」的下载记录（remark 为 0 不展示，其余含空视为展示）
     * @param userId 用户ID
     * @return 下载记录数
     * @author zac
     */
    public long countVisibleByUserId(Long userId);
}
