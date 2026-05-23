// 修改人：LYZ
package com.ptmj.datum.service.impl;

import java.util.List;
import java.util.HashMap; // @author sxm @date 2026-04-15 @reason 注册功能需要使用Map封装返回数据
import java.util.Map; // @author sxm @date 2026-04-15 @reason 注册功能返回类型改为Map
import java.util.Locale;
import org.apache.commons.lang3.StringUtils;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.exception.ServiceException; // @author sxm @date 2026-04-15 @reason 注册功能使用ServiceException处理业务异常
import com.ruoyi.common.constant.CacheConstants; // @author SXM @date 2026-04-27 @reason 将其从controller层中挪至service层中：验证码常量
import com.ruoyi.common.core.redis.RedisCache; // @author SXM @date 2026-04-27 @reason 将其从controller层中挪至service层中：Redis缓存
import com.ruoyi.system.service.ISysConfigService; // @author SXM @date 2026-04-27 @reason 将其从controller层中挪至service层中：系统配置服务
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.multipart.MultipartFile;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ptmj.datum.mapper.PtmjUserMapper;
import com.ptmj.datum.domain.PtmjUser;
import com.ptmj.datum.service.IPtmjUserService;
import com.ptmj.datum.domain.PtmjSecurity;
import com.ptmj.datum.mapper.PtmjSecurityMapper;
import com.ptmj.datum.dto.PtmjRegisterDto;
import com.ptmj.datum.service.IPtmjAuthService; // @author SXM @date 2026-04-27 @reason 将其从controller层中挪至service层中：认证服务
import com.ruoyi.common.constant.Constants; // @author SXM @date 2026-04-27 @reason 将其从controller层中挪至service层中：常量
import com.google.code.kaptcha.Producer; // @author SXM @date 2026-04-28 @reason 将其从controller层中挪至service层中：验证码生成器
import com.ruoyi.common.config.RuoYiConfig; // @author SXM @date 2026-04-28 @reason 将其从controller层中挪至service层中：若依配置
import com.ruoyi.common.utils.sign.Base64; // @author SXM @date 2026-04-28 @reason 将其从controller层中挪至service层中：Base64编码
import com.ruoyi.common.utils.uuid.IdUtils; // @author SXM @date 2026-04-28 @reason 将其从controller层中挪至service层中：UUID工具
import org.springframework.util.FastByteArrayOutputStream; // @author SXM @date 2026-04-28 @reason 将其从controller层中挪至service层中：快速字节数组输出流
import javax.imageio.ImageIO; // @author SXM @date 2026-04-28 @reason 将其从controller层中挪至service层中：图片IO
import java.awt.image.BufferedImage; // @author SXM @date 2026-04-28 @reason 将其从controller层中挪至service层中：缓冲图片
import java.io.IOException; // @author SXM @date 2026-04-28 @reason 将其从controller层中挪至service层中：IO异常
import java.util.concurrent.TimeUnit; // @author SXM @date 2026-04-28 @reason 将其从controller层中挪至service层中：时间单位

/**
 * 平台用户Service业务层处理
 *
 * @author pk
 * @date 2026-04-02
 */
@Service
public class PtmjUserServiceImpl implements IPtmjUserService
{
    private static final Logger log = LoggerFactory.getLogger(PtmjUserServiceImpl.class);
    private static final int SECURITY_TEXT_MAX_LENGTH = 50;

    @Autowired
    private PtmjUserMapper ptmjUserMapper;

    // @author sxm
    // @date 2026-04-08
    // @reason 注册功能需要注入 PtmjSecurityMapper 用于插入密保数据
    @Autowired
    private PtmjSecurityMapper ptmjSecurityMapper;

    // @author sxm
    // @date 2026-04-08
    // @reason 注册功能需要注入 BCryptPasswordEncoder 用于密码和密保答案加密
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    // @author sxm
    // @date 2026-04-10
    // @reason 注册功能需要注入 MinioClient（来自RuoYi的MinioConfig）用于头像文件上传
    @Autowired
    private MinioClient minioClient;

    // @author SXM
    // @date 2026-04-27
    // @reason 将其从controller层中挪至service层中：注入RedisCache用于验证码校验
    @Autowired
    private RedisCache redisCache;

