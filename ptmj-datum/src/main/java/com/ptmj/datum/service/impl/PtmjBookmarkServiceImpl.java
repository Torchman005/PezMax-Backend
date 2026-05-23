package com.ptmj.datum.service.impl;

import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.ptmj.datum.domain.PtmjBookmark;
import com.ptmj.datum.mapper.PtmjBookmarkMapper;
import com.ptmj.datum.service.IPtmjBookmarkService;
import com.ptmj.datum.service.IPtmjUserService;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.SetBucketPolicyArgs;

/**
 * @author Lzj
 *
 * 外部书签Service业务层处理
 */
@Service
public class PtmjBookmarkServiceImpl implements IPtmjBookmarkService
{
    @Autowired
    private PtmjBookmarkMapper ptmjBookmarkMapper;

    @Autowired
    private IPtmjUserService ptmjUserService;

    @Autowired
    private MinioClient minioClient;

    @Value("${minio.url}")
    private String minioUrl;

    @Value("${minio.bucketName}")
    private String bucketName;

    /**
     * 查询外部书签
     *
     * @param id 主键
     * @return 外部书签
     */
    @Override
    public PtmjBookmark selectPtmjBookmarkById(Long id)
    {
        PtmjBookmark bookmark = ptmjBookmarkMapper.selectPtmjBookmarkById(id);
        if (bookmark == null || Long.valueOf(1L).equals(bookmark.getDelFlag()))
        {
            return null;
        }
        return bookmark;
    }

    /**
     * 查询外部书签列表
     *
     * @param ptmjBookmark 外部书签
     * @return 外部书签
     */
    @Override
    public List<PtmjBookmark> selectPtmjBookmarkList(PtmjBookmark ptmjBookmark)
    {
        if (StringUtils.isNotEmpty(ptmjBookmark.getKeyword()))
        {
            ptmjBookmark.setKeyword(ptmjBookmark.getKeyword().trim());
        }
        normalizeResourceType(ptmjBookmark, false);
        normalizeCollection(ptmjBookmark);
        return ptmjBookmarkMapper.selectPtmjBookmarkList(ptmjBookmark);
    }

    /**
     * 新增外部书签
     *
     * @param ptmjBookmark 外部书签
     * @return 结果
     */
    @Override
    public int insertPtmjBookmark(PtmjBookmark ptmjBookmark)
    {
        validateBookmark(ptmjBookmark);
        normalizeDescription(ptmjBookmark);
        normalizeResourceType(ptmjBookmark, true);
        normalizeCollection(ptmjBookmark);
        ptmjBookmark.setUserId(SecurityUtils.getUserId());
        ptmjBookmark.setCreateBy(SecurityUtils.getUsername());
        ptmjBookmark.setCreateTime(DateUtils.getNowDate());
        ptmjBookmark.setStatus(0L);
        ptmjBookmark.setDelFlag(0L);
        int result = ptmjBookmarkMapper.insertPtmjBookmark(ptmjBookmark);
        if (result > 0 && ptmjBookmark.getUserId() != null)
        {
            ptmjUserService.incrementCountByUserId(ptmjBookmark.getUserId());
        }
        return result;
    }

