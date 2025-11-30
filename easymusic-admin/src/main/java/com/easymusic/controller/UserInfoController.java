package com.easymusic.controller;

import com.easymusic.entity.enums.UserIntegralRecordTypeEnum;
import com.easymusic.entity.po.UserInfo;
import com.easymusic.entity.query.UserInfoQuery;
import com.easymusic.entity.vo.PaginationResultVO;
import com.easymusic.entity.vo.ResponseVO;
import com.easymusic.service.UserInfoService;
import com.easymusic.service.UserIntegralRecordService;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户信息 Controller
 */
@RestController
@RequestMapping("/user")
@Slf4j
@Validated
public class UserInfoController extends ABaseController {


    @Resource
    private UserInfoService userInfoService;

    @Resource
    private UserIntegralRecordService userIntegralRecordService;

    @RequestMapping("/loadUser")
    public ResponseVO loadUser(UserInfoQuery query) {
        query.setOrderBy("u.create_time desc");
        PaginationResultVO<UserInfo> res = userInfoService.findListByPage(query);
        return getSuccessResponseVO(res);
    }

    @RequestMapping("/changeUserStatus")
    public ResponseVO changeUserStatus(@NotEmpty String userId, @NotNull Integer status) {
        UserInfo updateInfo = new UserInfo();
        updateInfo.setStatus(status);
        userInfoService.updateUserInfoByUserId(updateInfo, userId);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/changeIntegral")
    public ResponseVO changeIntegral(@NotEmpty String userId, @NotNull Integer integral) {
        UserIntegralRecordTypeEnum type = integral > 0 ? UserIntegralRecordTypeEnum.ADMIN_ADD :
                UserIntegralRecordTypeEnum.ADMIN_DEDUCT;

        userIntegralRecordService.changeUserIntegral(type, null, userId, integral, null);
        return getSuccessResponseVO(null);
    }

}