package com.ptmj.datum.service;

import com.ptmj.datum.domain.PtmjSecurity;
import com.ptmj.datum.domain.desktop.*;

import java.util.Set;

/**
 * 桌面端认证 Service 接口
 * 撰写人：LYX
 */
public interface IDesktopAuthService
{
    /**
     * 桌面端登录
     * 撰写人：LYX
     *
     * @param loginBody 登录参数
     * @return token
     */
    public String login(DesktopLoginBody loginBody);

    /**
     * 桌面端注册
     * 撰写人：LYX
     *
     * @param registerBody 注册参数
     */
    public void register(DesktopRegisterBody registerBody);

    /**
     * 查询密保问题
     * 撰写人：LYX
     *
     * @param securityQuestionBody 查询参数
     * @return 密保问题
     */
    public String getSecurityQuestion(DesktopSecurityQuestionBody securityQuestionBody);

    /**
     * 重置密码
     * 撰写人：LYX
     *
     * @param resetPasswordBody 重置密码参数
     */
    public void resetPassword(DesktopResetPasswordBody resetPasswordBody);

    /**
     * 获取当前登录桌面端用户信息
     * 撰写人：LYX
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    public DesktopUserInfoVo getLoginUserInfo(Long userId);

    /**
     * 获取桌面端角色集合
     * 撰写人：LYX
     *
     * @return 角色集合
     */
    public Set<String> getRolePermission();

    /**
     * 获取桌面端权限集合
     * 撰写人：LYX
     *
     * @return 权限集合
     */
    public Set<String> getMenuPermission();

    /**
     * 修改桌面端用户个人资料（用户名）
     *
     * @param userId  用户ID
     * @param userName 新用户名
     * @Author zac
     */
    public void updateProfile(Long userId, String userName);

    /**
     * 修改桌面端用户头像
     *
     * @param userId 用户ID
     * @param avatar 头像地址
     * @Author zac
     */
    public void updateAvatar(Long userId, String avatar);

    /**
     * 修改桌面端用户密码
     *
     * @param userId      用户ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @Author zac
     */
    public void updatePassword(Long userId, String oldPassword, String newPassword);

    /**
     * 修改桌面端用户密保
     *
     * @param userId   用户ID
     * @param question 密保问题
     * @param answer   密保答案
     * @Author zac
     */
    public void updateSecurity(Long userId, String question, String answer);

    /**
     * 获取桌面端用户密保信息
     *
     * @param userId 用户ID
     * @return 密保对象
     * @Author zac
     */
    public PtmjSecurity getSecurityByUserId(Long userId);

    /**
     * 验证当前密码是否正确
     *
     * @param userId   用户ID
     * @param password 待验证的密码
     * @Author zac
     */
    public void verifyPassword(Long userId, String password);

    /**
     * 验证当前账号密保答案
     * @param userId 用户ID
     * @param answer 密保答案
     * @Author zac
     */
    public void verifySecurityAnswer(Long userId, String answer);

    /**
     * 已登录用户通过密保答案重置登录密码（无需验证码）
     * @param userId 用户ID
     * @param answer 密保答案
     * @param newPassword 新密码
     * @param confirmPassword 确认密码
     * @Author zac
     */
    public void resetPasswordBySecurityForLoggedInUser(Long userId, String answer, String newPassword, String confirmPassword);
}
