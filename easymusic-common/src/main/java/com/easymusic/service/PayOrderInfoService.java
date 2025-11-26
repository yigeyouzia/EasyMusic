package com.easymusic.service;

import com.easymusic.entity.dto.PayInfoDTO;
import com.easymusic.entity.dto.TokenUserInfoDTO;
import com.easymusic.entity.po.PayOrderInfo;
import com.easymusic.entity.query.PayOrderInfoQuery;
import com.easymusic.entity.vo.PaginationResultVO;

import java.util.List;
import java.util.Map;


/**
 * 支付订单信息 业务接口
 */
public interface PayOrderInfoService {

    /**
     * 根据条件查询列表
     */
    List<PayOrderInfo> findListByParam(PayOrderInfoQuery param);

    /**
     * 根据条件查询列表
     */
    Integer findCountByParam(PayOrderInfoQuery param);

    /**
     * 分页查询
     */
    PaginationResultVO<PayOrderInfo> findListByPage(PayOrderInfoQuery param);

    /**
     * 新增
     */
    Integer add(PayOrderInfo bean);

    /**
     * 批量新增
     */
    Integer addBatch(List<PayOrderInfo> listBean);

    /**
     * 批量新增/修改
     */
    Integer addOrUpdateBatch(List<PayOrderInfo> listBean);

    /**
     * 多条件更新
     */
    Integer updateByParam(PayOrderInfo bean, PayOrderInfoQuery param);

    /**
     * 多条件删除
     */
    Integer deleteByParam(PayOrderInfoQuery param);

    /**
     * 根据OrderId查询对象
     */
    PayOrderInfo getPayOrderInfoByOrderId(String orderId);


    /**
     * 根据OrderId修改
     */
    Integer updatePayOrderInfoByOrderId(PayOrderInfo bean, String orderId);


    /**
     * 根据OrderId删除
     */
    Integer deletePayOrderInfoByOrderId(String orderId);


    /**
     * 返回待支付订单信息
     *
     * @param productId
     * @param payType
     * @return
     */
    PayInfoDTO getPayInfo(TokenUserInfoDTO tokenUserInfoDTO, String productId, Integer payType);

    /**
     * 支付回调 验签 成功后 操作数据库
     *
     * @param payType 支付方式
     * @param params
     * @param body
     */
    void payNotify(Integer payType, Map<String, Object> params, String body);

    /**
     * 订单是否支付
     *
     * @param userId
     * @param orderId
     * @return 用户积分
     */
    Integer checkPay(String userId, String orderId);

    void buyByPayCode(String productId, String payCode, String userId);
}