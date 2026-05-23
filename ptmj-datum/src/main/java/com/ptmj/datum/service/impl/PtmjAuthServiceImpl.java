package com.ptmj.datum.service.impl;

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ptmj.datum.domain.PtmjLoginBody;
import com.ptmj.datum.domain.PtmjUser;
import com.ptmj.datum.mapper.PtmjAuthMapper;
import com.ptmj.datum.service.IPtmjAuthService;
import com.ptmj.datum.service.IPtmjClientSessionService;
import com.ruoyi.common.constant.CacheConstants;
import com.ruoyi.common.constant.UserConstants;
import com.ruoyi.common.core.domain.entity.SysRole;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.framework.web.service.TokenService;
import com.ruoyi.system.service.ISysConfigService;

/**
 * Ptmj 登录认证 Service 实现
 * 撰写人：LYX
 */
@Service
public class PtmjAuthServiceImpl implements IPtmjAuthService
{
    /**
     * Ptmj 用户正常状态
     * 撰写人：LYX
     */
    private static final String PTMJ_USER_STATUS_NORMAL = "1";

    /**
     * Ptmj 固定角色标识
     * 撰写人：LYX
     */
    private static final String PTMJ_ROLE_KEY = "ptmj_user";

    @Autowired
    private PtmjAuthMapper ptmjAuthMapper;

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private ISysConfigService configService;

    @Autowired
    private IPtmjClientSessionService ptmjClientSessionService;

    @Override
    public String login(PtmjLoginBody loginBody)
    {
        String username = safeTrim(loginBody.getUsername());
        String password = safeTrim(loginBody.getPassword());

        validateCaptcha(username, loginBody.getCode(), loginBody.getUuid());
        validateUsername(username);
        validatePassword(password);

        PtmjUser ptmjUser = ptmjAuthMapper.selectUserByUserName(username);
        if (StringUtils.isNull(ptmjUser))
        {
            throw new ServiceException("Ptmj 用户不存在");
        }
        if (!PTMJ_USER_STATUS_NORMAL.equals(ptmjUser.getStatus()))
        {
            throw new ServiceException("当前 Ptmj 账号已被停用");
        }
        if (!matchesPassword(password, ptmjUser.getPassword()))
        {
            throw new ServiceException("用户名或密码错误");
        }
        // 撰写人：LYX 先按若依标准流程生成 token，再追加客户端固定 5 天会话策略
        LoginUser loginUser = buildLoginUser(ptmjUser);
        String token = tokenService.createToken(loginUser);
        ptmjClientSessionService.applyFiveDaySession(loginUser);
        return token;
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
    private void validatePassword(String password)
    {
        if (StringUtils.isEmpty(password))
        {
            throw new ServiceException("登录密码不能为空");
        }
        if (password.length() < UserConstants.PASSWORD_MIN_LENGTH || password.length() > UserConstants.PASSWORD_MAX_LENGTH)
        {
            throw new ServiceException("登录密码长度必须在5到20位之间");
        }
    }

    /**
     * 构造若依登录对象，目的是复用若依 token 体系
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
        sysUser.setRoles(Collections.singletonList(buildPtmjRole()));

        return new LoginUser(ptmjUser.getUserId(), null, sysUser, new HashSet<>(Collections.singleton(PTMJ_ROLE_KEY)));
    }

    /**
     * 构造固定角色，避免权限对象为空
     * 撰写人：LYX
     */
    private SysRole buildPtmjRole()
    {
        SysRole sysRole = new SysRole();
        sysRole.setRoleId(-1L);
        sysRole.setRoleName("Ptmj 用户");
        sysRole.setRoleKey(PTMJ_ROLE_KEY);
        sysRole.setStatus(UserConstants.ROLE_NORMAL);
        return sysRole;
    }

    /**
     * 兼容 BCrypt 密文和历史明文密码
     * 撰写人：LYX
     */
    /**
     * 密码匹配验证（统一使用BCrypt加密验证）
     * SXM于2026-05-19修改：移除明文兼容逻辑，统一使用BCrypt加密验证以提高安全性
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
        // SXM于2026-05-19注释：移除明文兼容逻辑，统一使用BCrypt加密验证
        // return StringUtils.equals(source, target);
        return false;
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
