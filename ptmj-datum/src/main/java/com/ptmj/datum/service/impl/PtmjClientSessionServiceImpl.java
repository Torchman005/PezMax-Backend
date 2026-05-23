package com.ptmj.datum.service.impl;

import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ptmj.datum.service.IPtmjClientSessionService;
import com.ruoyi.common.constant.CacheConstants;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.utils.StringUtils;

/**
 * Ptmj 客户端会话策略 Service 实现
 * 撰写人：LYX
 */
@Service
public class PtmjClientSessionServiceImpl implements IPtmjClientSessionService
{
    /**
     * Ptmj 客户端会话固定 5 天
     * 撰写人：LYX
     */
    private static final int PTMJ_CLIENT_SESSION_DAYS = 5;

    /**
     * Ptmj 客户端会话过期缓冲天数（只用于避免触发若依自动续期，不代表真实会话时长）
     * 撰写人：LYX
     */
    private static final long PTMJ_CLIENT_SESSION_EXPIRE_BUFFER_DAYS = 1L;

    @Autowired
    private RedisCache redisCache;

    @Override
    public void applyFiveDaySession(LoginUser loginUser)
    {
        if (StringUtils.isNull(loginUser) || StringUtils.isEmpty(loginUser.getToken()))
        {
            return;
        }

        long now = System.currentTimeMillis();
        // 撰写人：LYX 真实过期由 Redis 5 天 TTL 决定；这里额外加 1 天避免触发若依 verifyToken 的 20 分钟自动续期。
        long expireTime = now + TimeUnit.DAYS.toMillis(PTMJ_CLIENT_SESSION_DAYS + PTMJ_CLIENT_SESSION_EXPIRE_BUFFER_DAYS);
        loginUser.setLoginTime(now);
        loginUser.setExpireTime(expireTime);

        // 撰写人：LYX 使用若依 token 缓存 key 规范，保证和现有鉴权链路一致
        String userKey = CacheConstants.LOGIN_TOKEN_KEY + loginUser.getToken();
        redisCache.setCacheObject(userKey, loginUser, PTMJ_CLIENT_SESSION_DAYS, TimeUnit.DAYS);
    }
}
