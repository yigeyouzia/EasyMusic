package com.easymusic.service.impl;

import com.easymusic.entity.config.AppConfig;
import com.easymusic.entity.constants.Constants;
import com.easymusic.entity.dto.PayInfoDTO;
import com.easymusic.entity.dto.PayOrderNotifyDTO;
import com.easymusic.entity.dto.TokenUserInfoDTO;
import com.easymusic.entity.enums.*;
import com.easymusic.entity.po.PayCodeInfo;
import com.easymusic.entity.po.PayOrderInfo;
import com.easymusic.entity.po.ProductInfo;
import com.easymusic.entity.po.UserInfo;
import com.easymusic.entity.query.PayCodeInfoQuery;
import com.easymusic.entity.query.PayOrderInfoQuery;
import com.easymusic.entity.query.ProductInfoQuery;
import com.easymusic.entity.query.SimplePage;
import com.easymusic.entity.vo.PaginationResultVO;
import com.easymusic.exception.BusinessException;
import com.easymusic.mappers.PayCodeInfoMapper;
import com.easymusic.mappers.PayOrderInfoMapper;
import com.easymusic.mappers.ProductInfoMapper;
import com.easymusic.redis.RedisComponent;
import com.easymusic.service.PayChannelService;
import com.easymusic.service.PayOrderInfoService;
import com.easymusic.service.UserInfoService;
import com.easymusic.service.UserIntegralRecordService;
import com.easymusic.spring.SpringContext;
import com.easymusic.utils.DateUtil;
import com.easymusic.utils.StringTools;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;


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

    @Resource
    private AppConfig appConfig;

    @Resource
    private UserIntegralRecordService userIntegralRecordService;


    @Resource
    private RedisComponent redisComponent;

    @Resource
    private UserInfoService userInfoService;

    @Resource
    private PayCodeInfoMapper<PayCodeInfo, PayCodeInfoQuery> payCodeInfoMapper;

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
        payOrderInfo.setIntegral(productInfo.getIntegral()); // 积分
        payOrderInfo.setUserId(tokenUserInfoDTO.getUserId());
        payOrderInfo.setAmount(productInfo.getPrice());
        payOrderInfo.setProductId(productId);
        payOrderInfo.setProductName(productInfo.getProductName());
        payOrderInfo.setStatus(PayOrderStatusEnum.NO_PAY.getStatus()); // 待支付
        payOrderInfo.setPayInfo(payUrl);
        payOrderInfo.setPayType(payType);
        payOrderInfoMapper.insert(payOrderInfo);

        // 将订单放入延时队列 从11分钟开始，二维码失效时间是10分钟
        redisComponent.addOrder2DelayQueue(Constants.ORDER_TIMEOUT_MIN + 1, orderId);

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


    /**
     * 前端主动轮询订单支付状态
     *
     * @param payType 支付方式
     * @param params
     * @param body
     */
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
        payOrderInfoSuccess(payOrderInfo, payOrderNotifyDTO.getChannelOrderId());

    }

    private void payOrderInfoSuccess(PayOrderInfo payOrderInfo, String channelOrderId) {
        if (payOrderInfo.getStatus().equals(PayOrderStatusEnum.HAVE_PAY.getStatus())) {
            return;
        }

        PayOrderInfo updateInfo = new PayOrderInfo();
        updateInfo.setStatus(PayOrderStatusEnum.HAVE_PAY.getStatus());
        updateInfo.setChannelOrderId(channelOrderId);
        updateInfo.setPayTime(new Date());

        // 乐观锁
        PayOrderInfoQuery query = new PayOrderInfoQuery();
        query.setOrderId(payOrderInfo.getOrderId());
        query.setStatus(PayOrderStatusEnum.NO_PAY.getStatus());
        Integer updateCount = payOrderInfoMapper.updateByParam(updateInfo, query);
        if (updateCount == 0) {
            throw new BusinessException("订单" + payOrderInfo.getOrderId() + "已支付" + " 支付订单更新失败");
        }
        // 更新用户积分
        userIntegralRecordService.changeUserIntegral(UserIntegralRecordTypeEnum.RECHARGE,
                payOrderInfo.getOrderId(),
                payOrderInfo.getUserId(),
                payOrderInfo.getIntegral(),
                payOrderInfo.getAmount());
        // 将已支付的订单放入缓存
        redisComponent.cacheHavePayOrder(payOrderInfo.getOrderId());
    }

    @Override
    public Integer checkPay(String userId, String orderId) {
        String havePayOrder = redisComponent.getHavePayOrder(orderId);
        if (StringTools.isEmpty(havePayOrder)) {
            return null;
        }

        UserInfo userInfo = userInfoService.getUserInfoByUserId(userId);
        return userInfo.getIntegral();
    }

    /**
     * 定时任务， 自动查询未支付订单
     */
    @PostConstruct
    public void checkPayOrder() {
        if (!appConfig.getAutoCheckPay()) {
            return;
        }

        ExecutorServiceSingletonEnum.INSTANCE.getExecutorService().execute(() -> {
            // 查询未支付订单
            while (true) {
                try {
                    // 找到订单表中状态为待支付的订单
                    PayOrderInfoQuery query = new PayOrderInfoQuery();
                    query.setStatus(PayOrderStatusEnum.NO_PAY.getStatus());
                    List<PayOrderInfo> payOrderInfoList = payOrderInfoMapper.selectList(query);
                    for (PayOrderInfo payOrderInfo : payOrderInfoList) {
                        // 调用支付渠道接口
                        PayOrderTypeEnum payOrderTypeEnum = PayOrderTypeEnum.getByType(payOrderInfo.getPayType());
                        String beanName = payOrderTypeEnum.getBeanName();
                        PayChannelService payChannelService = (PayChannelService) SpringContext.getBean(beanName);
                        // 主动查询微信接口  是否完成支付
                        PayOrderNotifyDTO payOrderNotifyDTO = payChannelService.queryOrder(payOrderInfo.getOrderId());
                        // 支付没有支付订单id
                        if (payOrderNotifyDTO == null) {
                            continue;
                        }
                        // 已经完成支付
                        // 更新用户积分
                        payOrderInfoSuccess(payOrderInfo, payOrderNotifyDTO.getChannelOrderId());
                        Thread.sleep(10000);
                    }
                    Thread.sleep(10000);
                } catch (Exception e) {
                    log.error("查询未支付订单失败", e);
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException ex) {
                        log.error("休眠失败", ex);
                    }
                }
            }
        });
    }

    /**
     * 定时任务， 延时队列 订单超时
     */
    @PostConstruct
    public void consumeDelayOrder() {
        ExecutorServiceSingletonEnum.INSTANCE.getExecutorService().execute(() -> {
            // 查询未支付订单
            while (true) {
                try {
                    Set<String> queueOrderList = redisComponent.getTimeOutOrder();
                    if (queueOrderList == null || queueOrderList.isEmpty()) {
                        Thread.sleep(10000);
                        continue;
                    }
                    for (String orderId : queueOrderList) {
                        if (redisComponent.removeTimeOutOrder(orderId) > 0) {
                            // 先移除 若有
                            PayOrderInfo payOrderInfo = payOrderInfoMapper.selectByOrderId(orderId);
                            if (payOrderInfo.getStatus().equals(PayOrderStatusEnum.HAVE_PAY.getStatus())) {
                                continue;
                            }
                            // 订单超时
                            PayOrderInfo updateInfo = new PayOrderInfo();
                            updateInfo.setStatus(PayOrderStatusEnum.TIME_OUT.getStatus());

                            PayOrderInfoQuery query = new PayOrderInfoQuery();
                            query.setOrderId(payOrderInfo.getOrderId());
                            query.setStatus(PayOrderStatusEnum.NO_PAY.getStatus());
                            payOrderInfoMapper.updateByParam(updateInfo, query);
                        }
                    }

                } catch (Exception e) {
                    log.error("查询未支付订单失败", e);
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException ex) {
                        log.error("休眠失败", ex);
                    }
                }
            }
        });
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void buyByPayCode(String productId, String payCode, String userId) {
        // 判断商品是否存在
        PayCodeInfo payCodeInfo = payCodeInfoMapper.selectByPayCode(payCode);
        if (payCodeInfo == null) {
            throw new BusinessException("支付码不正确或已过期或者已使用");
        }

        if (PayCodeStatusEnum.USED.getStatus().equals(payCodeInfo.getStatus()) ||
                System.currentTimeMillis() - payCodeInfo.getCreateTime().getTime() > 1000 * 60 * 30) {
            throw new BusinessException("支付码不正确或已过期或者已使用");
        }

        // 商品是否存在 在售
        ProductInfo productInfo = productInfoMapper.selectByProductId(productId);
        if (productInfo == null || !productInfo.getOnsaleType().equals(ProductOnSaleTypeEnum.ON_SALE.getType())) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }

        if (productInfo.getPrice().compareTo(payCodeInfo.getAmount()) != 0){
            throw new BusinessException("支付码金额与商品金额不一致");
        }

        // 插入支付信息
        Date curDate = new Date();
        String orderId = getOrderId();
        PayOrderInfo payOrderInfo = new PayOrderInfo();
        payOrderInfo.setOrderId(orderId);
        payOrderInfo.setCreateTime(curDate);
        payOrderInfo.setIntegral(productInfo.getIntegral()); // 积分
        payOrderInfo.setUserId(userId);
        payOrderInfo.setAmount(productInfo.getPrice());
        payOrderInfo.setProductId(productId);
        payOrderInfo.setProductName(productInfo.getProductName());
        payOrderInfo.setStatus(PayOrderStatusEnum.HAVE_PAY.getStatus()); // 待支付
        payOrderInfo.setPayType(PayOrderTypeEnum.PAY_CODE.getType());
        payOrderInfo.setPayTime(curDate);
        payOrderInfoMapper.insert(payOrderInfo);

        // 更新支付码状态
        PayCodeInfo updateInfo = new PayCodeInfo();
        updateInfo.setStatus(PayCodeStatusEnum.USED.getStatus());
        updateInfo.setUseUserId(userId);
        updateInfo.setUseTime(curDate);
        PayCodeInfoQuery query = new PayCodeInfoQuery();
        query.setPayCode(payCode);
        query.setStatus(PayCodeStatusEnum.NO_USE.getStatus());

        Integer count = payCodeInfoMapper.updateByParam(updateInfo, query);
        if (count == 0) {
            throw new BusinessException("支付码支付失败");
        }

        userIntegralRecordService.changeUserIntegral(UserIntegralRecordTypeEnum.RECHARGE,
                orderId,
                userId,
                productInfo.getIntegral(),
                productInfo.getPrice());

    }
}