    // @author SXM
    // @date 2026-04-27
    // @reason 将其从controller层中挪至service层中：注入ISysConfigService用于获取验证码开关配置
    @Autowired
    private ISysConfigService configService;

    // @author SXM
    // @date 2026-04-27
    // @reason 将其从controller层中挪至service层中：注入IPtmjAuthService用于登录逻辑
    @Autowired
    private IPtmjAuthService ptmjAuthService;

    // @author SXM
    // @date 2026-04-28
    // @reason 将其从controller层中挪至service层中：注入验证码生成器（字符类型）
    @Autowired
    private Producer captchaProducer;

    // @author SXM
    // @date 2026-04-28
    // @reason 将其从controller层中挪至service层中：注入验证码生成器（数学类型）
    @Autowired
    private Producer captchaProducerMath;

    @Value("${minio.bucketName}")
    private String bucketName;

    @Value("${minio.url}")
    private String minioUrl;

    /**
     * 查询平台用户
     *
     * @param userId 平台用户主键
     * @return 平台用户
     */
    @Override
    public PtmjUser selectPtmjUserByUserId(Long userId)
    {
        return ptmjUserMapper.selectPtmjUserByUserId(userId);
    }

    /**
     * 查询平台用户列表
     *
     * @param ptmjUser 平台用户
     * @return 平台用户
     */
    @Override
    public List<PtmjUser> selectPtmjUserList(PtmjUser ptmjUser)
    {
        return ptmjUserMapper.selectPtmjUserList(ptmjUser);
    }

    /**
     * 新增平台用户
     *
     * @param ptmjUser 平台用户
     * @return 结果
     */
    @Override
    public int insertPtmjUser(PtmjUser ptmjUser)
    {
        ptmjUser.setCreateTime(DateUtils.getNowDate());
        return ptmjUserMapper.insertPtmjUser(ptmjUser);
    }

    /**
     * 修改平台用户
     *
     * @param ptmjUser 平台用户
     * @return 结果
     */
    @Override
    public int updatePtmjUser(PtmjUser ptmjUser)
    {
        ptmjUser.setUpdateTime(DateUtils.getNowDate());
        return ptmjUserMapper.updatePtmjUser(ptmjUser);
    }

    /**
     * 批量删除平台用户
     *
     * @param userIds 需要删除的平台用户主键
     * @return 结果
     */
    @Override
    public int deletePtmjUserByUserIds(Long[] userIds)
    {
        return ptmjUserMapper.deletePtmjUserByUserIds(userIds);
    }

    /**
     * 删除平台用户信息
     *
     * @param userId 平台用户主键
     * @return 结果
     */
    @Override
    public int deletePtmjUserByUserId(Long userId)
    {
        return ptmjUserMapper.deletePtmjUserByUserId(userId);
    }

