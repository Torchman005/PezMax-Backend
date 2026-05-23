package com.ptmj.datum.service.impl;

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ptmj.datum.domain.PtmjSecurity;
import com.ptmj.datum.domain.PtmjUser;
import com.ptmj.datum.domain.desktop.DesktopLoginBody;
import com.ptmj.datum.domain.desktop.DesktopRegisterBody;
import com.ptmj.datum.domain.desktop.DesktopResetPasswordBody;
import com.ptmj.datum.domain.desktop.DesktopSecurityQuestionBody;
import com.ptmj.datum.domain.desktop.DesktopUserInfoVo;
import com.ptmj.datum.mapper.DesktopAuthMapper;
import com.ptmj.datum.service.IDesktopAuthService;
import com.ruoyi.common.constant.CacheConstants;
import com.ruoyi.common.constant.UserConstants;
import com.ruoyi.common.core.domain.entity.SysRole;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.framework.web.service.TokenService;
import com.ruoyi.system.service.ISysConfigService;

/**
 * 桌面端认证 Service 实现
 * 撰写人：LYX
 */
@Service
public class DesktopAuthServiceImpl implements IDesktopAuthService
{
    /**
     * 桌面端正常状态值，依据 ptmj_user 表现有设计使用 1 表示可登录
     * 撰写人：LYX
     */
    private static final String DESKTOP_USER_STATUS_NORMAL = "1";

    /**
     * 桌面端固定角色标识
     * 撰写人：LYX
     */
    private static final String DESKTOP_ROLE_KEY = "desktop_user";

    /**
     * 桌面端固定权限标识
     * 撰写人：LYX
     */
    private static final String DESKTOP_PERMISSION_KEY = "desktop:auth:user";

    private static final int SECURITY_TEXT_MAX_LENGTH = 50;

    @Autowired
    private DesktopAuthMapper desktopAuthMapper;

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private ISysConfigService configService;

