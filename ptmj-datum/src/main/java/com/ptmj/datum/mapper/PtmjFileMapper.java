package com.ptmj.datum.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.ptmj.datum.domain.PtmjFile;
import com.ptmj.datum.domain.vo.SubjectSuggestionVo;
import com.ptmj.datum.domain.vo.SchoolSuggestionVo;
import org.apache.ibatis.annotations.Mapper;

/**
 * 试卷文件Mapper接口
 *
 * @author pk
 * @date 2026-04-02
 */
@Mapper
public interface PtmjFileMapper
{
    /**
     * 查询试卷文件
     *
     * @param fileId 试卷文件主键
     * @return 试卷文件
     */
    public PtmjFile selectPtmjFileByFileId(Long fileId);

    /**
     * 查询试卷文件列表
     *
     * @param ptmjFile 试卷文件
     * @return 试卷文件集合
     */
    public List<PtmjFile> selectPtmjFileList(PtmjFile ptmjFile);

    /**
     * 查询学科联想推荐列表
     *
     * @param keyword 模糊搜索关键字
     * @param limit 返回条数
     * @return 学科推荐集合
     */
    public List<SubjectSuggestionVo> selectSubjectSuggestions(@Param("keyword") String keyword, @Param("limit") Integer limit);

    /**
     * 查询学校联想推荐列表
     *
     * @param keyword 模糊搜索关键字
     * @param limit 返回条数
     * @return 学校推荐集合
     */
    public List<SchoolSuggestionVo> selectSchoolSuggestions(@Param("keyword") String keyword, @Param("limit") Integer limit);

    /**
     * 检查学校名称是否已存在
     *
     * @param schoolName 学校名称
     * @return 存在返回true，否则返回false
     */
    public boolean checkSchoolNameExists(@Param("schoolName") String schoolName);

    /**
     * 新增试卷文件
     *
     * @param ptmjFile 试卷文件
     * @return 结果
     */
    public int insertPtmjFile(PtmjFile ptmjFile);

    /**
     * 修改试卷文件
     *
     * @param ptmjFile 试卷文件
     * @return 结果
     */
    public int updatePtmjFile(PtmjFile ptmjFile);

    /**
     * 按上传用户ID批量通过未审核文件
     *
     * @param userId 上传用户ID
     * @param reviewer 审核人
     * @param updateTime 更新时间
     * @return 更新数量
     */
    public int approvePendingFilesByUserId(@Param("userId") Long userId,
                                           @Param("reviewer") String reviewer,
                                           @Param("updateTime") java.util.Date updateTime);

    /**
     * 删除试卷文件
     *
     * @param fileId 试卷文件主键
     * @return 结果
     */
    public int deletePtmjFileByFileId(Long fileId);

    /**
     * 批量删除试卷文件
     *
     * @param fileIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deletePtmjFileByFileIds(Long[] fileIds);

    /**
     * 按关键词搜索文件（同时匹配文件名和学科名称）
     * @param keyword 搜索关键词
     * @return 匹配的文件列表，学科命中优先排列
     * @Author lxq
     */
    public List<PtmjFile> searchByKeyword(@Param("keyword") String keyword);

    /**
     * 统计某用户上传且未删除的文件数
     * @param userId 用户ID
     * @return 文件数
     * @Author zac
     */
    public long countUploadedByUserId(Long userId);
}
