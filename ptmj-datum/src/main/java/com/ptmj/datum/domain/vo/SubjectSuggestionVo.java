package com.ptmj.datum.domain.vo;

/**
 * 学科联想推荐返回对象
 */
public class SubjectSuggestionVo
{
    /** 规范学科名 */
    private String value;

    /** 该学科关联的文件数量 */
    private Long count;

    public SubjectSuggestionVo()
    {
    }

    public SubjectSuggestionVo(String value, Long count)
    {
        this.value = value;
        this.count = count;
    }

    public String getValue()
    {
        return value;
    }

    public void setValue(String value)
    {
        this.value = value;
    }

    public Long getCount()
    {
        return count;
    }

    public void setCount(Long count)
    {
        this.count = count;
    }
}
