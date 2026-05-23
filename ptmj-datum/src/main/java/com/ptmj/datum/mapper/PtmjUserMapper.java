package com.ptmj.datum.mapper;

import com.ptmj.datum.domain.PtmjUser;
import com.ptmj.datum.domain.vo.PtmjUserRankVO;
import org.apache.ibatis.annotations.Mapper;  // @author sxm @date 2026-04-08 @reason 解决无法自动装配的问题
import org.apache.ibatis.annotations.Param; // 修改人：LYZ-多参数SQL绑定

import java.util.List;

/**
 * 平台用户Mapper接口
 *
 * @author pk
 * @date 2026-04-02
 */

// @author sxm
// @date 2026-04-08
// @reason 原代码在PtmjUserService.java中无法自动装配，添加 @Mapper 注解让 Spring 扫描到该 Bean
@Mapper
public interface PtmjUserMapper
{
    /**
     * 查询平台用户
     *
     * @param userId 平台用户主键
     * @return 平台用户
     */
    public PtmjUser selectPtmjUserByUserId(Long userId);

    /**
     * 查询平台用户列表
     *
     * @param ptmjUser 平台用户
     * @return 平台用户集合
     */
    public List<PtmjUser> selectPtmjUserList(PtmjUser ptmjUser);

    /**
     * 新增平台用户
     *
     * @param ptmjUser 平台用户
     * @return 结果
     */
    public int insertPtmjUser(PtmjUser ptmjUser);

    /**
     * 修改平台用户
     *
     * @param ptmjUser 平台用户
     * @return 结果
     */
    public int updatePtmjUser(PtmjUser ptmjUser);

    /**
     * 删除平台用户
     *
     * @param userId 平台用户主键
     * @return 结果
     */
    public int deletePtmjUserByUserId(Long userId);

    /**
     * 批量删除平台用户
     *
     * @param userIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deletePtmjUserByUserIds(Long[] userIds);

    // PtmjFileMapper.java
    /**
     * 查询上传次数前12的用户ID————范光友
     */
    public List<PtmjUserRankVO> selectTopUploaders();
    // @author sxm
    // @date 2026-04-08
    // @reason 注册功能需要校验用户名是否已存在
    /**
     * 根据用户名查询用户
     *
     * @param userName 用户名
     * @return 用户信息，不存在则返回null
     */
    public PtmjUser selectPtmjUserByUserName(String userName);

    /**
     * 修改人：LYZ-根据用户ID更新密码
     *
     * @param userId 用户ID
     * @param password 新密码（已加密）
     * @return 结果
     */
    public int updatePasswordByUserId(@Param("userId") Long userId, @Param("password") String password);

    /**
     * 审核通过时用户上传计数+1
     * fc
     */
    public int incrementCountByUserId(@Param("userId") Long userId);
}
