package com.easymusic.controller;

import com.easymusic.entity.po.ProductInfo;
import com.easymusic.entity.query.ProductInfoQuery;
import com.easymusic.entity.vo.PaginationResultVO;
import com.easymusic.entity.vo.ResponseVO;
import com.easymusic.service.ProductInfoService;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotEmpty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 用户信息 Controller
 */
@RestController
@RequestMapping("/product")
@Slf4j
@Validated
public class ProductInfoController extends ABaseController {

    @Resource
    private ProductInfoService productInfoService;

    @RequestMapping("/loadProduct")
    public ResponseVO loadProduct() {
        ProductInfoQuery query = new ProductInfoQuery();
        query.setOrderBy("p.sort asc");
        PaginationResultVO<ProductInfo> res = productInfoService.findListByPage(query);
        return getSuccessResponseVO(res);
    }

    @RequestMapping("/saveProduct")
    public ResponseVO saveProduct(MultipartFile coverFile, ProductInfo productInfo) {
        productInfoService.saveProduct(coverFile, productInfo);
        return getSuccessResponseVO(null);
    }


    /**
     * 修改排序
     *
     * @param productIds
     * @return
     */
    @RequestMapping("/changeProductSort")
    public ResponseVO changeProductSort(@NotEmpty String productIds) {
        productInfoService.changeProductSort(productIds);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/delProduct")
    public ResponseVO delProduct(@NotEmpty String productId) {
        productInfoService.deleteProductInfoByProductId(productId);
        return getSuccessResponseVO(null);
    }

}