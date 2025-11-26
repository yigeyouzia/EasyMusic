package com.easymusic.entity.enums;

/**
 * @author cyt
 * * @date 2025/11/20 15:42:44
 */

// 订单状态枚举类
public enum PayCodeStatusEnum {

    NO_USE(0, "未使用"),
    USED(1, "已使用");


    private Integer status;
    private String desc;

    PayCodeStatusEnum(Integer status, String desc) {
        this.status = status;
        this.desc = desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getStatus() {
        return status;
    }

    public String getDesc() {
        return desc;
    }

}
