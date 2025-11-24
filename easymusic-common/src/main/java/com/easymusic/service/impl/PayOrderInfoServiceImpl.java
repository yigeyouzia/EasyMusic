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
import com.easymusic.utils.JsonUtils;
import com.easymusic.utils.OKHttpUtils;
import com.easymusic.utils.StringTools;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import okhttp3.HttpUrl;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.*;


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
        PayOrderTypeEnum payOrderType = PayOrderTypeEnum.getByType(payType);
        if (payOrderType == null || PayOrderTypeEnum.PAY_CODE == payOrderType) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }

        // 构造订单信息
        String orderId = getOrderId();
        PayOrderInfo payOrderInfo = new PayOrderInfo();
        // TODO 调用微信支付 先调微信 再选择入库（具体看业务场景）
        String payUrl = getPayUrl(orderId, productInfo.getPrice(), productInfo.getProductName());

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

    public static void main(String[] args) {
//        String merchantId = "1682060827";
//        String cerSerialNo = "54CEED93E0574136CD7B55BA3D5AC35353B9F8F2";
//        String keyPath = "/Users/cyt/projects/i/cache/easymusic/key/apiclient_test_key.pem";
//        String sign = getToken4WeChat("POST", "https://api.mch.weixin.qq.com/v3/pay/transactions/native", "", merchantId,
//                cerSerialNo, keyPath);
//        log.info("\n" + sign);

        // getPayUrl(System.currentTimeMillis() + "test11111111", new BigDecimal("0.5"), "体验卡");
        // getCertificates();


    }

    private static String getPayUrl(String orderId, BigDecimal amount, String productName) {
        String mchId = "1682060827";
        String appId = "wx3d7c9ce879ea7141";
        String notifyUrl = "http://easyshop.byte361.com/api/WechatPayApiNotify";
        String certSerialNo = "54CEED93E0574136CD7B55BA3D5AC35353B9F8F2";
        String keyPath = "/Users/cyt/projects/i/cache/easymusic/key/apiclient_test_key.pem";


        HashMap<String, Object> paramMap = new HashMap<>();
        paramMap.put("appid", appId);
        paramMap.put("mchid", mchId);
        paramMap.put("description", productName);
        paramMap.put("out_trade_no", orderId);
        paramMap.put("notify_url", notifyUrl);

        Map<String, Object> amountMap = new HashMap<>();
        amountMap.put("total", StringTools.convertYuan2fenBigDecimal(amount));
        amountMap.put("currency", CURRENCY);

        paramMap.put("amount", amountMap);

        String jsonBody = JsonUtils.convertObj2Json(paramMap);
        String payUrl = "https://easyshop.byte361.com/api/v3/pay/transactions/native";
        String token = getToken4LaoLuo(); //getToken4WeChat("POST", payUrl, jsonBody, mchId, certSerialNo, keyPath);

        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("Authorization", "WECHATPAY2-SHA256-RSA2048 " + token);
        headerMap.put("accept", "application/json");
        headerMap.put("Content-Type", "application/json");
        headerMap.put("courseOrderId", "20251120141238QBM5Y");

        String response = OKHttpUtils.postRequest4Json(payUrl, headerMap, jsonBody);
        log.info("请求微信返回信息：{}", response);
        Map<String, String> responseMap  = JsonUtils.convertJson2Obj(response, Map.class);
        String codeUrlStr = responseMap.get("code_url");
        return codeUrlStr;
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

    private static String getToken4LaoLuo() {
        String courseOrderId = "20251120141238QBM5Y";
        return StringTools.encodeByMD5(courseOrderId + StringTools.encodeByMD5(courseOrderId));
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

    private List<Certificate> getCertificate(String mchId, String serialNo, String keyPath, String apiV3Key) throws BusinessException {
        //String mchId = "1682060827";
        String appId = "wx3d7c9ce879ea7141";
        String notifyUrl = "http://easyshop.byte361.com/api/WechatPayApiNotify";
        String certSerialNo = "54CEED93E0574136CD7B55BA3D5AC35353B9F8F2";
        //String keyPath = "/Users/cyt/projects/i/cache/easymusic/key/apiclient_test_key.pem";
        String CERTIFICATES_URL = "https://aeasyshop.byte361.com/api/v3/certificates";

        try {
            String token = getToken4WeChat("GET", CERTIFICATES_URL, "", mchId, serialNo, keyPath);
            Map<String, String> headerMap = new HashMap<>();
            headerMap.put("Authorization", "WECHATPAY2-SHA256-RSA2048 " + token);
            headerMap.put("Content-Type", "application/json");
            headerMap.put("Accept", "application/json");
            String body = OKHttpUtils.getRequest(CERTIFICATES_URL, headerMap);
            log.info("获取微信平台证书返回信息:{}}", body);
            Map<String, Object> bodyMap = JsonUtils.convertJson2Obj(body, Map.class);
            List<Map> data = ((List<Map>) bodyMap.get("data"));
            List<Certificate> certificateList = new ArrayList<>();
            for (Map dataMap : data) {
                Map<String, String> encrypt_certificate = (Map<String, String>) dataMap.get("encrypt_certificate");
                String associatedData = encrypt_certificate.get("associated_data");
                String nonce = encrypt_certificate.get("nonce");
                String ciphertext = encrypt_certificate.get("ciphertext");
                String publicKey = decryptResponseBody(apiV3Key, associatedData, nonce, ciphertext);
                final CertificateFactory cf = CertificateFactory.getInstance("X509");
                ByteArrayInputStream inputStream = new ByteArrayInputStream(publicKey.getBytes(StandardCharsets.UTF_8));
                Certificate certificate = cf.generateCertificate(inputStream);
                certificateList.add(certificate);
            }
            return certificateList;
        } catch (BusinessException e) {
            log.error("获取微信平台证书失败", e);
            throw e;
        } catch (Exception e) {
            log.error("获取微信平台证书失败", e);
            throw new BusinessException("获取微信平台证书失败");
        }
    }


    // 微信V3接口解密信息
    private static String decryptResponseBody(String apiV3Key, String associatedData, String nonce, String ciphertext) throws BusinessException {
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            SecretKeySpec key = new SecretKeySpec(apiV3Key.getBytes(StandardCharsets.UTF_8), "AES");
            GCMParameterSpec spec = new GCMParameterSpec(128, nonce.getBytes(StandardCharsets.UTF_8));
            cipher.init(Cipher.DECRYPT_MODE, key, spec);
            cipher.updateAAD(associatedData.getBytes(StandardCharsets.UTF_8));
            byte[] bytes = cipher.doFinal(Base64.getDecoder().decode(ciphertext));
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("微信V3接口解密信息失败，apiV3Key:{},associatedData:{},nonce:{},ciphertext:{}", apiV3Key, associatedData, nonce, ciphertext, e);
            throw new BusinessException("解密失败");
        }
    }

    private Boolean verifySign(String mchId, String serialNo, String keyPath, String apiV3Key, String wechatpaySignature, String mySign) throws BusinessException {
        List<Certificate> certificateList = getCertificate(mchId, serialNo, keyPath, apiV3Key);
        Integer index = 0;
        for (Certificate certificate : certificateList) {
            try {
                index++;
                Signature sign = Signature.getInstance("SHA256withRSA");
                sign.initVerify(certificate);
                sign.update(mySign.getBytes(StandardCharsets.UTF_8));
                Boolean verify = sign.verify(Base64.getDecoder().decode(wechatpaySignature));
                log.info("第{}次验证签名结果{}", index, verify);
                if (verify) {
                    return true;
                }
            } catch (Exception e) {
                log.error("微信回调验证签名失败", e);
            }
        }
        return false;
    }


}