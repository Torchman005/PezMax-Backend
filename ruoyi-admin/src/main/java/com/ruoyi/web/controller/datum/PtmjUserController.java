package com.ruoyi.web.controller.datum;


import com.ptmj.datum.domain.PtmjLoginBody;
import com.ptmj.datum.domain.PtmjSecurity;
import com.ptmj.datum.domain.PtmjUser;
import com.ptmj.datum.domain.vo.PtmjDesktopUserVO;
import com.ptmj.datum.dto.PtmjRegisterDto;
import com.ptmj.datum.mapper.PtmjFileDownloadMapper;
import com.ptmj.datum.mapper.PtmjFileFavoriteMapper;
import com.ptmj.datum.mapper.PtmjFileMapper;
import com.ptmj.datum.service.IDesktopAuthService;
import com.ptmj.datum.service.IPtmjDesktopUserService;
import com.ptmj.datum.service.IPtmjAuthService;
import com.ptmj.datum.service.IPtmjUserService;
import com.ptmj.datum.service.PtmjFileRankCacheService;
import com.google.code.kaptcha.Producer;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.constant.CacheConstants;
import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.file.FileUploadUtils;
import com.ruoyi.common.utils.file.FileUtils;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.utils.sign.Base64;
import com.ruoyi.common.utils.uuid.IdUtils;
import com.ruoyi.framework.config.ServerConfig;
import com.ruoyi.system.service.ISysConfigService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.validation.annotation.Validated;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.FastByteArrayOutputStream;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 平台用户Controller
 *
 * @author pk
 * @date 2026-04-02
 */
@RestController
@RequestMapping("/datum")
public class PtmjUserController extends BaseController
{
    @Resource(name = "captchaProducer")
    private Producer captchaProducer;

    @Resource(name = "captchaProducerMath")
    private Producer captchaProducerMath;

    @Autowired
    private IPtmjUserService ptmjUserService;
    @Autowired
    private PtmjFileRankCacheService ptmjfileRankCacheService;

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private ISysConfigService configService;

    @Autowired
    private IPtmjAuthService ptmjAuthService;

    @Autowired
    private IPtmjDesktopUserService ptmjDesktopUserService;

    @Autowired
    private IDesktopAuthService desktopAuthService;

    @Autowired
    private PtmjFileFavoriteMapper ptmjFileFavoriteMapper;

    @Autowired
    private PtmjFileDownloadMapper ptmjFileDownloadMapper;

    @Autowired
    private PtmjFileMapper ptmjFileMapper;

    @Autowired
    private ServerConfig serverConfig;

    /**
     * 查询平台用户列表
     */
    @PreAuthorize("@ss.hasPermi('datum:user:list')")
    @GetMapping("/user/list")
    public TableDataInfo list(PtmjUser ptmjUser)
    {
        startPage();
        List<PtmjUser> list = ptmjUserService.selectPtmjUserList(ptmjUser);
        return getDataTable(list);
    }

    /**
     * 导出平台用户列表
     */
    @PreAuthorize("@ss.hasPermi('datum:user:export')")
    @Log(title = "平台用户", businessType = BusinessType.EXPORT)
    @PostMapping("/user/export")
    public void export(HttpServletResponse response, PtmjUser ptmjUser)
    {
        List<PtmjUser> list = ptmjUserService.selectPtmjUserList(ptmjUser);
        ExcelUtil<PtmjUser> util = new ExcelUtil<PtmjUser>(PtmjUser.class);
        util.exportExcel(response, list, "平台用户数据");
    }

    /**
     * 获取平台用户详细信息
     */
    // @PreAuthorize("@ss.hasPermi('datum:user:query')")
    @GetMapping(value = "/user/{userId}")
    public AjaxResult getInfo(@PathVariable("userId") Long userId)
    {
        return success(ptmjUserService.selectPtmjUserByUserId(userId));
    }

    /**
     * 新增平台用户
     */
    @PreAuthorize("@ss.hasPermi('datum:user:add')")
    @Log(title = "平台用户", businessType = BusinessType.INSERT)
    @PostMapping("/user")
    public AjaxResult add(@RequestBody PtmjUser ptmjUser)
    {
        return toAjax(ptmjUserService.insertPtmjUser(ptmjUser));
    }

