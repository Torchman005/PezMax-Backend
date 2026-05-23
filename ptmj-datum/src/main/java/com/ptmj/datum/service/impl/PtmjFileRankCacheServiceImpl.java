package com.ptmj.datum.service.impl;

import com.ptmj.datum.domain.vo.PtmjUserRankVO;
import com.ptmj.datum.mapper.PtmjUserMapper;
import com.ptmj.datum.service.PtmjFileRankCacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class PtmjFileRankCacheServiceImpl implements PtmjFileRankCacheService {
    @Autowired
    private PtmjUserMapper userMapper;

    @Autowired
    private RedisTemplate<Object, Object> redisTemplate;

    private static final String RANK_KEY = "ptmj:rank:upload";
    private static final long CACHE_EXPIRE_HOURS = 2;

    @Override
    public List<PtmjUserRankVO> getTopUploaders() {
//        clearRankCache();
        Object cached = redisTemplate.opsForValue().get(RANK_KEY);
        if (cached != null) {
            return (List<PtmjUserRankVO>) cached;
        }

        List<PtmjUserRankVO> topUsers = userMapper.selectTopUploaders();
        redisTemplate.opsForValue().set(RANK_KEY, topUsers, CACHE_EXPIRE_HOURS, TimeUnit.HOURS);

        return topUsers;
    }

    @Override
    public void clearRankCache() {
        redisTemplate.delete(RANK_KEY);
    }
}
