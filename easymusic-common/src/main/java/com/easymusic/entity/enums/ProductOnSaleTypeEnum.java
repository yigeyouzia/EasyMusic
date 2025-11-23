package com.easymusic.entity.enums;

/**
 * @author cyt
 * * @date 2025/11/20 15:42:44
 */
public enum ProductOnSaleTypeEnum {

    OFF_SALE(0, "下架"),
    ON_SALE(1, "上架");


    private Integer type;
    private String desc;

    ProductOnSaleTypeEnum(Integer status, String desc) {
        this.type = status;
        this.desc = desc;
    }
    public Integer getStatus() {
        return type;
    }

    public String getDesc() {
        return desc;
    }
}
