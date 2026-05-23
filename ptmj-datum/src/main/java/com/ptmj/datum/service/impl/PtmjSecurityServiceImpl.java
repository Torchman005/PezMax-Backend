package com.ptmj.datum.service.impl;

import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ptmj.datum.mapper.PtmjSecurityMapper;
import com.ptmj.datum.domain.PtmjSecurity;
import com.ptmj.datum.service.IPtmjSecurityService;

/**
 * 用户密保Service业务层处理
 *
 * @author pk
 * @date 2026-04-02
 */
@Service
public class PtmjSecurityServiceImpl implements IPtmjSecurityService
{
    @Autowired
    private PtmjSecurityMapper ptmjSecurityMapper;

    /**
     * 查询用户密保
     *
     * @param id 用户密保主键
     * @return 用户密保
     */
    @Override
    public PtmjSecurity selectPtmjSecurityById(Long id)
    {
        return ptmjSecurityMapper.selectPtmjSecurityById(id);
    }

    /**
     * 查询用户密保列表
     *
     * @param ptmjSecurity 用户密保
     * @return 用户密保
     */
    @Override
    public List<PtmjSecurity> selectPtmjSecurityList(PtmjSecurity ptmjSecurity)
    {
        return ptmjSecurityMapper.selectPtmjSecurityList(ptmjSecurity);
    }

    /**
     * 新增用户密保
     *
     * @param ptmjSecurity 用户密保
     * @return 结果
     */
    @Override
    public int insertPtmjSecurity(PtmjSecurity ptmjSecurity)
    {
        ptmjSecurity.setCreateTime(DateUtils.getNowDate());
        return ptmjSecurityMapper.insertPtmjSecurity(ptmjSecurity);
    }

    /**
     * 修改用户密保
     *
     * @param ptmjSecurity 用户密保
     * @return 结果
     */
    @Override
    public int updatePtmjSecurity(PtmjSecurity ptmjSecurity)
    {
        ptmjSecurity.setUpdateTime(DateUtils.getNowDate());
        return ptmjSecurityMapper.updatePtmjSecurity(ptmjSecurity);
    }

    /**
     * 批量删除用户密保
     *
     * @param ids 需要删除的用户密保主键
     * @return 结果
     */
    @Override
    public int deletePtmjSecurityByIds(Long[] ids)
    {
        return ptmjSecurityMapper.deletePtmjSecurityByIds(ids);
    }

    /**
     * 删除用户密保信息
     *
     * @param id 用户密保主键
     * @return 结果
     */
    @Override
    public int deletePtmjSecurityById(Long id)
    {
        return ptmjSecurityMapper.deletePtmjSecurityById(id);
    }
}
