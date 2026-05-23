package com.ptmj.datum.service;

import java.util.List;
import com.ptmj.datum.domain.PtmjNotification;

/**
 * 通知Service接口
 * 
 * @author ruoyi
 * @date 2026-04-09
 */
public interface IPtmjNotificationService 
{
    /**
     * 查询通知
     * 
     * @param notifyId 通知主键
     * @return 通知
     */
    public PtmjNotification selectPtmjNotificationByNotifyId(Long notifyId);

    /**
     * 查询通知列表
     * 
     * @param ptmjNotification 通知
     * @return 通知集合
     */
    public List<PtmjNotification> selectPtmjNotificationList(PtmjNotification ptmjNotification);

    /**
     * 新增通知
     * 
     * @param ptmjNotification 通知
     * @return 结果
     */
    public int insertPtmjNotification(PtmjNotification ptmjNotification);

    /**
     * 修改通知
     * 
     * @param ptmjNotification 通知
     * @return 结果
     */
    public int updatePtmjNotification(PtmjNotification ptmjNotification);

    /**
     * 批量删除通知
     * 
     * @param notifyIds 需要删除的通知主键集合
     * @return 结果
     */
    public int deletePtmjNotificationByNotifyIds(Long[] notifyIds);

    /**
     * 删除通知信息
     * 
     * @param notifyId 通知主键
     * @return 结果
     */
    public int deletePtmjNotificationByNotifyId(Long notifyId);
    /**
     * lxq
     * 获取用户端需要以弹窗形式展示的通知列表
     * @param userId     用户ID
     * @return 通知集合
     */
    public List<PtmjNotification> getUserPopupNotifications(Long userId);
    /**
     * lxq
     * 获取用户端需要以滚动形式展示的通知列表
     * @return 通知集合
     */
    public List<PtmjNotification> getUserScrollNotifications();
}