    /**
     * 修改平台用户
     */
    @PreAuthorize("@ss.hasPermi('datum:user:edit')")
    @Log(title = "平台用户", businessType = BusinessType.UPDATE)
    @PutMapping("/user")
    public AjaxResult edit(@RequestBody PtmjUser ptmjUser)
    {
        return toAjax(ptmjUserService.updatePtmjUser(ptmjUser));
    }

    /**
     * 删除平台用户
     */
    @PreAuthorize("@ss.hasPermi('datum:user:remove')")
    @Log(title = "平台用户", businessType = BusinessType.DELETE)
    @DeleteMapping("/user/{userIds}")
    public AjaxResult remove(@PathVariable Long[] userIds)
    {
        return toAjax(ptmjUserService.deletePtmjUserByUserIds(userIds));
    }

    /**
     * 获取上传排行榜————范光友
     */
    @GetMapping("/user/rank")
    public AjaxResult getUploadRank()
    {
        return success(ptmjfileRankCacheService.getTopUploaders());
    }

    /**
     * 清除排行缓存————范光友
     */
    @Log(title = "上传排行缓存", businessType = BusinessType.CLEAN)
    @DeleteMapping("/user/rank/cache")
    public AjaxResult clearRankCache()
    {
        ptmjfileRankCacheService.clearRankCache();
        return success();
    }
    /*
     * 桌面端验证码
     * 撰写人：LYX
     * SXM于2026-04-28迁移：将其从controller层中挪至service层中
     */
    @Anonymous
    @GetMapping("/user/captchaImage")
    public AjaxResult desktopGetCode()
    {
        // @author SXM
        // @date 2026-04-28
        // @reason 调用Service层生成验证码图片
        Map<String, Object> result = ptmjUserService.generateCaptchaImage();
        return AjaxResult.success(result);
    }

    // @author SXM
    // @date 2026-04-28
    // @reason 平台用户注册接口(支持用户自定义3个密保问题和答案)，使用DTO接收参数并启用自动校验
    @Anonymous
    @PostMapping("/user/register")
    public AjaxResult register(@RequestBody @Validated PtmjRegisterDto registerDto)
    {
        // @author SXM
        // @date 2026-04-28
        // @reason 调用Service层处理注册业务逻辑（含验证码校验）
        Map<String, Object> result = ptmjUserService.register(registerDto);
        return AjaxResult.success("注册成功", result);
    }

    /**
     * 桌面端登录
     * 修改人：LYZ-登录返回客户端用户信息
     * SXM于2026-04-27迁移：将其从controller层中挪至service层中
     */
    @Anonymous
    @PostMapping("/user/login")
    public AjaxResult desktopLogin(@RequestBody PtmjLoginBody loginBody)
    {
        java.util.Map<String, Object> result = ptmjUserService.executeDesktopLogin(loginBody);
        return AjaxResult.success(result);
    }

    /**
     * 客户端获取当前登录用户信息
     * 修改人：LYZ-客户端获取当前登录用户信息
     * SXM于2026-04-27迁移：将其从controller层中挪至service层中
     */
    @GetMapping("/user/getInfo")
    public AjaxResult getClientInfo()
    {
        java.util.Map<String, Object> result = ptmjUserService.executeGetClientInfo();
        return AjaxResult.success(result);
    }

    /**
     * 客户端通过密保找回密码
     */
    @Anonymous
    @PostMapping("/user/resetPasswordBySecurity")
    public AjaxResult resetPasswordBySecurity(@RequestBody Map<String, String> body)
    {
        // @author SXM
        // @date 2026-04-27
        // @reason 调用service层完成完整的找回密码业务逻辑（含密码一致性校验、验证码校验和密保重置）
        ptmjUserService.executeResetPassword(body);
        return AjaxResult.success("密码重置成功");
    }

