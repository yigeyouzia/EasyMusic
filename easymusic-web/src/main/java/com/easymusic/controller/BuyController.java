package com.easymusic.controller;

import com.easymusic.entity.enums.ProductOnSaleTypeEnum;
import com.easymusic.entity.po.ProductInfo;
import com.easymusic.entity.query.ProductInfoQuery;
import com.easymusic.entity.vo.ResponseVO;
import com.easymusic.service.ProductInfoService;
import jakarta.annotation.Resource;
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

    @RequestMapping("/loadProduct")
    public ResponseVO loadProduct() {
        ProductInfoQuery query = new ProductInfoQuery();
        query.setOrderBy("p.sort asc");
        query.setOnsaleType(ProductOnSaleTypeEnum.ON_SALE.getStatus());
        List<ProductInfo> res = productInfoService.findListByParam(query);
        return getSuccessResponseVO(res);
    }

    @RequestMapping("/getPayInfo")
    public ResponseVO getPayInfo() {
        return getSuccessResponseVO(null);
    }
}
