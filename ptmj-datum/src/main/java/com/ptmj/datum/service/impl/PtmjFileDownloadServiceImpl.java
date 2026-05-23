package com.ptmj.datum.service.impl;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import com.ptmj.datum.domain.PtmjFile;
import com.ptmj.datum.domain.PtmjFileDownload;
import com.ptmj.datum.mapper.PtmjFileDownloadMapper;
import com.ptmj.datum.mapper.PtmjFileMapper;
import com.ptmj.datum.service.IPtmjFileDownloadService;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.file.FileUtils;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 试卷下载Service业务层处理
 *
 * @author pk
 * @date 2026-04-02
 */
@Service
public class PtmjFileDownloadServiceImpl implements IPtmjFileDownloadService
{
    @Autowired
    private PtmjFileDownloadMapper ptmjFileDownloadMapper;

    @Autowired
    private PtmjFileMapper ptmjFileMapper;

    @Autowired
    private MinioClient minioClient;

    @Value("${minio.bucketName}")
    private String bucketName;

    @Value("${minio.url}")
    private String minioUrl;

    /**
     * 查询试卷下载
     *
     * @param downloadId 试卷下载主键
     * @return 试卷下载
     */
    @Override
    public PtmjFileDownload selectPtmjFileDownloadByDownloadId(Long downloadId)
    {
        return ptmjFileDownloadMapper.selectPtmjFileDownloadByDownloadId(downloadId);
    }

    /**
     * 查询试卷下载列表
     *
     * @param ptmjFileDownload 试卷下载
     * @return 试卷下载
     */
    @Override
    public List<PtmjFileDownload> selectPtmjFileDownloadList(PtmjFileDownload ptmjFileDownload)
    {
        return ptmjFileDownloadMapper.selectPtmjFileDownloadList(ptmjFileDownload);
    }

    /**
     * 流式下载文件
     *
     * @param fileId 文件ID，可选
     * @param fileUrl 文件地址，可选
     * @param response 响应对象
     * @throws Exception 下载异常
     */
    @Override
    public void downloadFile(Long fileId, String fileUrl, HttpServletResponse response) throws Exception
    {
        PtmjFile file = this.resolveDownloadFile(fileId, fileUrl);
        String objectName = this.resolveObjectName(file.getFileUrl());
        String fileName = StringUtils.isNotEmpty(file.getFileName()) ? file.getFileName() : FileUtils.getName(objectName);

        StatObjectResponse stat = minioClient.statObject(StatObjectArgs.builder()
                .bucket(bucketName)
                .object(objectName)
                .build());

        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20");

        response.reset();
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentLengthLong(stat.size());
        response.setHeader("Content-Length", String.valueOf(stat.size()));
        response.setHeader("Content-Disposition", "attachment; filename=\"" + encodedFileName + "\"");
        response.setHeader("Access-Control-Expose-Headers", "Content-Length,Content-Disposition");

        try (InputStream inputStream = minioClient.getObject(GetObjectArgs.builder()
                .bucket(bucketName)
                .object(objectName)
                .build());
             OutputStream outputStream = response.getOutputStream())
        {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1)
            {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.flush();
        }

        if (file.getFileId() != null)
        {
            PtmjFileDownload downloadRecord = new PtmjFileDownload();
            downloadRecord.setFileId(file.getFileId());
            downloadRecord.setUserId(SecurityUtils.getUserId());
            downloadRecord.setCreatBy(SecurityUtils.getUsername());
            downloadRecord.setCreatTime(DateUtils.getNowDate());
            downloadRecord.setUpdateBy(SecurityUtils.getUsername());
            downloadRecord.setUpdateTime(DateUtils.getNowDate());
            ptmjFileDownloadMapper.insertPtmjFileDownload(downloadRecord);
        }
    }

    /**
     * 新增试卷下载
     *
     * @param ptmjFileDownload 试卷下载
     * @return 结果
     */
    @Override
    public int insertPtmjFileDownload(PtmjFileDownload ptmjFileDownload)
    {
        return ptmjFileDownloadMapper.insertPtmjFileDownload(ptmjFileDownload);
    }

    /**
     * 修改试卷下载
     *
     * @param ptmjFileDownload 试卷下载
     * @return 结果
     */
    @Override
    public int updatePtmjFileDownload(PtmjFileDownload ptmjFileDownload)
    {
        ptmjFileDownload.setUpdateTime(DateUtils.getNowDate());
        return ptmjFileDownloadMapper.updatePtmjFileDownload(ptmjFileDownload);
    }

    /**
     * 批量删除试卷下载
     *
     * @param downloadIds 需要删除的试卷下载主键
     * @return 结果
     */
    @Override
    public int deletePtmjFileDownloadByDownloadIds(Long[] downloadIds)
    {
        return ptmjFileDownloadMapper.deletePtmjFileDownloadByDownloadIds(downloadIds);
    }

    /**
     * 删除试卷下载信息
     *
     * @param downloadId 试卷下载主键
     * @return 结果
     */
    @Override
    public int deletePtmjFileDownloadByDownloadId(Long downloadId)
    {
        return ptmjFileDownloadMapper.deletePtmjFileDownloadByDownloadId(downloadId);
    }

    private PtmjFile resolveDownloadFile(Long fileId, String fileUrl)
    {
        if (fileId != null)
        {
            PtmjFile file = ptmjFileMapper.selectPtmjFileByFileId(fileId);
            if (file == null)
            {
                throw new IllegalArgumentException("文件不存在或已被删除");
            }
            if (StringUtils.isEmpty(file.getFileUrl()))
            {
                throw new IllegalArgumentException("文件地址为空，无法下载");
            }
            return file;
        }
        if (StringUtils.isEmpty(fileUrl))
        {
            throw new IllegalArgumentException("请传入 fileId 或 fileUrl");
        }
        PtmjFile file = new PtmjFile();
        file.setFileUrl(fileUrl);
        file.setFileName(FileUtils.getName(this.resolveObjectName(fileUrl)));
        return file;
    }

    private String resolveObjectName(String fileUrl)
    {
        if (StringUtils.isEmpty(fileUrl))
        {
            throw new IllegalArgumentException("文件地址为空，无法解析对象路径");
        }

        String trimmedUrl = fileUrl.trim();
        String bucketPrefix = this.minioUrl.replaceAll("/+$", "") + "/" + this.bucketName + "/";

        if (trimmedUrl.startsWith(bucketPrefix))
        {
            return trimmedUrl.substring(bucketPrefix.length());
        }

        if (trimmedUrl.startsWith("minio://"))
        {
            String path = trimmedUrl.substring("minio://".length());
            int firstSlash = path.indexOf('/');
            if (firstSlash >= 0 && firstSlash < path.length() - 1)
            {
                return path.substring(firstSlash + 1);
            }
        }

        if (!trimmedUrl.startsWith("http://") && !trimmedUrl.startsWith("https://"))
        {
            return trimmedUrl.startsWith("/") ? trimmedUrl.substring(1) : trimmedUrl;
        }

        try
        {
            URL url = new URL(trimmedUrl);
            String path = url.getPath();
            String bucketPath = "/" + bucketName + "/";
            int bucketIndex = path.indexOf(bucketPath);
            if (bucketIndex >= 0)
            {
                return path.substring(bucketIndex + bucketPath.length());
            }
        }
        catch (Exception e)
        {
            throw new IllegalArgumentException("文件地址格式不正确，无法解析对象路径");
        }

        throw new IllegalArgumentException("文件地址格式不正确，无法解析对象路径");
    }
}