    // @author SXM
    // @date 2026-04-19
    // @reason 根据用户名查询用户的3个自定义密保问题(不返回答案),用于找回密码时前端展示
    @Anonymous
    @GetMapping("/user/securityQuestions")
    public AjaxResult getSecurityQuestions(
    @RequestParam(value = "userName", required = false) String userName,
    @RequestParam(value = "username", required = false) String username)//LYZ三次修改：增加@RequestParam注解，支持前端传递"userName"或"username"参数
    {
         String finalUserName = org.springframework.util.StringUtils.hasText(userName) ? userName : username;//LYZ三次修改：优先使用"userName"参数，如果"userName"参数不存在或为空，则使用"username"参数
        try
        {
            // @author SXM
            // @date 2026-04-19
            // @reason 调用Service层获取密保问题列表(已转换为前端期望的List结构)
            java.util.List<java.util.Map<String, String>> questionList = ptmjUserService.selectSecurityQuestionsListByUserName(finalUserName);
            return AjaxResult.success(questionList);
        }
        catch (ServiceException e)
        {
            return AjaxResult.error(e.getMessage());
        }
    }

    /**
     * 管理员按用户名重置三条密保答案
     * LYZ修改：管理员按用户名重置三条密保答案
     * SXM于2026-04-27迁移：将其从controller层中挪至service层中
     */
    @PreAuthorize("@ss.hasPermi('datum:user:edit')")
    @PostMapping("/user/resetSecurityAnswers")
    public AjaxResult resetSecurityAnswers(@RequestBody Map<String, String> body)
    {
        ptmjUserService.executeResetSecurityAnswers(body);
        return AjaxResult.success("密保重置成功");
    }

    /**
     * 修改人：LYZ-注册验证码校验
     */
    private void validateCaptcha(String code, String uuid)
    {
        boolean captchaEnabled = configService.selectCaptchaEnabled();
        if (!captchaEnabled)
        {
            return;
        }
        validateCaptchaCore(code, uuid);
    }

    /**
     * 修改人：LYZ-找回密码验证码强制校验（不受全局开关关闭影响）
     */
    private void validateCaptchaStrict(String code, String uuid)
    {
        // LYZ修改：找回密码接口强制验证码校验，不受全局开关影响
        validateCaptchaCore(code, uuid);
    }

    /**
     * 修改人：LYZ-验证码核心校验逻辑
     */
    private void validateCaptchaCore(String code, String uuid)
    {
        if (code == null || code.isEmpty() || uuid == null || uuid.isEmpty())
        {
            throw new ServiceException("验证码不能为空");
        }
        String verifyKey = CacheConstants.CAPTCHA_CODE_KEY + uuid;
        String captcha = redisCache.getCacheObject(verifyKey);
        redisCache.deleteObject(verifyKey);
        if (captcha == null)
        {
            throw new ServiceException("验证码已失效，请重新获取");
        }
        if (!code.equalsIgnoreCase(captcha))
        {
            throw new ServiceException("验证码错误");
        }
    }

    /**
     * 获取用户信息
     * @param userId
     * @Author: zac
     * @Date: 2026-04-08 19:43
     */
    @GetMapping("/desktop/user/{userId}")
    public AjaxResult getUserInfo(@PathVariable("userId") Long userId) {
        PtmjDesktopUserVO userVO = ptmjDesktopUserService.selectPtmjDeskUserByUserId(userId);
        return AjaxResult.success(userVO);
    }

    // 获取用户信息
    // zac
    @GetMapping("/desktop/user/profile")
    public AjaxResult getProfile() {
        return AjaxResult.success(desktopAuthService.getLoginUserInfo(SecurityUtils.getUserId()));
    }

    // 获取用户统计信息
    // zac
    @GetMapping("/desktop/user/profile/stats")
    public AjaxResult getProfileStats() {
        Long userId = SecurityUtils.getUserId();
        AjaxResult ajax = AjaxResult.success();
        ajax.put("favoriteCount", ptmjFileFavoriteMapper.countByUserId(userId));
        ajax.put("downloadCount", ptmjFileDownloadMapper.countVisibleByUserId(userId));
        ajax.put("uploadCount", ptmjFileMapper.countUploadedByUserId(userId));
        return ajax;
    }

