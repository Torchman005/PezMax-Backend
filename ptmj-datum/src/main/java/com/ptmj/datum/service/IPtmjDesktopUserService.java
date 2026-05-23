package com.ptmj.datum.service;

import com.ptmj.datum.domain.vo.PtmjDesktopUserVO;

public interface IPtmjDesktopUserService {

    /**
     * 根据用户id查询桌面用户信息
     * @param userId
     * @Author: zac
     * @Date: 2026-04-08 20:10
     */
    PtmjDesktopUserVO selectPtmjDeskUserByUserId(Long userId);
}
