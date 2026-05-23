package com.ptmj.datum.service;

import com.ptmj.datum.domain.PtmjLoginBody;

/**
 * Ptmj 登录认证 Service 接口
 * 撰写人：LYX
 */
public interface IPtmjAuthService
{
    /**
     * Ptmj 用户登录
     * 撰写人：LYX
     *
     * @param loginBody 登录参数
     * @return token
     */
    public String login(PtmjLoginBody loginBody);
}
