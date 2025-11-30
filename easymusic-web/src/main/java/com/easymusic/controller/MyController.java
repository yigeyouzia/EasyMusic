package com.easymusic.controller;

import com.easymusic.annotation.GlobalInterceptor;
import com.easymusic.entity.dto.MusicSettingDTO;
import com.easymusic.entity.dto.TokenUserInfoDTO;
import com.easymusic.entity.po.MusicCreation;
import com.easymusic.entity.po.MusicInfo;
import com.easymusic.entity.po.SysDict;
import com.easymusic.entity.po.UserIntegralRecord;
import com.easymusic.entity.query.MusicInfoQuery;
import com.easymusic.entity.query.UserIntegralRecordQuery;
import com.easymusic.entity.vo.PaginationResultVO;
import com.easymusic.entity.vo.ResponseVO;
import com.easymusic.redis.RedisComponent;
import com.easymusic.service.MusicCreationService;
import com.easymusic.service.MusicInfoService;
import com.easymusic.service.SysDictService;
import com.easymusic.service.UserIntegralRecordService;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 我的页面控制器
 *
 * @author cyt
 * * @date 2025/11/23 16:57:34
 */

@Slf4j
@RestController
@RequestMapping("/my")
@Validated
public class MyController extends ABaseController {


    @Resource
    private RedisComponent redisComponent;

    @Resource
    private UserIntegralRecordService userIntegralRecordService;

    @Resource
    private SysDictService dictService;

    @Resource
    private SysDictService sysDictService;

    @Resource
    private MusicCreationService musicCreationService;


    @Resource
    private MusicInfoService musicInfoService;

    @RequestMapping("/integralRecords")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO loadProduct(Integer page) {
        UserIntegralRecordQuery query = new UserIntegralRecordQuery();
        query.setUserId(getTokenUserInfo(null).getUserId());
        query.setPageNo(page);
        query.setOrderBy("record_id desc");
        PaginationResultVO<UserIntegralRecord> res = userIntegralRecordService.findListByPage(query);
        return getSuccessResponseVO(res);
    }

    @RequestMapping("/loadSysDict")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO loadSysDict() {
        Map<String, List<SysDict>> res = sysDictService.getDictFromCache();
        return getSuccessResponseVO(res);
    }

    @RequestMapping("/createMusic")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO createMusic(@NotEmpty @Size(max = 500) String prompt,
                                  @Size(max = 1500) String lyrics,
                                  @NotNull Integer musicType,
                                  @NotEmpty String model,
                                  @Size(max = 200) String musicGener,
                                  @Size(max = 150) String musicEmotion,
                                  @Size(max = 5) String musicSex,
                                  @NotNull Integer modeType) {

        TokenUserInfoDTO tokenUserInfo = getTokenUserInfo(null);
        MusicCreation creation = new MusicCreation();
        creation.setUserId(tokenUserInfo.getUserId());
        creation.setMusicType(musicType);
        creation.setLyrics(lyrics);
        creation.setPrompt(prompt);
        creation.setModel(model);
        creation.setModeType(modeType);

        // 设置音乐属性 配置settings
        MusicSettingDTO musicSettingDTO = new MusicSettingDTO();
        musicSettingDTO.setMusicGener(musicGener);
        musicSettingDTO.setMusicEmotion(musicEmotion);
        musicSettingDTO.setMusicSex(musicSex);

        List<String> musicList = musicCreationService.createMusic(creation, musicSettingDTO);

        return getSuccessResponseVO(musicList);

    }

    /**
     * 个人我的
     *
     * @param pageNo
     * @param queryLikeMusic
     * @return
     */
    @RequestMapping("/loadMyMusic")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO loadMusicList(Integer pageNo, Boolean queryLikeMusic) {
        TokenUserInfoDTO tokenUserInfo = getTokenUserInfo(null);
        if (tokenUserInfo == null) {
            return getSuccessResponseVO(new PaginationResultVO<>());
        }

        MusicInfoQuery query = new MusicInfoQuery();
        query.setPageNo(pageNo);
        query.setOrderBy("create_time desc");
        if (queryLikeMusic != null && queryLikeMusic) {
            query.setQueryLikeMusic(true);
            query.setLikeUserId(tokenUserInfo.getUserId());
        } else {
            query.setUserId(tokenUserInfo.getUserId());
        }

        PaginationResultVO<MusicInfo> res = musicInfoService.findListByPage(query);
        return getSuccessResponseVO(res);
    }

    /**
     * 加载创建中的音乐
     *
     * @param musicIds
     * @return
     */
    @RequestMapping("/loadCreatingMusic")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO loadCreatingMusic(@NotEmpty String musicIds) {
        MusicInfoQuery query = new MusicInfoQuery();

        query.setUserId(getTokenUserInfo(null).getUserId());
        query.setMusicIdList(Arrays.asList(musicIds.split(",")));

        List<MusicInfo> res = musicInfoService.findListByParam(query);
        return getSuccessResponseVO(res);
    }

    @RequestMapping("/uploadMusicCover")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO uploadMusicCover(@NotNull MultipartFile cover, String musicId) {

        TokenUserInfoDTO tokenUserInfo = getTokenUserInfo(null);
        String res = musicInfoService.uploadMusicCover(cover, musicId, tokenUserInfo.getUserId());
        return getSuccessResponseVO(res);
    }
}
