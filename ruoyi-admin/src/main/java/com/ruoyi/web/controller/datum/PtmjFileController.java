package com.ruoyi.web.controller.datum;

import java.util.List;
import java.util.Map;

import com.ptmj.datum.domain.PtmjFile;
import com.ptmj.datum.domain.vo.FileTreeVo;
import com.ptmj.datum.domain.vo.SubjectSuggestionVo;
import com.ptmj.datum.domain.vo.SchoolSuggestionVo;
import com.ptmj.datum.service.IPtmjFileDownloadService;
import com.ptmj.datum.service.IPtmjFileService;
import com.ruoyi.common.utils.file.MinioStorageService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ptmj.datum.domain.PtmjFile;
import com.ptmj.datum.service.IPtmjFileService;
import com.ptmj.datum.service.PtmjFileTreeCacheService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.file.MinioStorageService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


//package com.ptmj.datum.domain.vo;

/**
 * 试卷文件Controller
 *
 * @author pk
 * @date 2026-04-02
 */
@RestController
@RequestMapping("/datum/file")
public class PtmjFileController extends BaseController
{
    @Autowired
    private IPtmjFileService ptmjFileService;

    //@Autowired
    //private PtmjFileTreeCacheService ptmjFileTreeCacheService;

    @Autowired
    private MinioStorageService minioStorageService;

//    @Autowired
//    private IPtmjFileDownloadService ptmjFileDownloadService;

    /**
     * 上传文件至 MinIO（ptmj 桶根目录）
     * fc
     */
//    @PreAuthorize("@ss.hasPermi('datum:file:add')")
    @Log(title = "试卷文件", businessType = BusinessType.OTHER)
    @PostMapping("/upload")
    public AjaxResult upload(@RequestParam("file") MultipartFile file)
    {
        try
        {
            Map<String, Object> data = minioStorageService.uploadToBucketRoot(file);
            return success(data);
        }
        catch (Exception e)
        {
            return error("上传失败：" + e.getMessage());
        }
    }

    /**
     * 查询试卷文件列表
     */
//    @PreAuthorize("@ss.hasPermi('datum:file:list')")
    @Anonymous
    @GetMapping("/list")
    public TableDataInfo list(PtmjFile ptmjFile)
    {
        startPage();
        List<PtmjFile> list = ptmjFileService.selectPtmjFileList(ptmjFile);
        return getDataTable(list);
    }

    /**
     * @author Lzj
     *
     * 获取文件树形结构数据
     */
//    @PreAuthorize("@ss.hasPermi('datum:file:list')")
    @Anonymous
    @GetMapping("/tree")
    public AjaxResult getTree(PtmjFile ptmjFile)
    {
        List<FileTreeVo> tree = ptmjFileService.getPtmjFileTree(ptmjFile);
        return success(tree);
    }

    /**
     * @author Lzj
     *
     * 获取学科联想推荐列表
     */
    @GetMapping("/subjects")
    public AjaxResult getSubjects(@RequestParam(value = "keyword", required = false) String keyword,
                                  @RequestParam(value = "limit", required = false) Integer limit)
    {
        List<SubjectSuggestionVo> subjects = ptmjFileService.getSubjectSuggestions(keyword, limit);
        return success(subjects);
    }

    /**
     * 获取学校联想推荐列表
     */
    @Anonymous
    @GetMapping("/schools")
    public AjaxResult getSchools(@RequestParam(value = "keyword", required = false) String keyword,
                                 @RequestParam(value = "limit", required = false) Integer limit)
    {
        List<SchoolSuggestionVo> schools = ptmjFileService.getSchoolSuggestions(keyword, limit);
        return success(schools);
    }

    /**
     * 检查学校名称是否已存在
     */
    @GetMapping("/schools/check")
    public AjaxResult checkSchoolName(@RequestParam("schoolName") String schoolName)
    {
        boolean exists = ptmjFileService.checkSchoolNameExists(schoolName);
        return success(exists);
    }


    /**
     * 导出试卷文件列表
     */
//    @PreAuthorize("@ss.hasPermi('datum:file:export')")
    @Log(title = "试卷文件", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, PtmjFile ptmjFile)
    {
        List<PtmjFile> list = ptmjFileService.selectPtmjFileList(ptmjFile);
        ExcelUtil<PtmjFile> util = new ExcelUtil<PtmjFile>(PtmjFile.class);
        util.exportExcel(response, list, "试卷文件数据");
    }