    // @author sxm
    // @date 2026-04-15
    // @reason 注册功能核心逻辑(整合版)：融合PtmjRegister和DesktopAuth的校验优势
    /**
     * 用户注册(整合版)
     *
     * @author sxm
     * @date 2026-04-15
     * @param registerDto 注册信息DTO,包含用户名、密码、确认密码、密保问题、密保答案、头像文件
     * @return Map<String, Object> 注册结果(包含userId和userName)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> register(PtmjRegisterDto registerDto) {
        // @author SXM
        // @date 2026-04-28
        // @reason 在Service层进行验证码校验，确保业务逻辑完整性
        validateCaptcha(registerDto.getCode(), registerDto.getUuid());

        // @author SXM
        // @date 2026-04-19
        // @reason 校验用户自定义的3个密保问题和答案都不能为空
        String securityQuestionOne = registerDto.getSecurityQuestionOne();
        String securityAnswerOne = registerDto.getSecurityAnswerOne();
        String securityQuestionTwo = registerDto.getSecurityQuestionTwo();
        String securityAnswerTwo = registerDto.getSecurityAnswerTwo();
        String securityQuestionThree = registerDto.getSecurityQuestionThree();
        String securityAnswerThree = registerDto.getSecurityAnswerThree();
        
        if (StringUtils.isBlank(securityQuestionOne) || StringUtils.isBlank(securityAnswerOne) ||
            StringUtils.isBlank(securityQuestionTwo) || StringUtils.isBlank(securityAnswerTwo) ||
            StringUtils.isBlank(securityQuestionThree) || StringUtils.isBlank(securityAnswerThree)) {
            throw new ServiceException("三条密保问题和答案都不能为空");
        }
        validateSecurityTextLength(securityQuestionOne, "密保问题一");
        validateSecurityTextLength(securityAnswerOne, "密保答案一");
        validateSecurityTextLength(securityQuestionTwo, "密保问题二");
        validateSecurityTextLength(securityAnswerTwo, "密保答案二");
        validateSecurityTextLength(securityQuestionThree, "密保问题三");
        validateSecurityTextLength(securityAnswerThree, "密保答案三");

        // @author SXM-用户名长度校验(从DesktopAuth迁移):2-20位
        String userName = registerDto.getUserName();
        if (userName == null || userName.trim().isEmpty()) {
            throw new ServiceException("用户名不能为空");
        }
        if (userName.length() < 2 || userName.length() > 20) {
            throw new ServiceException("用户名长度必须在2到20位之间");
        }

        // @author SXM-密码长度校验(从DesktopAuth迁移):5-20位
        String password = registerDto.getPassword();
        if (password == null || password.trim().isEmpty()) {
            throw new ServiceException("密码不能为空");
        }
        if (password.length() < 5 || password.length() > 20) {
            throw new ServiceException("密码长度必须在5到20位之间");
        }

        // @author SXM-密码确认校验(从DesktopAuth迁移):两次密码必须一致
        String confirmPassword = registerDto.getConfirmPassword();
        if (confirmPassword == null || !password.equals(confirmPassword)) {
            throw new ServiceException("两次输入的密码不一致");
        }


        // @author SXM-用户名唯一性校验(从PtmjRegister迁移)
        PtmjUser existUser = ptmjUserMapper.selectPtmjUserByUserName(userName);
        if (existUser != null) {
            throw new ServiceException("用户名已存在");
        }

        // @author SXM-创建用户对象并设置默认值
        PtmjUser user = new PtmjUser();
        user.setUserName(userName);
        
        // @author SXM-密码加密(使用PtmjRegister的BCryptPasswordEncoder)
        user.setPassword(passwordEncoder.encode(password));

        // @author SXM-头像上传处理(使用PtmjRegister的MinIO上传逻辑)
        String avatarValue;
        MultipartFile avatarFile = registerDto.getAvatarFile();

        if (avatarFile != null && !avatarFile.isEmpty()) {
            // 情况1：上传文件到 MinIO
            try {
                String originalFilename = avatarFile.getOriginalFilename();
                String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                String fileName = UUID.randomUUID().toString().replace("-", "") + extension;
                String objectName = "avatar/" + fileName;

                try (java.io.InputStream inputStream = avatarFile.getInputStream()) {
                    minioClient.putObject(
                            PutObjectArgs.builder()
                                    .bucket(bucketName)
                                    .object(objectName)
                                    .stream(inputStream, avatarFile.getSize(), -1)
                                    .contentType(avatarFile.getContentType())
                                    .build()
                    );
                }

                avatarValue = minioUrl + "/" + bucketName + "/" + objectName;
                log.info("=== SXM-MinIO头像上传成功 === URL: [{}]", avatarValue);
            } catch (Exception e) {
                log.error("=== SXM-MinIO头像上传失败 === 错误信息: [{}]", e.getMessage());
                avatarValue = "null";
            }
        } else if (registerDto.getAvatar() != null && !registerDto.getAvatar().isEmpty()) {
            // 情况2：前端直接传了头像 URL（sxm 2026-05-10 新增）
            avatarValue = registerDto.getAvatar();
            log.info("=== SXM-使用前端传入的头像URL: [{}]", avatarValue);
        } else {
            // 情况3：都没有，使用默认头像
            avatarValue = "null";
        }

        user.setAvatar(avatarValue);
        log.info("=== SXM-最终头像URL: [{}]", avatarValue);
        
        user.setCount(0L);
        user.setStatus("1");
        
        // @author SXM-创建者标识(使用PtmjRegister的方式):使用注册的用户名
        user.setCreatBy(userName);
        user.setCreateTime(DateUtils.getNowDate());

        // @author SXM-插入用户表
        log.info("=== SXM-插入前 user 对象: {}", user.toString());
        int result = ptmjUserMapper.insertPtmjUser(user);
        log.info("=== SXM-插入结果: result={}, userId={}, avatar={}", result, user.getUserId(), user.getAvatar());
        if (result == 0) {
            throw new ServiceException("注册失败,请稍后重试");
        }

        // @author SXM
        // @date 2026-04-19
        // @reason 将用户自定义的3个密保问题和答案合并为一条记录插入数据库
        // question字段：3个问题用|分隔，answer字段：3个加密答案用|分隔
        String combinedQuestions = securityQuestionOne + "|" + securityQuestionTwo + "|" + securityQuestionThree;
        
        // 答案加密后用|分隔存储（由于前面已校验不为空，这里直接加密）
        String answerOne = passwordEncoder.encode(securityAnswerOne.trim());
        String answerTwo = passwordEncoder.encode(securityAnswerTwo.trim());
        String answerThree = passwordEncoder.encode(securityAnswerThree.trim());
        String combinedAnswers = answerOne + "|" + answerTwo + "|" + answerThree;
        
        PtmjSecurity security = new PtmjSecurity();
        security.setUserId(user.getUserId());
        security.setQuestion(combinedQuestions);  // 3个问题用|分隔存储
        security.setAnswer(combinedAnswers);  // 3个答案用|分隔存储
        security.setCreateBy(userName);
        security.setCreateTime(DateUtils.getNowDate());
        int secResult = ptmjSecurityMapper.insertPtmjSecurity(security);

        if (secResult == 1) {
            // @author SXM-注册成功,使用Map封装返回数据(userId和userName)
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("userId", user.getUserId());
            resultMap.put("userName", user.getUserName());
            return resultMap;
        } else {
            throw new ServiceException("注册失败,请稍后重试");
        }
    }

    /**
     * 修改人：LYZ-根据用户名查询平台用户
     */
    @Override
    public PtmjUser selectPtmjUserByUserName(String userName)
    {
        return ptmjUserMapper.selectPtmjUserByUserName(userName);
    }

