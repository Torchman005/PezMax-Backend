package com.ptmj.datum.mapper;

import org.apache.ibatis.annotations.Param;
import com.ptmj.datum.domain.PtmjUser;

/**
 * Ptmj 登录认证专用 Mapper
 * 撰写人：LYX
 */
public interface PtmjAuthMapper
{
    /**
     * 按用户名精确查询 Ptmj 用户
     * 撰写人：LYX
     *
     * @param userName 用户名
     * @return 用户信息
     */
    public PtmjUser selectUserByUserName(@Param("userName") String userName);
}
