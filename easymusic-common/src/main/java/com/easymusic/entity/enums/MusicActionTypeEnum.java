package com.easymusic.entity.enums;

/**
 * @author cyt
 * * @date 2025/11/20 16:06:32
 */
public enum MusicActionTypeEnum {
    GOOD(1, "点赞");

    private Integer type;
    private String desc;

    MusicActionTypeEnum(Integer status, String desc) {
        this.type = status;
        this.desc = desc;
    }

    public Integer getType() {
        return type;
    }

    public String getDesc() {
        return desc;
    }
}
