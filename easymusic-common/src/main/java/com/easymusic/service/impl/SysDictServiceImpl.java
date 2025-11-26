package com.easymusic.service.impl;

import com.easymusic.entity.enums.PageSize;
import com.easymusic.entity.po.SysDict;
import com.easymusic.entity.query.SimplePage;
import com.easymusic.entity.query.SysDictQuery;
import com.easymusic.entity.vo.PaginationResultVO;
import com.easymusic.mappers.SysDictMapper;
import com.easymusic.service.SysDictService;
import com.easymusic.utils.StringTools;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;


/**
 * 系统字典 业务接口实现
 */
@Service("sysDictService")
public class SysDictServiceImpl implements SysDictService {

	@Resource
	private SysDictMapper<SysDict, SysDictQuery> sysDictMapper;

	/**
	 * 根据条件查询列表
	 */
	@Override
	public List<SysDict> findListByParam(SysDictQuery param) {
		return this.sysDictMapper.selectList(param);
	}

	/**
	 * 根据条件查询列表
	 */
	@Override
	public Integer findCountByParam(SysDictQuery param) {
		return this.sysDictMapper.selectCount(param);
	}

	/**
	 * 分页查询方法
	 */
	@Override
	public PaginationResultVO<SysDict> findListByPage(SysDictQuery param) {
		int count = this.findCountByParam(param);
		int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<SysDict> list = this.findListByParam(param);
		PaginationResultVO<SysDict> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * 新增
	 */
	@Override
	public Integer add(SysDict bean) {
		return this.sysDictMapper.insert(bean);
	}

	/**
	 * 批量新增
	 */
	@Override
	public Integer addBatch(List<SysDict> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.sysDictMapper.insertBatch(listBean);
	}

	/**
	 * 批量新增或者修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<SysDict> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.sysDictMapper.insertOrUpdateBatch(listBean);
	}

	/**
	 * 多条件更新
	 */
	@Override
	public Integer updateByParam(SysDict bean, SysDictQuery param) {
		StringTools.checkParam(param);
		return this.sysDictMapper.updateByParam(bean, param);
	}

	/**
	 * 多条件删除
	 */
	@Override
	public Integer deleteByParam(SysDictQuery param) {
		StringTools.checkParam(param);
		return this.sysDictMapper.deleteByParam(param);
	}

	/**
	 * 根据DictId获取对象
	 */
	@Override
	public SysDict getSysDictByDictId(Integer dictId) {
		return this.sysDictMapper.selectByDictId(dictId);
	}

	/**
	 * 根据DictId修改
	 */
	@Override
	public Integer updateSysDictByDictId(SysDict bean, Integer dictId) {
		return this.sysDictMapper.updateByDictId(bean, dictId);
	}

	/**
	 * 根据DictId删除
	 */
	@Override
	public Integer deleteSysDictByDictId(Integer dictId) {
		return this.sysDictMapper.deleteByDictId(dictId);
	}
}