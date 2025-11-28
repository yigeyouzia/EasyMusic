package com.easymusic.entity.dto;

import java.util.List;

/**
 * 天谱乐api查询音乐返回结果
 *
 * @author cyt
 * * @date 2025/11/28 19:07:16
 */
public class MusicCreationResultDTO {
    private String taskId;
    private String title;
    private Integer duration;
    private String audioUrl;
    private String audioHiUrl;
    private List<Lyrics> lyricsList;

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAudioUrl() {
        return audioUrl;
    }

    public void setAudioUrl(String audioUrl) {
        this.audioUrl = audioUrl;
    }

    public String getAudioHiUrl() {
        return audioHiUrl;
    }

    public void setAudioHiUrl(String audioHiUrl) {
        this.audioHiUrl = audioHiUrl;
    }

    public List<Lyrics> getLyricsList() {
        return lyricsList;
    }

    public void setLyricsList(List<Lyrics> lyricsList) {
        this.lyricsList = lyricsList;
    }

    public class Lyrics {
        private double start;
        private double end;
        private String text;

        public double getStart() {
            return start;
        }

        public void setStart(double start) {
            this.start = start;
        }

        public double getEnd() {
            return end;
        }

        public void setEnd(double end) {
            this.end = end;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }
    }

}