    @Override
    public String login(DesktopLoginBody loginBody)
    {
        String username = safeTrim(loginBody.getUsername());
        String password = safeTrim(loginBody.getPassword());
        validateCaptcha(username, loginBody.getCode(), loginBody.getUuid());
        validateUsername(username);
        validatePassword(password, "登录密码");

        PtmjUser ptmjUser = getUserByUserName(username);
        if (StringUtils.isNull(ptmjUser))
        {
            throw new ServiceException("桌面端用户不存在");
        }
        if (!DESKTOP_USER_STATUS_NORMAL.equals(ptmjUser.getStatus()))
        {
            throw new ServiceException("当前桌面端账号已被停用");
        }
        if (!matchesPassword(password, ptmjUser.getPassword()))
        {
            throw new ServiceException("用户名或密码错误");
        }
        return tokenService.createToken(buildLoginUser(ptmjUser));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(DesktopRegisterBody registerBody)
    {
        String username = safeTrim(registerBody.getUsername());
        String password = safeTrim(registerBody.getPassword());
        String confirmPassword = safeTrim(registerBody.getConfirmPassword());
        String question = safeTrim(registerBody.getQuestion());
        String answer = normalizeAnswer(registerBody.getAnswer());

        validateUsername(username);
        validatePassword(password, "注册密码");
        if (!StringUtils.equals(password, confirmPassword))
        {
            throw new ServiceException("两次输入的密码不一致");
        }
        if (StringUtils.isEmpty(question))
        {
            throw new ServiceException("密保问题不能为空");
        }
        validateSecurityQuestions(question);
        validateSecurityTextLength(question, "密保问题");
        if (StringUtils.isEmpty(answer))
        {
            throw new ServiceException("密保答案不能为空");
        }
        validateSecurityTextLength(answer, "密保答案");
        if (desktopAuthMapper.countUserByUserName(username) > 0)
        {
            throw new ServiceException("当前用户名已存在");
        }

        PtmjUser ptmjUser = new PtmjUser();
        ptmjUser.setUserName(username);
        ptmjUser.setPassword(SecurityUtils.encryptPassword(password));
        ptmjUser.setAvatar(null);
        ptmjUser.setCount(0L);
        ptmjUser.setStatus(DESKTOP_USER_STATUS_NORMAL);
        ptmjUser.setCreatBy("desktop_register");
        ptmjUser.setCreateTime(DateUtils.getNowDate());
        ptmjUser.setRemark("撰写人：LYX 桌面端注册用户");
        desktopAuthMapper.insertDesktopUser(ptmjUser);

        PtmjSecurity ptmjSecurity = new PtmjSecurity();
        ptmjSecurity.setUserId(ptmjUser.getUserId());
        ptmjSecurity.setQuestion(question);
        ptmjSecurity.setAnswer(SecurityUtils.encryptPassword(answer));
        ptmjSecurity.setCreateBy("desktop_register");
        ptmjSecurity.setCreateTime(DateUtils.getNowDate());
        ptmjSecurity.setRemark("撰写人：LYX 桌面端注册密保");
        desktopAuthMapper.insertDesktopSecurity(ptmjSecurity);
    }

    @Override
    public String getSecurityQuestion(DesktopSecurityQuestionBody securityQuestionBody)
    {
        String username = safeTrim(securityQuestionBody.getUsername());
        validateCaptcha(username, securityQuestionBody.getCode(), securityQuestionBody.getUuid());
        validateUsername(username);

        PtmjUser ptmjUser = getUserByUserName(username);
        if (StringUtils.isNull(ptmjUser))
        {
            throw new ServiceException("当前用户名不存在");
        }

        PtmjSecurity ptmjSecurity = desktopAuthMapper.selectSecurityByUserId(ptmjUser.getUserId());
        if (StringUtils.isNull(ptmjSecurity) || StringUtils.isEmpty(ptmjSecurity.getQuestion()))
        {
            throw new ServiceException("当前账号尚未配置密保问题");
        }
        return ptmjSecurity.getQuestion();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(DesktopResetPasswordBody resetPasswordBody)
    {
        String username = safeTrim(resetPasswordBody.getUsername());
        String answer = normalizeAnswer(resetPasswordBody.getAnswer());
        String newPassword = safeTrim(resetPasswordBody.getNewPassword());
        String confirmPassword = safeTrim(resetPasswordBody.getConfirmPassword());

        validateCaptcha(username, resetPasswordBody.getCode(), resetPasswordBody.getUuid());
        validateUsername(username);
        validatePassword(newPassword, "新密码");
        if (!StringUtils.equals(newPassword, confirmPassword))
        {
            throw new ServiceException("两次输入的新密码不一致");
        }
        if (StringUtils.isEmpty(answer))
        {
            throw new ServiceException("密保答案不能为空");
        }

        PtmjUser ptmjUser = getUserByUserName(username);
        if (StringUtils.isNull(ptmjUser))
        {
            throw new ServiceException("当前用户名不存在");
        }

        PtmjSecurity ptmjSecurity = desktopAuthMapper.selectSecurityByUserId(ptmjUser.getUserId());
        if (StringUtils.isNull(ptmjSecurity) || StringUtils.isEmpty(ptmjSecurity.getAnswer()))
        {
            throw new ServiceException("当前账号尚未配置密保答案");
        }
        if (!matchesAnswer(answer, ptmjSecurity.getAnswer()))
        {
            throw new ServiceException("密保答案错误");
        }

        PtmjUser updateUser = new PtmjUser();
        updateUser.setUserId(ptmjUser.getUserId());
        updateUser.setPassword(SecurityUtils.encryptPassword(newPassword));
        updateUser.setUpdateBy("desktop_reset_password");
        updateUser.setUpdateTime(DateUtils.getNowDate());
        desktopAuthMapper.updateDesktopUserPassword(updateUser);
    }

    @Override
    public DesktopUserInfoVo getLoginUserInfo(Long userId)
    {
        PtmjUser ptmjUser = desktopAuthMapper.selectUserByUserId(userId);
        if (StringUtils.isNull(ptmjUser))
        {
            throw new ServiceException("当前登录用户不存在");
        }

        DesktopUserInfoVo userInfoVo = new DesktopUserInfoVo();
        userInfoVo.setUserId(ptmjUser.getUserId());
        userInfoVo.setUserName(ptmjUser.getUserName());
        userInfoVo.setAvatar(ptmjUser.getAvatar());
        userInfoVo.setStatus(ptmjUser.getStatus());
        userInfoVo.setUserType("desktop");
        return userInfoVo;
    }

    @Override
    public Set<String> getRolePermission()
    {
        return Collections.singleton(DESKTOP_ROLE_KEY);
    }

    @Override
    public Set<String> getMenuPermission()
    {
        return Collections.singleton(DESKTOP_PERMISSION_KEY);
    }

    /*
     * 修改用户信息
     *
     * @param userId  用户ID
     * @param userName 新用户名
     * @Author zac
     * */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProfile(Long userId, String userName)
    {
        if (StringUtils.isEmpty(userName))
        {
            throw new ServiceException("用户名不能为空");
        }
        validateUsername(userName);
        // 检查用户名是否被他人占用
        PtmjUser currentUser = desktopAuthMapper.selectUserByUserId(userId);
        if (StringUtils.isNull(currentUser))
        {
            throw new ServiceException("用户不存在");
        }
        if (!currentUser.getUserName().equals(userName))
        {
            if (desktopAuthMapper.countUserByUserName(userName) > 0)
            {
                throw new ServiceException("该用户名已被占用");
            }
        }
        PtmjUser updateUser = new PtmjUser();
        updateUser.setUserId(userId);
        updateUser.setUserName(userName);
        updateUser.setUpdateBy("desktop_profile");
        desktopAuthMapper.updateDesktopUserProfile(updateUser);
    }

    /*
     * 修改用户头像
     *
     * @param userId  用户ID
     * @param avatar 新头像
     * @Author zac
     * */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAvatar(Long userId, String avatar)
    {
        PtmjUser updateUser = new PtmjUser();
        updateUser.setUserId(userId);
        updateUser.setAvatar(avatar);
        updateUser.setUpdateBy("desktop_profile");
        desktopAuthMapper.updateDesktopUserAvatar(updateUser);
    }

    /*
     * 修改用户密码
     *
     * @param userId  用户ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @Author zac
     * */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePassword(Long userId, String oldPassword, String newPassword)
    {
        if (StringUtils.isEmpty(oldPassword))
        {
            throw new ServiceException("旧密码不能为空");
        }
        validatePassword(newPassword, "新密码");

        PtmjUser ptmjUser = desktopAuthMapper.selectUserByUserId(userId);
        if (StringUtils.isNull(ptmjUser))
        {
            throw new ServiceException("用户不存在");
        }
        if (!matchesPassword(oldPassword, ptmjUser.getPassword()))
        {
            throw new ServiceException("旧密码错误");
        }
        PtmjUser updateUser = new PtmjUser();
        updateUser.setUserId(userId);
        updateUser.setPassword(SecurityUtils.encryptPassword(newPassword));
        updateUser.setUpdateBy(userId.toString());
        updateUser.setUpdateTime(DateUtils.getNowDate());
        desktopAuthMapper.updateDesktopUserPassword(updateUser);
    }

    /*
     * 修改用户密保
     *
     * @param userId  用户ID
     * @param question 密保问题
     * @param answer 密保答案
     * @Author zac
     * */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSecurity(Long userId, String question, String answer)
    {
        if (StringUtils.isEmpty(question))
        {
            throw new ServiceException("密保问题不能为空");
        }
        validateSecurityTextLength(question, "密保问题");
        if (StringUtils.isEmpty(answer))
        {
            throw new ServiceException("密保答案不能为空");
        }
        validateSecurityTextLength(answer, "密保答案");
        String encAnswer = encodeSecurityAnswers(answer);
        PtmjSecurity existing = desktopAuthMapper.selectSecurityByUserId(userId);
        if (StringUtils.isNull(existing))
        {
            PtmjSecurity insert = new PtmjSecurity();
            insert.setUserId(userId);
            insert.setQuestion(question);
            insert.setAnswer(encAnswer);
            insert.setCreateBy(userId.toString());
            insert.setCreateTime(DateUtils.getNowDate());
            insert.setRemark("desktop_security_init");
            desktopAuthMapper.insertDesktopSecurity(insert);
        }
        else
        {
            PtmjSecurity security = new PtmjSecurity();
            security.setUserId(userId);
            security.setQuestion(question);
            security.setAnswer(encAnswer);
            security.setUpdateBy(userId.toString());
            desktopAuthMapper.updateDesktopSecurity(security);
        }
    }

    /*
     * 获取密保问题
     *
     * @param userId 用户ID
     * @Author zac
     * */
    @Override
    public PtmjSecurity getSecurityByUserId(Long userId)
    {
        return desktopAuthMapper.selectSecurityByUserId(userId);
    }


    /**
     * 验证密码
     * @param userId   用户ID
     * @param password 待验证的密码
     * @Author zac
     */
    @Override
    public void verifyPassword(Long userId, String password)
    {
        if (StringUtils.isEmpty(password))
        {
            throw new ServiceException("密码不能为空");
        }
        PtmjUser ptmjUser = desktopAuthMapper.selectUserByUserId(userId);
        if (StringUtils.isNull(ptmjUser))
        {
            throw new ServiceException("用户不存在");
        }
        if (!matchesPassword(password, ptmjUser.getPassword()))
        {
            throw new ServiceException("密码错误");
        }
    }

    @Override
    public void verifySecurityAnswer(Long userId, String rawAnswer)
    {
        String answer = normalizeAnswer(rawAnswer);
        if (StringUtils.isEmpty(answer))
        {
            throw new ServiceException("密保答案不能为空");
        }
        PtmjSecurity ptmjSecurity = desktopAuthMapper.selectSecurityByUserId(userId);
        if (StringUtils.isNull(ptmjSecurity) || StringUtils.isEmpty(ptmjSecurity.getAnswer()))
        {
            throw new ServiceException("当前账号尚未配置密保答案");
        }
        if (!matchesAnswer(answer, ptmjSecurity.getAnswer()))
        {
            throw new ServiceException("密保答案错误");
        }
    }

    /**
     * 重置密码
     * @param userId
     * @param rawAnswer
     * @param newPassword
     * @param confirmPassword
     *
     * @Author zac
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPasswordBySecurityForLoggedInUser(Long userId, String rawAnswer, String newPassword, String confirmPassword)
    {
        String newPwd = safeTrim(newPassword);
        String confirmPwd = safeTrim(confirmPassword);
        validatePassword(newPwd, "新密码");
        if (!StringUtils.equals(newPwd, confirmPwd))
        {
            throw new ServiceException("两次输入的密码不一致");
        }
        String answer = normalizeAnswer(rawAnswer);
        if (StringUtils.isEmpty(answer))
        {
            throw new ServiceException("密保答案不能为空");
        }
        PtmjSecurity ptmjSecurity = desktopAuthMapper.selectSecurityByUserId(userId);
        if (StringUtils.isNull(ptmjSecurity) || StringUtils.isEmpty(ptmjSecurity.getAnswer()))
        {
            throw new ServiceException("当前账号尚未配置密保答案");
        }
        if (!matchesAnswer(answer, ptmjSecurity.getAnswer()))
        {
            throw new ServiceException("密保答案错误");
        }
        PtmjUser updateUser = new PtmjUser();
        updateUser.setUserId(userId);
        updateUser.setPassword(SecurityUtils.encryptPassword(newPwd));
        updateUser.setUpdateBy(userId.toString());
        updateUser.setUpdateTime(DateUtils.getNowDate());
        desktopAuthMapper.updateDesktopUserPassword(updateUser);
    }

    /**
     * 查询桌面端用户并做空值保护
     * 撰写人：LYX
     */
    private PtmjUser getUserByUserName(String userName)
    {
        return desktopAuthMapper.selectUserByUserName(userName);
    }

    /**
     * 校验验证码
     * 撰写人：LYX
     */
    private void validateCaptcha(String username, String code, String uuid)
    {
        boolean captchaEnabled = configService.selectCaptchaEnabled();
        if (!captchaEnabled)
        {
            return;
        }
        if (StringUtils.isEmpty(code) || StringUtils.isEmpty(uuid))
        {
            throw new ServiceException("验证码不能为空");
        }

        String verifyKey = CacheConstants.CAPTCHA_CODE_KEY + StringUtils.nvl(uuid, "");
        String captcha = redisCache.getCacheObject(verifyKey);
        if (captcha == null)
        {
            throw new ServiceException("验证码已失效，请重新获取");
        }
        redisCache.deleteObject(verifyKey);
        if (!code.equalsIgnoreCase(captcha))
        {
            throw new ServiceException("验证码错误");
        }
    }

    /**
     * 校验用户名
     * 撰写人：LYX
     */
    private void validateUsername(String username)
    {
        if (StringUtils.isEmpty(username))
        {
            throw new ServiceException("用户名不能为空");
        }
        if (username.length() < UserConstants.USERNAME_MIN_LENGTH || username.length() > UserConstants.USERNAME_MAX_LENGTH)
        {
            throw new ServiceException("用户名长度必须在2到20位之间");
        }
    }

    /**
     * 校验密码
     * 撰写人：LYX
     */
    private void validatePassword(String password, String fieldName)
    {
        if (StringUtils.isEmpty(password))
        {
            throw new ServiceException(fieldName + "不能为空");
        }
        if (password.length() < UserConstants.PASSWORD_MIN_LENGTH || password.length() > UserConstants.PASSWORD_MAX_LENGTH)
        {
            throw new ServiceException(fieldName + "长度必须在5到20位之间");
        }
    }

    /**
     * 构造若依可识别的登录用户对象，目的是复用若依现有 token 体系
     * 撰写人：LYX
     */
    private LoginUser buildLoginUser(PtmjUser ptmjUser)
    {
        SysUser sysUser = new SysUser();
        sysUser.setUserId(ptmjUser.getUserId());
        sysUser.setUserName(ptmjUser.getUserName());
        sysUser.setNickName(ptmjUser.getUserName());
        sysUser.setAvatar(ptmjUser.getAvatar());
        sysUser.setPassword(ptmjUser.getPassword());
        sysUser.setStatus(UserConstants.NORMAL);
        sysUser.setLoginDate(new Date());
        sysUser.setRoles(Collections.singletonList(buildDesktopRole()));

        return new LoginUser(ptmjUser.getUserId(), null, sysUser, new HashSet<>(getMenuPermission()));
    }

    /**
     * 构造桌面端固定角色，避免后续权限工具类在取角色时出现空集合问题
     * 撰写人：LYX
     */
    private SysRole buildDesktopRole()
    {
        SysRole sysRole = new SysRole();
        sysRole.setRoleId(-1L);
        sysRole.setRoleName("桌面端用户");
        sysRole.setRoleKey(DESKTOP_ROLE_KEY);
        sysRole.setStatus(UserConstants.ROLE_NORMAL);
        return sysRole;
    }

    /**
     * 同时兼容 BCrypt 密文和历史明文，避免你库里已有旧数据时无法登录
     * 撰写人：LYX
     */
    private boolean matchesPassword(String source, String target)
    {
        if (StringUtils.isEmpty(source) || StringUtils.isEmpty(target))
        {
            return false;
        }
        try
        {
            if (target.startsWith("$2a$") || target.startsWith("$2b$") || target.startsWith("$2y$"))
            {
                return SecurityUtils.matchesPassword(source, target);
            }
        }
        catch (Exception e)
        {
            return false;
        }
       return false;
        //sxm 2026-05-19注释掉，删去明文登录方式 return StringUtils.equals(source, target);
    }

    /**
     * 同时兼容 BCrypt 密文和历史明文密保答案
     * 撰写人：LYX
     */
    private boolean matchesAnswer(String source, String target)
    {
        if (StringUtils.isEmpty(source) || StringUtils.isEmpty(target))
        {
            return false;
        }
        try
        {
            if (target.startsWith("$2a$") || target.startsWith("$2b$") || target.startsWith("$2y$"))
            {
                return SecurityUtils.matchesPassword(source, target);
            }
        }
        catch (Exception e)
        {
            return false;
        }
       return false;
        //sxm 2026-05-18 return StringUtils.equals(normalizeAnswer(source), normalizeAnswer(target));
    }

    private String encodeSecurityAnswers(String answer)
    {
        if (answer != null && answer.contains("|"))
        {
            String[] answers = answer.split("\\|", -1);
            if (answers.length != 3)
            {
                throw new ServiceException("请设置三条密保答案");
            }
            for (int i = 0; i < answers.length; i++)
            {
                String normalized = normalizeAnswer(answers[i]);
                if (StringUtils.isEmpty(normalized))
                {
                    throw new ServiceException("三条密保答案都不能为空");
                }
                validateSecurityTextLength(normalized, "密保答案");
                answers[i] = SecurityUtils.encryptPassword(normalized);
            }
            return String.join("|", answers);
        }
        String normalized = normalizeAnswer(answer);
        validateSecurityTextLength(normalized, "密保答案");
        return SecurityUtils.encryptPassword(normalized);
    }

    private void validateSecurityQuestions(String question)
    {
        if (question != null && question.contains("|"))
        {
            String[] questions = question.split("\\|", -1);
            if (questions.length != 3)
            {
                throw new ServiceException("请设置三条密保问题");
            }
            for (String item : questions)
            {
                if (StringUtils.isEmpty(safeTrim(item)) || "null".equalsIgnoreCase(safeTrim(item)))
                {
                    throw new ServiceException("三条密保问题都不能为空");
                }
                validateSecurityTextLength(item, "密保问题");
            }
            return;
        }
        validateSecurityTextLength(question, "密保问题");
    }

    private void validateSecurityTextLength(String value, String fieldName)
    {
        if (StringUtils.isEmpty(value))
        {
            return;
        }
        String[] items = value.contains("|") ? value.split("\\|", -1) : new String[] { value };
        for (String item : items)
        {
            String text = safeTrim(item);
            if (!StringUtils.isEmpty(text) && text.length() > SECURITY_TEXT_MAX_LENGTH)
            {
                throw new ServiceException(fieldName + "不能超过" + SECURITY_TEXT_MAX_LENGTH + "个字");
            }
        }
    }

    /**
     * 统一处理密保答案，减少前后空格和大小写造成的误判
     * 撰写人：LYX
     */
    private String normalizeAnswer(String answer)
    {
        if (answer == null)
        {
            return null;
        }
        return answer.trim().toLowerCase(Locale.ROOT);
    }

    /**
     * 统一处理普通字符串
     * 撰写人：LYX
     */
    private String safeTrim(String value)
    {
        return value == null ? null : value.trim();
    }
}
