package com.ptmj.datum.mapper;

import java.util.List;
import com.ptmj.datum.domain.PtmjNotification;

/**
 * 通知Mapper接口
 * 
 * @author ruoyi
 * @date 2026-04-09
 */
public interface PtmjNotificationMapper 
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
     * 删除通知
     * 
     * @param notifyId 通知主键
     * @return 结果
     */
    public int deletePtmjNotificationByNotifyId(Long notifyId);

    /**
     * 批量删除通知
     * 
     * @param notifyIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deletePtmjNotificationByNotifyIds(Long[] notifyIds);
}
