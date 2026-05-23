package com.ptmj.datum.domain.desktop;

/**
 * 桌面端注册参数对象
 * 撰写人：LYX
 */
public class DesktopRegisterBody extends DesktopLoginBody
{
    /**
     * 二次确认密码
     * 撰写人：LYX
     */
    private String confirmPassword;

    /**
     * 密保问题
     * 撰写人：LYX
     */
    private String question;

    /**
     * 密保答案
     * 撰写人：LYX
     */
    private String answer;

    public String getConfirmPassword()
    {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword)
    {
        this.confirmPassword = confirmPassword;
    }

    public String getQuestion()
    {
        return question;
    }

    public void setQuestion(String question)
    {
        this.question = question;
    }

    public String getAnswer()
    {
        return answer;
    }

    public void setAnswer(String answer)
    {
        this.answer = answer;
    }
}
