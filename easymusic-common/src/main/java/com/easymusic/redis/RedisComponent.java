package com.easymusic.redis;

import com.easymusic.entity.constants.Constants;
import com.easymusic.entity.dto.TokenUserInfoDTO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * @author cyt
 * * @date 2025/11/19 14:03:07
 */

@Component
public class RedisComponent {
    @Resource
    private RedisUtils redisUtils;

    // 保存验证码到redis 1分钟过期
    public String saveCheckCode(String code) {
        String checkCodeKey = UUID.randomUUID().toString();
        redisUtils.setex(Constants.REDIS_KEY_CHECK_CODE + checkCodeKey, code, Constants.REDIS_KEY_EXPIRES_ONE_MIN * 10);
        return checkCodeKey;
    }

    // 获取验证码
    public String getCheckCode(String checkCodeKey) {
        return (String) redisUtils.get(Constants.REDIS_KEY_CHECK_CODE + checkCodeKey);
    }

    // 清除验证码
    public void cleanCheckCode(String checkCodeKey) {
        redisUtils.delete(Constants.REDIS_KEY_CHECK_CODE + checkCodeKey);
    }

    // 保存token到redis 7天过期
    public void saveUserTokenInfoDto(TokenUserInfoDTO tokenUserInfoDTO) {
        redisUtils.setex(Constants.REDIS_KEY_TOKEN_WEB_USER + tokenUserInfoDTO.getToken(),
                tokenUserInfoDTO,
                Constants.REDIS_KEY_EXPIRES_ONE_DAY * 7);
    }

    // 获取token
    public TokenUserInfoDTO getUserTokenInfoDto(String token) {
        return (TokenUserInfoDTO) redisUtils.get(Constants.REDIS_KEY_TOKEN_WEB_USER + token);
    }
}
