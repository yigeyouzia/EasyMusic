package com.easymusic.controller;

import com.easymusic.service.ProductInfoService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户信息 Controller
 */
@RestController
@RequestMapping("/product")
@Slf4j
@Validated
public class ProductInfoController extends ABaseController {

    @Resource
    private ProductInfoService productInfoService;



}