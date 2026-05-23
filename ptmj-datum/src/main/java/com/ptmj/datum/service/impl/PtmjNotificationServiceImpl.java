package com.ptmj.datum.service.impl;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import com.ruoyi.common.utils.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ptmj.datum.mapper.PtmjNotificationMapper;
import com.ptmj.datum.domain.PtmjNotification;
import com.ptmj.datum.service.IPtmjNotificationService;

/**
 * 通知Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-04-09
 */
@Service
public class PtmjNotificationServiceImpl implements IPtmjNotificationService 
{
    private static final Logger log = LoggerFactory.getLogger(PtmjNotificationServiceImpl.class);

    @Autowired
    private PtmjNotificationMapper ptmjNotificationMapper;

    /**
     * 查询通知
     * 
     * @param notifyId 通知主键
     * @return 通知
     */
    @Override
    public PtmjNotification selectPtmjNotificationByNotifyId(Long notifyId)
    {
        return ptmjNotificationMapper.selectPtmjNotificationByNotifyId(notifyId);
    }

    /**
     * 查询通知列表
     * 
     * @param ptmjNotification 通知
     * @return 通知
     */
    @Override
    public List<PtmjNotification> selectPtmjNotificationList(PtmjNotification ptmjNotification)
    {
        return ptmjNotificationMapper.selectPtmjNotificationList(ptmjNotification);
    }

    /**
     * 新增通知（资料下架类通知同一 material_id 只保留一条，自动转 update）
     * 
     * @param ptmjNotification 通知
     * @return 结果
     */
    @Override
    public int insertPtmjNotification(PtmjNotification ptmjNotification)
    {
        Long materialId = ptmjNotification.getMaterialId();
        if (materialId != null)
        {
            PtmjNotification query = new PtmjNotification();
            query.setMaterialId(materialId);
            List<PtmjNotification> existingList = ptmjNotificationMapper.selectPtmjNotificationList(query);
            if (existingList != null && !existingList.isEmpty())
            {
                PtmjNotification existing = existingList.get(0);
                log.info("资料下架通知已存在，转为更新 - materialId={}, existingNotifyId={}",
                    materialId, existing.getNotifyId());
                ptmjNotification.setNotifyId(existing.getNotifyId());
                ptmjNotification.setUpdateTime(DateUtils.getNowDate());
                return ptmjNotificationMapper.updatePtmjNotification(ptmjNotification);
            }
        }
        ptmjNotification.setCreateTime(DateUtils.getNowDate());
        return ptmjNotificationMapper.insertPtmjNotification(ptmjNotification);
    }

    /**
     * 修改通知
     * 
     * @param ptmjNotification 通知
     * @return 结果
     */
    @Override
    public int updatePtmjNotification(PtmjNotification ptmjNotification)
    {
        ptmjNotification.setUpdateTime(DateUtils.getNowDate());
        return ptmjNotificationMapper.updatePtmjNotification(ptmjNotification);
    }

    /**
     * 批量删除通知
     * 
     * @param notifyIds 需要删除的通知主键
     * @return 结果
     */
    @Override
    public int deletePtmjNotificationByNotifyIds(Long[] notifyIds)
    {
        return ptmjNotificationMapper.deletePtmjNotificationByNotifyIds(notifyIds);
    }

    /**
     * 删除通知信息
     * 
     * @param notifyId 通知主键
     * @return 结果
     */
    @Override
    public int deletePtmjNotificationByNotifyId(Long notifyId)
    {
        return ptmjNotificationMapper.deletePtmjNotificationByNotifyId(notifyId);
    }
    /**
     * lxq
     *获取用户端需要以弹窗形式展示的通知列表
     */
    @Override
    public List<PtmjNotification> getUserPopupNotifications(Long userId)
    {
        PtmjNotification notification =new PtmjNotification();
        notification.setStatus("0");//只查询启用的通知
        notification.setDisplayMode("0");//只查询弹窗通知
        List<PtmjNotification> notifications = ptmjNotificationMapper.selectPtmjNotificationList(notification);
        //过滤并排序通知
        return notifications.stream().filter(n->{
                    Date now = new Date();
                    //根据通知类型过滤
                    switch(n.getNotifyType()){
                        case "1"://版本更新通知
                            return true;//只要有新版本就必须更新
                        case "2"://系统故障通知
                            if(n.getFaultStartTime()==null||now.before(n.getFaultStartTime())){
                                return false;//故障开始时间不能为空，且当前时间必须在故障开始时间之后(否则，不发故障通知)
                            }
                            if(n.getFaultEndTime()!=null&&now.after(n.getFaultEndTime())){
                                return false;//如果故障结束时间不为空，现在时间在结束时间之后，则不发故障通知
                            }
                            return true;
                        case "3"://系统维护通知
                            Date maintenanceStart=n.getMaintenanceStartTime();
                            Date maintenanceEnd=n.getMaintenanceEndTime();
                            if(maintenanceStart!=null){
                                long remindMinutes;
                                if(n.getRemindBeforeMinutes()!=null){
                                    remindMinutes=n.getRemindBeforeMinutes();
                                }
                                else{
                                    remindMinutes=60;
                                }
                                long timeDiff=(maintenanceStart.getTime()-now.getTime())/(1000*60);//timeDiff = 维护开始时间 和 当前时间 的 分钟差
                                // 提前提醒：维护开始前 remindMinutes 分钟内
                                boolean isReminder=(timeDiff<=remindMinutes&&timeDiff>=0);
                                // 正式维护：维护开始后到维护结束前
                                boolean isMaintenancePeriod=(timeDiff<0)&&(maintenanceEnd==null||now.before(maintenanceEnd));
                                return isReminder || isMaintenancePeriod;
                            }
                            return false;
                        case "4"://资料下架通知，只返回给指定用户的通知
                            return n.getUploadUserId()!=null&&n.getUploadUserId().equals(userId);
                        default:
                            return false;
                    }
                })
                .sorted(Comparator.comparing(PtmjNotification::getSort).reversed()) // 按优先级排序
                .collect(Collectors.toList());

    }
    /**
     * lxq
     *获取用户端需要以滚动形式展示的通知列表
     */
    @Override
    public List<PtmjNotification> getUserScrollNotifications(){
        PtmjNotification notification =new PtmjNotification();
        notification.setStatus("0");
        notification.setDisplayMode("1");
        List<PtmjNotification> notifications=ptmjNotificationMapper.selectPtmjNotificationList(notification);
        Date now=new Date();
        return notifications.stream()
                .filter(n ->{
                    return "5".equals(n.getNotifyType()) &&
                            (n.getPublishStart()==null||now.after(n.getPublishStart()))&&
                            (n.getPublishEnd()==null||now.before(n.getPublishEnd()));
                })
                .collect(Collectors.toList());

    }

}
