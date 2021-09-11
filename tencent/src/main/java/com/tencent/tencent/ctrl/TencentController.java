/*
 * 腾讯数据
 */
package com.tencent.tencent.ctrl;

import com.tencent.tencent.entity.Tencent;
import com.tencent.tencent.service.TencentService;
import com.tencent.tencent.vo.TencentPageVO;
import com.tencent.tencent.vo.TencentSaveVO;
import com.tencent.tencent.vo.TencentVO;
import com.tencent.core.exceptions.TencentException;
import com.tencent.core.exceptions.BaseException;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tencent.core.entity.PageVO;
import com.tencent.core.entity.R;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;

/**
 * 腾讯数据
 *
 * @author watson
 */
@RestController
@RequestMapping("/tencent")
@Slf4j
@Api(value = "腾讯数据控制器", tags = "腾讯数据控制器")
public class TencentController {
    @Autowired
    private TencentService tencentService;


    /**
     * 创建 腾讯数据
     *
     * @return R
     */
    @ApiOperation(value = "创建Tencent", notes = "创建Tencent")
    @PostMapping("/build")
    public TencentSaveVO build(@ApiParam(name = "创建Tencent", value = "传入json格式", required = true)
                               @RequestBody TencentSaveVO tencentSaveVO) {
        if (StringUtils.isBlank(tencentSaveVO.getId())) {
            throw new TencentException(BaseException.BaseExceptionEnum.Empty_Param);
        }
        if (StringUtils.isBlank(tencentSaveVO.getQq())) {
            throw new TencentException(BaseException.BaseExceptionEnum.Empty_Param);
        }
        if (StringUtils.isBlank(tencentSaveVO.getEmail())) {
            throw new TencentException(BaseException.BaseExceptionEnum.Empty_Param);
        }
        if (StringUtils.isBlank(tencentSaveVO.getPhone())) {
            throw new TencentException(BaseException.BaseExceptionEnum.Empty_Param);
        }

        int count = tencentService.count(new LambdaQueryWrapper<Tencent>()
                .eq(Tencent::getId, tencentSaveVO.getId())
                .eq(Tencent::getQq, tencentSaveVO.getQq())
                .eq(Tencent::getEmail, tencentSaveVO.getEmail())
                .eq(Tencent::getPhone, tencentSaveVO.getPhone())
        );
        if (count > 0) {
            throw new TencentException(BaseException.BaseExceptionEnum.Exists);
        }

        Tencent newTencent = new Tencent();
        BeanUtils.copyProperties(tencentSaveVO, newTencent);

        tencentService.save(newTencent);

        tencentSaveVO = new TencentSaveVO();
        BeanUtils.copyProperties(newTencent, tencentSaveVO);
        log.debug(JSON.toJSONString(tencentSaveVO));
        return tencentSaveVO;
    }


    /**
     * 根据条件qq查询腾讯数据一个详情信息
     *
     * @param qq qq
     * @return TencentVO
     */
    @ApiOperation(value = "创建Tencent", notes = "创建Tencent")
    @GetMapping("/load/qq/{qq}")
    public TencentVO loadByQq(@PathVariable java.lang.String qq) {
        Tencent tencent = tencentService.getOne(new LambdaQueryWrapper<Tencent>()
                .eq(Tencent::getQq, qq));
        TencentVO tencentVO = new TencentVO();
        BeanUtils.copyProperties(tencent, tencentVO);
        log.debug(JSON.toJSONString(tencentVO));
        return tencentVO;
    }

    /**
     * 根据条件email查询腾讯数据一个详情信息
     *
     * @param email 邮箱
     * @return TencentVO
     */
    @ApiOperation(value = "创建Tencent", notes = "创建Tencent")
    @GetMapping("/load/email/{email}")
    public TencentVO loadByEmail(@PathVariable java.lang.String email) {
        Tencent tencent = tencentService.getOne(new LambdaQueryWrapper<Tencent>()
                .eq(Tencent::getEmail, email));
        TencentVO tencentVO = new TencentVO();
        BeanUtils.copyProperties(tencent, tencentVO);
        log.debug(JSON.toJSONString(tencentVO));
        return tencentVO;
    }

    /**
     * 引入數據
     *
     * @return TencentVO
     */
    @ApiOperation(value = "引入數據", notes = "引入數據")
    @GetMapping("/import")
    public Boolean importTxt() {
        tencentService.importTxt();
        return true;
    }

    /**
     * 查询腾讯数据信息集合
     *
     * @return 分页对象
     */
    @ApiOperation(value = "查询Tencent信息集合", notes = "查询Tencent信息集合")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "curPage", value = "当前页", required = true, paramType = "query"),
            @ApiImplicitParam(name = "pageSize", value = "分页大小", required = true, paramType = "query"),
    })
    @GetMapping(value = "/list")
    public IPage<TencentPageVO> list(@ApiIgnore TencentPageVO tencentVO, Integer curPage, Integer pageSize) {
        IPage<Tencent> page = new Page<>(curPage, pageSize);
        QueryWrapper<Tencent> queryWrapper = new QueryWrapper<>();
        int total = tencentService.count(queryWrapper);
        if (total > 0) {
            queryWrapper.lambda().orderByDesc(Tencent::getId);

            IPage<Tencent> tencentPage = tencentService.page(page, queryWrapper);
            List<TencentPageVO> tencentPageVOList = JSON.parseArray(JSON.toJSONString(tencentPage.getRecords()), TencentPageVO.class);
            IPage<TencentPageVO> iPage = new Page<>();
            iPage.setPages(tencentPage.getPages());
            iPage.setCurrent(curPage);
            iPage.setSize(pageSize);
            iPage.setTotal(tencentPage.getTotal());
            iPage.setRecords(tencentPageVOList);
            log.debug(JSON.toJSONString(iPage));
            return iPage;
        }
        return new Page<>();
    }


    /**
     * 修改 腾讯数据
     *
     * @return R
     */
    @ApiOperation(value = "修改Tencent", notes = "修改Tencent")
    @PutMapping("/modify")
    public boolean modify(@ApiParam(name = "修改Tencent", value = "传入json格式", required = true)
                          @RequestBody TencentVO tencentVO) {
        if (StringUtils.isBlank(tencentVO.getId())) {
            throw new TencentException(BaseException.BaseExceptionEnum.Ilegal_Param);
        }
        Tencent newTencent = new Tencent();
        BeanUtils.copyProperties(tencentVO, newTencent);
        boolean isUpdated = tencentService.update(newTencent, new LambdaQueryWrapper<Tencent>()
                .eq(Tencent::getId, tencentVO.getId()));
        return isUpdated;
    }


    /**
     * 删除 腾讯数据
     *
     * @return R
     */
    @ApiOperation(value = "删除Tencent", notes = "删除Tencent")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "id", paramType = "query"),
            @ApiImplicitParam(name = "qq", value = "qq", paramType = "query"),
            @ApiImplicitParam(name = "email", value = "邮箱", paramType = "query")
    })
    @DeleteMapping("/delete")
    public R delete(@ApiIgnore TencentVO tencentVO) {
        if (StringUtils.isBlank(tencentVO.getId())) {
            throw new TencentException(BaseException.BaseExceptionEnum.Ilegal_Param);
        }
        Tencent newTencent = new Tencent();
        BeanUtils.copyProperties(tencentVO, newTencent);
        tencentService.remove(new LambdaQueryWrapper<Tencent>()
                .eq(Tencent::getId, tencentVO.getId()));
        return R.success("删除成功");
    }

}
