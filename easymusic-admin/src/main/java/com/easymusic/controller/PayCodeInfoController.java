package com.easymusic.controller;

import com.easymusic.entity.constants.Constants;
import com.easymusic.entity.enums.PayCodeStatusEnum;
import com.easymusic.entity.po.PayCodeInfo;
import com.easymusic.entity.query.PayCodeInfoQuery;
import com.easymusic.entity.vo.PaginationResultVO;
import com.easymusic.entity.vo.ResponseVO;
import com.easymusic.service.PayCodeInfoService;
import com.easymusic.utils.StringTools;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author cyt
 * * @date 2025/11/26 13:14:01
 */

@RestController
@RequestMapping("/payCode")
@Slf4j
@Validated
public class PayCodeInfoController extends ABaseController {

    @Resource
    private PayCodeInfoService payCodeInfoService;

    @RequestMapping("/loadPayCodeList")
    public ResponseVO loadPayCodeList(PayCodeInfoQuery query) {
        query.setOrderBy("create_time desc");
        query.setQueryUser(true);
        PaginationResultVO<PayCodeInfo> res = payCodeInfoService.findListByPage(query);
        return getSuccessResponseVO(res);
    }

    @RequestMapping("/createCode")
    public ResponseVO createCode(@NotNull BigDecimal amount) {
        PayCodeInfo bean = new PayCodeInfo();
        String payCode = StringTools.getRandomNumber(Constants.LENGTH_8);
        bean.setAmount(amount);
        bean.setPayCode(payCode);
        bean.setStatus(PayCodeStatusEnum.NO_USE.getStatus());
        bean.setCreateTime(new Date());

        payCodeInfoService.add(bean);
        return getSuccessResponseVO(payCode);
    }

    @RequestMapping("/delCode")
    public ResponseVO deleteCode(@NotEmpty String payCode) {
        payCodeInfoService.deletePayCodeInfoByPayCode(payCode);
        return getSuccessResponseVO(null);
    }


}
