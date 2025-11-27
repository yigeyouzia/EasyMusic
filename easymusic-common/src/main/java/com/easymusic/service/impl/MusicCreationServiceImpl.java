package com.easymusic.service.impl;

import com.easymusic.entity.constants.Constants;
import com.easymusic.entity.dto.MusicSettingDTO;
import com.easymusic.entity.enums.*;
import com.easymusic.entity.po.MusicCreation;
import com.easymusic.entity.po.SysDict;
import com.easymusic.entity.query.MusicCreationQuery;
import com.easymusic.entity.query.SimplePage;
import com.easymusic.entity.vo.PaginationResultVO;
import com.easymusic.exception.BusinessException;
import com.easymusic.mappers.MusicCreationMapper;
import com.easymusic.redis.RedisComponent;
import com.easymusic.service.MusicCreationService;
import com.easymusic.service.UserInfoService;
import com.easymusic.utils.JsonUtils;
import com.easymusic.utils.StringTools;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;
import java.util.Optional;


/**
 * 音乐创作信息 业务接口实现
 */
@Service("musicCreationService")
@Slf4j
public class MusicCreationServiceImpl implements MusicCreationService {

    @Resource
    private MusicCreationMapper<MusicCreation, MusicCreationQuery> musicCreationMapper;

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private UserInfoService userInfoService;

    @Resource
    private UserIntegralRecordServiceImpl userIntegralRecordService;

    /**
     * 根据条件查询列表
     */
    @Override
    public List<MusicCreation> findListByParam(MusicCreationQuery param) {
        return this.musicCreationMapper.selectList(param);
    }

    /**
     * 根据条件查询列表
     */
    @Override
    public Integer findCountByParam(MusicCreationQuery param) {
        return this.musicCreationMapper.selectCount(param);
    }

    /**
     * 分页查询方法
     */
    @Override
    public PaginationResultVO<MusicCreation> findListByPage(MusicCreationQuery param) {
        int count = this.findCountByParam(param);
        int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

        SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
        param.setSimplePage(page);
        List<MusicCreation> list = this.findListByParam(param);
        PaginationResultVO<MusicCreation> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
        return result;
    }

    /**
     * 新增
     */
    @Override
    public Integer add(MusicCreation bean) {
        return this.musicCreationMapper.insert(bean);
    }

    /**
     * 批量新增
     */
    @Override
    public Integer addBatch(List<MusicCreation> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.musicCreationMapper.insertBatch(listBean);
    }

    /**
     * 批量新增或者修改
     */
    @Override
    public Integer addOrUpdateBatch(List<MusicCreation> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.musicCreationMapper.insertOrUpdateBatch(listBean);
    }

    /**
     * 多条件更新
     */
    @Override
    public Integer updateByParam(MusicCreation bean, MusicCreationQuery param) {
        StringTools.checkParam(param);
        return this.musicCreationMapper.updateByParam(bean, param);
    }

    /**
     * 多条件删除
     */
    @Override
    public Integer deleteByParam(MusicCreationQuery param) {
        StringTools.checkParam(param);
        return this.musicCreationMapper.deleteByParam(param);
    }

    /**
     * 根据CreationId获取对象
     */
    @Override
    public MusicCreation getMusicCreationByCreationId(String creationId) {
        return this.musicCreationMapper.selectByCreationId(creationId);
    }

    /**
     * 根据CreationId修改
     */
    @Override
    public Integer updateMusicCreationByCreationId(MusicCreation bean, String creationId) {
        return this.musicCreationMapper.updateByCreationId(bean, creationId);
    }

    /**
     * 根据CreationId删除
     */
    @Override
    public Integer deleteMusicCreationByCreationId(String creationId) {
        return this.musicCreationMapper.deleteByCreationId(creationId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<String> createMusic(MusicCreation creation, MusicSettingDTO settingDTO) {
        // 1.扣减积分
        MusicTypeEnum musicTypeEnum = MusicTypeEnum.getByType(creation.getModeType());
        if (musicTypeEnum == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        ModelInfo modelInfo = getModelInfo(musicTypeEnum, creation.getModel());
        String model = modelInfo.model;
        // 音乐下面的配置
        List<SysDict> sysDictSubList = redisComponent.getDictSubList(musicTypeEnum.getDictCode());

        Optional<SysDict> dictInfo = sysDictSubList.stream()
                .filter(value -> value.getDictCode().equals(creation.getModel()))
                .findFirst();
        SysDict sysDict = dictInfo.get();
        if (sysDict == null) {
            throw new BusinessException("系统配置错误，请联系管理员");
        }

        String creationId = StringTools.getRandomString(Constants.LENGTH_14);
        int integral = Integer.parseInt(sysDict.getDictValue()); // 积分
        String apiCode = modelInfo.apiCode;

        // 扣减积分
        userIntegralRecordService.changeUserIntegral(UserIntegralRecordTypeEnum.CREATE_MUSIC, creationId, creation.getUserId(), -integral, null);

        Date curDate = new Date();
        creation.setCreationId(creationId);
        creation.setSettings(JsonUtils.convertObj2Json(settingDTO));
        creation.setCreateTime(curDate);
        musicCreationMapper.insert(creation);

        // 2.提示词处理
        String prompt = creation.getPrompt();
        if (MusicModeTypeEnum.ADVANCED.getModeType().equals(creation.getModeType())) {
            // 高级模式，提示词处理
            try {
                for (MusicSettingEnum settingEnum : MusicSettingEnum.values()) {
                    //反射获取字段值 musicGener
                    PropertyDescriptor pd = new PropertyDescriptor(settingEnum.getKeyCode(), MusicSettingDTO.class);
                    Method method = pd.getReadMethod();
                    Object obj = method.invoke(settingDTO);
                    if (obj == null) {
                        continue;
                    }
                    // 曲风：摇滚，人声：女声
                    prompt = prompt + " " + settingEnum.getTypeDesc() + ":" + obj;
                }

            } catch (Exception e) {
                log.error("高级模式提示词处理失败", e);
            }

            // TODO 调用高级模式api
        } else {
            // TODO 普通模式调用引用api
        }

        return List.of();
    }

    // 模型和api前缀
    record ModelInfo(String model, String apiCode) {

    }

    private ModelInfo getModelInfo(MusicTypeEnum musicTypeEnum, String modelId) {
        // 音乐类型
        if (MusicTypeEnum.MUSIC == musicTypeEnum) {
            ModelType4MusicEnum musicEnum = ModelType4MusicEnum.getById(modelId);
            if (musicEnum == null) {
                throw new BusinessException(ResponseCodeEnum.CODE_600);
            }
            return new ModelInfo(musicEnum.getModelCode(), musicEnum.getApiCode());
        } else if (MusicTypeEnum.PURE == musicTypeEnum) {
            // 纯音乐
            ModelType4PureMusicEnum musicEnum = ModelType4PureMusicEnum.getById(modelId);
            if (musicEnum == null) {
                throw new BusinessException(ResponseCodeEnum.CODE_600);
            }
            return new ModelInfo(musicEnum.getModelCode(), musicEnum.getApiCode());
        }
        throw new BusinessException(ResponseCodeEnum.CODE_600);
    }
}