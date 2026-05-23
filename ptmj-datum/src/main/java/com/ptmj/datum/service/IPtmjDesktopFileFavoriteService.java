package com.ptmj.datum.service;

import com.ptmj.datum.domain.PtmjFile;

import java.util.List;

public interface IPtmjDesktopFileFavoriteService {
    /**
     * 桌面端用户收藏试卷列表
     * @param userId 用户id
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @Author zac
     * @Date 2026/4/9 12:41
     */
    List<PtmjFile> selectFileFavoriteList(Long userId, Integer pageNum, Integer pageSize);
}
