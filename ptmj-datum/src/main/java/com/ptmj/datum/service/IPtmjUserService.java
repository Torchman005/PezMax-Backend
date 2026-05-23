// 修改人：LYZ
package com.ptmj.datum.service;

import java.util.List;
import java.util.Map;
import com.ptmj.datum.domain.PtmjUser;
import com.ptmj.datum.dto.PtmjRegisterDto;

/**
 * 平台用户Service接口
 *
 * @author pk
 * @date 2026-04-02
 */
public interface IPtmjUserService
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
     * 批量删除平台用户
     *
     * @param userIds 需要删除的平台用户主键集合
     * @return 结果
     */
    public int deletePtmjUserByUserIds(Long[] userIds);

    /**
     * 删除平台用户信息
     *
     * @param userId 平台用户主键
     * @return 结果
     */
    public int deletePtmjUserByUserId(Long userId);

    /**
     * 用户注册
     *
     * @author sxm
     * @date 2026-04-08
     * @param registerDto 注册信息DTO
     * @return 注册结果（包含userId和userName）
     */
    public Map<String, Object> register(PtmjRegisterDto registerDto);

    /**
     * 修改人：LYZ-根据用户名查询平台用户
     *
     * @param userName 用户名
     * @return 平台用户
     */
    public PtmjUser selectPtmjUserByUserName(String userName);

    /**
     * 修改人：LYZ-通过三条密保重置密码
     *
     * @param userName 用户名
     * @param securityAnswerOne 密保问题一答案
     * @param securityAnswerTwo 密保问题二答案
     * @param securityAnswerThree 密保问题三答案
     * @param newPassword 新密码
     */
    public void resetPasswordBySecurity(String userName, String securityAnswerOne, String securityAnswerTwo, String securityAnswerThree, String newPassword);

    /**
     * LYZ修改：管理员按用户名重置三条密保答案
     *
     * @param userName 用户名
     * @param securityAnswerOne 密保问题一答案
     * @param securityAnswerTwo 密保问题二答案
     * @param securityAnswerThree 密保问题三答案
     */
    public void resetSecurityAnswersByUserName(String userName, String securityAnswerOne, String securityAnswerTwo, String securityAnswerThree);

    // @author SXM
    // @date 2026-04-19
    // @reason 根据用户名查询用户的3个自定义密保问题(不返回答案),用于找回密码时前端展示
    //
    // @param userName 用户名
    // @return Map包含三个问题:questionOne, questionTwo, questionThree
    public Map<String, String> selectSecurityQuestionsByUserName(String userName);

    // @author SXM
    // @date 2026-04-19
    // @reason 根据用户名查询用户的3个自定义密保问题列表(转换为前端期望的List结构)
    //
    // @param userName 用户名
    // @return List<Map>格式:[{question: '...'}, {question: '...'}, {question: '...'}]
    public java.util.List<java.util.Map<String, String>> selectSecurityQuestionsListByUserName(String userName);

    // @author SXM
    // @date 2026-04-27
    // @reason 将其从controller层中挪至service层中：注册/通用验证码校验
    public void validateCaptcha(String code, String uuid);

    // @author SXM
    // @date 2026-04-27
    // @reason 将其从controller层中挪至service层中：找回密码强制验证码校验
    public void validateCaptchaStrict(String code, String uuid);

    // @author SXM
    // @date 2026-04-27
    // @reason 将其从controller层中挪至service层中：完整的找回密码业务逻辑（含密码一致性校验和验证码校验）
    public void executeResetPassword(java.util.Map<String, String> body);

    /**
     * 修改人：LYZ-管理员按用户名重置三条密保答案
     * SXM于2026-04-27迁移：将其从controller层中挪至service层中
     */
    public void executeResetSecurityAnswers(java.util.Map<String, String> body);

    /**
     * 修改人：LYZ-桌面端登录返回客户端用户信息
     * SXM于2026-04-27迁移：将其从controller层中挪至service层中
     */
    public java.util.Map<String, Object> executeDesktopLogin(com.ptmj.datum.domain.PtmjLoginBody loginBody);

    /**
     * 修改人：LYZ-客户端获取当前登录用户信息
     * SXM于2026-04-27迁移：将其从controller层中挪至service层中
     */
    public java.util.Map<String, Object> executeGetClientInfo();

    // @author SXM
    // @date 2026-04-28
    // @reason 将其从controller层中挪至service层中：生成桌面端验证码图片
    // @return Map包含uuid和img(base64编码的图片)
    public java.util.Map<String, Object> generateCaptchaImage();

    /**
     * 审核通过时用户上传计数+1
     * fc
     * @param userId 用户ID
     * @return 结果
     */
    public int incrementCountByUserId(Long userId);
}
