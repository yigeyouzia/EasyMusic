package com.easymusic.controller;

import com.easymusic.annotation.GlobalInterceptor;
import com.easymusic.entity.dto.PayInfoDTO;
import com.easymusic.entity.dto.TokenUserInfoDTO;
import com.easymusic.entity.enums.ProductOnSaleTypeEnum;
import com.easymusic.entity.po.ProductInfo;
import com.easymusic.entity.query.ProductInfoQuery;
import com.easymusic.entity.vo.ResponseVO;
import com.easymusic.exception.BusinessException;
import com.easymusic.redis.RedisComponent;
import com.easymusic.service.PayOrderInfoService;
import com.easymusic.service.ProductInfoService;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author cyt
 * * @date 2025/11/23 16:57:34
 */

@Slf4j
@RestController
@RequestMapping("/buy")
@Validated
public class BuyController extends ABaseController {

    @Resource
    private ProductInfoService productInfoService;

    @Resource
    private PayOrderInfoService payOrderInfoService;

    @Resource
    private RedisComponent redisComponent;

    @RequestMapping("/loadProduct")
    public ResponseVO loadProduct() {
        ProductInfoQuery query = new ProductInfoQuery();
        query.setOrderBy("p.sort asc");
        query.setOnsaleType(ProductOnSaleTypeEnum.ON_SALE.getType());
        List<ProductInfo> res = productInfoService.findListByParam(query);
        return getSuccessResponseVO(res);
    }

    @RequestMapping("/getPayInfo")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO getPayInfo(@NotEmpty String productId, @NotNull Integer payType) {
        TokenUserInfoDTO tokenUserInfo = getTokenUserInfo(null);
        PayInfoDTO res = payOrderInfoService.getPayInfo(tokenUserInfo, productId, payType);
        return getSuccessResponseVO(res);
    }

    /**
     * 用户个人轮询是否已经支付
     *
     * @param orderId
     * @return 用户积分
     */
    @RequestMapping("/checkPayOrder")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO checkPayOrder(@NotEmpty String orderId) {
        TokenUserInfoDTO tokenUserInfo = getTokenUserInfo(null);
        Integer res = payOrderInfoService.checkPay(tokenUserInfo.getUserId(), orderId);
        if (res == null) {
            return getSuccessResponseVO(null);
        }
        tokenUserInfo.setIntegral(res);
        return getSuccessResponseVO(tokenUserInfo);
    }

    /**
     * 付款码支付
     *
     * @param checkCodeKey 验证码的key
     * @param checkCode 验证码
     * @param payCode 付款码 支付码
     * @param productId 商品id
     * @return
     */
    @RequestMapping("/buyByPayCode")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO buyByPayCode(@NotEmpty String checkCodeKey,
                                   @NotEmpty String checkCode,
                                   @NotEmpty String payCode,
                                   @NotEmpty String productId) {
        try {
            if (!checkCode.equals(redisComponent.getCheckCode(checkCodeKey))) {
                throw new BusinessException("验证码错误");
            }
            TokenUserInfoDTO tokenUserInfo = getTokenUserInfo(null);
            payOrderInfoService.buyByPayCode(productId, payCode, tokenUserInfo.getUserId());
            return getSuccessResponseVO(null);
        } finally {
            redisComponent.cleanCheckCode(checkCodeKey);
        }
    }
}
