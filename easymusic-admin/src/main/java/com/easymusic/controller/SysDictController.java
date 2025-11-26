package com.easymusic.controller;

import com.easymusic.entity.po.SysDict;
import com.easymusic.entity.query.SysDictQuery;
import com.easymusic.entity.vo.PaginationResultVO;
import com.easymusic.entity.vo.ResponseVO;
import com.easymusic.service.SysDictService;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户信息 Controller
 */
@RestController
@RequestMapping("/settings")
@Slf4j
@Validated
public class SysDictController extends ABaseController {

    @Resource
    private SysDictService sysDictService;

    @RequestMapping("/loadSysDictList")
    public ResponseVO loadSysDictList(SysDictQuery query) {
        query.setOrderBy("sort asc");
        PaginationResultVO<SysDict> res = sysDictService.findListByPage(query);
        return getSuccessResponseVO(res);
    }

    @RequestMapping("/saveSysDict")
    public ResponseVO saveSysDict(SysDict sysDict) {
        sysDictService.saveSysDict(sysDict);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/delSysDict")
    public ResponseVO delSysDict(@NotNull Integer dictId) {
        sysDictService.delSysDictByDictId(dictId);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/changeSort")
    public ResponseVO changeSort(@NotEmpty String dictPcode, @NotEmpty String dictIds) {
        sysDictService.changeSort(dictPcode, dictIds);
        return getSuccessResponseVO(null);
    }

}