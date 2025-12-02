package com.easymusic.controller;

import com.easymusic.entity.dto.TokenUserInfoDTO;
import com.easymusic.entity.po.UserInfo;
import com.easymusic.entity.vo.CheckCodeVO;
import com.easymusic.entity.vo.ResponseVO;
import com.easymusic.exception.BusinessException;
import com.easymusic.redis.RedisComponent;
import com.easymusic.service.UserInfoService;
import com.easymusic.utils.FileUtils;
import com.wf.captcha.ArithmeticCaptcha;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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
    private FileUtils fileUtils;

    @RequestMapping("/avatar")
    public void avatar(String userId) {
        fileUtils.copyAvatar(userId);
    }

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
    public ResponseVO register(@NotEmpty String checkCodeKey,
                               @NotEmpty String checkCode,
                               @NotEmpty @Email @Size(max = 50) String email,
                               @NotEmpty @Size(min = 10, max = 18) String password,
                               @NotEmpty @Size(max = 20) String nickName) {
        try {
            if (!checkCode.equals(redisComponent.getCheckCode(checkCodeKey))) {
                throw new BusinessException("验证码错误");
            }
            userInfoService.register(email, password, nickName);
            return getSuccessResponseVO(null);
        } finally {
            redisComponent.cleanCheckCode(checkCodeKey);
        }
    }

    @RequestMapping("/login")
    public ResponseVO register(@NotEmpty String checkCodeKey,
                               @NotEmpty String checkCode,
                               @NotEmpty @Email @Size(max = 50) String email,
                               @NotEmpty @Size(max = 32) String password) {
        try {
            if (!checkCode.equals(redisComponent.getCheckCode(checkCodeKey))) {
                throw new BusinessException("验证码错误");
            }
            TokenUserInfoDTO tokenUserInfoDTO = userInfoService.login(email, password);
            return getSuccessResponseVO(tokenUserInfoDTO);
        } finally {
            redisComponent.cleanCheckCode(checkCodeKey);
        }
    }

    @RequestMapping("/getLoginInfo")
    public ResponseVO getLoginInfo() {
        TokenUserInfoDTO res = getTokenUserInfo(null);
        if (res == null) {
            return getSuccessResponseVO(null);
        }
        redisComponent.saveUserTokenInfoDto(res);
        UserInfo userInfo = userInfoService.getUserInfoByUserId(res.getUserId());
        res.setIntegral(userInfo.getIntegral());
        return getSuccessResponseVO(res);
    }

    @RequestMapping("/logout")
    public ResponseVO logout() {
        TokenUserInfoDTO tokenUserInfo = getTokenUserInfo(null);
        if (tokenUserInfo == null) {
            return getSuccessResponseVO(null);
        }
        redisComponent.cleanTokenUserInfo4Web(tokenUserInfo.getToken());
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/updatePassword")
    public ResponseVO updatePassword(@NotEmpty String oldPassword, @NotEmpty String password) {
        TokenUserInfoDTO tokenUserInfo = getTokenUserInfo(null);
        userInfoService.updatePassword(tokenUserInfo.getUserId(), oldPassword, password);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/updateUserInfo")
    public ResponseVO updateUserInfo(MultipartFile avatar,
                                     @Size(max = 20) @NotEmpty String nickName) {
        TokenUserInfoDTO tokenUserInfo = getTokenUserInfo(null);
        userInfoService.updateUserInfo(tokenUserInfo, avatar, nickName);
        return getSuccessResponseVO(tokenUserInfo);
    }
}