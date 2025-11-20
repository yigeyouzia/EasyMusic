package com.easymusic.service.impl;

import java.util.List;

import jakarta.annotation.Resource;

import org.springframework.stereotype.Service;

import com.easymusic.entity.enums.PageSize;
import com.easymusic.entity.query.MusicCreationQuery;
import com.easymusic.entity.po.MusicCreation;
import com.easymusic.entity.vo.PaginationResultVO;
import com.easymusic.entity.query.SimplePage;
import com.easymusic.mappers.MusicCreationMapper;
import com.easymusic.service.MusicCreationService;
import com.easymusic.utils.StringTools;


/**
 * 音乐创作信息 业务接口实现
 */
@Service("musicCreationService")
public class MusicCreationServiceImpl implements MusicCreationService {

	@Resource
	private MusicCreationMapper<MusicCreation, MusicCreationQuery> musicCreationMapper;

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
}