    /**
     * @author SXM
     * @date 2026-04-19
     * @reason 通过用户自定义的三条密保重置密码（简化验证逻辑，直接按顺序匹配答案）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPasswordBySecurity(String userName, String securityAnswerOne, String securityAnswerTwo, String securityAnswerThree, String newPassword)
    {
        if (StringUtils.isBlank(userName))
        {
            throw new ServiceException("用户名不能为空");
        }
        if (StringUtils.isBlank(securityAnswerOne) || StringUtils.isBlank(securityAnswerTwo) || StringUtils.isBlank(securityAnswerThree))
        {
            throw new ServiceException("三条密保答案都不能为空");
        }
        if (StringUtils.isBlank(newPassword) || newPassword.length() < 5 || newPassword.length() > 20)
        {
            throw new ServiceException("新密码长度必须在5到20位之间");
        }

        PtmjUser user = ptmjUserMapper.selectPtmjUserByUserName(userName);
        if (user == null)
        {
            throw new ServiceException("用户不存在");
        }

        // @author SXM
        // @date 2026-04-19
        // @reason 查询用户的密保记录（一条记录，question和answer字段都包含3个值用|分隔）
        List<PtmjSecurity> securityList = ptmjSecurityMapper.selectPtmjSecurityList(buildSecurityQuery(user.getUserId()));
        if (securityList == null || securityList.isEmpty())
        {
            throw new ServiceException("密保信息不存在，请联系管理员");
        }

        PtmjSecurity securityRecord = securityList.get(0);
        String combinedQuestions = securityRecord.getQuestion();
        String combinedAnswers = securityRecord.getAnswer();
        
        if (StringUtils.isBlank(combinedQuestions) || StringUtils.isBlank(combinedAnswers))
        {
            throw new ServiceException("密保信息不完整");
        }
        
        // @author SXM
        // @date 2026-04-19
        // @reason 从合并的字符串中解析出3个问题和3个答案
        String[] questions = combinedQuestions.split("\\|");
        String[] savedAnswers = combinedAnswers.split("\\|");
        
        if (questions.length != 3 || savedAnswers.length != 3)
        {
            throw new ServiceException("密保信息格式错误");
        }
        
        // @author SXM
        // @date 2026-04-19
        // @reason 按顺序验证3个答案
        String[] inputAnswers = {securityAnswerOne, securityAnswerTwo, securityAnswerThree};
        for (int i = 0; i < 3; i++)
        {
            // 如果存储的答案是"Y/N"，表示注册时该答案未填写，跳过此题验证
            if ("Y/N".equals(savedAnswers[i]))
            {
                continue;
            }
            
            // 验证用户输入的答案是否与存储的加密答案匹配
            if (!matchesSecurityAnswer(inputAnswers[i], savedAnswers[i]))
            {
                throw new ServiceException("第" + (i + 1) + "个密保答案错误，请确认答案与注册时一致");
            }
        }

        // @author SXM
        // @date 2026-04-19
        // @reason 更新密码
        int rows = ptmjUserMapper.updatePasswordByUserId(user.getUserId(), passwordEncoder.encode(newPassword));
        if (rows == 0)
        {
            throw new ServiceException("密码重置失败");
        }
    }

    /**
     * @author SXM
     * @date 2026-04-19
     * @reason 构造按 userId 查询密保列表的条件对象
     */
    private PtmjSecurity buildSecurityQuery(Long userId)
    {
        PtmjSecurity query = new PtmjSecurity();
        query.setUserId(userId);
        return query;
    }

