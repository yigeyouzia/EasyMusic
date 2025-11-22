package com.easymusic.controller;

import com.easymusic.entity.config.AppConfig;
import com.easymusic.entity.constants.Constants;
import com.easymusic.entity.dto.TokenUserInfo4AdminDTO;
import com.easymusic.entity.vo.CheckCodeVO;
import com.easymusic.entity.vo.ResponseVO;
import com.easymusic.exception.BusinessException;
import com.easymusic.redis.RedisComponent;
import com.easymusic.service.UserInfoService;
import com.easymusic.utils.StringTools;
import com.wf.captcha.ArithmeticCaptcha;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户信息 Controller
 */
@RestController
@RequestMapping("/account")
@Slf4j
@Validated
public class AccountController extends ABaseController {

    @Resource
    private UserInfoService userInfoService;

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private AppConfig appConfig;

    @RequestMapping("/checkCode")
    public ResponseVO checkCode() {
        ArithmeticCaptcha captcha = new ArithmeticCaptcha(100, 42);
        String code = captcha.text();
        log.info("code:{}", code);
        // redis存储结果的key
        String checkCodeKey = redisComponent.saveCheckCode(code);
        // 图片base
        String base64 = captcha.toBase64();

        CheckCodeVO res = new CheckCodeVO();
        res.setCheckCode(base64);
        res.setCheckCodeKey(checkCodeKey);
        return getSuccessResponseVO(res);
    }

    @RequestMapping("/login")
    public ResponseVO register(@NotEmpty String checkCodeKey,
                               @NotEmpty String checkCode,
                               @NotEmpty String account,
                               @NotEmpty @Size(max = 32) String password) {
        try {
            if (!redisComponent.getCheckCode(checkCodeKey).equals(checkCode)) {
                throw new BusinessException("验证码错误");
            }

            if (!account.equals(appConfig.getAdminAccount()) || !password.equals(StringTools.encodeByMD5(appConfig.getAdminPassword()))) {
                throw new BusinessException("账号或密码错误");
            }
            TokenUserInfo4AdminDTO adminDTO = new TokenUserInfo4AdminDTO();
            adminDTO.setAccount(account);
            adminDTO.setToken(StringTools.getRandomNumber(Constants.LENGTH_30));
            redisComponent.saveTokenUserInfo4Admin(adminDTO);
            return getSuccessResponseVO(adminDTO.getToken());
        } finally {
            redisComponent.cleanCheckCode(checkCodeKey);
        }
    }

    @RequestMapping("/logout")
    public ResponseVO logout() {
        TokenUserInfo4AdminDTO adminDTO = getTokenUser4AdminInfo(null);
        if (adminDTO == null) {
            return getSuccessResponseVO(null);
        }
        redisComponent.cleanTokenUserInfo4Admin(adminDTO.getToken());
        return getSuccessResponseVO(null);
    }

}