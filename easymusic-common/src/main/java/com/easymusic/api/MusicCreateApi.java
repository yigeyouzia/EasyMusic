package com.easymusic.api;

import com.easymusic.entity.dto.MusicCreateResultDTO;

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
     *
     * @param itemId 音乐Id
     * @return
     */
    default MusicCreateResultDTO musicQuery(String itemId) {
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

    default MusicCreateResultDTO pureMusicQuery(String itemId) {
        return null;
    }

    /**
     * 创建音乐通知
     *
     * @param musicType    音乐类型
     * @param responseBody 返回体
     * @return
     */
    default MusicCreateResultDTO createMusicNotify(Integer musicType, String responseBody) {
        return null;
    }
}
