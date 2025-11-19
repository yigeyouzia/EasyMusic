package com.easymusic.controller;

import com.easymusic.entity.vo.CheckCodeVO;
import com.easymusic.entity.vo.ResponseVO;
import com.easymusic.exception.BusinessException;
import com.easymusic.redis.RedisComponent;
import com.easymusic.service.UserInfoService;
import com.wf.captcha.ArithmeticCaptcha;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.Email;
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
public class AccountoController extends ABaseController {

    @Resource
    private UserInfoService userInfoService;

    @Resource
    private RedisComponent redisComponent;

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

    @RequestMapping("/register")
    public ResponseVO register(@NotEmpty String checkCodekey,
                               @NotEmpty String checkCode,
                               @NotEmpty @Email @Size(max = 50) String email,
                               @NotEmpty @Size(min = 10, max = 18) String password,
                               @NotEmpty @Size(max = 20) String nickName) {
        try {
            if (!redisComponent.getCheckCode(checkCodekey).equals(checkCode)) {
                throw new BusinessException("验证码错误");
            }
            userInfoService.register(email, password, nickName);
            return getSuccessResponseVO(null);
        } finally {
            redisComponent.cleanCheckCode(checkCodekey);
        }
    }
}