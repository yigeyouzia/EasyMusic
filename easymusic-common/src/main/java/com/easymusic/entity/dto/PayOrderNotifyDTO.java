package com.easymusic.entity.dto;

/**
 * @author cyt
 * * @date 2025/11/24 17:07:52
 */
public class PayOrderNotifyDTO {
    /**
     * 订单号 easyMusic订单号
     */
    private String orderId;
    /**
     * 支付订单号 通道订单号 微信订单号
     */
    private String channelOrderId;


    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getChannelOrderId() {
        return channelOrderId;
    }

    public void setChannelOrderId(String channelOrderId) {
        this.channelOrderId = channelOrderId;
    }
}