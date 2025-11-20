package com.easymusic.entity.enums;

/**
 * @author cyt
 * * @date 2025/11/20 15:42:44
 */
public enum CommendTypeEnum {

    NOT_COMMEND(0, "未推荐"),
    COMMEND(1, "已推荐");


    private Integer status;
    private String desc;

    CommendTypeEnum(Integer status, String desc) {
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
