package com.ptmj.datum.service.impl;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ruoyi.common.utils.StringUtils;
import org.jodconverter.core.office.OfficeUtils;
import org.jodconverter.local.JodConverter;
import org.jodconverter.local.office.LocalOfficeManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import com.ptmj.datum.domain.PtmjFile;
import com.ptmj.datum.domain.vo.FileTreeVo;
import com.ptmj.datum.domain.vo.SubjectSuggestionVo;
import com.ptmj.datum.domain.vo.SchoolSuggestionVo;
import com.ptmj.datum.mapper.PtmjFileMapper;
import com.ptmj.datum.service.IPtmjFileService;
import com.ptmj.datum.service.IPtmjUserService;
import com.ptmj.datum.service.PtmjFileRankCacheService;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.SetBucketPolicyArgs;



/**
 * 试卷文件Service业务层处理
 *
 * @author pk
 * @date 2026-04-02
 */
@Service
public class PtmjFileServiceImpl implements IPtmjFileService
{
    private static final Logger log = LoggerFactory.getLogger(PtmjFileServiceImpl.class);

    @Autowired
    private PtmjFileMapper ptmjFileMapper;

    @Autowired
    private MinioClient minioClient;

    @Autowired
    private IPtmjUserService ptmjUserService;

    @Autowired
    private PtmjFileRankCacheService ptmjFileRankCacheService;

    @Value("${minio.url}")
    private String minioUrl;

    @Value("${minio.bucketName}")
    private String bucketName;

    @Value("${ptmj.file.office-home}")
    private String officeHome;

    @Value("${ptmj.file.allow-format}")
    private String allowFormatStr;

    @Value("${ptmj.file.min-year}")
    private int minYear;

    @Value("${ptmj.file.type-map}")
    private String typeMapStr;

    @Value("${ptmj.file.default-type:不明资料}")
    private String defaultType;

    @Value("${ptmj.file.default-subject:未知科目}")
    private String defaultSubject;

    /**
     * 获取默认年份（当前现实时间的年份）。
     * 当上传文件时填写的年份不合规（为空、小于下限或大于当前年份），则回退到当前年份。
     */
    private Long getDefaultYear() {
        return (long) LocalDate.now().getYear();
    }

    private LocalOfficeManager officeManager;

    @PostConstruct
    public void init() {
        log.info("正在启动 LibreOffice 服务，路径: {}", officeHome);
        try {
            officeManager = LocalOfficeManager.builder()
                    .officeHome(officeHome)
                    .install()
                    .build();
            officeManager.start();
            log.info("LibreOffice 服务启动成功！");
        } catch (Exception e) {
            log.error("LibreOffice 服务启动失败，请检查路径配置或环境！", e);
        }
    }

    @PreDestroy
    public void destroy() {
        log.info("正在停止 LibreOffice 服务...");
        OfficeUtils.stopQuietly(officeManager);
    }

    private Map<Long, String> getTypeMap() {
        Map<Long, String> map = new HashMap<>();
        if (typeMapStr != null && !typeMapStr.isEmpty()) {
            String[] pairs = typeMapStr.split(",");
            for (String pair : pairs) {
                String[] kv = pair.split(":");
                if (kv.length == 2) {
                    try {
                        map.put(Long.parseLong(kv[0]), kv[1]);
                    } catch (NumberFormatException e) {
                        // ignore
                    }
                }
            }
        }
        return map;
    }

    /**
     * 清理路径中的非法字符
     */
    private String sanitizePath(String path) {
        if (path == null) return "";
        return path.trim().replaceAll("[\\\\/:*?\"<>|]", "_");
    }



    /**
     * 查询试卷文件
     *
     * @param fileId 试卷文件主键
     * @return 试卷文件
     */
    @Override
    public PtmjFile selectPtmjFileByFileId(Long fileId)
    {
        return ptmjFileMapper.selectPtmjFileByFileId(fileId);
    }

    /**
     * 查询试卷文件列表
     *
     * @param ptmjFile 试卷文件
     * @return 试卷文件
     */
    @Override
    public List<PtmjFile> selectPtmjFileList(PtmjFile ptmjFile)
    {
        return ptmjFileMapper.selectPtmjFileList(ptmjFile);
    }

    /**
     * @author lxq
     *
     * 按关键词搜索文件（同时匹配文件名和学科名称）
     *
     * @param keyword 搜索关键词
     * @return 匹配的文件列表，学科命中优先排列
     */
    @Override
    public List<PtmjFile> searchByKeyword(String keyword)
    {
        String safeKeyword = (keyword == null || keyword.trim().isEmpty()) ? null : keyword.trim();
        return ptmjFileMapper.searchByKeyword(safeKeyword);
    }

