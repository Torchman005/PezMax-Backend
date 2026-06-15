package com.ptmj.datum.service;

import java.util.List;

import com.ptmj.datum.domain.PtmjFile;
import com.ptmj.datum.domain.vo.FileTreeVo;
import com.ptmj.datum.domain.vo.SubjectSuggestionVo;
import com.ptmj.datum.domain.vo.SchoolSuggestionVo;

/**
 * 试卷文件Service接口
 *
 * @author pk
 * @date 2026-04-02
 */
public interface IPtmjFileService
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
     * 获取按 科目 -> 类型 -> 年份 聚合成的文件树
     *
     * @param ptmjFile 过滤条件（可选）
     * @return 文件树集合
     */
    public List<FileTreeVo> getPtmjFileTree(PtmjFile ptmjFile);

    /**
     * 获取学科联想推荐列表
     *
     * @param keyword 模糊搜索关键字
     * @param limit 返回条数
     * @return 学科推荐集合
     */
    public List<SubjectSuggestionVo> getSubjectSuggestions(String keyword, Integer limit);

    /**
     * 获取学校联想推荐列表
     *
     * @param keyword 模糊搜索关键字
     * @param limit 返回条数
     * @return 学校推荐集合
     */
    public List<SchoolSuggestionVo> getSchoolSuggestions(String keyword, Integer limit);

    /**
     * 检查学校名称是否已存在
     *
     * @param schoolName 学校名称
     * @return 存在返回true，否则返回false
     */
    public boolean checkSchoolNameExists(String schoolName);

    /**
     * 按关键词搜索文件（同时匹配文件名和学科名称）
     *
     * @param keyword 搜索关键词
     * @return 匹配的文件列表，学科命中优先排列
     * @Author lxq
     */
    public List<PtmjFile> searchByKeyword(String keyword);

    /**
     * 新增试卷文件
     *
     * @param ptmjFile 试卷文件
     * @return 结果
     */
    public int insertPtmjFile(PtmjFile ptmjFile);

    /**
     * 上传并新增试卷文件
     *
     * @param file 实际文件
     * @param ptmjFile 试卷文件信息
     * @return 结果
     * @throws Exception 异常
     */
    public int uploadPtmjFile(org.springframework.web.multipart.MultipartFile file, PtmjFile ptmjFile) throws Exception;

    /**
     * 修改试卷文件
     *
     * @param ptmjFile 试卷文件
     * @return 结果
     */
    public int updatePtmjFile(PtmjFile ptmjFile);

    /**
     * 批量删除试卷文件
     *
     * @param fileIds 需要删除的试卷文件主键集合
     * @return 结果
     */
    public int deletePtmjFileByFileIds(Long[] fileIds);

    /**
     * 删除试卷文件信息
     *
     * @param fileId 试卷文件主键
     * @return 结果
     */
    public int deletePtmjFileByFileId(Long fileId);
}
