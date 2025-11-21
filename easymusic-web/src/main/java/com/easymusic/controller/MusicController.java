package com.easymusic.controller;

import com.easymusic.annotation.GlobalInterceptor;
import com.easymusic.entity.dto.TokenUserInfoDTO;
import com.easymusic.entity.enums.CommendTypeEnum;
import com.easymusic.entity.enums.MusicStatusEnum;
import com.easymusic.entity.enums.PageSize;
import com.easymusic.entity.po.MusicCreation;
import com.easymusic.entity.po.MusicInfo;
import com.easymusic.entity.query.MusicInfoQuery;
import com.easymusic.entity.vo.PaginationResultVO;
import com.easymusic.entity.vo.ResponseVO;
import com.easymusic.service.MusicCreationService;
import com.easymusic.service.MusicInfoActionService;
import com.easymusic.service.MusicInfoService;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotEmpty;
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

    @Resource
    private MusicInfoActionService musicInfoActionService;

    @Resource
    private MusicCreationService musicCreationService;

    /**
     * 加载推荐音乐
     *
     * @return
     */
    @RequestMapping("/loadCommendMusic")
    public ResponseVO loadCommendMusic() {

        TokenUserInfoDTO tokenUserInfo = getTokenUserInfo(null);

        MusicInfoQuery query = new MusicInfoQuery();
        query.setCommendType(CommendTypeEnum.COMMEND.getStatus());
        query.setOrderBy("m.create_time desc");
        query.setQueryUser(true);
        query.setCurrentUserId(tokenUserInfo == null ? null : tokenUserInfo.getUserId()); // 是否已经点赞
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

        TokenUserInfoDTO tokenUserInfo = getTokenUserInfo(null);


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
        query.setCurrentUserId(tokenUserInfo == null ? null : tokenUserInfo.getUserId()); // 是否已经点赞
        PaginationResultVO<MusicInfo> res = musicInfoService.findListByPage(query);
        return getSuccessResponseVO(res);
    }

    /**
     * 加载音乐详情个 歌词
     *
     * @param musicId
     * @return
     */
    @RequestMapping("/musicDetail")
    public ResponseVO loadLatestMusic(@NotEmpty String musicId) {
        MusicInfo res = musicInfoService.getMusicInfoByMusicId(musicId);
        return getSuccessResponseVO(res);
    }

    /**
     * 更新音乐的播放次数
     *
     * @param musicId
     * @return
     */
    @RequestMapping("/updatePlayCount")
    public ResponseVO updatePlayCount(@NotEmpty String musicId) {
        musicInfoService.updateMusicCount(musicId);
        return getSuccessResponseVO(null);
    }

    /**
     * 更新音乐的点赞状态
     *
     * @param musicId
     * @return
     */
    @RequestMapping("/doGood")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO doGood(@NotEmpty String musicId) {
        musicInfoActionService.doGood(musicId, getTokenUserInfo(null).getUserId());
        return getSuccessResponseVO(null);
    }


    @RequestMapping("/getCreation")
    public ResponseVO getCreation(@NotEmpty String creationId) {
        MusicCreation musicCreation = musicCreationService.getMusicCreationByCreationId(creationId);
        return getSuccessResponseVO(musicCreation);
    }
}