    /**
     * @author Lzj
     *
     * 获取学科联想推荐列表
     *
     * @param keyword 模糊搜索关键字
     * @param limit 返回条数
     * @return 学科推荐集合
     */
    @Override
    public List<SubjectSuggestionVo> getSubjectSuggestions(String keyword, Integer limit)
    {
        int safeLimit = (limit == null || limit <= 0) ? 10 : Math.min(limit, 20);
        String safeKeyword = keyword == null ? null : keyword.trim();
        if (safeKeyword != null && safeKeyword.isEmpty())
        {
            safeKeyword = null;
        }
        return ptmjFileMapper.selectSubjectSuggestions(safeKeyword, safeLimit);
    }

    /**
     * 获取学校联想推荐列表
     *
     * @param keyword 模糊搜索关键字
     * @param limit 返回条数
     * @return 学校推荐集合
     */
    @Override
    public List<SchoolSuggestionVo> getSchoolSuggestions(String keyword, Integer limit)
    {
        int safeLimit = (limit == null || limit <= 0) ? 10 : Math.min(limit, 20);
        String safeKeyword = keyword == null ? null : keyword.trim();
        if (safeKeyword != null && safeKeyword.isEmpty())
        {
            safeKeyword = null;
        }
        return ptmjFileMapper.selectSchoolSuggestions(safeKeyword, safeLimit);
    }

    /**
     * 检查学校名称是否已存在
     *
     * @param schoolName 学校名称
     * @return 存在返回true，否则返回false
     */
    @Override
    public boolean checkSchoolNameExists(String schoolName)
    {
        if (schoolName == null || schoolName.trim().isEmpty())
        {
            return false;
        }
        return ptmjFileMapper.checkSchoolNameExists(schoolName.trim());
    }

    /**
     * @author Lzj
     * 获取按 科目 -> 学校 -> 类型 -> 年份 -> [自定义目录] -> 聚合成的文件树
     *
     * @param ptmjFile 过滤条件（可选）
     * @return 文件树集合
     */
    @Override
    public List<FileTreeVo> getPtmjFileTree(PtmjFile ptmjFile) {
        // 1. 查询出所有扁平的文件数据
        List<PtmjFile> fileList = ptmjFileMapper.selectPtmjFileList(ptmjFile);

        // 类型映射
        Map<Long, String> typeMap = this.getTypeMap();

        List<FileTreeVo> rootTree = new ArrayList<>();

        for (PtmjFile file : fileList) {
            // 解析前四层基础路径
            String subject = (file.getFileSubject() != null && !file.getFileSubject().isEmpty()) ? file.getFileSubject() : "不填科目的←_←";
            String school = (file.getFileSchool() != null && !file.getFileSchool().isEmpty()) ? file.getFileSchool() : "不填学校的←_←";
            String typeName = typeMap.getOrDefault(file.getFileType() != null ? file.getFileType() : -1L, "不填类型的→_→");
            String year = (file.getFileYear() != null && file.getFileYear() >= minYear && file.getFileYear() <= LocalDate.now().getYear()) ? String.valueOf(file.getFileYear()) : String.valueOf(getDefaultYear());

            // 组装所有目录层级
            List<String> pathParts = new ArrayList<>();
            pathParts.add(subject);
            pathParts.add(school);
            pathParts.add(typeName);
            pathParts.add(year);

            // 解析额外的自定义目录 remark (复用为 folderPath)
            if (file.getRemark() != null && !file.getRemark().trim().isEmpty()) {
                String[] folders = file.getRemark().split("/");
                for (String folder : folders) {
                    if (!folder.trim().isEmpty()) {
                        pathParts.add(folder.trim());
                    }
                }
            }

            // 动态构建 Trie 树
            String currentPathId = "node";
            List<FileTreeVo> currentChildren = rootTree;

            for (String part : pathParts) {
                currentPathId = currentPathId + "-" + part.hashCode(); // 生成唯一节点ID
                FileTreeVo existingNode = null;

                // 在当前层级中查找是否已经存在该目录节点
                for (FileTreeVo child : currentChildren) {
                    if (child.getLabel().equals(part) && "folder".equals(child.getType())) {
                        existingNode = child;
                        break;
                    }
                }

                // 如果不存在，则创建新目录节点
                if (existingNode == null) {
                    existingNode = new FileTreeVo(currentPathId, part, "folder");
                    currentChildren.add(existingNode);
                }

                // 进入下一层级
                currentChildren = existingNode.getChildren();
            }

            // 目录层级遍历完毕，将文件作为叶子节点添加到当前 children 中
            FileTreeVo fileNode = new FileTreeVo(
                    "file-" + file.getFileId(),
                    file.getFileName(),
                    "file"
            );
            fileNode.setFileInfo(file);
            currentChildren.add(fileNode);
        }

        return rootTree;
    }

