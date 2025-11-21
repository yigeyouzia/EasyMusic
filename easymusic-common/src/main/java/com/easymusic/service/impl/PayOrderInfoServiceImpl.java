package com.easymusic.service.impl;

import java.util.List;

import jakarta.annotation.Resource;

import org.springframework.stereotype.Service;

import com.easymusic.entity.enums.PageSize;
import com.easymusic.entity.query.PayOrderInfoQuery;
import com.easymusic.entity.po.PayOrderInfo;
import com.easymusic.entity.vo.PaginationResultVO;
import com.easymusic.entity.query.SimplePage;
import com.easymusic.mappers.PayOrderInfoMapper;
import com.easymusic.service.PayOrderInfoService;
import com.easymusic.utils.StringTools;


/**
 * 支付订单信息 业务接口实现
 */
@Service("payOrderInfoService")
public class PayOrderInfoServiceImpl implements PayOrderInfoService {

	@Resource
	private PayOrderInfoMapper<PayOrderInfo, PayOrderInfoQuery> payOrderInfoMapper;

	/**
	 * 根据条件查询列表
	 */
	@Override
	public List<PayOrderInfo> findListByParam(PayOrderInfoQuery param) {
		return this.payOrderInfoMapper.selectList(param);
	}

	/**
	 * 根据条件查询列表
	 */
	@Override
	public Integer findCountByParam(PayOrderInfoQuery param) {
		return this.payOrderInfoMapper.selectCount(param);
	}

	/**
	 * 分页查询方法
	 */
	@Override
	public PaginationResultVO<PayOrderInfo> findListByPage(PayOrderInfoQuery param) {
		int count = this.findCountByParam(param);
		int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<PayOrderInfo> list = this.findListByParam(param);
		PaginationResultVO<PayOrderInfo> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * 新增
	 */
	@Override
	public Integer add(PayOrderInfo bean) {
		return this.payOrderInfoMapper.insert(bean);
	}

	/**
	 * 批量新增
	 */
	@Override
	public Integer addBatch(List<PayOrderInfo> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.payOrderInfoMapper.insertBatch(listBean);
	}

	/**
	 * 批量新增或者修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<PayOrderInfo> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.payOrderInfoMapper.insertOrUpdateBatch(listBean);
	}

	/**
	 * 多条件更新
	 */
	@Override
	public Integer updateByParam(PayOrderInfo bean, PayOrderInfoQuery param) {
		StringTools.checkParam(param);
		return this.payOrderInfoMapper.updateByParam(bean, param);
	}

	/**
	 * 多条件删除
	 */
	@Override
	public Integer deleteByParam(PayOrderInfoQuery param) {
		StringTools.checkParam(param);
		return this.payOrderInfoMapper.deleteByParam(param);
	}

	/**
	 * 根据OrderId获取对象
	 */
	@Override
	public PayOrderInfo getPayOrderInfoByOrderId(String orderId) {
		return this.payOrderInfoMapper.selectByOrderId(orderId);
	}

	/**
	 * 根据OrderId修改
	 */
	@Override
	public Integer updatePayOrderInfoByOrderId(PayOrderInfo bean, String orderId) {
		return this.payOrderInfoMapper.updateByOrderId(bean, orderId);
	}

	/**
	 * 根据OrderId删除
	 */
	@Override
	public Integer deletePayOrderInfoByOrderId(String orderId) {
		return this.payOrderInfoMapper.deleteByOrderId(orderId);
	}
}