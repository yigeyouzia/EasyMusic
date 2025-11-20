package com.easymusic.controller;

import com.easymusic.entity.enums.CommendTypeEnum;
import com.easymusic.entity.enums.MusicStatusEnum;
import com.easymusic.entity.enums.PageSize;
import com.easymusic.entity.po.MusicInfo;
import com.easymusic.entity.query.MusicInfoQuery;
import com.easymusic.entity.vo.PaginationResultVO;
import com.easymusic.entity.vo.ResponseVO;
import com.easymusic.service.MusicInfoService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用户信息 Controller
 */
@RestController
@RequestMapping("/music")
@Slf4j
@Validated
public class MusicController extends ABaseController {

    @Resource
    private MusicInfoService musicInfoService;

    /**
     * 加载推荐音乐
     *
     * @return
     */
    @RequestMapping("/loadCommendMusic")
    public ResponseVO loadCommendMusic() {
        MusicInfoQuery query = new MusicInfoQuery();
        query.setCommendType(CommendTypeEnum.COMMEND.getStatus());
        query.setOrderBy("m.create_time desc");
        query.setQueryUser(true);
        List<MusicInfo> res = musicInfoService.findListByParam(query);
        return getSuccessResponseVO(res);
    }

    /**
     * 加载最新音乐
     *
     * @param pageNo
     * @param indexType 是否首页
     * @return
     */
    @RequestMapping("/loadLatestMusic")
    public ResponseVO loadLatestMusic(Integer pageNo, Integer indexType) {
        MusicInfoQuery query = new MusicInfoQuery();
        query.setCommendType(CommendTypeEnum.NOT_COMMEND.getStatus());
        query.setMusicStatus(MusicStatusEnum.CREATED.getStatus());
        query.setOrderBy("m.create_time desc");
        if (indexType != null) {
            query.setPageSize(PageSize.SIZE12.getSize());
        } else {
            query.setPageSize(PageSize.SIZE20.getSize());
        }
        query.setPageNo(pageNo);
        PaginationResultVO<MusicInfo> res = musicInfoService.findListByPage(query);
        return getSuccessResponseVO(res);
    }
}