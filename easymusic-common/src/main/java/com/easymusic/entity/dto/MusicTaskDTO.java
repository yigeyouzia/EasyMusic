package com.easymusic.entity.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

/**
 * 创建音乐返回DTO
 * @author cyt
 * * @date 2025/11/28 18:14:19
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class MusicTaskDTO implements Serializable {
    private String musicId;
    private String taskId;
    private String apiCode;
    private Integer musicType;

    public Integer getMusicType() {
        return musicType;
    }

    public void setMusicType(Integer musicType) {
        this.musicType = musicType;
    }

    public String getMusicId() {
        return musicId;
    }

    public void setMusicId(String musicId) {
        this.musicId = musicId;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getApiCode() {
        return apiCode;
    }

    public void setApiCode(String apiCode) {
        this.apiCode = apiCode;
    }
}