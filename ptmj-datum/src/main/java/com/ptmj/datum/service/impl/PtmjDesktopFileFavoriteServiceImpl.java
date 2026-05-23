package com.ptmj.datum.service.impl;

import com.ptmj.datum.domain.PtmjFile;
import com.ptmj.datum.mapper.PtmjDesktopFileFavoriteMapper;
import com.ptmj.datum.mapper.PtmjDesktopFileMapper;
import com.ptmj.datum.service.IPtmjDesktopFileFavoriteService;
import com.ruoyi.common.utils.PageUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PtmjDesktopFileFavoriteServiceImpl implements IPtmjDesktopFileFavoriteService {

    @Autowired
    private PtmjDesktopFileFavoriteMapper ptmjDesktopFileFavoriteMapper;
    @Autowired
    private PtmjDesktopFileMapper ptmjDesktopFileMapper;

    /**
     * 桌面端用户收藏试卷列表
     *
     * @param userId   用户id
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @Author zac
     * @Date 2026/4/9 12:41
     */
    @Override
    public List<PtmjFile> selectFileFavoriteList(Long userId, Integer pageNum, Integer pageSize) {
        List<Integer> fileId = ptmjDesktopFileFavoriteMapper.selectPtmjDesktopFileFavoriteList(userId);

        if (fileId == null || fileId.isEmpty()) {
            return new ArrayList<>();
        }

        PageUtils.startPage(pageNum, pageSize);

        List<PtmjFile> ptmjFileList = ptmjDesktopFileMapper.selectPtmjDesktopFileList(fileId);
        return ptmjFileList;
    }
}
