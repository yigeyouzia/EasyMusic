package com.easymusic.controller.notify;

import com.easymusic.controller.ABaseController;
import com.easymusic.service.MusicInfoService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author cyt
 * * @date 2025/11/24 19:30:26
 */


@RestController
@Slf4j
@RequestMapping("/musicNotify")
public class MusicNotifyController extends ABaseController {

    @Resource
    private MusicInfoService musicInfoService;

    @RequestMapping("/tianpuyue/{musicType}")
    public String tianpuyueMusicCreateNotify(@PathVariable("musicType") Integer musicType,
                                             @RequestBody String body) {
        log.info("天谱乐音乐创建回调信息，musicType：{}，body：{}", musicType, body);
        musicInfoService.musicCreateNotify(musicType, body);
        return STATUC_SUCCESS;
    }

}
