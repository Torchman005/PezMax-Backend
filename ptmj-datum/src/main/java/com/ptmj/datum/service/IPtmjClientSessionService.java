package com.ptmj.datum.service;

import com.ruoyi.common.core.domain.model.LoginUser;

/**
 * Ptmj 客户端会话策略 Service 接口
 * 撰写人：LYX
 */
public interface IPtmjClientSessionService
{
    /**
     * 把 Ptmj 客户端登录会话固定为 5 天有效（到期必须重新登录）
     * 撰写人：LYX
     *
     * @param loginUser 登录用户
     */
    public void applyFiveDaySession(LoginUser loginUser);
}
