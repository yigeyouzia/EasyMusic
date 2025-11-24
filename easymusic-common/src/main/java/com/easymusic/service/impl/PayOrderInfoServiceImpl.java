package com.easymusic.service.impl;

import com.easymusic.entity.constants.Constants;
import com.easymusic.entity.dto.PayInfoDTO;
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
import com.easymusic.service.PayOrderInfoService;
import com.easymusic.utils.DateUtil;
import com.easymusic.utils.StringTools;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import okhttp3.HttpUrl;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.List;


/**
 * 支付订单信息 业务接口实现
 */
@Service("payOrderInfoService")
@Slf4j
public class PayOrderInfoServiceImpl implements PayOrderInfoService {

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
        PayOrderTypeEnum payOrderType = PayOrderTypeEnum.getByType(payType);
        if (payOrderType == null || PayOrderTypeEnum.PAY_CODE == payOrderType) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }

        // 构造订单信息
        String orderId = getOrderId();
        PayOrderInfo payOrderInfo = new PayOrderInfo();
        // TODO 调用微信支付 先调微信 再选择入库（具体看业务场景）
        payOrderInfo.setOrderId(orderId);
        payOrderInfo.setCreateTime(new Date());
        productInfo.setIntegral(productInfo.getIntegral()); // 积分
        payOrderInfo.setUserId(tokenUserInfoDTO.getUserId());
        payOrderInfo.setAmount(productInfo.getPrice());
        payOrderInfo.setProductId(productId);
        payOrderInfo.setProductName(productInfo.getProductName());
        payOrderInfo.setStatus(PayOrderStatusEnum.NO_PAY.getStatus()); // 待支付

        payOrderInfoMapper.insert(payOrderInfo);

        // 返给前端支付信息
        PayInfoDTO payInfoDTO = new PayInfoDTO();
        payInfoDTO.setOrderId(orderId);
        payInfoDTO.setPayUrl("");

        return payInfoDTO;
    }

    private String getOrderId() {
        //  生成订单号 30位
        return DateUtil.format(new Date(),
                DateTimePatternEnum.YYYYMMDDHHMMSS.getPattern() +
                        StringTools.getRandomNumber(Constants.LENGTH_14).toLowerCase());
    }

    public static void main(String[] args) {
        String merchantId = "1900007291";
        String cerSerialNo = "408B07E79B8269FEC3D5D3E6AB8ED163A6A380DB";
        String sign = getToken4WeChat("POST", "https://api.mch.weixin.qq.com/v3/pay/transactions/native", "", merchantId,
                cerSerialNo, "/Users/cyt/projects/i/cache/easymusic/key/apiclient_test_key.pem");
        log.info("\n" + sign);
    }

    private static String getToken4WeChat(String method, String url, String body, String merchantId, String certSerialNo, String keyPath) {
        HttpUrl httpUrl = HttpUrl.parse(url);
        String nonceStr = StringTools.getRandomNumber(Constants.LENGTH_30);
        long timestamp = System.currentTimeMillis() / 1000;
        if (StringTools.isEmpty(body)) {
            body = "";
        }
        String message = buildMessage(method, httpUrl, timestamp, nonceStr, body);
        String signature = sign(message, keyPath);
        String signstr = "mchid=\"" + merchantId + "\"," +
                "nonce_str=\"" + nonceStr + "\"," +
                "signature=\"" + signature + "\"," +
                "timestamp=\"" + timestamp + "\"," +
                "serial_no=\"" + certSerialNo + "\"";
        return signstr;
    }

    private static String buildMessage(String method, HttpUrl url, long timestamp, String nonceStr, String body) {
        String canonicalUrl = url.encodedPath();
        if (url.encodedQuery() != null) {
            canonicalUrl += "?" + url.encodedQuery();
        }
        return method + "\n" +
                canonicalUrl + "\n" +
                timestamp + "\n" +
                nonceStr + "\n" +
                body + "\n";
    }

    private static String sign(String str, String keyPath) {
        try {
            File file = new File(keyPath);
            if (!file.exists()) {
                throw new BusinessException("私钥文件不存在");
            }
            byte[] message = str.getBytes(StandardCharsets.UTF_8);
            // 签名
            Signature sign = Signature.getInstance("SHA256withRSA");
            // 加载私钥
            sign.initSign(getPrivateKey(keyPath));
            // 开始签名
            sign.update(message);
            return new String(Base64.getEncoder().encode(sign.sign()));
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("签名失败", e);
            throw new BusinessException("生成签名失败");
        }
    }

    private static PrivateKey getPrivateKey(String fileName) {
        try {
            String content = new String(Files.readAllBytes(Paths.get(fileName)), StandardCharsets.UTF_8);
            // 去除头尾
            String privateKey = content
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s+", "");
            // 解析私钥
            KeyFactory kf = KeyFactory.getInstance("RSA");
            // 得到私钥对象
            return kf.generatePrivate(new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKey)));
        } catch (Exception e) {
            log.error("读取私钥失败", e);
            throw new BusinessException("读取私钥失败");
        }
    }

}