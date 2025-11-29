package com.easymusic.api;

import com.easymusic.entity.dto.MusicCreationResultDTO;

import java.util.ArrayList;
import java.util.List;

/**
 * @author cyt
 * * @date 2025/11/28 18:08:55
 */
public interface MusicCreateApi {

    /**
     * 创建音乐
     *
     * @param model
     * @param prompt
     * @param lyrics
     * @return
     */
    default List<String> createMusic(String model, String prompt, String lyrics) {
        return new ArrayList<>();
    }

    /**
     * 查询音乐
     * 用于redis主动轮询方法
     *
     * @param taskId 音乐Id 天谱乐返回
     * @return
     */
    default MusicCreationResultDTO musicQuery(String taskId) {
        return null;
    }

    /**
     * 创建纯音乐
     *
     * @param model
     * @param prompt
     * @return
     */
    default List<String> createPureMusic(String model, String prompt) {
        return new ArrayList<>();
    }

    default MusicCreationResultDTO pureMusicQuery(String taskId) {
        return null;
    }

    /**
     * 创建音乐通知
     *
     * @param musicType    音乐类型
     * @param responseBody 返回体
     * @return
     */
    default MusicCreationResultDTO createMusicNotify(Integer musicType, String responseBody) {
        return null;
    }
}
