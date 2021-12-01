/*
 * 腾讯数据
 */
package com.tencent.tencent.ctrl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import com.alibaba.fastjson.JSON;
import com.baidu.fsg.uid.UidGenerator;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tencent.cache.entity.RedisKey;
import com.tencent.cache.service.RedisServiceSVImpl;
import com.tencent.core.constant.RegularConst;
import com.tencent.core.entity.R;
import com.tencent.core.exceptions.BaseException;
import com.tencent.core.exceptions.TencentException;
import com.tencent.tencent.entity.Tencent;
import com.tencent.tencent.service.TencentEsServiceImpl;
import com.tencent.tencent.vo.TencentPageVO;
import com.tencent.tencent.vo.TencentSaveVO;
import com.tencent.tencent.vo.TencentVO;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.builder.SearchSourceBuilder;
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
import java.util.regex.Pattern;

/**
 * 腾讯数据
 *
 * @author amit
 */
@RestController
@RequestMapping("/es/tencent")
@Slf4j
@Api(value = "腾讯数据Es控制器", tags = "腾讯数据Es控制器")
public class TencentEsController {
    @Autowired
    private TencentEsServiceImpl tencentEsService;

    @Autowired
    private RedisServiceSVImpl redisServiceSV;

    @Autowired
    private UidGenerator uidGenerator;

    //索引庫
    private final static String DB_INDEX = "tencent";

    /**
     * 创建 腾讯数据
     *
     * @return R
     */
    @ApiOperation(value = "创建Cache", notes = "创建Cache")
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

    /**
     * 测试缓存
     *
     * @return
     */
    @ApiOperation(value = "测试缓存", notes = "测试缓存")
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

        Boolean flag = tencentEsService.indexExist(tencentSaveVO.getQq());
        if (!flag) {
            throw new TencentException(BaseException.BaseExceptionEnum.Exists);
        }

        Tencent newTencent = new Tencent();
        BeanUtils.copyProperties(tencentSaveVO, newTencent);

        tencentEsService.insertOrUpdateOne(DB_INDEX, newTencent);

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
        List<Tencent> newTencentList = JSON.parseArray(JSON.toJSONString(list), Tencent.class);
        for (Tencent tencent : newTencentList) {
            tencent.setId(String.valueOf(uidGenerator.getUID()));
        }

        tencentEsService.insertBatch(DB_INDEX, newTencentList);
        return true;
    }


    /**
     * 根据条件qq查询腾讯数据一个详情信息
     *
     * @param qq qq
     * @return TencentVO
     */
    @ApiOperation(value = "根据条件qq查询腾讯数据一个详情信息", notes = "根据qq查询")
    @GetMapping("/load/qq/{qq}")
    public TencentVO loadByQq(@PathVariable String qq) {
        if (StringUtils.isBlank(qq)) {
            throw new TencentException(BaseException.BaseExceptionEnum.Empty_Param);
        }
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        TermQueryBuilder termQuery = QueryBuilders.termQuery("qq", qq);
        searchSourceBuilder.query(termQuery);
        List<TencentVO> tencentList = tencentEsService.search(DB_INDEX, searchSourceBuilder, TencentVO.class);
        if (CollectionUtils.isEmpty(tencentList)) {
            throw new TencentException(BaseException.BaseExceptionEnum.Result_Not_Exist);
        }
        return tencentList.get(0);
    }

    /**
     * 根据条件email查询腾讯数据一个详情信息
     *
     * @param email 邮箱
     * @return TencentVO
     */
    @ApiOperation(value = "根据条件email查询腾讯数据一个详情信息", notes = "根据email查询")
    @GetMapping("/load/email/{email}")
    public TencentVO loadByEmail(@PathVariable String email) {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        TermQueryBuilder termQuery = QueryBuilders.termQuery("email", email);
        searchSourceBuilder.query(termQuery);
        List<TencentVO> tencentList = tencentEsService.search(DB_INDEX, searchSourceBuilder, TencentVO.class);
        if (CollectionUtils.isEmpty(tencentList)) {
            throw new TencentException(BaseException.BaseExceptionEnum.Result_Not_Exist);
        }
        return tencentList.get(0);
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


            //组装数据
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("手机号,qq,qq邮箱\n");
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            String content = list.get(0);
            String para = "qq";
            if (Pattern.matches(RegularConst.CHINA_PATTERN, content)) {
                para = "phone";
            }
            TermsQueryBuilder termsQueryBuilder = QueryBuilders.termsQuery(para, list);
            searchSourceBuilder.query(termsQueryBuilder);

            List<TencentVO> tencentList = tencentEsService.search(DB_INDEX, searchSourceBuilder, TencentVO.class);
            if (CollectionUtils.isEmpty(tencentList)) {
                throw new TencentException(BaseException.BaseExceptionEnum.Ilegal_Param);
            }

            for (TencentVO tencent : tencentList) {
                stringBuffer.append(tencent.getPhone() + "," + tencent.getQq() + "," + tencent.getEmail() + "\n");
            }


            //下载数据
            String downloadName = DateUtil.format(new Date(), "yyyyMMddHHmmss");
            downloadName = URLEncoder.encode(downloadName, StandardCharsets.UTF_8.name());
            response.setContentType("application/csv");
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setHeader("Pragma", "public");
            response.setHeader("Cache-Control", "max-age=30");
            response.setHeader("Content-Disposition", "attachment; filename=" + downloadName + ".csv");
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
            @ApiImplicitParam(name = "qq", value = "qq", required = true, paramType = "query"),
            @ApiImplicitParam(name = "phone", value = "phone", required = true, paramType = "query")
    })
    @GetMapping(value = "/list")
    public R list(TencentPageVO tencentPageVO, Integer curPage, Integer pageSize) {
        IPage<TencentVO> page = new Page<>(curPage, pageSize);
        String qq = tencentPageVO.getQq();
        String email = tencentPageVO.getEmail();
        String phone = tencentPageVO.getPhone();
        BoolQueryBuilder query = QueryBuilders.boolQuery();
        if (StringUtils.isNotBlank(qq)) {
            TermQueryBuilder qqQuery = QueryBuilders.termQuery("qq", qq);
            query.should(qqQuery);
        }
        if (StringUtils.isNotBlank(email)) {
            WildcardQueryBuilder emailQuery = QueryBuilders.wildcardQuery("email", email + "*");
            query.should(emailQuery);
        }
        if (StringUtils.isNotBlank(phone)) {
            TermQueryBuilder phoneQuery = QueryBuilders.termQuery("phone", phone);
            query.should(phoneQuery);
        }
        page.setTotal(tencentEsService.count(DB_INDEX, query));
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder()
                .query(query)
                .from((int) ((page.getCurrent() - 1) * page.getSize()))
                .size((int) page.getSize()).trackTotalHits(true);
        List<TencentVO> list = tencentEsService.search(DB_INDEX, sourceBuilder, TencentVO.class);
        if (!CollectionUtils.isEmpty(list)) {
            page.setRecords(list);
            log.debug(JSON.toJSONString(page));
            return R.success(page);
        }
        return R.success(new Page<>());
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
        tencentEsService.insertOrUpdateOne(DB_INDEX, newTencent);
        return true;
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
        tencentEsService.deleteOne(DB_INDEX, newTencent);
        return R.success("删除成功");
    }

}
