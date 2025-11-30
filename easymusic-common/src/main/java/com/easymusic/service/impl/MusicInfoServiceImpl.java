package com.easymusic.service.impl;

import com.easymusic.api.MusicCreateApi;
import com.easymusic.entity.constants.Constants;
import com.easymusic.entity.dto.MusicCreationResultDTO;
import com.easymusic.entity.dto.MusicTaskDTO;
import com.easymusic.entity.enums.*;
import com.easymusic.entity.po.MusicInfo;
import com.easymusic.entity.po.UserInfo;
import com.easymusic.entity.po.UserIntegralRecord;
import com.easymusic.entity.query.MusicInfoQuery;
import com.easymusic.entity.query.SimplePage;
import com.easymusic.entity.query.UserInfoQuery;
import com.easymusic.entity.query.UserIntegralRecordQuery;
import com.easymusic.entity.vo.PaginationResultVO;
import com.easymusic.exception.BusinessException;
import com.easymusic.mappers.MusicInfoMapper;
import com.easymusic.mappers.UserInfoMapper;
import com.easymusic.redis.RedisComponent;
import com.easymusic.service.MusicInfoService;
import com.easymusic.service.UserIntegralRecordService;
import com.easymusic.spring.SpringContext;
import com.easymusic.utils.FileUtils;
import com.easymusic.utils.JsonUtils;
import com.easymusic.utils.StringTools;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;


/**
 * 音乐信息 业务接口实现
 */
@Service("musicInfoService")
@Slf4j
public class MusicInfoServiceImpl implements MusicInfoService {

    @Resource
    private MusicInfoMapper<MusicInfo, MusicInfoQuery> musicInfoMapper;

    @Resource
    private UserInfoMapper<UserInfo, UserInfoQuery> userInfoMapper;

    @Resource
    private FileUtils fileUtils;

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private UserIntegralRecordService userIntegralRecordService;

    @Resource
    @Lazy
    private MusicInfoService musicInfoService;

    /**
     * 根据条件查询列表
     */
    @Override
    public List<MusicInfo> findListByParam(MusicInfoQuery param) {
        return this.musicInfoMapper.selectList(param);
    }

    /**
     * 根据条件查询列表
     */
    @Override
    public Integer findCountByParam(MusicInfoQuery param) {
        return this.musicInfoMapper.selectCount(param);
    }

    /**
     * 分页查询方法
     */
    @Override
    public PaginationResultVO<MusicInfo> findListByPage(MusicInfoQuery param) {
        int count = this.findCountByParam(param);
        int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

        SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
        param.setSimplePage(page);
        List<MusicInfo> list = this.findListByParam(param);
        PaginationResultVO<MusicInfo> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
        return result;
    }

    /**
     * 新增
     */
    @Override
    public Integer add(MusicInfo bean) {
        return this.musicInfoMapper.insert(bean);
    }

    /**
     * 批量新增
     */
    @Override
    public Integer addBatch(List<MusicInfo> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.musicInfoMapper.insertBatch(listBean);
    }

    /**
     * 批量新增或者修改
     */
    @Override
    public Integer addOrUpdateBatch(List<MusicInfo> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.musicInfoMapper.insertOrUpdateBatch(listBean);
    }

    /**
     * 多条件更新
     */
    @Override
    public Integer updateByParam(MusicInfo bean, MusicInfoQuery param) {
        StringTools.checkParam(param);
        return this.musicInfoMapper.updateByParam(bean, param);
    }

    /**
     * 多条件删除
     */
    @Override
    public Integer deleteByParam(MusicInfoQuery param) {
        StringTools.checkParam(param);
        return this.musicInfoMapper.deleteByParam(param);
    }

    /**
     * 根据MusicId获取对象
     */
    @Override
    public MusicInfo getMusicInfoByMusicId(String musicId) {
        MusicInfo musicInfo = musicInfoMapper.selectByMusicId(musicId);
        if (musicInfo != null) {
            UserInfo userInfo = userInfoMapper.selectByUserId(musicInfo.getUserId());
            musicInfo.setNickName(userInfo.getNickName());
        }
        return musicInfo;
    }

    /**
     * 根据MusicId修改
     */
    @Override
    public Integer updateMusicInfoByMusicId(MusicInfo bean, String musicId) {
        return this.musicInfoMapper.updateByMusicId(bean, musicId);
    }

    /**
     * 根据MusicId删除
     */
    @Override
    public Integer deleteMusicInfoByMusicId(String musicId) {
        return this.musicInfoMapper.deleteByMusicId(musicId);
    }

    /**
     * 根据TaskId获取对象
     */
    @Override
    public MusicInfo getMusicInfoByTaskId(String taskId) {
        return this.musicInfoMapper.selectByTaskId(taskId);
    }

    /**
     * 根据TaskId修改
     */
    @Override
    public Integer updateMusicInfoByTaskId(MusicInfo bean, String taskId) {
        return this.musicInfoMapper.updateByTaskId(bean, taskId);
    }

    /**
     * 根据TaskId删除
     */
    @Override
    public Integer deleteMusicInfoByTaskId(String taskId) {
        return this.musicInfoMapper.deleteByTaskId(taskId);
    }

    @Override
    public void updateMusicCount(String musicId) {
        musicInfoMapper.updateMusicCount(musicId);
    }

