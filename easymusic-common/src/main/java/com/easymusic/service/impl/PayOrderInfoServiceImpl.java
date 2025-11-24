package com.easymusic.service.impl;

import com.easymusic.entity.constants.Constants;
import com.easymusic.entity.dto.PayInfoDTO;
import com.easymusic.entity.dto.PayOrderNotifyDTO;
import com.easymusic.entity.dto.TokenUserInfoDTO;
import com.easymusic.entity.enums.*;
import com.easymusic.entity.po.PayOrderInfo;
import com.easymusic.entity.po.ProductInfo;
import com.easymusic.entity.query.PayOrderInfoQuery;
import com.easymusic.entity.query.ProductInfoQuery;
import com.easymusic.entity.query.SimplePage;
import com.easymusic.entity.vo.PaginationResultVO;
import com.easymusic.exception.BusinessException;
import com.easymusic.mappers.PayOrderInfoMapper;
import com.easymusic.mappers.ProductInfoMapper;
import com.easymusic.service.PayChannelService;
import com.easymusic.service.PayOrderInfoService;
import com.easymusic.spring.SpringContext;
import com.easymusic.utils.DateUtil;
import com.easymusic.utils.StringTools;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 * 支付订单信息 业务接口实现
 */
@Service("payOrderInfoService")
@Slf4j
public class PayOrderInfoServiceImpl implements PayOrderInfoService {

    private static final String CURRENCY = "CNY"; // 币种

    @Resource
    private PayOrderInfoMapper<PayOrderInfo, PayOrderInfoQuery> payOrderInfoMapper;

    @Resource
    private ProductInfoMapper<ProductInfo, ProductInfoQuery> productInfoMapper;

    /**
     * 根据条件查询列表
     */
    @Override
    public List<PayOrderInfo> findListByParam(PayOrderInfoQuery param) {
        return this.payOrderInfoMapper.selectList(param);
    }

    /**
     * 根据条件查询列表
     */
    @Override
    public Integer findCountByParam(PayOrderInfoQuery param) {
        return this.payOrderInfoMapper.selectCount(param);
    }

    /**
     * 分页查询方法
     */
    @Override
    public PaginationResultVO<PayOrderInfo> findListByPage(PayOrderInfoQuery param) {
        int count = this.findCountByParam(param);
        int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

        SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
        param.setSimplePage(page);
        List<PayOrderInfo> list = this.findListByParam(param);
        PaginationResultVO<PayOrderInfo> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
        return result;
    }

    /**
     * 新增
     */
    @Override
    public Integer add(PayOrderInfo bean) {
        return this.payOrderInfoMapper.insert(bean);
    }

    /**
     * 批量新增
     */
    @Override
    public Integer addBatch(List<PayOrderInfo> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.payOrderInfoMapper.insertBatch(listBean);
    }

    /**
     * 批量新增或者修改
     */
    @Override
    public Integer addOrUpdateBatch(List<PayOrderInfo> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.payOrderInfoMapper.insertOrUpdateBatch(listBean);
    }

    /**
     * 多条件更新
     */
    @Override
    public Integer updateByParam(PayOrderInfo bean, PayOrderInfoQuery param) {
        StringTools.checkParam(param);
        return this.payOrderInfoMapper.updateByParam(bean, param);
    }

    /**
     * 多条件删除
     */
    @Override
    public Integer deleteByParam(PayOrderInfoQuery param) {
        StringTools.checkParam(param);
        return this.payOrderInfoMapper.deleteByParam(param);
    }

    /**
     * 根据OrderId获取对象
     */
    @Override
    public PayOrderInfo getPayOrderInfoByOrderId(String orderId) {
        return this.payOrderInfoMapper.selectByOrderId(orderId);
    }

    /**
     * 根据OrderId修改
     */
    @Override
    public Integer updatePayOrderInfoByOrderId(PayOrderInfo bean, String orderId) {
        return this.payOrderInfoMapper.updateByOrderId(bean, orderId);
    }

    /**
     * 根据OrderId删除
     */
    @Override
    public Integer deletePayOrderInfoByOrderId(String orderId) {
        return this.payOrderInfoMapper.deleteByOrderId(orderId);
    }

    /**
     * 1. 进入支付页面， 扫码支付， 返回支付信息
     *
     * @param tokenUserInfoDTO
     * @param productId
     * @param payType
     * @return
     */
    @Override
    public PayInfoDTO getPayInfo(TokenUserInfoDTO tokenUserInfoDTO, String productId, Integer payType) {
        // 校验商品是否存在
        ProductInfo productInfo = productInfoMapper.selectByProductId(productId);
        if (productInfo == null || productInfo.getOnsaleType().equals(ProductOnSaleTypeEnum.OFF_SALE.getType())) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }

        // 校验支付方式
        PayOrderTypeEnum payOrderTypeEnum = PayOrderTypeEnum.getByType(payType);
        if (payOrderTypeEnum == null || PayOrderTypeEnum.PAY_CODE == payOrderTypeEnum) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }

        // 构造订单信息
        String orderId = getOrderId();
        PayOrderInfo payOrderInfo = new PayOrderInfo();
        // TODO 调用微信支付 先调微信 再选择入库（具体看业务场景）
        String beanName = payOrderTypeEnum.getBeanName();
        PayChannelService payChannelService = (PayChannelService) SpringContext.getBean(beanName);

        String payUrl = payChannelService.getPayUrl(orderId, productInfo.getPrice(), productInfo.getProductName());
        payOrderInfo.setOrderId(orderId);
        payOrderInfo.setCreateTime(new Date());
        productInfo.setIntegral(productInfo.getIntegral()); // 积分
        payOrderInfo.setUserId(tokenUserInfoDTO.getUserId());
        payOrderInfo.setAmount(productInfo.getPrice());
        payOrderInfo.setProductId(productId);
        payOrderInfo.setProductName(productInfo.getProductName());
        payOrderInfo.setStatus(PayOrderStatusEnum.NO_PAY.getStatus()); // 待支付
        payOrderInfo.setPayInfo(payUrl);

        payOrderInfoMapper.insert(payOrderInfo);

        // 返给前端支付信息
        PayInfoDTO payInfoDTO = new PayInfoDTO();
        payInfoDTO.setOrderId(orderId);
        payInfoDTO.setPayUrl(payUrl);

        return payInfoDTO;
    }

    private String getOrderId() {
        //  生成订单号 30位
        return DateUtil.format(new Date(),
                DateTimePatternEnum.YYYYMMDDHHMMSS.getPattern() +
                        StringTools.getRandomNumber(Constants.LENGTH_14).toLowerCase());
    }


    @Override
    public void payNotify(Integer payType, Map<String, Object> params, String body) {
        PayOrderTypeEnum payOrderTypeEnum = PayOrderTypeEnum.getByType(payType);

        String beanName = payOrderTypeEnum.getBeanName();
        PayChannelService payChannelService = (PayChannelService) SpringContext.getBean(beanName);
        // 返回easyMusic 订单id 和微信订单id
        PayOrderNotifyDTO payOrderNotifyDTO = payChannelService.checkPayNotify(params, body);

        // 订单信息
        PayOrderInfo payOrderInfo = payOrderInfoMapper.selectByOrderId(payOrderNotifyDTO.getOrderId());
        if (payOrderInfo == null) {
            throw new BusinessException("支付回调失败， 订单" + payOrderNotifyDTO.getOrderId() + "不存在");
        }
        // TODO 更新用户积分

    }
}