    // 更新用户名
    // zac
    @PutMapping("/desktop/user/profile/username")
    public AjaxResult updateUserName(@RequestBody Map<String, String> body) {
        desktopAuthService.updateProfile(SecurityUtils.getUserId(), body.get("userName"));
        return AjaxResult.success("用户名更新成功");
    }

    // 更新头像
    // zac
    @PutMapping("/desktop/user/profile/avatar")
    public AjaxResult updateAvatar(@RequestBody Map<String, String> body) {
        desktopAuthService.updateAvatar(SecurityUtils.getUserId(), body.get("avatar"));
        return AjaxResult.success("头像更新成功");
    }

    // 上传头像
    // zac
    @PostMapping("/desktop/user/profile/avatar/upload")
    public AjaxResult uploadAvatar(@RequestParam("file") MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) {
            return AjaxResult.error("请选择要上传的头像文件");
        }
        String fileName = FileUploadUtils.upload(RuoYiConfig.getAvatarPath(), file);
        String avatar = serverConfig.getUrl() + fileName;
        desktopAuthService.updateAvatar(SecurityUtils.getUserId(), avatar);
        AjaxResult ajax = AjaxResult.success("头像上传成功");
        ajax.put("avatar", avatar);
        ajax.put("fileName", fileName);
        ajax.put("newFileName", FileUtils.getName(fileName));
        return ajax;
    }

    // 验证密码
    // zac
    @PostMapping("/desktop/user/profile/password/verify")
    public AjaxResult verifyPassword(@RequestBody Map<String, String> body) {
        desktopAuthService.verifyPassword(SecurityUtils.getUserId(), body.get("password"));
        return AjaxResult.success("密码校验通过");
    }

    // 更新密码
    // zac
    @PutMapping("/desktop/user/profile/password")
    public AjaxResult updatePassword(@RequestBody Map<String, String> body) {
        desktopAuthService.updatePassword(SecurityUtils.getUserId(), body.get("oldPassword"), body.get("newPassword"));
        return AjaxResult.success("密码更新成功");
    }

    // 获取密保问题
    // zac
    @GetMapping("/desktop/user/profile/security")
    public AjaxResult getSecurity() {
        PtmjSecurity security = desktopAuthService.getSecurityByUserId(SecurityUtils.getUserId());
        AjaxResult ajax = AjaxResult.success();
        String question = security == null ? "" : security.getQuestion();
        ajax.put("question", question);
        ajax.put("questions", question.split("\\|", -1));
        return ajax;
    }
    // 更新密保
    // zac
    @PutMapping("/desktop/user/profile/security")
    public AjaxResult updateSecurity(@RequestBody Map<String, String> body) {
        String question = body.get("question");
        String answer = body.get("answer");
        if (body.containsKey("securityQuestionOne")) {
            question = body.get("securityQuestionOne") + "|" + body.get("securityQuestionTwo") + "|" + body.get("securityQuestionThree");
            answer = body.get("securityAnswerOne") + "|" + body.get("securityAnswerTwo") + "|" + body.get("securityAnswerThree");
        }
        desktopAuthService.updateSecurity(SecurityUtils.getUserId(), question, answer);
        return AjaxResult.success("密保更新成功");
    }

    // 验证密保答案
    // zac
    @PostMapping("/desktop/user/profile/security/answer/verify")
    public AjaxResult verifySecurityAnswer(@RequestBody Map<String, String> body) {
        desktopAuthService.verifySecurityAnswer(SecurityUtils.getUserId(), body.get("answer"));
        return AjaxResult.success("密保答案校验通过");
    }

    // 密保密码重置
    // zac
    @PutMapping("/desktop/user/profile/password/by-security")
    public AjaxResult resetDesktopPasswordBySecurity(@RequestBody Map<String, String> body) {
        desktopAuthService.resetPasswordBySecurityForLoggedInUser(
                SecurityUtils.getUserId(),
                body.get("answer"),
                body.get("newPassword"),
                body.get("confirmPassword")
        );
        return AjaxResult.success("密码找回成功");
    }
}

