package com.ptmj.datum.service;

import java.util.List;
import com.ptmj.datum.domain.PtmjSecurity;

/**
 * 用户密保Service接口
 *
 * @author pk
 * @date 2026-04-02
 */
public interface IPtmjSecurityService
{
    /**
     * 查询用户密保
     *
     * @param id 用户密保主键
     * @return 用户密保
     */
    public PtmjSecurity selectPtmjSecurityById(Long id);

    /**
     * 查询用户密保列表
     *
     * @param ptmjSecurity 用户密保
     * @return 用户密保集合
     */
    public List<PtmjSecurity> selectPtmjSecurityList(PtmjSecurity ptmjSecurity);

    /**
     * 新增用户密保
     *
     * @param ptmjSecurity 用户密保
     * @return 结果
     */
    public int insertPtmjSecurity(PtmjSecurity ptmjSecurity);

    /**
     * 修改用户密保
     *
     * @param ptmjSecurity 用户密保
     * @return 结果
     */
    public int updatePtmjSecurity(PtmjSecurity ptmjSecurity);

    /**
     * 批量删除用户密保
     *
     * @param ids 需要删除的用户密保主键集合
     * @return 结果
     */
    public int deletePtmjSecurityByIds(Long[] ids);

    /**
     * 删除用户密保信息
     *
     * @param id 用户密保主键
     * @return 结果
     */
    public int deletePtmjSecurityById(Long id);
}
