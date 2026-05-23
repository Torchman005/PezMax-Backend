package com.ruoyi.common.utils.file;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.common.utils.StringUtils;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;

/**
 * MinIO �洢���Ծ��ҵ���ļ���
 */
@Service
public class MinioStorageService
{
    @Autowired
    private MinioClient minioClient;

    @Value("${minio.bucketName}")
    private String bucketName;

    @Value("${minio.url}")
    private String minioUrl;

    /**
     */
    public Map<String, Object> uploadToBucketRoot(MultipartFile file) throws Exception
    {
        if (file == null || file.isEmpty())
        {
            throw new IllegalArgumentException("�ļ�����Ϊ��");
        }
        ensureBucketExists();
        String original = file.getOriginalFilename();
        if (StringUtils.isEmpty(original))
        {
            original = "file";
        }
        original = original.replace("\\", "/");
        if (original.contains("/"))
        {
            original = original.substring(original.lastIndexOf('/') + 1);
        }
        String ext = FilenameUtils.getExtension(original);
        String objectName = UUID.randomUUID().toString().replace("-", "");
        if (StringUtils.isNotEmpty(ext))
        {
            objectName = objectName + "." + ext.toLowerCase();
        }
        String contentType = StringUtils.isNotEmpty(file.getContentType()) ? file.getContentType() : "application/octet-stream";
        try (InputStream in = file.getInputStream())
        {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(in, file.getSize(), -1)
                    .contentType(contentType)
                    .build());
        }
        String base = minioUrl.endsWith("/") ? minioUrl.substring(0, minioUrl.length() - 1) : minioUrl;
        String fileUrl = base + "/" + bucketName + "/" + objectName;
        Map<String, Object> data = new HashMap<>();
        data.put("fileName", original);
        data.put("fileUrl", fileUrl);
        data.put("fileSize", file.getSize());
        data.put("fileFormat", StringUtils.isNotEmpty(ext) ? ext.toLowerCase() : "");
        data.put("objectName", objectName);
        return data;
    }

    private void ensureBucketExists() throws Exception
    {
        boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        if (!found)
        {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
        }
    }
}