    /**
     * 获取试卷文件详细信息
     */
//    @PreAuthorize("@ss.hasPermi('datum:file:query')")
    @Anonymous
    @GetMapping(value = "/{fileId}")
    public AjaxResult getInfo(@PathVariable("fileId") Long fileId)
    {
        return success(ptmjFileService.selectPtmjFileByFileId(fileId));
    }

    /**
     * @author Lzj
     *
     * 新增试卷文件
     */
//    @PreAuthorize("@ss.hasPermi('datum:file:add')")
    @Log(title = "试卷文件", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@org.springframework.web.bind.annotation.RequestParam("file") org.springframework.web.multipart.MultipartFile file, PtmjFile ptmjFile) throws Exception
    {
        return toAjax(ptmjFileService.uploadPtmjFile(file, ptmjFile));
    }

    /**
     * 新增试卷文件（管理端）
     * bug，暂时弃用
     */
//    @PreAuthorize("@ss.hasPermi('datum:file:addfile')")
//    @Log(title = "试卷文件", businessType = BusinessType.INSERT)
//    @PostMapping("/save")
//    public AjaxResult addFile(@RequestBody PtmjFile ptmjFile)
//    {
//        LoginUser loginUser = getLoginUser();
//        if (loginUser != null && loginUser.getUser() != null)
//        {
//            String nick = loginUser.getUser().getNickName();
//            if (StringUtils.isEmpty(nick))
//            {
//                nick = loginUser.getUser().getUserName();
//            }
//            ptmjFile.setReviewer(nick);
//        }
//        ptmjFile.setFileStatus(1L);
//        return toAjax(ptmjFileService.insertPtmjFile(ptmjFile));
//    }

    /**
     * 修改试卷文件
     */
//    @PreAuthorize("@ss.hasPermi('datum:file:edit')")
    @Log(title = "试卷文件", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody PtmjFile ptmjFile)
    {
        return toAjax(ptmjFileService.updatePtmjFile(ptmjFile));
    }

    /**
     * 按上传用户ID一键通过该用户未审核文件
     */
//    @PreAuthorize("@ss.hasPermi('datum:file:edit')")
    @Log(title = "试卷文件审核", businessType = BusinessType.UPDATE)
    @PutMapping("/approvePendingByUser/{userId}")
    public AjaxResult approvePendingByUser(@PathVariable("userId") Long userId)
    {
        if (userId == null)
        {
            return error("上传用户ID不能为空");
        }
        String reviewer = String.valueOf(getUserId());
        int rows = ptmjFileService.approvePendingFilesByUserId(userId, reviewer);
        return success("已通过该用户未审核文件 " + rows + " 个").put("count", rows);
    }

    /**
     * 删除试卷文件
     */
//    @PreAuthorize("@ss.hasPermi('datum:file:remove')")
    @Log(title = "试卷文件", businessType = BusinessType.DELETE)
    @DeleteMapping("/{fileIds}")
    public AjaxResult remove(@PathVariable Long[] fileIds)
    {
        return toAjax(ptmjFileService.deletePtmjFileByFileIds(fileIds));
    }
    /**
     * lxq
     * 按关键词搜索文件（同时匹配文件名和学科名称），学科命中优先排列
     */
    @GetMapping("/search")
    public List<PtmjFile> search(@RequestParam(value = "keyword", required = false) String keyword)
    {
        return ptmjFileService.searchByKeyword(keyword);
    }

//    /**
//     * 与lzj的getTree方法功能冲突，弃用
//     * 获取文件树————范光友
//     */
//    @PreAuthorize("@ss.hasPermi('datum:file:tree')")
//    @GetMapping("/tree")
//    public AjaxResult getFileTree()
//    {
//        return success(ptmjFileTreeCacheService.getFileTree());
//    }
//

//    /**
//     * 清除文件树缓存————范光友
//     */
//    @PreAuthorize("@ss.hasPermi('datum:file:remove')")
//    @Log(title = "文件树缓存", businessType = BusinessType.CLEAN)
//    @DeleteMapping("/cache")
//    public AjaxResult clearCache()
//    {
//        ptmjFileTreeCacheService.clearCache();
//        return success();
//    }
}