    /**
     * 修改外部书签
     *
     * @param ptmjBookmark 外部书签
     * @return 结果
     */
    @Override
    public int updatePtmjBookmark(PtmjBookmark ptmjBookmark)
    {
        if (ptmjBookmark.getId() == null)
        {
            throw new ServiceException("书签ID不能为空");
        }
        if (StringUtils.isBlank(ptmjBookmark.getTitle()))
        {
            throw new ServiceException("书签标题不能为空");
        }

        PtmjBookmark exists = this.selectPtmjBookmarkById(ptmjBookmark.getId());
        if (exists == null)
        {
            throw new ServiceException("书签不存在");
        }
        checkBookmarkOwner(exists);

        PtmjBookmark updateEntity = new PtmjBookmark();
        updateEntity.setId(ptmjBookmark.getId());
        updateEntity.setUserId(SecurityUtils.getUserId());
        updateEntity.setUrl(ptmjBookmark.getUrl());
        updateEntity.setTitle(ptmjBookmark.getTitle().trim());
        updateEntity.setSubject(ptmjBookmark.getSubject());
        updateEntity.setDescription(ptmjBookmark.getDescription());
        updateEntity.setCoverImage(ptmjBookmark.getCoverImage());
        updateEntity.setRemark(ptmjBookmark.getRemark());
        updateEntity.setResourceType(ptmjBookmark.getResourceType());
        updateEntity.setCollection(ptmjBookmark.getCollection());
        normalizeDescription(updateEntity);
        normalizeResourceType(updateEntity, false);
        normalizeCollection(updateEntity);
        updateEntity.setUpdateBy(SecurityUtils.getUsername());
        updateEntity.setUpdateTime(DateUtils.getNowDate());
        return ptmjBookmarkMapper.updatePtmjBookmark(updateEntity);
    }

    @Override
    public int updateBookmarkCoverImage(Long id, String coverImage)
    {
        if (id == null)
        {
            throw new ServiceException("书签ID不能为空");
        }
        if (StringUtils.isBlank(coverImage))
        {
            throw new ServiceException("封面图URL不能为空");
        }

        PtmjBookmark exists = this.selectPtmjBookmarkById(id);
        if (exists == null)
        {
            throw new ServiceException("书签不存在");
        }
        checkBookmarkOwner(exists);

        PtmjBookmark updateEntity = new PtmjBookmark();
        updateEntity.setId(id);
        updateEntity.setUserId(SecurityUtils.getUserId());
        updateEntity.setCoverImage(coverImage.trim());
        updateEntity.setUpdateBy(SecurityUtils.getUsername());
        updateEntity.setUpdateTime(DateUtils.getNowDate());
        return ptmjBookmarkMapper.updatePtmjBookmark(updateEntity);
    }

    /**
     * 删除外部书签
     *
     * @param id 主键
     * @return 结果
     */
    @Override
    public int deletePtmjBookmarkById(Long id)
    {
        PtmjBookmark exists = this.selectPtmjBookmarkById(id);
        if (exists == null)
        {
            throw new ServiceException("书签不存在");
        }
        checkBookmarkOwner(exists);

        PtmjBookmark deleteEntity = new PtmjBookmark();
        deleteEntity.setId(id);
        deleteEntity.setUserId(SecurityUtils.getUserId());
        deleteEntity.setUpdateBy(SecurityUtils.getUsername());
        deleteEntity.setUpdateTime(DateUtils.getNowDate());
        return ptmjBookmarkMapper.deletePtmjBookmarkById(deleteEntity);
    }

    private void checkBookmarkOwner(PtmjBookmark bookmark)
    {
        if (!SecurityUtils.getUserId().equals(bookmark.getUserId()))
        {
            throw new ServiceException("无权操作该书签");
        }
    }

    private void validateBookmark(PtmjBookmark ptmjBookmark)
    {
        if (StringUtils.isBlank(ptmjBookmark.getUrl()))
        {
            throw new ServiceException("书签链接不能为空");
        }
        if (StringUtils.isBlank(ptmjBookmark.getTitle()))
        {
            throw new ServiceException("书签标题不能为空");
        }
        ptmjBookmark.setUrl(ptmjBookmark.getUrl().trim());
        ptmjBookmark.setTitle(ptmjBookmark.getTitle().trim());
    }

    private void normalizeDescription(PtmjBookmark ptmjBookmark)
    {
        if (StringUtils.isBlank(ptmjBookmark.getDescription()) && StringUtils.isNotBlank(ptmjBookmark.getRemark()))
        {
            ptmjBookmark.setDescription(ptmjBookmark.getRemark().trim());
        }
    }

    private void normalizeResourceType(PtmjBookmark ptmjBookmark, boolean applyDefaultWhenBlank)
    {
        String resourceType = ptmjBookmark.getResourceType();
        if (StringUtils.isBlank(resourceType))
        {
            if (applyDefaultWhenBlank)
            {
                ptmjBookmark.setResourceType("other");
            }
            return;
        }
        ptmjBookmark.setResourceType(resourceType.trim());
    }

