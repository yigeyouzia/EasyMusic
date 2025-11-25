package com.easymusic.controller.notify;

import com.easymusic.service.PayOrderInfoService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * @author cyt
 * * @date 2025/11/24 19:30:26
 */


@RestController
@Slf4j
@RequestMapping("/payNotify")
public class PayNotifyController {

    @Resource
    private PayOrderInfoService payOrderInfoService;

    @RequestMapping("/payNotify4WeChat/{payType}")
    public ResponseEntity payNotify4WeChat(@PathVariable("payType") Integer payType,
                                           @RequestBody String body,
                                           @RequestHeader(value = "Wechatpay-Timestamp", required = false) String wechatpayTimestamp,
                                           @RequestHeader(value = "Wechatpay-Nonce", required = false) String wechatpayNonce,
                                           @RequestHeader(value = "Wechatpay-Signature", required = false) String wechatpaySignature) {

        try {

            log.info("微信回调返回信息：{}", body);
            Map<String, Object> params = new HashMap<>();
            params.put("wechatpayTimestamp", wechatpayTimestamp);
            params.put("wechatpayNonce", wechatpayNonce);
            params.put("wechatpaySignature", wechatpaySignature);
            payOrderInfoService.payNotify(payType, params, body);
            return new ResponseEntity(HttpStatus.OK);
        } catch (Exception e) {
            log.error("微信回调处理失败", e);
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

}