    private void validateSecurityTextLength(String value, String fieldName)
    {
        if (StringUtils.isNotBlank(value) && value.trim().length() > SECURITY_TEXT_MAX_LENGTH)
        {
            throw new ServiceException(fieldName + "长度不能超过" + SECURITY_TEXT_MAX_LENGTH + "个字符");
        }
    }

    /**
     * @author SXM
     * @date 2026-04-19
     * @reason 统一答案匹配（支持 bcrypt 与明文）
     */
    private boolean matchesSecurityAnswer(String inputAnswer, String savedAnswer)
    {
        if (StringUtils.isBlank(inputAnswer) || StringUtils.isBlank(savedAnswer))
        {
            return false;
        }
        String normalizedInput = inputAnswer.trim();
        if (savedAnswer.startsWith("$2a$") || savedAnswer.startsWith("$2b$") || savedAnswer.startsWith("$2y$"))
        {
            return passwordEncoder.matches(normalizedInput, savedAnswer);
        }
        return savedAnswer.equals(normalizedInput);
    }

    // @author SXM
    // @date 2026-04-19
    // @reason 根据用户名查询用户的3个自定义密保问题(不返回答案),用于找回密码时前端展示
    @Override
    public Map<String, String> selectSecurityQuestionsByUserName(String userName)
    {
        if (StringUtils.isBlank(userName))
        {
            throw new ServiceException("用户名不能为空");
        }
        
        PtmjUser user = ptmjUserMapper.selectPtmjUserByUserName(userName);
        if (user == null)
        {
            throw new ServiceException("用户不存在");
        }
        
        // @author SXM
        // @date 2026-04-19
        // @reason 查询用户的密保记录(一条记录)
        List<PtmjSecurity> securityList = ptmjSecurityMapper.selectPtmjSecurityList(buildSecurityQuery(user.getUserId()));
        if (securityList == null || securityList.isEmpty())
        {
            throw new ServiceException("密保信息不完整");
        }
        
        PtmjSecurity securityRecord = securityList.get(0);
        String combinedQuestions = securityRecord.getQuestion();
        
        if (StringUtils.isBlank(combinedQuestions))
        {
            throw new ServiceException("密保问题不存在");
        }
        
        // @author SXM
        // @date 2026-04-19
        // @reason 从合并的问题字符串中解析出3个问题(不返回答案)
        String[] questions = combinedQuestions.split("\\|");
        if (questions.length != 3)
        {
            throw new ServiceException("密保问题格式错误");
        }
        
        Map<String, String> result = new HashMap<>();
        result.put("questionOne", questions[0]);
        result.put("questionTwo", questions[1]);
        result.put("questionThree", questions[2]);
        
        return result;
    }

