package com.ptmj.datum.domain.desktop;

/**
 * 桌面端找回密码参数对象
 * 撰写人：LYX
 */
public class DesktopResetPasswordBody extends DesktopSecurityQuestionBody
{
    /**
     * 密保答案
     * 撰写人：LYX
     */
    private String answer;

    /**
     * 新密码
     * 撰写人：LYX
     */
    private String newPassword;

    /**
     * 二次确认密码
     * 撰写人：LYX
     */
    private String confirmPassword;

    public String getAnswer()
    {
        return answer;
    }

    public void setAnswer(String answer)
    {
        this.answer = answer;
    }

    public String getNewPassword()
    {
        return newPassword;
    }

    public void setNewPassword(String newPassword)
    {
        this.newPassword = newPassword;
    }

    public String getConfirmPassword()
    {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword)
    {
        this.confirmPassword = confirmPassword;
    }
}
