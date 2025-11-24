package com.easymusic.entity.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * @author cyt
 * * @date 2025/11/19 15:30:14
 */

@Configuration
public class AppConfig {

    @Value("${project.folder:}")
    private String projectFolder;

    @Value("${admin.account:}")
    private String adminAccount;

    @Value("${admin.password:}")
    private String adminPassword;

    //微信支付 appid
    @Value("${pay.wechat.appid:}")
    private String payWechatAppId;


    @Value("${pay.wechat.mchid:}")
    private String payWechatMchid;

    //整数
    @Value("${pay.wechat.serialNo:}")
    private String payWechatSerialNo;

    //api密钥
    @Value("${pay.wechat.apiclientKeypath:}")
    private String payWechataApiclientKeyPath;

    // v3key密钥
    @Value("${pay.wechat.apiV3key:}")
    private String payWechatApiV3Key;

    //支付域名
    @Value("${pay.payDomain:}")
    private String payDomain;

    @Value("${pay.payType:wechat}")
    private String payType;

    //课程ID
    @Value("${pay.courseOrderId:}")
    private String courseOrderId;


    @Value("${auto.checkPay:false}")
    private Boolean autoCheckPay;

    @Value("${auto.checkMusic:false}")
    private Boolean autoCheckMusic;

    public String getPayWechatAppId() {
        return payWechatAppId;
    }

    public String getPayWechatMchid() {
        return payWechatMchid;
    }

    public String getPayWechatSerialNo() {
        return payWechatSerialNo;
    }

    public String getPayWechataApiclientKeyPath() {
        return payWechataApiclientKeyPath;
    }

    public String getPayWechatApiV3Key() {
        return payWechatApiV3Key;
    }

    public String getPayDomain() {
        return payDomain;
    }

    public String getPayType() {
        return payType;
    }

    public String getCourseOrderId() {
        return courseOrderId;
    }

    public Boolean getAutoCheckPay() {
        return autoCheckPay;
    }

    public Boolean getAutoCheckMusic() {
        return autoCheckMusic;
    }

    public String getProjectFolder() {
        return projectFolder;
    }

    public String getAdminAccount() {
        return adminAccount;
    }

    public String getAdminPassword() {
        return adminPassword;
    }
}