    // @author SXM
    // @date 2026-04-19
    // @reason 根据用户名查询用户的3个自定义密保问题列表(转换为前端期望的List结构)
    @Override
    public java.util.List<java.util.Map<String, String>> selectSecurityQuestionsListByUserName(String userName)
    {
        // @author SXM
        // @date 2026-04-19
        // @reason 先获取Map格式的密保问题
        Map<String, String> questions = selectSecurityQuestionsByUserName(userName);
        
        // @author SXM
        // @date 2026-04-19
        // @reason 将Map转换为前端期望的List结构 [{question: '...'}, ...]
        java.util.List<java.util.Map<String, String>> questionList = new java.util.ArrayList<>();
        questionList.add(java.util.Collections.singletonMap("question", questions.get("questionOne")));
        questionList.add(java.util.Collections.singletonMap("question", questions.get("questionTwo")));
        questionList.add(java.util.Collections.singletonMap("question", questions.get("questionThree")));
        
        return questionList;
    }

    /**
     * LYZ修改：管理员按用户名重置三条密保答案
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetSecurityAnswersByUserName(String userName, String securityAnswerOne, String securityAnswerTwo, String securityAnswerThree) {
        if (StringUtils.isBlank(userName)) {
            throw new ServiceException("用户名不能为空");
        }
        if (StringUtils.isBlank(securityAnswerOne) || StringUtils.isBlank(securityAnswerTwo) || StringUtils.isBlank(securityAnswerThree)) {
            throw new ServiceException("三条密保答案都不能为空");
        }

        PtmjUser user = ptmjUserMapper.selectPtmjUserByUserName(userName);
        if (user == null) {
            throw new ServiceException("用户不存在");
        }
    }

    // @author SXM
    // @date 2026-04-27
    // @reason 将其从controller层中挪至service层中：注册/通用验证码校验逻辑
    @Override
    public void validateCaptcha(String code, String uuid)
    {
        boolean captchaEnabled = configService.selectCaptchaEnabled();
        if (!captchaEnabled)
        {
            return;
        }
        validateCaptchaCore(code, uuid);
    }

    // @author SXM
    // @date 2026-04-27
    // @reason 将其从controller层中挪至service层中：找回密码强制验证码校验逻辑
    @Override
    public void validateCaptchaStrict(String code, String uuid)
    {
        validateCaptchaCore(code, uuid);
    }

    // @author SXM
    // @date 2026-04-27
    // @reason 将其从controller层中挪至service层中：验证码核心校验逻辑
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

    // @author SXM
    // @date 2026-04-27
    // @reason 将其从controller层中挪至service层中：完整的找回密码业务逻辑编排
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void executeResetPassword(Map<String, String> body)
    {
        String userName = org.apache.commons.lang3.StringUtils.defaultIfBlank(body.get("userName"), body.get("username")); //LYZ三次修改：兼容userName/username两种前端键名
        String code = body.get("code");
        String uuid = body.get("uuid");
        String securityAnswerOne = body.get("securityAnswerOne");
        String securityAnswerTwo = body.get("securityAnswerTwo");
        String securityAnswerThree = body.get("securityAnswerThree");
        String newPassword = body.get("newPassword");
        String confirmPassword = body.get("confirmPassword");

        // 密码一致性校验
        if (newPassword == null || !newPassword.equals(confirmPassword))
        {
            throw new ServiceException("两次输入的密码不一致");
        }

        // 验证码强制校验
        validateCaptchaStrict(code, uuid);

        // 调用原有的密保重置逻辑
        resetPasswordBySecurity(userName, securityAnswerOne, securityAnswerTwo, securityAnswerThree, newPassword);
    }

    /**
     * 修改人：LYZ-管理员按用户名重置三条密保答案
     * SXM于2026-04-27迁移：将其从controller层中挪至service层中
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void executeResetSecurityAnswers(Map<String, String> body)
    {
        String userName = body.get("userName");
        String securityAnswerOne = body.get("securityAnswerOne");
        String securityAnswerTwo = body.get("securityAnswerTwo");
        String securityAnswerThree = body.get("securityAnswerThree");
        resetSecurityAnswersByUserName(userName, securityAnswerOne, securityAnswerTwo, securityAnswerThree);
    }

    /**
     * 修改人：LYZ-桌面端登录返回客户端用户信息
     * SXM于2026-04-27迁移：将其从controller层中挪至service层中
     */
    @Override
    public Map<String, Object> executeDesktopLogin(com.ptmj.datum.domain.PtmjLoginBody loginBody)
    {
        String token = ptmjAuthService.login(loginBody);
        PtmjUser ptmjUser = selectPtmjUserByUserName(loginBody.getUsername());
        
        Map<String, Object> result = new HashMap<>();
        result.put(Constants.TOKEN, token);
        result.put("user", ptmjUser);
        result.put("roles", java.util.Collections.singleton("ptmj_user"));
        result.put("permissions", java.util.Collections.emptySet());
        return result;
    }

