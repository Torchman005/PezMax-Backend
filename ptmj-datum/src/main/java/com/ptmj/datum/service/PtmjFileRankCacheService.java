package com.ptmj.datum.service;

import com.ptmj.datum.domain.vo.PtmjUserRankVO;

import java.util.List;

public interface PtmjFileRankCacheService {
    // 获取上传排行榜————范光友
    List<PtmjUserRankVO> getTopUploaders();
    // 清空缓存————范光友
    void clearRankCache();
}
