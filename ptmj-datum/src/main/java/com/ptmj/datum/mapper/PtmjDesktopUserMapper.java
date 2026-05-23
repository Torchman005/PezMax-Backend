package com.ptmj.datum.mapper;

import com.ptmj.datum.domain.vo.PtmjDesktopUserVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface PtmjDesktopUserMapper {
    /**
     * 获取桌面用户信息
     *
     * @param userId
     * @return
     * @Author: zac
     */
    @Select("SELECT user_name AS userName, avatar FROM ptmj_user WHERE user_id = #{userId}")
    PtmjDesktopUserVO selectPtmjDeskUserByUserId(Long userId);
}
