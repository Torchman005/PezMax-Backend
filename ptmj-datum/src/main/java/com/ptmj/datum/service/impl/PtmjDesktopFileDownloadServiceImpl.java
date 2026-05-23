package com.ptmj.datum.service.impl;

import com.ptmj.datum.domain.PtmjFile;
import com.ptmj.datum.mapper.PtmjDesktopFileDownloadMapper;
import com.ptmj.datum.mapper.PtmjDesktopFileMapper;
import com.ptmj.datum.service.IPtmjDesktopFileDownloadService;
import com.ruoyi.common.utils.PageUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PtmjDesktopFileDownloadServiceImpl implements IPtmjDesktopFileDownloadService {

    @Autowired
    private PtmjDesktopFileDownloadMapper ptmjDesktopFileDownloadMapper;

    @Autowired
    private PtmjDesktopFileMapper ptmjDesktopFileMapper;

    /**
     * 查询桌面端用户试卷下载列表
     * @param userId 用户ID
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @Author zac
     * @Date 2026/4/9 12:23
     */
    @Override
    public List<PtmjFile> selectPtmjDesktopFileDownloadList(Long userId, Integer pageNum, Integer pageSize) {
        List<Integer> fileId = ptmjDesktopFileDownloadMapper.selectPtmjDesktopFileDownloadList(userId);
        
        if (fileId == null || fileId.isEmpty()) {
            return new ArrayList<>();
        }
        
        PageUtils.startPage(pageNum, pageSize);
        
        List<PtmjFile> ptmjFileList = ptmjDesktopFileMapper.selectPtmjDesktopFileList(fileId);
        return ptmjFileList;
    }

    /**
     * 根据用户ID和试卷ID删除下载记录
     *
     * @param userId 用户ID
     * @param fileId 试卷ID
     * @return 影响行数
     * @Author zac
     */
    @Override
    public int hideByUserIdAndFileId(Long userId, Long fileId) {
        return ptmjDesktopFileDownloadMapper.hideByUserIdAndFileId(userId, fileId);
    }
}
