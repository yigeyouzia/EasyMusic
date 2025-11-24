package com.easymusic.service;

import com.easymusic.entity.dto.PayOrderNotifyDTO;

import java.math.BigDecimal;
import java.util.Map;

/**
 * @author cyt
 * * @date 2025/11/24 17:07:37
 */
public interface PayChannelService {

    /**
     * 获取支付信息
     *
     * @param orderId
     * @param amount
     * @param productName
     * @return
     */
    String getPayUrl(String orderId, BigDecimal amount, String productName);

    /**
     * 校验支付回调
     *
     * @param params
     * @param jsonBody
     * @return
     */
    PayOrderNotifyDTO checkPayNotify(Map<String, Object> params, String jsonBody);


    /**
     * 查询订单
     *
     * @param orderId
     * @return
     */
    PayOrderNotifyDTO queryOrder(String orderId);

}