    /**
     * 天谱乐创建音乐回调通知
     *
     * @param musicType
     * @param responseJson
     */
    @Override
    public void musicCreateNotify(Integer musicType, String responseJson) {
        String apiCode = MusicTypeEnum.MUSIC.getType().equals(musicType) ?
                ModelType4MusicEnum.V3.getApiCode() : ModelType4MusicEnum.V3.getApiCode();

        MusicCreateApi musicCreateApi = (MusicCreateApi) SpringContext.getBean(apiCode);
        MusicCreationResultDTO resultDTO = musicCreateApi.createMusicNotify(musicType, responseJson);

        if (resultDTO == null) {
            return;
        }

        musicInfoService.musicCreated(resultDTO);

    }

    /**
     * 音乐创建完成
     * 更新音乐信息 歌曲 歌词 音频文件
     *
     * @param resultDTO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void musicCreated(MusicCreationResultDTO resultDTO) {
        MusicInfo updateInfo = new MusicInfo();

        if (resultDTO.getCreateSuccess()) {
            updateInfo.setMusicTitle(resultDTO.getTitle());
            updateInfo.setDuration(resultDTO.getDuration());
            String lyrics = JsonUtils.convertObj2Json(resultDTO.getLyricsList());
            updateInfo.setLyrics(lyrics);
            updateInfo.setMusicStatus(MusicStatusEnum.CREATED.getStatus());
            String audioUrl = resultDTO.getAudioUrl();
            updateInfo.setAudioPath(audioUrl);
            // 保存音频文件
            String audioPath = fileUtils.downloadFile(audioUrl, Constants.AUDIO_SUFFIX);
            updateInfo.setAudioPath(audioPath);
        } else {
            // 创建失败
            updateInfo.setMusicStatus(MusicStatusEnum.CRAETE_FAIL.getStatus());
            // TODO 退还积分
            MusicInfo musicInfo = musicInfoMapper.selectByTaskId(resultDTO.getTaskId());
            if (musicInfo == null) {
                throw new BusinessException("音乐不存在");
            }

            UserIntegralRecordQuery query = new UserIntegralRecordQuery();
            query.setUserId(musicInfo.getUserId());
            query.setBusinessId(musicInfo.getCreationId());
            List<UserIntegralRecord> list = userIntegralRecordService.findListByParam(query);
            UserIntegralRecord record = list.get(0);
            userIntegralRecordService.changeUserIntegral(UserIntegralRecordTypeEnum.CREATE_MUSIC_BACK,
                    musicInfo.getUserId(),
                    musicInfo.getUserId(),
                    -record.getChangeIntegral(),
                    null);
        }

        MusicInfoQuery query = new MusicInfoQuery();
        query.setTaskId(resultDTO.getTaskId());
        query.setMusicStatus(MusicStatusEnum.CREATING.getStatus());

        Integer changeCount = musicInfoMapper.updateByParam(updateInfo, query);
        if (changeCount == 0) {
            throw new BusinessException("更新音乐状态失败");
        }
    }

    /**
     * 定时任务， 自动查询未支付订单
     */
    @PostConstruct
    public void getMusicFromQueue() {
        ExecutorServiceSingletonEnum.INSTANCE.getExecutorService().execute(() -> {
            // 查询未支付订单
            while (true) {
                try {
                    Set<MusicTaskDTO> queueDataList = redisComponent.getMusicTaskDto();
                    if (queueDataList == null || queueDataList.isEmpty()) {
                        Thread.sleep(5000);
                        continue;
                    }
                    for (MusicTaskDTO musicTaskDTO : queueDataList) {
                        // 调用接口获取音乐信息
                        redisComponent.removeMusicTaskDto(musicTaskDTO);
                        getMusicInfoFromAI(musicTaskDTO);
                    }
                } catch (Exception e) {
                    log.error("获取音乐信息队列失败", e);
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException ex) {
                        log.error("休眠失败", ex);
                    }
                }
            }
        });
    }

    /**
     * 主动轮询天谱乐
     *
     * @param musicTaskDTO
     */
    private void getMusicInfoFromAI(MusicTaskDTO musicTaskDTO) {
        MusicCreateApi musicCreateApi = (MusicCreateApi) SpringContext.getBean(musicTaskDTO.getApiCode());
        MusicCreationResultDTO resultDTO = null;
        if (musicTaskDTO.getMusicType().equals(MusicTypeEnum.MUSIC.getType())) {
            resultDTO = musicCreateApi.musicQuery(musicTaskDTO.getTaskId());
        } else {
            resultDTO = musicCreateApi.pureMusicQuery(musicTaskDTO.getTaskId());
        }

        // 获取失败 重新加入redis队列
        if (resultDTO == null) {
            redisComponent.addMusicCreateTask(musicTaskDTO);
            return;
        }
        // 保存音乐信息
        musicInfoService.musicCreated(resultDTO);
    }

    @Override
    public String uploadMusicCover(MultipartFile cover, String musicId, String userId) {
        MusicInfo musicInfo = musicInfoMapper.selectByMusicId(musicId);
        if (musicInfo == null || !musicInfo.getUserId().equals(userId)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }

        String suffix = StringTools.getFileSuffix(cover.getOriginalFilename());
        String fileName = musicId + suffix;
        String coverPath = fileUtils.uploadFile(cover, null, fileName)
                + "&" + System.currentTimeMillis();

        MusicInfo updateInfo = new MusicInfo();
        updateInfo.setCover(coverPath);
        musicInfoMapper.updateByMusicId(updateInfo, musicId);


        return coverPath;
    }
}