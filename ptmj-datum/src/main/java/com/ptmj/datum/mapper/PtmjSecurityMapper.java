package com.ptmj.datum.mapper;

import java.util.List;
import com.ptmj.datum.domain.PtmjSecurity;
import com.ptmj.datum.domain.PtmjUser;
import org.apache.ibatis.annotations.Mapper;  // @author sxm @date 2026-04-08 @reason 解决无法自动装配的问题
import org.apache.ibatis.annotations.Param; // 修改人：LYZ-多参数SQL绑定

/**
 * 用户密保Mapper接口
 *
 * @author pk
 * @date 2026-04-02
 */

@Mapper
/*
@author sxm
@date 2026-04-08
@reason 原代码在PtmjUserServiceImpl.java中无法自动装配，添加 @Mapper 注解让 Spring 扫描到该 Bean
*/
public interface PtmjSecurityMapper
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
     * 删除用户密保
     *
     * @param id 用户密保主键
     * @return 结果
     */
    public int deletePtmjSecurityById(Long id);

    /**
     * 批量删除用户密保
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deletePtmjSecurityByIds(Long[] ids);

    /**
     * 根据用户名查询用户（用于注册时校验用户名是否已存在）
     * @author sxm
     * @date 2026-04-08
     * @param userName 用户名
     * @return 用户信息，不存在则返回null
     */
    public PtmjUser selectPtmjUserByUserName(String userName);

    /**
     * 修改人：LYZ-根据用户ID和问题编号查询密保
     *
     * @param userId 用户ID
     * @param questionNo 密保问题编号（1/2/3）
     * @return 密保信息
     */
    public PtmjSecurity selectByUserIdAndQuestionNo(@Param("userId") Long userId, @Param("questionNo") String questionNo);

    /**
     * LYZ修改：按用户和题号更新密保答案
     */
    public int updateAnswerByUserIdAndQuestionNo(@Param("userId") Long userId, @Param("questionNo") String questionNo, @Param("answer") String answer, @Param("updateBy") String updateBy);
}