    /**
     * @author Lzj
     * 新增试卷文件
     *
     * @param ptmjFile 试卷文件
     * @return 结果
     */
    @Override
    public int insertPtmjFile(PtmjFile ptmjFile)
    {
        if (ptmjFile.getUserId() == null) {
            ptmjFile.setUserId(SecurityUtils.getUserId());
        }
        ptmjFile.setCreateTime(DateUtils.getNowDate());
        int result = ptmjFileMapper.insertPtmjFile(ptmjFile);
        if (result > 0 && ptmjFile.getUserId() != null) {
            ptmjUserService.incrementCountByUserId(ptmjFile.getUserId());
        }
        return result;
    }

    /**
     * 上传并新增试卷文件
     *
     * @author Luminous
     *
     * @param file 实际文件
     * @param ptmjFile 试卷文件信息
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int uploadPtmjFile(MultipartFile file, PtmjFile ptmjFile) throws Exception
    {
        // 判断桶是否存在，不存在则创建
        boolean isExist = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        if (!isExist) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            
            // 设置桶的访问策略为公开读，允许匿名通过URL访问和预览
            String policyJson = "";
            try (InputStream stream = this.getClass().getClassLoader().getResourceAsStream("minio-public-policy.json")) {
                if (stream != null) {
                    policyJson = IOUtils.toString(stream, StandardCharsets.UTF_8);
                    policyJson = policyJson.replace("{bucketName}", bucketName);
                } else {
                    throw new Exception("MinIO 策略配置文件读取失败");
                }
            }
            
            minioClient.setBucketPolicy(SetBucketPolicyArgs.builder()
                    .bucket(bucketName)
                    .config(policyJson)
                    .build());
        }

        List<String> allowFormat = Arrays.asList(allowFormatStr.split(","));
        String originalFilename = file.getOriginalFilename();
        String fileFormat = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileFormat = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
            if (!allowFormat.contains(fileFormat)) {
                throw new Exception("暂不支持此文件格式！");
            }
            // 判断上传的文件是否为doc或docx格式，如果是，则转换为pdf格式
            if (fileFormat.equals("doc") || fileFormat.equals("docx") || fileFormat.equals("ppt") || fileFormat.equals("pptx")) {
                if (officeManager == null || !officeManager.isRunning()) {
                    throw new Exception("LibreOffice 服务未运行，无法执行文件转换！");
                }

                File outputFile = new File(originalFilename.substring(0, originalFilename.indexOf('.')) + ".pdf");
                File inputFile = null;
                try {
                    // 将 MultipartFile 其保存为临时文件
                    inputFile = File.createTempFile("temp-", "." + fileFormat);
                    file.transferTo(inputFile);

                    JodConverter
                            .convert(inputFile)
                            .to(outputFile)
                            .execute();

                    originalFilename = outputFile.getName();
                    fileFormat = "pdf";
                    log.info("文件格式转换完成：{} -> {}", file.getOriginalFilename(), originalFilename);
                } finally {
                    // 删除临时输入文件
                    if (inputFile != null && inputFile.exists()) {
                        inputFile.delete();
                    }
                }
            }
        }
        
        // 获取前端传递的文件名
        String fileName = ptmjFile.getFileName();
        if (fileName == null || fileName.trim().isEmpty()) {
            fileName = originalFilename;
            ptmjFile.setFileName(fileName);
        } else if (!fileFormat.isEmpty() && !fileName.endsWith("." + fileFormat)) {
            // 如果前端传的 fileName 没有携带扩展名，自动补充以保证文件可用性
            fileName = fileName + "." + fileFormat;
            ptmjFile.setFileName(fileName);
        }

        Map<Long, String> typeMap = this.getTypeMap();
        // 构建存储路径: file_subject/file_school/file_type/file_year/
        String subjectPath = (ptmjFile.getFileSubject() != null && !ptmjFile.getFileSubject().isEmpty()) ? sanitizePath(ptmjFile.getFileSubject()) : sanitizePath(defaultSubject);
        String schoolPath = (ptmjFile.getFileSchool() != null && !ptmjFile.getFileSchool().isEmpty()) ? sanitizePath(ptmjFile.getFileSchool()) : "齐鲁工业大学";
        String typePath = typeMap.get(ptmjFile.getFileType()) != null ? sanitizePath(typeMap.get(ptmjFile.getFileType())) : sanitizePath(defaultType);
        Long yearPath = (ptmjFile.getFileYear() != null && ptmjFile.getFileYear() >= minYear && ptmjFile.getFileYear() <= LocalDate.now().getYear()) ? ptmjFile.getFileYear() : getDefaultYear();
        
        // 处理文件夹相对路径 (复用 remark 字段)
        String folderPath = (ptmjFile.getRemark() != null && !ptmjFile.getRemark().trim().isEmpty()) 
            ? ptmjFile.getRemark().trim() 
            : "";
        
        // 预处理 folderPath，只允许 / 作为分隔符，其他非法字符替换为 _
        if (!folderPath.isEmpty()) {
            String[] parts = folderPath.split("[\\\\/]");
            StringBuilder sb = new StringBuilder();
            for (String part : parts) {
                if (!part.trim().isEmpty()) {
                    if (sb.length() > 0) sb.append("/");
                    sb.append(sanitizePath(part));
                }
            }
            folderPath = sb.toString();
        }
        
        String objectName = subjectPath + "/" + schoolPath + "/" + typePath + "/" + yearPath + "/";
        if (!folderPath.isEmpty()) {
            objectName += folderPath + "/";
        }
        
        // 对文件名进行清理，保留扩展名
        String baseName = FilenameUtils.getBaseName(fileName);
        String extension = FilenameUtils.getExtension(fileName);
        String safeFileName = sanitizePath(baseName) + (StringUtils.isEmpty(extension) ? "" : "." + extension.toLowerCase());
        
        objectName += safeFileName;


        // 处理填写信息为空情况
        if (ptmjFile.getFileSubject() == null) {
            ptmjFile.setFileSubject("未知");
        } else if (ptmjFile.getFileYear() == null) {
            ptmjFile.setFileYear(0L);
        } else if (ptmjFile.getFileType() == null) {
            throw new Exception("文件类型不能为空");
        }

        // 处理最终的上传流（可能是原始流，也可能是转换后的PDF流）
        try (InputStream inputStream = (fileFormat.equals("pdf") && new File(originalFilename).exists()) 
                ? new java.io.FileInputStream(new File(originalFilename)) 
                : file.getInputStream()) {
            
            long uploadSize = (fileFormat.equals("pdf") && new File(originalFilename).exists()) 
                ? new File(originalFilename).length() 
                : file.getSize();
                
            String contentType = file.getContentType();
            if (fileFormat.equals("pdf")) {
                contentType = "application/pdf";
            } else if (fileFormat.equals("txt") || fileFormat.equals("md")) {
                // 强制将 txt 和 md 设置为 UTF-8 编码的 text/plain，这样浏览器会直接渲染且原生支持滚动
                contentType = "text/plain; charset=utf-8";
            }

            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(inputStream, uploadSize, -1)
                    .contentType(contentType)
                    .build());
        } finally {
             // 上传完后删除临时生成的 PDF 文件
             if (fileFormat.equals("pdf") && new File(originalFilename).exists()) {
                 new File(originalFilename).delete();
             }
        }

        // 构建访问URL
        String fileUrl = minioUrl + "/" + bucketName + "/" + objectName;
        
        // 设置文件信息
        ptmjFile.setFileUrl(fileUrl);
        // 更新文件大小，如果是转换后的 PDF 则存 PDF 的大小
        ptmjFile.setFileSize((fileFormat.equals("pdf") && new File(originalFilename).exists()) 
            ? new File(originalFilename).length() 
            : file.getSize());
        ptmjFile.setFileFormat(fileFormat);

        if (ptmjFile.getFileStatus() == null) {
            ptmjFile.setFileStatus(0L);
        }

        return this.insertPtmjFile(ptmjFile);
    }

    /**
     * 修改试卷文件
     *
     * @param ptmjFile 试卷文件
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updatePtmjFile(PtmjFile ptmjFile)
    {
        // 审核通过时清除排行榜缓存
        if (ptmjFile.getFileStatus() != null && ptmjFile.getFileStatus() == 1L) {
            PtmjFile existing = ptmjFileMapper.selectPtmjFileByFileId(ptmjFile.getFileId());
            if (existing != null && existing.getFileStatus() != null && existing.getFileStatus() != 1L) {
                ptmjFileRankCacheService.clearRankCache();
            }
        }

        ptmjFile.setUpdateTime(DateUtils.getNowDate());
        return ptmjFileMapper.updatePtmjFile(ptmjFile);
    }

    /**
     * 批量删除试卷文件
     *
     * @param fileIds 需要删除的试卷文件主键
     * @return 结果
     */
    @Override
    public int deletePtmjFileByFileIds(Long[] fileIds)
    {
        return ptmjFileMapper.deletePtmjFileByFileIds(fileIds);
    }

    /**
     * 删除试卷文件信息
     *
     * @param fileId 试卷文件主键
     * @return 结果
     */
    @Override
    public int deletePtmjFileByFileId(Long fileId)
    {
        return ptmjFileMapper.deletePtmjFileByFileId(fileId);
    }
}
