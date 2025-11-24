package com.easymusic.entity.enums;

import java.util.Arrays;
import java.util.Optional;

/**
 * @author cyt
 * * @date 2025/11/20 15:42:44
 */

// 订单状态枚举类
public enum PayOrderStatusEnum {

    TIME_OUT(-1, "订单超时"),
    NO_PAY(0, "未支付"),
    HAVE_PAY(1, "已支付");


    private Integer status;
    private String desc;

    PayOrderStatusEnum(Integer status, String desc) {
        this.status = status;
        this.desc = desc;
    }

    public Integer getStatus() {
        return status;
    }

    public String getDesc() {
        return desc;
    }

    public static PayOrderStatusEnum getByStatus(Integer status) {
        Optional<PayOrderStatusEnum> typeEnum = Arrays.stream(PayOrderStatusEnum.values())
                .filter(value -> value.getStatus().equals(status)).findFirst();
        return typeEnum.orElse(null);
    }
}
