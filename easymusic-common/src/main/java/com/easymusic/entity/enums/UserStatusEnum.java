package com.easymusic.entity.enums;

/**
 * @author cyt
 * * @date 2025/11/19 15:20:11
 */

public enum UserStatusEnum {

    DISABLE(0, "禁用"),
    ENABLE(1, "启用");


    private Integer status;
    private String desc;

    UserStatusEnum(Integer status, String desc) {
        this.status = status;
        this.desc = desc;
    }
    public Integer getStatus() {
        return status;
    }

    public String getDesc() {
        return desc;
    }
}
