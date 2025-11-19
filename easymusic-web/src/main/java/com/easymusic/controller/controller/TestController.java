package com.easymusic.controller.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author cyt
 * * @date 2025/11/18 22:24:33
 */

@RestController
public class TestController {
    @RequestMapping("/test")
    public String test() {
        return "test";
    }
}
