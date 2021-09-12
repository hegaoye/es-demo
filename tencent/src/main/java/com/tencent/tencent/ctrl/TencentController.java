/*
 * 腾讯数据
 */
package com.tencent.tencent.ctrl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tencent.cache.entity.RedisKey;
import com.tencent.cache.service.RedisServiceSVImpl;
import com.tencent.core.entity.R;
import com.tencent.core.exceptions.BaseException;
import com.tencent.core.exceptions.TencentException;
import com.tencent.core.tools.Executors;
import com.tencent.tencent.entity.Tencent;
import com.tencent.tencent.service.TencentService;
import com.tencent.tencent.vo.TencentPageVO;
import com.tencent.tencent.vo.TencentSaveVO;
import com.tencent.tencent.vo.TencentVO;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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

    @Autowired
    private RedisServiceSVImpl redisServiceSV;


    /**
     * 创建 腾讯数据
     *
     * @return R
     */
    @ApiOperation(value = "创建Tencent", notes = "创建Tencent")
    @GetMapping("/cache")
    public R cacheNames(String name) {
        if (StringUtils.isBlank(name)) {
            return R.failed(BaseException.BaseExceptionEnum.Empty_Param);
        }
        log.info("缓存文件名-{}", name);

        String cacheKey = RedisKey.FileName.genFileNameCacheKey();
        List<String> list = new ArrayList<>();
        if (redisServiceSV.hasKey(cacheKey)) {
            list = (List<String>) redisServiceSV.get(cacheKey);
        }

        list.add(name);
        redisServiceSV.set(cacheKey, list);

        return R.success();
    }

    @ApiOperation(value = "创建Tencent", notes = "创建Tencent")
    @GetMapping("/check")
    public R checkNames() {
        String cacheKey = RedisKey.FileName.genFileNameCacheKey();
        List<String> list = new ArrayList<>();
        if (redisServiceSV.hasKey(cacheKey)) {
            list = (List<String>) redisServiceSV.get(cacheKey);
        }
        log.info("{}", list);
        return R.success(list);
    }


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
     * 批量保存 腾讯数据
     *
     * @return R
     */
    @ApiOperation(value = "批量保存", notes = "批量保存")
    @PostMapping("/build/batch")
    public Boolean batchBuild(@ApiParam(name = "批量保存", value = "传入json格式", required = true)
                              @RequestBody List<TencentSaveVO> list) {
        Executors.fixedThreadExecutor(new Runnable() {
            @Override
            public void run() {
                for (TencentSaveVO tencentSaveVO : list) {
                    int count = tencentService.count(new LambdaQueryWrapper<Tencent>()
                            .eq(Tencent::getQq, tencentSaveVO.getQq()));

                    if (count > 0) {
                        continue;
                    }

                    Tencent newTencent = new Tencent();
                    BeanUtils.copyProperties(tencentSaveVO, newTencent);

                    tencentService.save(newTencent);
                }
            }
        });

        return true;
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
     * 读取文件,搜索并导出
     *
     * @param multipartFile 上传文件
     * @return
     */
    @ApiOperation(value = "上传csv文件", notes = "上传csv文件")
    @PostMapping("/upload")
    public void upload(@RequestParam("file") MultipartFile multipartFile, HttpServletResponse response) {
        if (null == multipartFile) {
            log.error("文件为空错误");
            throw new TencentException(BaseException.BaseExceptionEnum.Ilegal_Param);
        }

        if (!multipartFile.getOriginalFilename().toLowerCase(Locale.ROOT).contains(".csv")) {
            log.error("文件格式错误-{}", multipartFile.getOriginalFilename());
            throw new TencentException(BaseException.BaseExceptionEnum.File_Ilegal);
        }

        File file = null;
        try {
            //读取文件
            file = new File("/tmp/" + RandomUtil.randomString(7) + ".csv");
            FileUtils.copyInputStreamToFile(multipartFile.getInputStream(), file);
            List<String> list = FileUtil.readLines(file, Charset.defaultCharset());
            if (CollectionUtils.isEmpty(list)) {
                log.error("文件为空-{}", multipartFile.getOriginalFilename());
                throw new TencentException(BaseException.BaseExceptionEnum.Ilegal_Param);
            }


            //数据分成10份
            List<List<String>> lists = new ArrayList<>();
            List<String> data = new ArrayList<>();
            for (int i = 0; i < list.size(); i++) {
                data.add(list.get(i));
                if (((i + 1) % 10 == 0) || (i + 1 == data.size())) {
                    lists.add(data);
                    data = new ArrayList<>();
                }
            }

            //组装数据
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("手机号,qq,qq邮箱\n");
            for (List<String> dataList : lists) {
                List<Tencent> tencentList = tencentService.list(new LambdaQueryWrapper<Tencent>()
                        .in(Tencent::getPhone, dataList));
                if (CollectionUtils.isEmpty(tencentList)) {
                    continue;
                }

                for (Tencent tencent : tencentList) {
                    stringBuffer.append(tencent.getPhone() + "," + tencent.getQq() + "," + tencent.getEmail());
                }
            }


            //下载数据
            String downloadName = DateUtil.format(new Date(), "yyyyMMddHHmmss");
            downloadName = URLEncoder.encode(downloadName, StandardCharsets.UTF_8.name());
            response.setContentType("application/csv");
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setHeader("Pragma", "public");
            response.setHeader("Cache-Control", "max-age=30");
            response.setHeader("Content-Disposition", "attachment; filename=" + downloadName);
            OutputStream outputStream = response.getOutputStream();
            outputStream.write(stringBuffer.toString().getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != file && file.exists()) {
                file.delete();
            }
        }
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
            @ApiImplicitParam(name = "keywords", value = "分页大小", required = true, paramType = "query")
    })
    @GetMapping(value = "/list")
    public IPage<TencentPageVO> list(String keywords, Integer curPage, Integer pageSize) {
        IPage<Tencent> page = new Page<>(curPage, pageSize);
        QueryWrapper<Tencent> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(keywords)) {
            queryWrapper.lambda().like(Tencent::getPhone, keywords);
            if (keywords.length() < 10) {
                queryWrapper.lambda().like(Tencent::getQq, keywords);
            }
        }
        int total = tencentService.count(queryWrapper);
        if (total > 0) {
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
