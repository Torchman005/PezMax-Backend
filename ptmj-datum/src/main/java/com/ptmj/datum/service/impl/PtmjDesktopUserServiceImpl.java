package com.ptmj.datum.service.impl;

import com.ptmj.datum.domain.vo.PtmjDesktopUserVO;
import com.ptmj.datum.mapper.PtmjDesktopUserMapper;
import com.ptmj.datum.service.IPtmjDesktopUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PtmjDesktopUserServiceImpl implements IPtmjDesktopUserService {

    @Autowired
    private PtmjDesktopUserMapper ptmjDesktopUserMapper;


    /**
     * 根据用户id查询桌面用户信息
     *
     * @param userId
     * @Author: zac
     * @Date: 2026-04-08 20:10
     */
    @Override
    public PtmjDesktopUserVO selectPtmjDeskUserByUserId(Long userId) {
        PtmjDesktopUserVO ptmjDesktopUserVO = new PtmjDesktopUserVO();
        ptmjDesktopUserVO = ptmjDesktopUserMapper.selectPtmjDeskUserByUserId(userId);
        return ptmjDesktopUserVO;
    }
}
