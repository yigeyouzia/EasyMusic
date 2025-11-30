package com.easymusic.controller;

import com.easymusic.entity.po.PayOrderInfo;
import com.easymusic.entity.query.PayOrderInfoQuery;
import com.easymusic.entity.vo.PaginationResultVO;
import com.easymusic.entity.vo.ResponseVO;
import com.easymusic.service.PayOrderInfoService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户信息 Controller
 */
@RestController
@RequestMapping("/order")
@Slf4j
@Validated
public class PayOrderController extends ABaseController {


    @Resource
    private PayOrderInfoService payOrderInfoService;

    @RequestMapping("/loadOrder")
    public ResponseVO loadOrder(PayOrderInfoQuery query) {
        query.setOrderBy("p.create_time desc");
        query.setQueryUser(true);
        PaginationResultVO<PayOrderInfo> res = payOrderInfoService.findListByPage(query);
        return getSuccessResponseVO(res);
    }

}