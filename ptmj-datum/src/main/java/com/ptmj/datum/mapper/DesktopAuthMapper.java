package com.ptmj.datum.mapper;

import com.ptmj.datum.domain.PtmjSecurity;
import com.ptmj.datum.domain.PtmjUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 桌面端认证专用 Mapper
 * 撰写人：LYX
 */
@Mapper
public interface DesktopAuthMapper
{
    /**
     * 按用户名精确查询桌面端用户
     * 撰写人：LYX
     *
     * @param userName 用户名
     * @return 桌面端用户
     */
    public PtmjUser selectUserByUserName(@Param("userName") String userName);

    /**
     * 按用户ID查询桌面端用户
     * 撰写人：LYX
     *
     * @param userId 用户ID
     * @return 桌面端用户
     */
    public PtmjUser selectUserByUserId(@Param("userId") Long userId);

    /**
     * 按用户名统计用户数量
     * 撰写人：LYX
     *
     * @param userName 用户名
     * @return 数量
     */
    public int countUserByUserName(@Param("userName") String userName);

    /**
     * 新增桌面端用户
     * 撰写人：LYX
     *
     * @param ptmjUser 用户对象
     * @return 结果
     */
    public int insertDesktopUser(PtmjUser ptmjUser);

    /**
     * 新增桌面端密保
     * 撰写人：LYX
     *
     * @param ptmjSecurity 密保对象
     * @return 结果
     */
    public int insertDesktopSecurity(PtmjSecurity ptmjSecurity);

    /**
     * 按用户ID查询密保信息
     * 撰写人：LYX
     *
     * @param userId 用户ID
     * @return 密保对象
     */
    public PtmjSecurity selectSecurityByUserId(@Param("userId") Long userId);

    /**
     * 更新桌面端用户密码
     * 撰写人：LYX
     *
     * @param ptmjUser 用户对象
     * @return 结果
     */
    public int updateDesktopUserPassword(PtmjUser ptmjUser);

    /**
     * 更新桌面端用户个人资料（用户名、头像）
     * author zac
     */
    public int updateDesktopUserProfile(PtmjUser ptmjUser);

    /**
     * 更新桌面端用户头像
     * author zac
     */
    public int updateDesktopUserAvatar(PtmjUser ptmjUser);

    /**
     * 更新桌面端用户密保
     * author zac
     */
    public int updateDesktopSecurity(PtmjSecurity ptmjSecurity);
}
