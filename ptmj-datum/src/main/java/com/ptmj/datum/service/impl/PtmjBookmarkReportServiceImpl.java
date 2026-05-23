package com.ptmj.datum.service.impl;

import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ptmj.datum.mapper.PtmjBookmarkReportMapper;
import com.ptmj.datum.mapper.PtmjBookmarkMapper;
import com.ptmj.datum.domain.PtmjBookmarkReport;
import com.ptmj.datum.domain.PtmjBookmark;
import com.ptmj.datum.service.IPtmjBookmarkReportService;
import com.ruoyi.common.utils.SecurityUtils;

/**
 * 书签举报Service业务层处理
 *
 * @author pk
 * @date 2026-05-21
 */
@Service
public class PtmjBookmarkReportServiceImpl implements IPtmjBookmarkReportService
{
    private static final Logger log = LoggerFactory.getLogger(PtmjBookmarkReportServiceImpl.class);

    private static final String RESULT_PENDING = "0";
    private static final String RESULT_TRUE = "1";
    private static final String RESULT_FALSE = "2";
    private static final Long BOOKMARK_STATUS_PASS = 1L;     // 审核通过
    private static final Long BOOKMARK_STATUS_FAIL = 2L;     // 审核未通过
    private static final Long BOOKMARK_STATUS_REPORTED = 3L; // 被举报

    @Autowired
    private PtmjBookmarkReportMapper ptmjBookmarkReportMapper;

    @Autowired
    private PtmjBookmarkMapper ptmjBookmarkMapper;

    @Override
    public PtmjBookmarkReport selectPtmjBookmarkReportByReportId(Long reportId)
    {
        return ptmjBookmarkReportMapper.selectPtmjBookmarkReportByReportId(reportId);
    }

