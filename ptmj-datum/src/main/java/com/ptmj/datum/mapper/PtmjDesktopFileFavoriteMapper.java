package com.ptmj.datum.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PtmjDesktopFileFavoriteMapper {
    /**
     * 获取桌面端用户收藏试卷列表
     *
     * @param userId 用户id
     * @return
     * @Author: zac
     * @Date: 2026-04-09 12:46
     */
    @Select("select file_id from ptmj_file_favorite where user_id = #{userId}")
    List<Integer> selectPtmjDesktopFileFavoriteList(Long userId);
}
