package com.ptmj.datum.service.impl;

import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ptmj.datum.mapper.PtmjReportMapper;
import com.ptmj.datum.mapper.PtmjFileMapper;
import com.ptmj.datum.mapper.PtmjUserMapper;
import com.ptmj.datum.mapper.PtmjNotificationMapper;
import com.ptmj.datum.domain.PtmjReport;
import com.ptmj.datum.domain.PtmjFile;
import com.ptmj.datum.domain.PtmjUser;
import com.ptmj.datum.domain.PtmjNotification;
import com.ptmj.datum.service.IPtmjReportService;
import com.ruoyi.common.utils.SecurityUtils;

/**
 * 举报Service业务层处理
 *
 * @author pk
 * @date 2026-04-02
 */
@Service
public class PtmjReportServiceImpl implements IPtmjReportService
{/**
 * 举报Service业务层处理
 *
 * @author lzt
 * @date 2026-04-13
 */
private static final Logger log = LoggerFactory.getLogger(PtmjReportServiceImpl.class);

    // 业务状态常量（统一使用 Long 类型，避免类型不匹配）
    private static final String RESULT_PENDING = "0";          // 未处理
    private static final String RESULT_TRUE = "1";             // 属实
    private static final String RESULT_FALSE = "2";            // 不属实
    private static final Long FILE_STATUS_PASS = 1L;           // 文件状态：审核通过
    private static final Long FILE_STATUS_FAIL = 2L;           // 文件状态：审核未通过
    private static final Long FILE_STATUS_REPORTED = 3L;       // 文件状态：被举报
    private static final String NOTIFY_TYPE_MATERIAL_DOWN = "4"; // 通知类型：资料下架
    private static final String USER_STATUS_NORMAL = "1";      // 账号正常
    private static final String USER_STATUS_BANNED = "0";      // 账号封禁

    @Autowired
    private PtmjReportMapper ptmjReportMapper;

    @Autowired
    private PtmjFileMapper ptmjFileMapper;

    @Autowired
    private PtmjUserMapper ptmjUserMapper;

    @Autowired
    private PtmjNotificationMapper ptmjNotificationMapper;

    /**
     * 查询举报
     *
     * @param reportId 举报主键
     * @return 举报
     */
    @Override
    public PtmjReport selectPtmjReportByReportId(Long reportId)
    {
        return ptmjReportMapper.selectPtmjReportByReportId(reportId);
    }

    /**
     * 查询举报列表
     *
     * @param ptmjReport 举报
     * @return 举报
     */
    @Override
    public List<PtmjReport> selectPtmjReportList(PtmjReport ptmjReport)
    {
        return ptmjReportMapper.selectPtmjReportList(ptmjReport);
    }

    /**
     * 新增举报（同时更新被举报文件的状态为"被举报"）
     *
     * @param ptmjReport 举报
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertPtmjReport(PtmjReport ptmjReport)
    {
        ptmjReport.setUserId(SecurityUtils.getUserId());
        ptmjReport.setCreateTime(DateUtils.getNowDate());
        int rows = ptmjReportMapper.insertPtmjReport(ptmjReport);

        if (ptmjReport.getFileId() != null)
        {
            PtmjFile file = ptmjFileMapper.selectPtmjFileByFileId(ptmjReport.getFileId());
            if (file != null && !FILE_STATUS_REPORTED.equals(file.getFileStatus()))
            {
                file.setFileStatus(FILE_STATUS_REPORTED);
                file.setUpdateTime(DateUtils.getNowDate());
                ptmjFileMapper.updatePtmjFile(file);
                log.info("举报文件状态已更新为被举报，fileId={}, fileName={}", file.getFileId(), file.getFileName());
            }
        }

        return rows;
    }

    /**
     * 修改举报（仅用于更新结果等字段）
     */
    @Override
    public int updatePtmjReport(PtmjReport ptmjReport)
    {
        ptmjReport.setUpdateTime(DateUtils.getNowDate());
        return ptmjReportMapper.updatePtmjReport(ptmjReport);
    }

    /**
     * 批量删除举报
     */
    @Override
    public int deletePtmjReportByReportIds(Long[] reportIds)
    {
        return ptmjReportMapper.deletePtmjReportByReportIds(reportIds);
    }

    /**
     * 删除举报信息
     */
    @Override
    public int deletePtmjReportByReportId(Long reportId)
    {
        return ptmjReportMapper.deletePtmjReportByReportId(reportId);
    }

