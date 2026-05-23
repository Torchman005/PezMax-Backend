package com.ptmj.datum.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import com.ptmj.datum.domain.PtmjBookmark;

/**
 * @author Lzj
 *
 * 外部书签Service接口
 */
public interface IPtmjBookmarkService
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
     * 删除外部书签
     *
     * @param id 主键
     * @return 结果
     */
    public int deletePtmjBookmarkById(Long id);

    /**
     * 上传书签封面图片到MinIO
     *
     * @param file 封面图片文件
     * @param resourceType 书签类型
     * @param collection 书签专栏
     * @param bookmarkName 书签名
     * @return 包含fileUrl/objectName/fileName的Map
     */
    Map<String, String> uploadBookmarkCover(MultipartFile file, String resourceType, String collection, String bookmarkName) throws Exception;

    /**
     * 更新书签封面图URL
     *
     * @param id 书签ID
     * @param coverImage 封面图真实URL
     * @return 结果
     */
    int updateBookmarkCoverImage(Long id, String coverImage);

    /**
     * 上传并保存书签封面图
     *
     * @param file 封面图片文件
     * @param resourceType 书签类型
     * @param collection 书签专栏
     * @param bookmarkId 书签ID (可选)
     * @param bookmarkName 书签名
     * @return 包含fileUrl/objectName/fileName的Map
     */
    Map<String, String> uploadAndSaveBookmarkCover(MultipartFile file, String resourceType, String collection, Long bookmarkId, String bookmarkName) throws Exception;
}
