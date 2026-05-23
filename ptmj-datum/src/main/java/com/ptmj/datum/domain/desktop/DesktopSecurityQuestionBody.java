package com.ptmj.datum.domain.desktop;

/**
 * 桌面端密保问题查询参数对象
 * 撰写人：LYX
 */
public class DesktopSecurityQuestionBody
{
    /**
     * 用户名
     * 撰写人：LYX
     */
    private String username;

    /**
     * 验证码
     * 撰写人：LYX
     */
    private String code;

    /**
     * 验证码唯一标识
     * 撰写人：LYX
     */
    private String uuid;

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public String getCode()
    {
        return code;
    }

    public void setCode(String code)
    {
        this.code = code;
    }

    public String getUuid()
    {
        return uuid;
    }

    public void setUuid(String uuid)
    {
        this.uuid = uuid;
    }
}
