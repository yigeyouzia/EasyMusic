package com.easymusic.api;

import com.alibaba.fastjson2.JSONPath;
import com.easymusic.entity.config.AppConfig;
import com.easymusic.entity.dto.MusicCreateResultDTO;
import com.easymusic.entity.enums.MusicTypeEnum;
import com.easymusic.utils.JsonUtils;
import com.easymusic.utils.OKHttpUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;

/**
 * 天谱乐api实现类
 *
 * @author cyt
 * * @date 2025/11/28 18:20:26
 */

@Slf4j
@Component("tianpuyueApi")
public class MusicCreateApi4TianPuYueImpl implements MusicCreateApi {
    /**
     * 生成音乐
     */
    private String URL_CREATE_MUSIC = "/open-apis/v1/song/generate";
    /**
     * 查询音乐生成
     */
    private String URL_QUERY_MUSIC = "/open-apis/v1/song/query";

    /**
     * 生成纯音乐
     */
    private String URL_CREATE_PURE_MUSIC = "/open-apis/v1/instrumental/generate";

    private String URL_QUERY_PURE_MUSIC = "/open-apis/v1/instrumental/query";

    // 自己的回调api
    private String CALL_BACL_URL = "/api/musicNotify/tianpuyu/%d";

    @Resource
    private AppConfig appConfig;

    private HashMap<String, String> getHeader() {
        HashMap<String, String> header = new HashMap<>();
        header.put("Content-Type", "application/json; charset=utf-8");
        header.put("Authorization", appConfig.getTianpuyueApiKey());
        return header;
    }

    @Override
    public List<String> createMusic(String model, String prompt, String lyrics) {
        HashMap<String, String> header = getHeader();
        HashMap<String, Object> params = new HashMap<>();
        params.put("prompt", prompt);
        params.put("lyrics", lyrics);
        params.put("model", model);
        params.put("callback_url", String.format(CALL_BACL_URL, MusicTypeEnum.MUSIC.getType()));
        String jsonParams = JsonUtils.convertObj2Json(params);

        String response = OKHttpUtils.postRequest4Json(appConfig.getTianpuyueApiDomain() + URL_CREATE_MUSIC,
                header,
                jsonParams);
        log.info("天谱乐api生成音乐返回结果：{}", response);
        List<String> itemList = (List<String>) JSONPath.eval(response, "$.data.item_ids");
        return itemList;
    }

    @Override
    public MusicCreateResultDTO musicQuery(String itemId) {
        return MusicCreateApi.super.musicQuery(itemId);
    }

    @Override
    public List<String> createPureMusic(String model, String prompt) {
        HashMap<String, String> header = getHeader();
        HashMap<String, Object> params = new HashMap<>();
        params.put("prompt", prompt);
        params.put("model", model);
        params.put("callback_url", String.format(CALL_BACL_URL, MusicTypeEnum.PURE.getType()));
        String jsonParams = JsonUtils.convertObj2Json(params);

        String response = OKHttpUtils.postRequest4Json(appConfig.getTianpuyueApiDomain() + URL_QUERY_PURE_MUSIC,
                header,
                jsonParams);
        log.info("天谱乐纯音乐api生成音乐返回结果：{}", response);
        List<String> itemList = (List<String>) JSONPath.eval(response, "$.data.item_ids");
        return itemList;
    }

    @Override
    public MusicCreateResultDTO pureMusicQuery(String itemId) {
        return MusicCreateApi.super.pureMusicQuery(itemId);
    }

    @Override
    public MusicCreateResultDTO createMusicNotify(Integer musicType, String responseBody) {
        return MusicCreateApi.super.createMusicNotify(musicType, responseBody);
    }
}