    private void normalizeCollection(PtmjBookmark ptmjBookmark)
    {
        if (ptmjBookmark.getCollection() != null)
        {
            ptmjBookmark.setCollection(ptmjBookmark.getCollection().trim());
        }
    }

    @Override
    public Map<String, String> uploadBookmarkCover(MultipartFile file, String resourceType, String collection, String bookmarkName) throws Exception
    {
        if (file == null || file.isEmpty())
        {
            throw new ServiceException("封面文件不能为空");
        }
        if (StringUtils.isBlank(resourceType))
        {
            throw new ServiceException("书签类型不能为空");
        }

        String safeResourceType = sanitizePathSegment(resourceType, "other");
        String safeCollection = sanitizePathSegment(collection, "default");
        String safeBookmarkName = sanitizePathSegment(bookmarkName, "unnamed");

        String originalFilename = file.getOriginalFilename();
        String ext = StringUtils.isNotEmpty(originalFilename) ? FilenameUtils.getExtension(originalFilename) : "";
        String uuidName = UUID.randomUUID().toString().replace("-", "");
        String finalFileName = StringUtils.isNotEmpty(ext) ? (uuidName + "." + ext.toLowerCase()) : uuidName;

        String objectName = "bookmark/" + safeResourceType + "/" + safeCollection + "/" + safeBookmarkName + "/" + finalFileName;

        ensureBucketExists();

        String contentType = StringUtils.isNotEmpty(file.getContentType()) ? file.getContentType() : "application/octet-stream";
        try (InputStream inputStream = file.getInputStream())
        {
            minioClient.putObject(PutObjectArgs.builder()
                .bucket(bucketName)
                .object(objectName)
                .stream(inputStream, file.getSize(), -1)
                .contentType(contentType)
                .build());
        }

        String base = minioUrl.endsWith("/") ? minioUrl.substring(0, minioUrl.length() - 1) : minioUrl;
        String fileUrl = base + "/" + bucketName + "/" + encodeObjectNameForUrl(objectName);

        Map<String, String> result = new HashMap<>();
        result.put("fileUrl", fileUrl);
        result.put("objectName", objectName);
        result.put("fileName", finalFileName);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, String> uploadAndSaveBookmarkCover(MultipartFile file, String resourceType, String collection, Long bookmarkId, String bookmarkName) throws Exception
    {
        Map<String, String> result = this.uploadBookmarkCover(file, resourceType, collection, bookmarkName);
        if (bookmarkId != null)
        {
            this.updateBookmarkCoverImage(bookmarkId, result.get("fileUrl"));
        }
        return result;
    }

    private void ensureBucketExists() throws Exception
    {
        boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        if (!found)
        {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            String policyJson = "{\"Version\":\"2012-10-17\",\"Statement\":[{\"Effect\":\"Allow\",\"Principal\":{\"AWS\":[\"*\"]},\"Action\":[\"s3:GetObject\"],\"Resource\":[\"arn:aws:s3:::" + bucketName + "/*\"]}]}";
            minioClient.setBucketPolicy(SetBucketPolicyArgs.builder()
                .bucket(bucketName)
                .config(policyJson)
                .build());
        }
    }

    private String sanitizePathSegment(String value, String defaultValue)
    {
        if (StringUtils.isBlank(value))
        {
            return defaultValue;
        }
        String normalized = value.trim();
        // 替换 MinIO 不支持或不建议的特殊字符
        normalized = normalized.replaceAll("[\\\\/:*?\"<>|]", "_");
        return StringUtils.isEmpty(normalized) ? defaultValue : normalized;
    }

    private String encodeObjectNameForUrl(String objectName)
    {
        String[] parts = objectName.split("/");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++)
        {
            if (i > 0)
            {
                sb.append("/");
            }
            sb.append(URLEncoder.encode(parts[i], StandardCharsets.UTF_8).replace("+", "%20"));
        }
        return sb.toString();
    }
}
