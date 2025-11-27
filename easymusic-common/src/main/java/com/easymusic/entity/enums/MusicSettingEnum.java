package com.easymusic.entity.enums;

/**
 * @author cyt
 * * @date 2025/11/27 13:53:57
 */
public enum MusicSettingEnum {
    MUSIC_GENER("musicGener", "曲风"),
    MUSIC_EMOTION("musicEmotion", "情绪"),
    MUSIC_SEX("musicSex", "人声");

    private String keyCode;
    private String typeDesc;

    MusicSettingEnum(String keyCode, String typeDesc) {
        this.keyCode = keyCode;
        this.typeDesc = typeDesc;
    }

    public String getKeyCode() {
        return keyCode;
    }

    public String getTypeDesc() {
        return typeDesc;
    }
}
