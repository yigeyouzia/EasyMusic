package com.easymusic.entity.dto;

import lombok.Data;

/**
 * @author cyt
 * * @date 2025/11/27 13:26:42
 */

@Data
public class MusicSettingDTO {
    // 曲风
    private String musicGener;
    // 情绪
    private String musicEmotion;
    // 性别
    private String musicSex;
}
