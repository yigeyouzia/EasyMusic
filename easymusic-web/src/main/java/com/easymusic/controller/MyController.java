package com.easymusic.controller;

import com.easymusic.annotation.GlobalInterceptor;
import com.easymusic.entity.po.UserIntegralRecord;
import com.easymusic.entity.query.UserIntegralRecordQuery;
import com.easymusic.entity.vo.PaginationResultVO;
import com.easymusic.entity.vo.ResponseVO;
import com.easymusic.redis.RedisComponent;
import com.easymusic.service.PayOrderInfoService;
import com.easymusic.service.ProductInfoService;
import com.easymusic.service.UserIntegralRecordService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 我的页面控制器
 * @author cyt
 * * @date 2025/11/23 16:57:34
 */

@Slf4j
@RestController
@RequestMapping("/my")
@Validated
public class MyController extends ABaseController {

    @Resource
    private ProductInfoService productInfoService;

    @Resource
    private PayOrderInfoService payOrderInfoService;

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private UserIntegralRecordService userIntegralRecordService;

    @RequestMapping("/integralRecords")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO loadProduct(Integer page) {
        UserIntegralRecordQuery query = new UserIntegralRecordQuery();
        query.setUserId(getTokenUserInfo(null).getUserId());
        query.setPageNo(page);
        query.setOrderBy("record_id desc");
        PaginationResultVO<UserIntegralRecord> res = userIntegralRecordService.findListByPage(query);
        return getSuccessResponseVO(res);
    }
}
