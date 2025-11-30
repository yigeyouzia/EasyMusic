package com.easymusic.api;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONPath;
import com.easymusic.entity.config.AppConfig;
import com.easymusic.entity.dto.MusicCreationResultDTO;
import com.easymusic.entity.enums.MusicTypeEnum;
import com.easymusic.utils.JsonUtils;
import com.easymusic.utils.OKHttpUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
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

    // 天谱乐创建音乐成功status
    private Integer STATUS_SUCCESS = 200000;

    private HashMap<String, String> getHeader() {
        HashMap<String, String> header = new HashMap<>();
        header.put("Content-Type", "application/json; charset=utf-8");
        header.put("Authorization", appConfig.getTianpuyueApiKey());
        // 老罗的api需要加上这个header
        header.put("courseOrderId", appConfig.getCourseOrderId());
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

    /**
     * 解析天谱乐api返回结果
     *
     * @return
     */
    private MusicCreationResultDTO getMusicCreateResultDTO(Integer status, JSONObject jsonObject, MusicTypeEnum musicTypeEnum) {
        // 创建失败
        if (status != null && !status.equals(STATUS_SUCCESS)) {
            MusicCreationResultDTO resultDTO = new MusicCreationResultDTO();
            resultDTO.setTaskId(jsonObject.getString("item_id"));
            resultDTO.setCreateSuccess(false);
            return resultDTO;
        }

        if (jsonObject == null) {
            return null;
        }

        List<MusicCreationResultDTO.Lyrics> lyricsList = new ArrayList<>();
        if (MusicTypeEnum.MUSIC.equals(musicTypeEnum)) {
            if (jsonObject.get("lyrics_sections") == null) {
                return null;
            }
            lyricsList = (List<MusicCreationResultDTO.Lyrics>) JsonUtils
                    .convertJsonArray2List(JsonUtils.convertObj2Json(jsonObject.get("lyrics_sections")),
                            MusicCreationResultDTO.Lyrics.class);
        }
        MusicCreationResultDTO resultDTO = new MusicCreationResultDTO();
        resultDTO.setTaskId(jsonObject.getString("item_id"));
        resultDTO.setTitle(jsonObject.getString("title"));
        resultDTO.setAudioUrl(jsonObject.getString("audio_url"));
        resultDTO.setAudioHiUrl(jsonObject.getString("audio_hi_url"));
        resultDTO.setDuration(jsonObject.getIntValue("duration"));
        resultDTO.setLyricsList(lyricsList);
        resultDTO.setCreateSuccess(true);
        return resultDTO;
    }

    @Override
    public MusicCreationResultDTO musicQuery(String itemId) {
        HashMap<String, String> header = getHeader();
        HashMap<String, Object> params = new HashMap<>();
        params.put("item_ids", new String[]{itemId});
        String jsonParams = JsonUtils.convertObj2Json(params);

        String response = OKHttpUtils.postRequest4Json(appConfig.getTianpuyueApiDomain() + URL_QUERY_MUSIC,
                header,
                jsonParams);
        log.info("请求天谱乐api查询音乐返回结果：{}", response);
        // 支取第一个
        JSONObject jsonObject = (JSONObject) JSONPath.eval(response, "$.data.songs[0]");
        Integer status = (Integer) JSONPath.eval(response, "$.status");
        return getMusicCreateResultDTO(status, jsonObject, MusicTypeEnum.MUSIC);
    }


    @Override
    public List<String> createPureMusic(String model, String prompt) {
        HashMap<String, String> header = getHeader();
        HashMap<String, Object> params = new HashMap<>();
        params.put("prompt", prompt);
        params.put("model", model);
        params.put("callback_url", String.format(CALL_BACL_URL, MusicTypeEnum.PURE.getType()));
        String jsonParams = JsonUtils.convertObj2Json(params);

        String response = OKHttpUtils.postRequest4Json(appConfig.getTianpuyueApiDomain() + URL_CREATE_PURE_MUSIC,
                header,
                jsonParams);
        log.info("天谱乐纯音乐api生成音乐返回结果：{}", response);
        List<String> itemList = (List<String>) JSONPath.eval(response, "$.data.item_ids");
        return itemList;
    }

    @Override
    public MusicCreationResultDTO pureMusicQuery(String itemId) {
        HashMap<String, String> header = getHeader();
        HashMap<String, Object> params = new HashMap<>();
        params.put("item_ids", new String[]{itemId});
        String jsonParams = JsonUtils.convertObj2Json(params);

        String response = OKHttpUtils.postRequest4Json(appConfig.getTianpuyueApiDomain() + URL_QUERY_PURE_MUSIC,
                header,
                jsonParams);
        log.info("请求天谱乐api查询音乐返回结果：{}", response);
        // 支取第一个
        JSONObject jsonObject = (JSONObject) JSONPath.eval(response, "$.data.instrumentals[0]");

        Integer status = (Integer) JSONPath.eval(response, "$.status");
        return getMusicCreateResultDTO(status, jsonObject, MusicTypeEnum.PURE);
    }

    /**
     * 天谱乐回调api实现
     *
     * @param musicType 音乐类型
     * @param response  返回体
     * @return
     */
    @Override
    public MusicCreationResultDTO createMusicNotify(Integer musicType, String response) {
        MusicTypeEnum musicTypeEnum = MusicTypeEnum.getByType(musicType);
        Integer status = (Integer) JSONPath.eval(response, "$.status");

        if (MusicTypeEnum.MUSIC == musicTypeEnum) {
            JSONObject jsonObject = (JSONObject) JSONPath.eval(response, "$.data.songs[0]");
            return getMusicCreateResultDTO(status, jsonObject, MusicTypeEnum.MUSIC);
        } else if (MusicTypeEnum.PURE == musicTypeEnum) {
            JSONObject jsonObject = (JSONObject) JSONPath.eval(response, "$.data.instrumentals[0]");
            return getMusicCreateResultDTO(status, jsonObject, MusicTypeEnum.PURE);
        }
        return null;
    }
}
