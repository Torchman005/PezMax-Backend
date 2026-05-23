
// 修改人：LYZ
package com.ptmj.datum.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.annotation.JsonAlias;
/**
 * 平台用户注册DTO
 *
 * @author sxm
 * @date 2026-04-08
 */
public class PtmjRegisterDto
{
    /** 用户名 */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 2, max = 20, message = "用户名长度必须在2到20个字符之间")
    @JsonAlias({"username", "userName"})//LYZ三次修改：增加@JsonAlias注解，支持前端传递"userName"或"username"参数
    private String userName;

    /** 密码 */
    @NotBlank(message = "密码不能为空")
    // LYZ修改：与前端及服务层规则统一为 5-20
    @Size(min = 5, max = 20, message = "密码长度必须在5到20个字符之间")
    private String password;

    // @author SXM-确认密码(从DesktopAuth迁移):用于校验两次密码一致性
    /** 确认密码 */
    private String confirmPassword;

    // @author SXM
    // @date 2026-04-19
    // @reason 用户自定义密保问题一（替代固定问题）
    /** 密保问题一 */
    @NotBlank(message = "密保问题一不能为空")
    @Size(min = 1, max = 50, message = "密保问题一长度不能超过50个字符")
    private String securityQuestionOne;

    /** 密保答案一 */
    @NotBlank(message = "密保答案一不能为空")
    @Size(min = 1, max = 50, message = "密保答案一长度不能超过50个字符")
    private String securityAnswerOne;

    // @author SXM
    // @date 2026-04-19
    // @reason 用户自定义密保问题二（替代固定问题）
    /** 密保问题二 */
    @NotBlank(message = "密保问题二不能为空")
    @Size(min = 1, max = 50, message = "密保问题二长度不能超过50个字符")
    private String securityQuestionTwo;

    /** 密保答案二 */
    @NotBlank(message = "密保答案二不能为空")
    @Size(min = 1, max = 50, message = "密保答案二长度不能超过50个字符")
    private String securityAnswerTwo;

    // @author SXM
    // @date 2026-04-19
    // @reason 用户自定义密保问题三（替代固定问题）
    /** 密保问题三 */
    @NotBlank(message = "密保问题三不能为空")
    @Size(min = 1, max = 50, message = "密保问题三长度不能超过50个字符")
    private String securityQuestionThree;

    /** 密保答案三 */
    @NotBlank(message = "密保答案三不能为空")
    @Size(min = 1, max = 50, message = "密保答案三长度不能超过50个字符")
    private String securityAnswerThree;

    /** 验证码 */
    private String code;

    /** 验证码唯一标识 */
    private String uuid;

    // @author sxm
    // @date 2026-04-09
    // @reason 注册功能支持接收头像参数（可选）
    /** 头像地址 */
    private String avatar;

    // ============================================================================
    // 原代码 - @author sxm @date 2026-04-09 @reason 旧版本：使用字符串存储头像地址
    // private String avatar;
    // ============================================================================

    // @author sxm
    // @date 2026-04-10
    // @reason 注册功能支持通过MinIO上传头像文件，使用MultipartFile接收上传的文件
    /** 头像文件（用于MinIO上传） */
    private MultipartFile avatarFile;

    /**
     * 获取用户名
     *
     * @author sxm
     * @date 2026-04-08
     * @return 用户名
     */
    public String getUserName()
    {
        return userName;
    }

    /**
     * 设置用户名
     *
     * @author sxm
     * @date 2026-04-08
     * @param userName 用户名
     */
    public void setUserName(String userName)
    {
        this.userName = userName;
    }

    /**
     * 获取密码
     *
     * @author sxm
     * @date 2026-04-08
     * @return 密码
     */
    public String getPassword()
    {
        return password;
    }

    /**
     * 设置密码
     *
     * @author sxm
     * @date 2026-04-08
     * @param password 密码
     */
    public void setPassword(String password)
    {
        this.password = password;
    }

    // @author SXM-获取确认密码(从DesktopAuth迁移)
    /**
     * 获取确认密码
     *
     * @author SXM
     * @date 2026-04-15
     * @return 确认密码
     */
    public String getConfirmPassword()
    {
        return confirmPassword;
    }

    // @author SXM-设置确认密码(从 DesktopAuth迁移)
    /**
     * 设置确认密码
     *
     * @author SXM
     * @date 2026-04-15
     * @param confirmPassword 确认密码
     */
    public void setConfirmPassword(String confirmPassword)
    {
        this.confirmPassword = confirmPassword;
    }
    
    // @author SXM
    // @date 2026-04-19
    // @reason 获取用户自定义密保问题一
    /**
     * 获取密保问题一
     *
     * @author SXM
     * @date 2026-04-19
     * @return 密保问题一
     */
    public String getSecurityQuestionOne()
    {
        return securityQuestionOne;
    }
    
    /**
     * 设置密保问题一
     *
     * @author SXM
     * @date 2026-04-19
     * @param securityQuestionOne 密保问题一
     */
    public void setSecurityQuestionOne(String securityQuestionOne)
    {
        this.securityQuestionOne = securityQuestionOne;
    }
    
    /**
     * 获取密保答案一
     *
     * @author SXM
     * @date 2026-04-19
     * @return 密保答案一
     */
    public String getSecurityAnswerOne()
    {
        return securityAnswerOne;
    }
    
    /**
     * 设置密保答案一
     *
     * @author SXM
     * @date 2026-04-19
     * @param securityAnswerOne 密保答案一
     */
    public void setSecurityAnswerOne(String securityAnswerOne)
    {
        this.securityAnswerOne = securityAnswerOne;
    }
    
    // @author SXM
    // @date 2026-04-19
    // @reason 获取用户自定义密保问题二
    /**
     * 获取密保问题二
     *
     * @author SXM
     * @date 2026-04-19
     * @return 密保问题二
     */
    public String getSecurityQuestionTwo()
    {
        return securityQuestionTwo;
    }
    
    /**
     * 设置密保问题二
     *
     * @author SXM
     * @date 2026-04-19
     * @param securityQuestionTwo 密保问题二
     */
    public void setSecurityQuestionTwo(String securityQuestionTwo)
    {
        this.securityQuestionTwo = securityQuestionTwo;
    }
    
    /**
     * 获取密保答案二
     *
     * @author SXM
     * @date 2026-04-19
     * @return 密保答案二
     */
    public String getSecurityAnswerTwo()
    {
        return securityAnswerTwo;
    }
    
    /**
     * 设置密保答案二
     *
     * @author SXM
     * @date 2026-04-19
     * @param securityAnswerTwo 密保答案二
     */
    public void setSecurityAnswerTwo(String securityAnswerTwo)
    {
        this.securityAnswerTwo = securityAnswerTwo;
    }
    
    // @author SXM
    // @date 2026-04-19
    // @reason 获取用户自定义密保问题三
    /**
     * 获取密保问题三
     *
     * @author SXM
     * @date 2026-04-19
     * @return 密保问题三
     */
    public String getSecurityQuestionThree()
    {
        return securityQuestionThree;
    }
    
    /**
     * 设置密保问题三
     *
     * @author SXM
     * @date 2026-04-19
     * @param securityQuestionThree 密保问题三
     */
    public void setSecurityQuestionThree(String securityQuestionThree)
    {
        this.securityQuestionThree = securityQuestionThree;
    }
    
    /**
     * 获取密保答案三
     *
     * @author SXM
     * @date 2026-04-19
     * @return 密保答案三
     */
    public String getSecurityAnswerThree()
    {
        return securityAnswerThree;
    }
    
    /**
     * 设置密保答案三
     *
     * @author SXM
     * @date 2026-04-19
     * @param securityAnswerThree 密保答案三
     */
    public void setSecurityAnswerThree(String securityAnswerThree)
    {
        this.securityAnswerThree = securityAnswerThree;
    }

    /**
     * 修改人：LYZ-获取注册验证码
     *
     * @return 验证码
     */
    public String getCode()
    {
        return code;
    }

    /**
     * 修改人：LYZ-设置注册验证码
     *
     * @param code 验证码
     */
    public void setCode(String code)
    {
        this.code = code;
    }

    /**
     * 修改人：LYZ-获取注册验证码标识
     *
     * @return 验证码标识
     */
    public String getUuid()
    {
        return uuid;
    }

    /**
     * 修改人：LYZ-设置注册验证码标识
     *
     * @param uuid 验证码标识
     */
    public void setUuid(String uuid)
    {
        this.uuid = uuid;
    }

    // @author sxm
    // @date 2026-04-09
    // @reason 注册功能支持获取头像参数
    /**
     * 获取头像地址
     *
     * @author sxm
     * @date 2026-04-09
     * @return 头像地址
     */
    public String getAvatar()
    {
        return avatar;
    }

    // @author sxm
    // @date 2026-04-09
    // @reason 注册功能支持设置头像参数
    /**
     * 设置头像地址
     *
     * @author sxm
     * @date 2026-04-09
     * @param avatar 头像地址
     */
    public void setAvatar(String avatar)
    {
        this.avatar = avatar;
    }

    // ============================================================================
    // 原代码 - @author sxm @date 2026-04-09 @reason 旧版本：仅支持字符串形式的头像地址
    /*
    public String getAvatar()
    {
        return avatar;
    }

    public void setAvatar(String avatar)
    {
        this.avatar = avatar;
    }
    */
    // ============================================================================

    // @author sxm
    // @date 2026-04-10
    // @reason 注册功能支持获取头像文件，用于MinIO上传
    /**
     * 获取头像文件
     *
     * @author sxm
     * @date 2026-04-10
     * @return 头像文件
     */
    public MultipartFile getAvatarFile()
    {
        return avatarFile;
    }

    // @author sxm
    // @date 2026-04-10
    // @reason 注册功能支持设置头像文件，用于MinIO上传
    /**
     * 设置头像文件
     *
     * @author sxm
     * @date 2026-04-10
     * @param avatarFile 头像文件
     */
    public void setAvatarFile(MultipartFile avatarFile)
    {
        this.avatarFile = avatarFile;
    }
}