    /**
     * 修改人：LYZ-客户端获取当前登录用户信息
     * SXM于2026-04-27迁移：将其从controller层中挪至service层中
     */
    @Override
    public Map<String, Object> executeGetClientInfo()
    {
        Long userId = com.ruoyi.common.utils.SecurityUtils.getUserId();
        PtmjUser ptmjUser = selectPtmjUserByUserId(userId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("user", ptmjUser);
        result.put("roles", java.util.Collections.singleton("ptmj_user"));
        result.put("permissions", java.util.Collections.emptySet());
        return result;
    }

    // @author SXM
    // @date 2026-04-28
    // @reason 将其从controller层中挪至service层中：生成桌面端验证码图片
    @Override
    public Map<String, Object> generateCaptchaImage()
    {
        Map<String, Object> result = new HashMap<>();
        
        // @author SXM
        // @date 2026-04-28
        // @reason 检查验证码开关配置
        boolean captchaEnabled = configService.selectCaptchaEnabled();
        result.put("captchaEnabled", captchaEnabled);
        if (!captchaEnabled)
        {
            return result;
        }

        // @author SXM
        // @date 2026-04-28
        // @reason 生成UUID和验证码
        String uuid = IdUtils.simpleUUID();
        String verifyKey = CacheConstants.CAPTCHA_CODE_KEY + uuid;

        String capStr = null;
        String code = null;
        BufferedImage image = null;

        // @author SXM
        // @date 2026-04-28
        // @reason 根据配置的验证码类型生成验证码（数学或字符）
        String captchaType = RuoYiConfig.getCaptchaType();
        if ("math".equals(captchaType))
        {
            String capText = captchaProducerMath.createText();
            capStr = capText.substring(0, capText.lastIndexOf("@"));
            code = capText.substring(capText.lastIndexOf("@") + 1);
            image = captchaProducerMath.createImage(capStr);
        }
        else if ("char".equals(captchaType))
        {
            capStr = captchaProducer.createText();
            code = capStr;
            image = captchaProducer.createImage(capStr);
        }

        // @author SXM
        // @date 2026-04-28
        // @reason 将验证码存入Redis，设置过期时间
        redisCache.setCacheObject(verifyKey, code, Constants.CAPTCHA_EXPIRATION, TimeUnit.MINUTES);

        // @author SXM
        // @date 2026-04-28
        // @reason 将图片转换为Base64编码
        FastByteArrayOutputStream os = new FastByteArrayOutputStream();
        try
        {
            ImageIO.write(image, "jpg", os);
        }
        catch (IOException e)
        {
            log.error("=== SXM-验证码图片生成失败 === 错误信息: [{}]", e.getMessage());
            throw new ServiceException("验证码生成失败");
        }

        // @author SXM
        // @date 2026-04-28
        // @reason 返回uuid和base64编码的图片
        result.put("uuid", uuid);
        result.put("img", Base64.encode(os.toByteArray()));
        return result;
    }


    /**
     * 审核通过时用户上传计数+1
     * fc
     */
    @Override
    public int incrementCountByUserId(Long userId)
    {
        return ptmjUserMapper.incrementCountByUserId(userId);
    }}