    /**
     * 处理举报（审核 + 可选封禁上传用户）
     *
     * @param reportId 举报ID
     * @param result   审核结果：1-属实，2-不属实
     * @param remark   审核备注
     * @param banUser  是否封禁上传用户（仅当result=1时有效）
     * @return 处理结果信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String handleReport(Long reportId, String result, String remark, boolean banUser)
    {
        // 1. 参数校验
        if (reportId == null || reportId <= 0)
        {
            return "举报ID无效";
        }
        if (!RESULT_TRUE.equals(result) && !RESULT_FALSE.equals(result))
        {
            return "审核结果参数错误，只能为1或2";
        }

        // 2. 查询举报记录
        PtmjReport report = ptmjReportMapper.selectPtmjReportByReportId(reportId);
        if (report == null)
        {
            log.warn("举报记录不存在，reportId={}", reportId);
            return "举报记录不存在";
        }
        if (!RESULT_PENDING.equals(report.getResult()))
        {
            log.warn("举报已被处理，reportId={}, currentResult={}", reportId, report.getResult());
            return "该举报已被处理，请勿重复操作";
        }

        // 3. 更新举报结果
        report.setResult(result);
        report.setRemark(remark);
        report.setUpdateTime(DateUtils.getNowDate());
        ptmjReportMapper.updatePtmjReport(report);

        // 4. 如果审核结果为“属实”，处理文件下架和通知
        if (RESULT_TRUE.equals(result))
        {
            // 4.1 查询被举报文件
            PtmjFile file = ptmjFileMapper.selectPtmjFileByFileId(report.getFileId());
            if (file == null)
            {
                log.error("被举报文件不存在，fileId={}", report.getFileId());
                return "举报已处理，但被举报文件不存在（可能已被删除）";
            }

            // 4.2 更新文件状态为“审核未通过”
            if (!FILE_STATUS_FAIL.equals(file.getFileStatus()))
            {
                file.setFileStatus(FILE_STATUS_FAIL);
                file.setUpdateTime(DateUtils.getNowDate());
                ptmjFileMapper.updatePtmjFile(file);
                log.info("举报属实，文件状态已改为未通过，fileId={}, fileName={}", file.getFileId(), file.getFileName());
            }

            // 4.3 发送下架通知给上传用户（使用唯一索引避免重复插入，不再手动检查）
            PtmjNotification notification = new PtmjNotification();
            notification.setNotifyType(NOTIFY_TYPE_MATERIAL_DOWN);
            notification.setTitle("您的资料已被下架");
            notification.setContent("您上传的资料《" + file.getFileName() + "》因被用户举报且经核实属实，现已下架。如有疑问请联系管理员。");
            notification.setStatus("0");
            notification.setSort(0L);      // 修改为 Long 类型
            notification.setDisplayMode("0");
            notification.setUploadUserId(file.getUserId());
            notification.setMaterialId(file.getFileId());
            notification.setMaterialTitleSnapshot(file.getFileName());
            notification.setCreateTime(DateUtils.getNowDate());
            ptmjNotificationMapper.insertPtmjNotification(notification);
            log.info("下架通知已发送，userId={}, fileId={}", file.getUserId(), file.getFileId());

            // 4.4 封禁上传用户（如果需要）
            if (banUser)
            {
                PtmjUser uploadUser = ptmjUserMapper.selectPtmjUserByUserId(file.getUserId());
                if (uploadUser != null && USER_STATUS_NORMAL.equals(uploadUser.getStatus()))
                {
                    uploadUser.setStatus(USER_STATUS_BANNED);
                    uploadUser.setUpdateTime(DateUtils.getNowDate());
                    ptmjUserMapper.updatePtmjUser(uploadUser);
                    log.info("用户已封禁，userId={}, userName={}", uploadUser.getUserId(), uploadUser.getUserName());
                }
                else if (uploadUser == null)
                {
                    log.warn("上传用户不存在，userId={}", file.getUserId());
                }
                else
                {
                    log.info("用户已被封禁，无需重复操作，userId={}", file.getUserId());
                }
            }
            return banUser ? "举报已处理，文件已下架且用户已封禁" : "举报已处理，文件已下架";
        }
        else
        {
            // 审核不属实，恢复文件状态为通过
            PtmjFile file = ptmjFileMapper.selectPtmjFileByFileId(report.getFileId());
            if (file != null && !FILE_STATUS_PASS.equals(file.getFileStatus()))
            {
                file.setFileStatus(FILE_STATUS_PASS);
                file.setUpdateTime(DateUtils.getNowDate());
                ptmjFileMapper.updatePtmjFile(file);
                log.info("举报不属实，文件状态已恢复为通过，fileId={}", file.getFileId());
            }
            log.info("举报已驳回，reportId={}", reportId);
            return "举报已驳回，文件状态已恢复为通过";
        }
    }
}