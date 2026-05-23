package com.ruoyi.common.constant;

import java.util.Arrays;
import java.util.List;

/**
 * 平台用户密保问题常量
 *
 * @author sxm
 * @date 2026-04-08
 */
public class PtmjSecurityConstant
{
    /** 密保问题列表 */
    public static final List<String> SECURITY_QUESTIONS = Arrays.asList(
        "你的专业是什么？",
        "你的母亲姓什么？",
        "你的父亲姓什么？"
    );

    /**
     * 校验密保问题是否合法
     *
     * @author sxm
     * @date 2026-04-08
     * @param question 密保问题
     * @return 是否合法
     */
    public static boolean isValidQuestion(String question)
    {
        return question != null && SECURITY_QUESTIONS.contains(question);
    }
}
