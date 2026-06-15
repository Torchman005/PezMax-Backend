package com.ptmj.datum.domain.vo;

/**
 * 学校联想推荐返回对象
 */
public class SchoolSuggestionVo
{
    /** 规范学校名 */
    private String value;

    /** 该学校关联的文件数量 */
    private Long count;

    public SchoolSuggestionVo()
    {
    }

    public SchoolSuggestionVo(String value, Long count)
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