    @Override
    public List<PtmjBookmarkReport> selectPtmjBookmarkReportList(PtmjBookmarkReport ptmjBookmarkReport)
    {
        return ptmjBookmarkReportMapper.selectPtmjBookmarkReportList(ptmjBookmarkReport);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertPtmjBookmarkReport(PtmjBookmarkReport ptmjBookmarkReport)
    {
        Long userId = SecurityUtils.getUserId();
        log.info("========== insertPtmjBookmarkReport 开始 ==========");
        log.info("入参 - userId={}, bookmarkId={}, reason={}, remark={}",
            userId, ptmjBookmarkReport.getBookmarkId(),
            ptmjBookmarkReport.getReason(), ptmjBookmarkReport.getRemark());

        ptmjBookmarkReport.setUserId(userId);
        ptmjBookmarkReport.setCreateTime(DateUtils.getNowDate());

        PtmjBookmarkReport check = new PtmjBookmarkReport();
        check.setUserId(ptmjBookmarkReport.getUserId());
        check.setBookmarkId(ptmjBookmarkReport.getBookmarkId());
        List<PtmjBookmarkReport> existing = ptmjBookmarkReportMapper.selectPtmjBookmarkReportList(check);
        log.info("重复举报检查 - 已有记录数={}", existing == null ? 0 : existing.size());
        if (existing != null && !existing.isEmpty())
        {
            log.warn("用户重复举报书签，userId={}, bookmarkId={}", ptmjBookmarkReport.getUserId(), ptmjBookmarkReport.getBookmarkId());
            throw new RuntimeException("您已举报过该书签，请勿重复举报");
        }

        log.info("开始插入举报记录...");
        int rows = ptmjBookmarkReportMapper.insertPtmjBookmarkReport(ptmjBookmarkReport);
        log.info("举报记录插入结果 - rows={}, reportId={}", rows, ptmjBookmarkReport.getReportId());

        if (ptmjBookmarkReport.getBookmarkId() != null)
        {
            log.info("开始查询书签 - bookmarkId={}", ptmjBookmarkReport.getBookmarkId());
            PtmjBookmark bookmark = ptmjBookmarkMapper.selectPtmjBookmarkById(ptmjBookmarkReport.getBookmarkId());
            if (bookmark == null)
            {
                log.warn("书签不存在 - bookmarkId={}", ptmjBookmarkReport.getBookmarkId());
            }
            else
            {
                log.info("查询到书签 - id={}, title={}, status={}", bookmark.getId(), bookmark.getTitle(), bookmark.getStatus());
                if (BOOKMARK_STATUS_REPORTED.equals(bookmark.getStatus()))
                {
                    log.info("书签状态已是「被举报(3)」，跳过更新 - bookmarkId={}", bookmark.getId());
                }
                else
                {
                    log.info("书签状态从 {} 改为 {} (被举报)", bookmark.getStatus(), BOOKMARK_STATUS_REPORTED);
                    bookmark.setStatus(BOOKMARK_STATUS_REPORTED);
                    bookmark.setUpdateTime(DateUtils.getNowDate());
                    int updated = ptmjBookmarkMapper.updatePtmjBookmark(bookmark);
                    log.info("书签状态更新结果 - bookmarkId={}, affectedRows={}, 新status={}",
                        bookmark.getId(), updated, bookmark.getStatus());
                    if (updated == 0)
                    {
                        log.error("!!! 书签状态更新影响0行 !!! - bookmarkId={}", bookmark.getId());
                    }
                }
            }
        }
        else
        {
            log.warn("bookmarkId 为空，跳过书签状态更新");
        }

        log.info("========== insertPtmjBookmarkReport 结束，返回 rows={} ==========", rows);
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updatePtmjBookmarkReport(PtmjBookmarkReport ptmjBookmarkReport)
    {
        log.info("========== updatePtmjBookmarkReport 开始 ==========");
        log.info("入参 - reportId={}, bookmarkId={}, result={}, remark={}",
            ptmjBookmarkReport.getReportId(), ptmjBookmarkReport.getBookmarkId(),
            ptmjBookmarkReport.getResult(), ptmjBookmarkReport.getRemark());

        Long reportId = ptmjBookmarkReport.getReportId();
        String newResult = ptmjBookmarkReport.getResult();

        PtmjBookmarkReport oldReport = null;
        if (reportId != null && (RESULT_TRUE.equals(newResult) || RESULT_FALSE.equals(newResult)))
        {
            oldReport = ptmjBookmarkReportMapper.selectPtmjBookmarkReportByReportId(reportId);
            log.info("旧举报记录 - reportId={}, oldResult={}, bookmarkId={}",
                reportId, oldReport != null ? oldReport.getResult() : "null",
                oldReport != null ? oldReport.getBookmarkId() : "null");
        }

        ptmjBookmarkReport.setUpdateTime(DateUtils.getNowDate());
        int updated = ptmjBookmarkReportMapper.updatePtmjBookmarkReport(ptmjBookmarkReport);
        log.info("举报记录更新结果 - affectedRows={}", updated);

        if (oldReport != null
            && (RESULT_TRUE.equals(newResult) || RESULT_FALSE.equals(newResult))
            && !newResult.equals(oldReport.getResult()))
        {
            log.info("检测到result变化，同步更新书签状态 - reportId={}, result: {}→{}",
                reportId, oldReport.getResult(), newResult);
            Long targetStatus = RESULT_TRUE.equals(newResult) ? BOOKMARK_STATUS_FAIL : BOOKMARK_STATUS_PASS;
            log.info("目标书签状态={} ({})", targetStatus, RESULT_TRUE.equals(newResult) ? "未通过" : "通过");

            PtmjBookmark bookmark = ptmjBookmarkMapper.selectPtmjBookmarkById(oldReport.getBookmarkId());
            if (bookmark == null)
            {
                log.error("书签不存在，无法更新状态 - bookmarkId={}", oldReport.getBookmarkId());
            }
            else
            {
                log.info("书签当前状态 - id={}, title={}, status={}", bookmark.getId(), bookmark.getTitle(), bookmark.getStatus());
                if (targetStatus.equals(bookmark.getStatus()))
                {
                    log.info("书签状态已是目标值，跳过更新 - bookmarkId={}, status={}", bookmark.getId(), bookmark.getStatus());
                }
                else
                {
                    log.info("书签状态从 {} 改为 {}", bookmark.getStatus(), targetStatus);
                    bookmark.setStatus(targetStatus);
                    bookmark.setUpdateTime(DateUtils.getNowDate());
                    int bmUpdated = ptmjBookmarkMapper.updatePtmjBookmark(bookmark);
                    log.info("书签状态更新结果 - bookmarkId={}, affectedRows={}, 新status={}",
                        bookmark.getId(), bmUpdated, bookmark.getStatus());
                    if (bmUpdated == 0)
                    {
                        log.error("!!! 书签状态更新影响0行 !!! - bookmarkId={}", bookmark.getId());
                    }
                }
            }
        }
        else if (oldReport != null && !RESULT_TRUE.equals(newResult) && !RESULT_FALSE.equals(newResult))
        {
            log.info("newResult不是1或2，不更新书签状态");
        }
        else if (oldReport != null && newResult.equals(oldReport.getResult()))
        {
            log.info("result未变化(result={})，不更新书签状态", newResult);
        }

        log.info("========== updatePtmjBookmarkReport 结束 ==========");
        return updated;
    }

    @Override
    public int deletePtmjBookmarkReportByReportIds(Long[] reportIds)
    {
        return ptmjBookmarkReportMapper.deletePtmjBookmarkReportByReportIds(reportIds);
    }

    @Override
    public int deletePtmjBookmarkReportByReportId(Long reportId)
    {
        return ptmjBookmarkReportMapper.deletePtmjBookmarkReportByReportId(reportId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String handleBookmarkReport(Long reportId, String result, String remark)
    {
        log.info("========== handleBookmarkReport 开始 ==========");
        log.info("入参 - reportId={}, result={}, remark={}", reportId, result, remark);

        if (reportId == null || reportId <= 0)
        {
            log.warn("举报ID无效 - reportId={}", reportId);
            return "举报ID无效";
        }
        if (!RESULT_TRUE.equals(result) && !RESULT_FALSE.equals(result))
        {
            log.warn("审核结果参数错误 - result={}", result);
            return "审核结果参数错误，只能为1或2";
        }

        log.info("查询举报记录 - reportId={}", reportId);
        PtmjBookmarkReport report = ptmjBookmarkReportMapper.selectPtmjBookmarkReportByReportId(reportId);
        if (report == null)
        {
            log.warn("书签举报记录不存在，reportId={}", reportId);
            return "举报记录不存在";
        }
        log.info("举报记录 - reportId={}, bookmarkId={}, userId={}, result={}, reason={}",
            report.getReportId(), report.getBookmarkId(), report.getUserId(),
            report.getResult(), report.getReason());

        if (!RESULT_PENDING.equals(report.getResult()))
        {
            log.warn("书签举报已被处理，reportId={}, currentResult={}", reportId, report.getResult());
            return "该举报已被处理，请勿重复操作";
        }

        log.info("更新举报结果 - reportId={}, result: {}→{}, remark={}",
            reportId, report.getResult(), result, remark);
        report.setResult(result);
        report.setRemark(remark);
        report.setUpdateTime(DateUtils.getNowDate());
        int reportUpdated = ptmjBookmarkReportMapper.updatePtmjBookmarkReport(report);
        log.info("举报结果更新完成 - affectedRows={}", reportUpdated);

        log.info("查询书签 - bookmarkId={}", report.getBookmarkId());
        PtmjBookmark bookmark = ptmjBookmarkMapper.selectPtmjBookmarkById(report.getBookmarkId());

        if (RESULT_TRUE.equals(result))
        {
            if (bookmark == null)
            {
                log.error("书签不存在，无法更新状态 - bookmarkId={}", report.getBookmarkId());
                return "举报已处理，但书签不存在(更新失败)";
            }
            log.info("书签当前状态 - id={}, title={}, status={}", bookmark.getId(), bookmark.getTitle(), bookmark.getStatus());
            if (BOOKMARK_STATUS_FAIL.equals(bookmark.getStatus()))
            {
                log.info("书签状态已是「未通过(2)」，跳过更新 - bookmarkId={}", bookmark.getId());
            }
            else
            {
                log.info("书签状态从 {} 改为 {} (未通过)", bookmark.getStatus(), BOOKMARK_STATUS_FAIL);
                bookmark.setStatus(BOOKMARK_STATUS_FAIL);
                bookmark.setUpdateTime(DateUtils.getNowDate());
                int updated = ptmjBookmarkMapper.updatePtmjBookmark(bookmark);
                log.info("书签状态更新结果 - bookmarkId={}, affectedRows={}, 新status={}",
                    bookmark.getId(), updated, bookmark.getStatus());
                if (updated == 0)
                {
                    log.error("!!! 书签状态更新影响0行 !!! - bookmarkId={}", bookmark.getId());
                }
            }
            log.info("========== handleBookmarkReport 结束(属实) ==========");
            return "举报已处理，书签状态已改为未通过";
        }
        else
        {
            if (bookmark == null)
            {
                log.error("书签不存在，无法更新状态 - bookmarkId={}", report.getBookmarkId());
                return "举报已驳回，但书签不存在(更新失败)";
            }
            log.info("书签当前状态 - id={}, title={}, status={}", bookmark.getId(), bookmark.getTitle(), bookmark.getStatus());
            if (BOOKMARK_STATUS_PASS.equals(bookmark.getStatus()))
            {
                log.info("书签状态已是「通过(1)」，跳过更新 - bookmarkId={}", bookmark.getId());
            }
            else
            {
                log.info("书签状态从 {} 改为 {} (通过)", bookmark.getStatus(), BOOKMARK_STATUS_PASS);
                bookmark.setStatus(BOOKMARK_STATUS_PASS);
                bookmark.setUpdateTime(DateUtils.getNowDate());
                int updated = ptmjBookmarkMapper.updatePtmjBookmark(bookmark);
                log.info("书签状态更新结果 - bookmarkId={}, affectedRows={}, 新status={}",
                    bookmark.getId(), updated, bookmark.getStatus());
                if (updated == 0)
                {
                    log.error("!!! 书签状态更新影响0行 !!! - bookmarkId={}", bookmark.getId());
                }
            }
            log.info("========== handleBookmarkReport 结束(不属实) ==========");
            return "举报已驳回，书签状态已恢复为通过";
        }
    }
}
