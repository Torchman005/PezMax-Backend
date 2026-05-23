package com.ptmj.datum.service;

import com.ptmj.datum.domain.PtmjFile;

import java.util.List;

public interface IPtmjDesktopFileDownloadService {
    /**
     * 查询桌面端用户试卷下载列表
     * @param userId 用户ID
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @Author zac
     * @Date 2026/4/9 12:22
     */
    List<PtmjFile> selectPtmjDesktopFileDownloadList(Long userId, Integer pageNum, Integer pageSize);

    /**
     * 根据用户ID和试卷ID删除下载记录
     *
     * @param userId 用户ID
     * @param fileId 试卷ID
     * @return 影响行数
     * @Author zac
     */
    int hideByUserIdAndFileId(Long userId, Long fileId);
}
