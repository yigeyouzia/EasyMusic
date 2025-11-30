package com.easymusic.controller;

import com.easymusic.entity.po.MusicInfo;
import com.easymusic.entity.query.MusicInfoQuery;
import com.easymusic.entity.vo.PaginationResultVO;
import com.easymusic.entity.vo.ResponseVO;
import com.easymusic.service.MusicInfoService;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotEmpty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户信息 Controller
 */
@RestController
@RequestMapping("/music")
@Slf4j
@Validated
public class MusicInfoController extends ABaseController {


    @Resource
    private MusicInfoService musicInfoService;

    @RequestMapping("/loadMusic")
    public ResponseVO logout(MusicInfoQuery query) {
        query.setOrderBy("m.create_time desc");
        query.setQueryUser(true);
        PaginationResultVO<MusicInfo> res = musicInfoService.findListByPage(query);
        return getSuccessResponseVO(res);
    }

    @RequestMapping("/changeMusicCommendType")
    public ResponseVO changeMusicCommendType(@NotEmpty String musicId, Integer commendType) {
        MusicInfo musicInfo = new MusicInfo();
        musicInfo.setCommendType(commendType);
        musicInfoService.updateMusicInfoByMusicId(musicInfo, musicId);
        return getSuccessResponseVO(null);
    }
}