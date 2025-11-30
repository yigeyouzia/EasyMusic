package com.easymusic.redis;

import com.easymusic.entity.constants.Constants;
import com.easymusic.entity.dto.MusicTaskDTO;
import com.easymusic.entity.dto.TokenUserInfo4AdminDTO;
import com.easymusic.entity.dto.TokenUserInfoDTO;
import com.easymusic.entity.po.SysDict;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
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

    public void cleanTokenUserInfo4Web(String token) {
        redisUtils.delete(Constants.REDIS_KEY_TOKEN_WEB_USER + token);
    }

    /**
     * 积分业务
     */

    /**
     * 保存订单号到redis 1min过期
     * 1min过期
     * 存入orderId
     *
     * @param orderId
     */
    public void cacheHavePayOrder(String orderId) {
        redisUtils.setex(Constants.REDIS_KEY_ORDER_HAVE_PAY + orderId, orderId, Constants.REDIS_KEY_EXPIRES_ONE_MIN * 10);
    }

    /**
     * 获取订单号
     *
     * @param orderId
     * @return orderId
     */
    public String getHavePayOrder(String orderId) {
        return (String) redisUtils.get(Constants.REDIS_KEY_ORDER_HAVE_PAY + orderId);
    }

    /**
     * 微信支付
     * 延时队列
     * 添加订单 orderId
     *
     * @param delayMin 延迟时间 单位分钟
     * @param orderId  订单号
     */
    public void addOrder2DelayQueue(Integer delayMin, String orderId) {
        long executeTime = System.currentTimeMillis() + delayMin * 60 * 1000;
        redisUtils.zsetAdd(Constants.REDIS_KEY_ORDER_DELAY_QUEUE, orderId, executeTime);
    }

    public Set<String> getTimeOutOrder() {
        return redisUtils.zsetRangeByScore(Constants.REDIS_KEY_ORDER_DELAY_QUEUE, 0, System.currentTimeMillis());
    }

    public Long removeTimeOutOrder(String orderId) {
        return redisUtils.zsetAddRemove(Constants.REDIS_KEY_ORDER_DELAY_QUEUE, orderId);
    }


    /**
     * 30s
     * 天谱乐
     * 延时队列
     */
    public void addMusicCreateTask(MusicTaskDTO musicTaskDto) {
        // TODO 音乐延时
        long executeTime = System.currentTimeMillis() + 3 * 1000;
        redisUtils.zsetAdd(Constants.REDIS_KEY_MUSIC_CREATE_QUEUE, musicTaskDto, executeTime);
    }

    public Set<MusicTaskDTO> getMusicTaskDto() {
        return redisUtils.zsetRangeByScore(Constants.REDIS_KEY_MUSIC_CREATE_QUEUE, 0, System.currentTimeMillis());
    }

    public Long removeMusicTaskDto(MusicTaskDTO taskDto) {
        return redisUtils.zsetAddRemove(Constants.REDIS_KEY_MUSIC_CREATE_QUEUE, taskDto);
    }
    // admin *********************************************************

    /**
     * 保存管理员token到redis 7天过期
     *
     * @param adminDTO
     */
    public void saveTokenUserInfo4Admin(TokenUserInfo4AdminDTO adminDTO) {
        redisUtils.setex(Constants.REDIS_KEY_TOKEN_ADMIN_USER + adminDTO.getToken(),
                adminDTO,
                Constants.REDIS_KEY_EXPIRES_ONE_DAY * 7);
    }

    public TokenUserInfo4AdminDTO getTokenUserInfo4Admin(String token) {
        return (TokenUserInfo4AdminDTO) redisUtils.get(Constants.REDIS_KEY_TOKEN_ADMIN_USER + token);
    }

    // 清除token
    public void cleanTokenUserInfo4Admin(String token) {
        redisUtils.delete(Constants.REDIS_KEY_TOKEN_ADMIN_USER + token);
    }

    /**
     * 字典 系统配置
     */

    /**
     * 保存字典到redis
     * hset
     * 键：父节点
     * 值：子节点列表
     *
     * @param dictPcode   父节点
     * @param sysDictList 子节点列表
     */
    public void saveDict(String dictPcode, List<SysDict> sysDictList) {
        redisUtils.hset(Constants.REDIS_KEY_SYS_DICT, dictPcode, sysDictList);
    }

    public List<SysDict> getDictSubList(String dictPcode) {
        return (List<SysDict>) redisUtils.hget(Constants.REDIS_KEY_SYS_DICT, dictPcode);
    }

    public Map<String, List<SysDict>> getAllDict() {
        return (Map<String, List<SysDict>>) redisUtils.entries(Constants.REDIS_KEY_SYS_DICT);
    }
}
